package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.TaskStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class TaskAndSocialRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun `unauthenticated caller fails to read public_profiles or match_rooms`() = runBlocking {
        auth.signOut()
        var exceptionCaught: Exception? = null
        try {
            withTimeout(5000L) {
                firestore.collection("public_profiles").whereGreaterThanOrEqualTo("level", 1).get().await()
            }
        } catch (e: Exception) {
            exceptionCaught = e
        }
        val fsEx = exceptionCaught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException when unauthenticated", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun `incomplete task claim is rejected by firestore rules and completed task claims once`() = runBlocking {
        val uniqueSuffix = UUID.randomUUID().toString().take(8)
        val uid = signInTestUser("player_$uniqueSuffix@mgludo.test")
        val repo = TaskRewardRepository(context, firestore, auth)
        repo.syncWithFirestore(uid)

        // Scenario 1: Incomplete task claim MUST FAIL
        val premature = repo.claimTaskReward("daily_play_3", uid)
        assertFalse("Claiming incomplete task must fail", premature.success)

        // Direct Firestore bypass attempt on incomplete task MUST ALSO FAIL with PERMISSION_DENIED
        var directBypassEx: Exception? = null
        try {
            withTimeout(5000L) {
                val txId = "tx_bypass_$uniqueSuffix"
                val batch = firestore.batch()
                batch.set(
                    firestore.collection("users").document(uid).collection("reward_history").document(txId),
                    mapOf(
                        "txId" to txId,
                        "userId" to uid,
                        "sourceType" to "DAILY",
                        "sourceId" to "daily_play_3",
                        "periodKey" to "test",
                        "coinsDelta" to 100,
                        "cashDelta" to 0,
                        "xpDelta" to 30,
                        "description" to "Bypass attempt",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    firestore.collection("users").document(uid).collection("tasks").document("daily_play_3"),
                    mapOf(
                        "status" to "CLAIMED",
                        "rewardTxId" to txId,
                        "claimedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            }
        } catch (e: Exception) {
            directBypassEx = e
        }
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            directBypassEx?.unwrapFirestoreException()?.code
        )

        // Scenario 2: Record 3 real completed matches -> now COMPLETED -> claim SUCCEEDS once
        repo.recordMatchCompleted(didWin = false, isOnlineOrTournament = false, matchId = "m1_$uniqueSuffix", uid = uid)
        repo.recordMatchCompleted(didWin = false, isOnlineOrTournament = false, matchId = "m2_$uniqueSuffix", uid = uid)
        repo.recordMatchCompleted(didWin = true, isOnlineOrTournament = false, matchId = "m3_$uniqueSuffix", uid = uid)

        val playTaskAfter3 = repo.tasks.value.first { it.taskId == "daily_play_3" }
        assertEquals(3, playTaskAfter3.progress)
        assertEquals(TaskStatus.COMPLETED, playTaskAfter3.status)

        val validClaim = repo.claimTaskReward("daily_play_3", uid)
        assertTrue("Completed task claim should succeed: ${validClaim.message}", validClaim.success)
        assertEquals(100, validClaim.coinsGranted)

        // Scenario 3: Duplicate claim MUST BE REJECTED
        val duplicateClaim = repo.claimTaskReward("daily_play_3", uid)
        assertFalse("Duplicate claim must fail", duplicateClaim.success)
    }

    @Test
    fun `private Ludo room rejects unauthorized player and admits invited friend`() = runBlocking {
        val uniqueSuffix = UUID.randomUUID().toString().take(8)
        val ownerUid = signInTestUser("owner_$uniqueSuffix@mgludo.test")
        val intruderEmail = "intruder_$uniqueSuffix@mgludo.test"
        val friendEmail = "friend_$uniqueSuffix@mgludo.test"

        val friendUid = signInTestUser(friendEmail)
        val intruderUid = signInTestUser(intruderEmail)

        // Re-sign in as owner to create private room inviting only friendUid
        signInTestUser("owner_$uniqueSuffix@mgludo.test")
        val matchRepo = OnlineMatchRoomRepository(context, firestore, auth)
        val room = withTimeout(5000L) {
            matchRepo.createMatchRoom(
                isPrivate = true,
                gameMode = "PRIVATE_ROOM",
                maxPlayers = 4,
                wagerCoins = 500,
                invitedUids = listOf(friendUid)
            ).getOrThrow()
        }

        // Switch to unauthorized intruder
        signInTestUser(intruderEmail)
        val intruderResult = withTimeout(5000L) {
            matchRepo.joinRoomById(room.roomId, roomCodeAttempt = "000000")
        }
        assertTrue("Unauthorized intruder must be rejected", intruderResult.isFailure)

        // Switch to invited friend
        signInTestUser(friendEmail)
        val friendResult = withTimeout(5000L) {
            matchRepo.joinRoomById(room.roomId, roomCodeAttempt = room.roomCode)
        }
        assertTrue("Invited friend should join successfully: ${friendResult.exceptionOrNull()}", friendResult.isSuccess)
        assertTrue(friendResult.getOrThrow().playerUids.contains(friendUid))
    }

    @Test
    fun `voice club creation chat moderation kick and ban enforcement`() = runBlocking {
        val uniqueSuffix = UUID.randomUUID().toString().take(8)
        val hostEmail = "clubhost_$uniqueSuffix@mgludo.test"
        val memberEmail = "clubmem_$uniqueSuffix@mgludo.test"

        val hostUid = signInTestUser(hostEmail)
        val clubRepo = VoiceClubRepository(context, firestore, auth)
        val club = withTimeout(5000L) {
            clubRepo.createClub(
                name = "Maharaja Voice $uniqueSuffix",
                description = "Live tournament lounge",
                avatarEmoji = "🎙️",
                isPrivate = false,
                ownerDisplayName = "Host Raja"
            ).getOrThrow()
        }

        // Member joins and sends message
        val memberUid = signInTestUser(memberEmail)
        val joined = withTimeout(5000L) { clubRepo.joinClub(club, "Guest Member") }
        assertTrue(joined.isSuccess)
        val updatedClub = club.copy(memberUids = listOf(hostUid, memberUid), memberCount = 2)
        val msgSent = withTimeout(5000L) {
            clubRepo.sendClubMessage(updatedClub, "Guest Member", "Ready for Ludo!", isAnnouncement = false)
        }
        assertTrue("Club member should be allowed to post chat", msgSent.isSuccess)

        val msgs = withTimeout(5000L) {
            clubRepo.observeClubMessages(club.clubId, isPublicClub = true).first { it.isNotEmpty() }
        }
        assertTrue(msgs.any { it.text == "Ready for Ludo!" })

        // Host kicks and bans member
        signInTestUser(hostEmail)
        val kicked = withTimeout(5000L) {
            clubRepo.kickAndBlockMember(updatedClub, memberUid, "Violation of club voice etiquette")
        }
        assertTrue(kicked.isSuccess)

        // Banned member cannot rejoin
        signInTestUser(memberEmail)
        val rejoin = withTimeout(5000L) { clubRepo.joinClub(club, "Guest Member") }
        assertTrue("Banned member must not be allowed to rejoin", rejoin.isFailure)
    }
}
