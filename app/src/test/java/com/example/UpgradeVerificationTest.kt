package com.example

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.OnlineMatchRoomRepository
import com.example.data.TaskRewardRepository
import com.example.model.*
import com.example.ui.components.TasksAndRewardsDialog
import com.example.ui.screens.VoiceClubScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.LudoVoiceChatManager
import com.example.util.ProfilePreferences
import com.example.util.VoiceConnectionState
import com.example.viewmodel.LudoViewModel
import com.example.viewmodel.MatchConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp-xxhdpi")
class UpgradeVerificationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var prefs: ProfilePreferences

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        application.getSharedPreferences("mg_ludo_profile_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        application.getSharedPreferences("mgludo_verified_tasks_v2", Context.MODE_PRIVATE).edit().clear().commit()
        application.getSharedPreferences("mg_ludo_theme_notif_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        runBlocking(Dispatchers.IO) {
            AppDatabase.getDatabase(application).clearAllTables()
        }
        prefs = ProfilePreferences(application)
        prefs.setCurrentPlayerId(ProfilePreferences.DEFAULT_PLAYER_ID)
        prefs.setUserCoins(10_000L)
        prefs.setUserGems(50)
        prefs.setPlayerLevelAndXp(1, 0)
    }

    private fun idleMainLooper(ms: Long = 120L) {
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(ms))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    private fun awaitUntil(timeoutMs: Long = 4000L, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            idleMainLooper(20L)
            if (condition()) return
            Thread.sleep(20L)
        }
        idleMainLooper(50L)
    }

    @Test
    fun `01 claiming incomplete task fails with zero coin delta and stays IN_PROGRESS`() {
        runBlocking {
        val repo = TaskRewardRepository(application)
        val uid = "player_verify_1"
        val now = 1_760_000_000_000L
        repo.initializeForUser(uid, now)

        val incompleteBefore = repo.tasks.value.first { it.taskId == "daily_play_3" }
        assertEquals(0, incompleteBefore.progress)
        assertEquals(TaskStatus.IN_PROGRESS, incompleteBefore.status)

        val outcome = repo.claimTaskReward("daily_play_3", uid = uid, nowMillis = now)
        assertFalse("Incomplete task must not be claimable", outcome.success)
        assertEquals(0, outcome.coinsGranted)
        assertEquals(0, outcome.cashGranted)
        assertTrue(repo.rewardHistory.value.isEmpty())

        val after = repo.tasks.value.first { it.taskId == "daily_play_3" }
        assertEquals(TaskStatus.IN_PROGRESS, after.status)

        // Also verify through ViewModel & UI dialog lock state
        val vm = LudoViewModel(application)
        idleMainLooper()
        val coinsBefore = vm.uiState.value.userCoins
        vm.claimVerifiedTaskReward("daily_play_3")
        idleMainLooper()
        assertEquals("ViewModel must grant 0 coins for incomplete task", coinsBefore, vm.uiState.value.userCoins)

        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            MyApplicationTheme {
                TasksAndRewardsDialog(
                    tasks = state.verifiedTasks,
                    achievements = state.verifiedAchievements,
                    rewardHistory = state.rewardHistory,
                    loginStreak = state.loginStreak,
                    onClaimTask = { vm.claimVerifiedTaskReward(it) },
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("claim_task_button_daily_play_3").assertIsNotEnabled()
        composeRule.onNodeWithTag("task_status_daily_play_3").assertTextEquals("In Progress")
        }
    }

    @Test
    fun `02 real gameplay events complete task, grant exact reward once, log history, and reset next UTC day`() {
        runBlocking {
        val repo = TaskRewardRepository(application)
        val uid = "player_verify_2"
        val day1 = 1_760_000_000_000L
        repo.initializeForUser(uid, day1)

        // Simulate 3 real completed matches
        repeat(3) { idx ->
            val outcomeBeforeDone = repo.claimTaskReward("daily_play_3", uid = uid, nowMillis = day1)
            if (idx < 2) assertFalse(outcomeBeforeDone.success)
            repo.recordMatchCompleted(
                didWin = idx < 2,
                isOnlineOrTournament = true,
                matchId = "real_match_$idx",
                uid = uid,
                nowMillis = day1
            )
        }

        val completedTask = repo.tasks.value.first { it.taskId == "daily_play_3" }
        assertEquals(3, completedTask.progress)
        assertEquals(TaskStatus.COMPLETED, completedTask.status)
        assertTrue(completedTask.canClaim)

        // Also test via UI click on Claim Reward button using ViewModel's repository
        val vm = LudoViewModel(application)
        idleMainLooper()
        val vmUid = vm.uiState.value.authUserId ?: prefs.getCurrentPlayerId()
        repeat(50) { r ->
            vm.taskRewardRepository.recordDiceRolled(
                rolledValue = if (r % 6 == 0) 6 else 3,
                eventId = "ui_roll_$r",
                uid = vmUid
            )
        }
        idleMainLooper()

        val initialCoins = vm.uiState.value.userCoins
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            MyApplicationTheme {
                TasksAndRewardsDialog(
                    tasks = state.verifiedTasks,
                    achievements = state.verifiedAchievements,
                    rewardHistory = state.rewardHistory,
                    loginStreak = state.loginStreak,
                    onClaimTask = { vm.claimVerifiedTaskReward(it) },
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("task_status_daily_roll_50").assertTextEquals("Completed")
        composeRule.onNodeWithTag("claim_task_button_daily_roll_50")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        idleMainLooper()
        composeRule.waitForIdle()

        assertEquals(initialCoins + 50L, vm.uiState.value.userCoins)
        awaitUntil { vm.uiState.value.verifiedTasks.firstOrNull { it.taskId == "daily_roll_50" }?.status == TaskStatus.CLAIMED }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("task_status_daily_roll_50").assertTextEquals("Claimed")
        composeRule.onNodeWithTag("claim_task_button_daily_roll_50").assertIsNotEnabled()

        // History tab shows the verified transaction
        composeRule.onNodeWithTag("tasks_tab_history").performClick()
        composeRule.waitForIdle()
        assertTrue(vm.uiState.value.rewardHistory.any { it.sourceId == "daily_roll_50" && it.coinsDelta == 50 })

        // Verify next UTC day (+24h) resets daily task progress & status while keeping lifetime history
        val claimDay1 = repo.claimTaskReward("daily_play_3", uid = uid, nowMillis = day1)
        assertTrue(claimDay1.success)
        assertEquals(100, claimDay1.coinsGranted)

        val day2 = day1 + 86_400_000L
        repo.initializeForUser(uid, day2)
        val resetDaily = repo.tasks.value.first { it.taskId == "daily_play_3" }
        assertEquals(0, resetDaily.progress)
        assertEquals(TaskStatus.IN_PROGRESS, resetDaily.status)
        assertEquals(1, repo.rewardHistory.value.size)
        }
    }

    @Test
    fun `03 duplicate claim is rejected with zero extra coins and single history entry`() {
        runBlocking {
        val vm = LudoViewModel(application)
        idleMainLooper()
        val uid = vm.uiState.value.authUserId ?: prefs.getCurrentPlayerId()

        vm.taskRewardRepository.recordVerifiedFriendInvite(
            referralId = "friend_join_tx_99",
            uid = uid
        )
        // Re-submitting the exact same referral eventId must also be deduplicated
        vm.taskRewardRepository.recordVerifiedFriendInvite(
            referralId = "friend_join_tx_99",
            uid = uid
        )
        idleMainLooper()

        val coinsBefore = vm.uiState.value.userCoins
        val cashBefore = vm.uiState.value.userGems

        vm.claimVerifiedTaskReward("daily_invite_1")
        idleMainLooper()
        val coinsAfterFirst = vm.uiState.value.userCoins
        val cashAfterFirst = vm.uiState.value.userGems
        assertEquals(coinsBefore + 200L, coinsAfterFirst)
        assertEquals(cashBefore + 3, cashAfterFirst)

        // Second & third claim attempts must fail cleanly
        vm.claimVerifiedTaskReward("daily_invite_1")
        vm.claimVerifiedTaskReward("daily_invite_1")
        idleMainLooper()

        assertEquals(coinsAfterFirst, vm.uiState.value.userCoins)
        assertEquals(cashAfterFirst, vm.uiState.value.userGems)
        assertEquals(
            1,
            vm.uiState.value.rewardHistory.count { it.sourceId == "daily_invite_1" }
        )
        }
    }

    @Test
    fun `04 simulated app restart preserves progress, claimed state, reward history, and wallet`() {
        runBlocking {
        val firstVm = LudoViewModel(application)
        idleMainLooper()
        val uid = firstVm.uiState.value.authUserId ?: prefs.getCurrentPlayerId()

        // Complete 2 captures & full daily_play_3 task
        repeat(2) { i ->
            firstVm.taskRewardRepository.recordPawnCaptured(eventId = "restart_cap_$i", uid = uid)
        }
        repeat(3) { m ->
            firstVm.taskRewardRepository.recordMatchCompleted(
                didWin = true,
                isOnlineOrTournament = false,
                matchId = "restart_match_$m",
                uid = uid
            )
        }
        idleMainLooper()
        firstVm.claimVerifiedTaskReward("daily_play_3")
        idleMainLooper()

        val expectedCoins = firstVm.uiState.value.userCoins
        val expectedXp = firstVm.uiState.value.playerXp
        val expectedHistorySize = firstVm.uiState.value.rewardHistory.size

        // Simulate complete app restart via fresh Repository + fresh ViewModel
        val restartedRepo = TaskRewardRepository(application)
        restartedRepo.initializeForUser(uid)
        assertEquals(TaskStatus.CLAIMED, restartedRepo.tasks.value.first { it.taskId == "daily_play_3" }.status)
        assertEquals(2, restartedRepo.tasks.value.first { it.taskId == "daily_capture_5" }.progress)
        assertEquals(TaskStatus.IN_PROGRESS, restartedRepo.tasks.value.first { it.taskId == "daily_capture_5" }.status)

        val restartedVm = LudoViewModel(application)
        awaitUntil { restartedVm.uiState.value.verifiedTasks.isNotEmpty() }
        assertEquals(expectedCoins, restartedVm.uiState.value.userCoins)
        assertEquals(expectedXp, restartedVm.uiState.value.playerXp)
        assertEquals(expectedHistorySize, restartedVm.uiState.value.rewardHistory.size)
        assertEquals(
            TaskStatus.CLAIMED,
            restartedVm.uiState.value.verifiedTasks.first { it.taskId == "daily_play_3" }.status
        )
        // Re-claiming after restart must still be rejected
        restartedVm.claimVerifiedTaskReward("daily_play_3")
        idleMainLooper()
        assertEquals(expectedCoins, restartedVm.uiState.value.userCoins)
        }
    }

    @Test
    fun `05 voice club roles, chat, rooms, moderation permissions, and VoiceClubScreen UI work properly`() {
        // Verify role permissions
        assertTrue(VoiceClubRole.OWNER.canModerate())
        assertTrue(VoiceClubRole.OWNER.canManageClub())
        assertTrue(VoiceClubRole.ADMIN.canModerate())
        assertTrue(VoiceClubRole.ADMIN.canManageClub())
        assertTrue(VoiceClubRole.MODERATOR.canModerate())
        assertFalse(VoiceClubRole.MODERATOR.canManageClub())
        assertFalse(VoiceClubRole.MEMBER.canModerate())

        val vm = LudoViewModel(application)
        idleMainLooper()
        val club = VoiceClubItem(
            clubId = "club_maharaja_1",
            name = "Royal Voice Legion",
            description = "Championship Ludo squad with live comms",
            avatarEmoji = "👑",
            ownerId = "owner_uid_1",
            ownerName = "King Kabir",
            isPrivate = false,
            inviteCode = "MG9021",
            announcement = "Squad scrimmage tonight at 8 PM!",
            memberCount = 2,
            clubXp = 450,
            memberUids = listOf("owner_uid_1", "member_uid_2")
        )
        val members = listOf(
            VoiceClubMemberItem("owner_uid_1", club.clubId, "King Kabir", VoiceClubRole.OWNER, isOnline = true),
            VoiceClubMemberItem("member_uid_2", club.clubId, "Rani Tara", VoiceClubRole.MODERATOR, isOnline = true)
        )
        val messages = listOf(
            ClubChatMessageItem("m1", club.clubId, "owner_uid_1", "King Kabir", "Welcome everyone! 🎲", isAnnouncement = true),
            ClubChatMessageItem("m2", club.clubId, "member_uid_2", "Rani Tara", "Ready for 2v2! 🔥")
        )

        var activeInUi by mutableStateOf<VoiceClubItem?>(null)
        composeRule.setContent {
            val base by vm.uiState.collectAsState()
            val decorated = base.copy(
                publicClubs = listOf(club),
                myClubs = listOf(club),
                activeClub = activeInUi,
                clubMembers = if (activeInUi != null) members else emptyList(),
                clubMessages = if (activeInUi != null) messages else emptyList(),
                clubVoiceRooms = if (activeInUi != null) listOf(
                    ClubVoiceRoomItem("vr_1", club.clubId, "Royal Strategy Stage", "owner_uid_1", true)
                ) else emptyList()
            )
            MyApplicationTheme {
                VoiceClubScreen(
                    uiState = decorated,
                    viewModel = vm,
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("voice_club_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("club_card_${club.clubId}").assertIsDisplayed()

        // Open club workspace and verify announcement, chat, voice controls & members list
        activeInUi = club
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Squad scrimmage tonight at 8 PM!").assertIsDisplayed()
        composeRule.onNodeWithTag("club_mic_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("club_speaker_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("club_chat_input").assertIsDisplayed()
        composeRule.onNodeWithText("Members (2)").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("King Kabir").assertIsDisplayed()
        composeRule.onNodeWithText("Rani Tara").assertIsDisplayed()
    }

    @Test
    fun `06 live match voice chat, team isolation, volume control, mute-all do not block dice roll or moves`() {
        val vm = LudoViewModel(application)
        vm.startNewGame(mode = GameMode.TEAM_2VS2, playerCount = 4, chosenColor = LudoColor.RED, wager = 500L)
        idleMainLooper()

        // Voice auto-starts muted in new match (or enable if off), switch channels, adjust peer volume, mute all
        if (!vm.uiState.value.isVoiceActive) {
            vm.toggleVoiceChat()
            idleMainLooper()
        }
        assertTrue(vm.uiState.value.isVoiceActive)
        vm.setVoiceChannelMode(VoiceChannelMode.TEAM)
        idleMainLooper()
        assertEquals(VoiceChannelMode.TEAM, vm.uiState.value.voiceChannelMode)

        vm.voiceChatManager.syncRosterPlayers(
            listOf("local" to "You", "teammate" to "Allied Bot", "enemy" to "Enemy Bot"),
            localUid = "local"
        )
        // Enemy voice in Team mode must be ignored (not marked speaking)
        val validPcm = android.util.Base64.encodeToString(ByteArray(16) { 20 }, android.util.Base64.NO_WRAP)
        vm.voiceChatManager.receiveRemotePcmFrame(
            senderUid = "enemy",
            senderName = "Enemy Bot",
            senderTeamId = 1,
            localTeamId = 0,
            frameChannel = VoiceChannelMode.TEAM,
            base64Pcm = validPcm,
            rms = 0.65f
        )
        assertFalse(
            "Enemy on another team must not appear speaking in Team Voice mode",
            vm.voiceChatManager.participants.value["enemy"]?.isSpeaking == true
        )

        // Switch back to ROOM and adjust peer volume & mute-all
        vm.setVoiceChannelMode(VoiceChannelMode.ROOM)
        vm.setPeerVoiceVolume("teammate", 0.35f)
        assertEquals(0.35f, vm.voiceChatManager.participants.value["teammate"]?.localVolume ?: 0f, 0.01f)
        vm.muteAllVoicePeers()
        assertEquals(0f, vm.voiceChatManager.participants.value["teammate"]?.localVolume ?: -1f, 0.01f)

        // Gameplay (authorized token move + dice roll) runs unhindered while voice is active
        val activeColor = vm.uiState.value.players[vm.uiState.value.currentPlayerIndex].color
        val seq0 = vm.uiState.value.moveSequenceNumber
        assertTrue(vm.syncRemoteMoveIfAuthorized(activeColor, 0, 6, seq0))
        repeat(40) { idleMainLooper(100L) }
        val movedPawn = vm.uiState.value.players.first { it.color == activeColor }.pawns.first { it.id == 0 }
        assertEquals(PawnState.ON_BOARD, movedPawn.state)
        assertEquals(activeColor.startTrackPos, movedPawn.trackPosition)

        vm.toggleVoiceChat()
        vm.leaveCurrentGame()
    }

    @Test
    fun `07 muted player transmits zero packets and zero bytes even with loud speech audio`() {
        val voiceManager = LudoVoiceChatManager(application)
        var relayCallbackCount = 0
        voiceManager.setFrameRelaySink { _, _, _, _, _ ->
            relayCallbackCount++
        }

        voiceManager.startVoiceChat(roomId = "mute_audit_room", localUid = "u_mute", localName = "Ishu", startMuted = true)
        assertTrue(voiceManager.isMicMuted.value)
        assertEquals(0L, voiceManager.transmittedPacketCount.value)

        // Loud 16kHz PCM speech buffer
        val loudSpeechFrame = ShortArray(320) { i -> if (i % 2 == 0) 14000 else -14000 }

        // While muted, 10 consecutive loud frames must send 0 bytes & 0 packets
        repeat(10) {
            val sent = voiceManager.processCapturedPcmFrame(loudSpeechFrame, loudSpeechFrame.size, "u_mute")
            assertEquals("Muted mic must send 0 bytes", 0, sent)
        }
        assertEquals(0, relayCallbackCount)
        assertEquals(0L, voiceManager.transmittedPacketCount.value)
        assertEquals(0, voiceManager.lastTransmittedBytes.value)
        assertFalse(voiceManager.isLocalSpeaking.value)

        // Unmute -> loud frame DOES transmit audio
        voiceManager.setMicMuted(false, "u_mute")
        val bytesWhenLive = voiceManager.processCapturedPcmFrame(loudSpeechFrame, loudSpeechFrame.size, "u_mute")
        assertTrue("Unmuted mic should transmit PCM payload", bytesWhenLive > 0)
        assertEquals(1, relayCallbackCount)
        assertEquals(1L, voiceManager.transmittedPacketCount.value)
        assertTrue(voiceManager.isLocalSpeaking.value)

        // Re-mute -> returns immediately to 0 bytes transmitted
        voiceManager.toggleMic("u_mute")
        assertTrue(voiceManager.isMicMuted.value)
        val bytesAfterRemute = voiceManager.processCapturedPcmFrame(loudSpeechFrame, loudSpeechFrame.size, "u_mute")
        assertEquals(0, bytesAfterRemute)
        assertEquals(1, relayCallbackCount)
        assertEquals(1L, voiceManager.transmittedPacketCount.value)
        assertEquals(0, voiceManager.lastTransmittedBytes.value)
        voiceManager.stopVoiceChat()
    }

    @Test
    fun `08 private room strictly rejects unauthorized user and accepts invited or exact room code`() {
        val privateRoom = MatchRoomItem(
            roomId = "room_priv_88",
            roomCode = "774499",
            ownerId = "owner_king",
            isPrivate = true,
            allowedUids = listOf("owner_king"),
            invitedUids = listOf("invited_queen"),
            playerUids = listOf("owner_king"),
            maxPlayers = 4,
            status = "WAITING"
        )

        // Unauthorized intruder without code or with wrong code is rejected
        assertFalse(OnlineMatchRoomRepository.canJoinRoom(privateRoom, "intruder_99", null))
        assertFalse(OnlineMatchRoomRepository.canJoinRoom(privateRoom, "intruder_99", "000000"))

        // Invited player can join without typing code
        assertTrue(OnlineMatchRoomRepository.canJoinRoom(privateRoom, "invited_queen", null))

        // Player with exact room code is accepted
        assertTrue(OnlineMatchRoomRepository.canJoinRoom(privateRoom, "guest_with_code", "774499"))

        // Finished rooms reject new joins
        assertFalse(OnlineMatchRoomRepository.canJoinRoom(privateRoom.copy(status = "FINISHED"), "invited_queen", "774499"))

        // ViewModel validation rejects short room codes
        val vm = LudoViewModel(application)
        vm.startMatchmaking(mode = GameMode.PRIVATE_ROOM, customRoomCode = "12")
        assertTrue(vm.uiState.value.bannerNotice?.contains("valid private room code", ignoreCase = true) == true)
        assertFalse(vm.validatePrivateRoomAccess(privateRoom, "999999"))
        assertTrue(vm.validatePrivateRoomAccess(privateRoom, "774499"))
    }

    @Test
    fun `09 network disconnect and reconnect preserves all 16 token positions, checksum, and voice connection`() {
        val vm = LudoViewModel(application)
        vm.startNewGame(mode = GameMode.ONLINE_MULTIPLAYER, playerCount = 4, chosenColor = LudoColor.BLUE, wager = 1000L)
        idleMainLooper()
        if (!vm.uiState.value.isVoiceActive) vm.toggleVoiceChat()

        // Release Blue token #1 and advance 4 cells
        assertTrue(vm.syncRemoteMoveIfAuthorized(LudoColor.BLUE, 1, 6, vm.uiState.value.moveSequenceNumber))
        repeat(20) { idleMainLooper(80L) }
        assertTrue(vm.syncRemoteMoveIfAuthorized(LudoColor.BLUE, 1, 4, vm.uiState.value.moveSequenceNumber))
        repeat(20) { idleMainLooper(80L) }

        val checksumBeforeDrop = vm.uiState.value.lastSyncedBoardChecksum
        val turnBeforeDrop = vm.uiState.value.currentPlayerIndex
        val bluePawn1Before = vm.uiState.value.players.first { it.color == LudoColor.BLUE }.pawns.first { it.id == 1 }
        assertEquals(LudoColor.BLUE.startTrackPos + 4, bluePawn1Before.trackPosition)

        // Drop connection & recover both match and voice
        vm.simulateNetworkDisconnect()
        assertEquals(MatchConnectionState.DISCONNECTED, vm.uiState.value.connectionState)
        assertTrue(vm.reconnectActiveMatch())
        vm.reconnectVoiceChat()
        idleMainLooper()

        assertEquals(MatchConnectionState.CONNECTED, vm.uiState.value.connectionState)
        assertEquals(VoiceConnectionState.CONNECTED, vm.voiceChatManager.connectionState.value)
        assertEquals(checksumBeforeDrop, vm.uiState.value.lastSyncedBoardChecksum)
        assertEquals(turnBeforeDrop, vm.uiState.value.currentPlayerIndex)
        assertEquals(16, vm.uiState.value.players.sumOf { it.pawns.size })

        val bluePawn1After = vm.uiState.value.players.first { it.color == LudoColor.BLUE }.pawns.first { it.id == 1 }
        assertEquals(bluePawn1Before.trackPosition, bluePawn1After.trackPosition)
        assertEquals(bluePawn1Before.state, bluePawn1After.state)

        vm.toggleVoiceChat()
        vm.leaveCurrentGame()
    }

    @Test
    fun `10 wallet balances, XP, level, and match stats stay consistent across gameplay and verified claims`() {
        runBlocking {
        val vm = LudoViewModel(application)
        idleMainLooper()
        val uid = vm.uiState.value.authUserId ?: prefs.getCurrentPlayerId()
        val startCoins = vm.uiState.value.userCoins
        val startGems = vm.uiState.value.userGems
        val startXp = vm.uiState.value.playerXp

        // Complete "Win 2 matches" (150 coins, 2 cash, 50 XP) and "Play 3 Ludo matches" (100 coins, 0 cash, 30 XP)
        repeat(3) { i ->
            vm.taskRewardRepository.recordMatchCompleted(
                didWin = i < 2,
                isOnlineOrTournament = true,
                matchId = "stat_match_$i",
                uid = uid
            )
        }
        idleMainLooper()

        // Verify today's activity counters incremented accurately
        awaitUntil { vm.uiState.value.todayActivity.matchesPlayed == 3 }
        assertEquals(3, vm.uiState.value.todayActivity.matchesPlayed)
        assertEquals(2, vm.uiState.value.todayActivity.matchesWon)

        // Claim both completed rewards
        vm.claimVerifiedTaskReward("daily_play_3")
        vm.claimVerifiedTaskReward("daily_win_2")
        awaitUntil { vm.uiState.value.userCoins == startCoins + 250L }

        assertEquals(startCoins + 250L, vm.uiState.value.userCoins)
        assertEquals(startGems + 2, vm.uiState.value.userGems)
        val expectedXp = startXp + 80
        assertEquals(expectedXp, vm.uiState.value.playerXp)
        assertEquals(1 + (expectedXp / 250), vm.uiState.value.playerLevel)

        val historySumCoins = vm.uiState.value.rewardHistory.sumOf { it.coinsDelta }
        val historySumCash = vm.uiState.value.rewardHistory.sumOf { it.cashDelta }
        assertEquals(250, historySumCoins)
        assertEquals(2, historySumCash)
        }
    }
}
