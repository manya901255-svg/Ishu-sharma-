package com.example.data

import android.content.Context
import com.example.model.ClubChatMessageItem
import com.example.model.ClubInvitationItem
import com.example.model.ClubVoiceRoomItem
import com.example.model.VoiceClubItem
import com.example.model.VoiceClubMemberItem
import com.example.model.VoiceClubRole
import com.example.util.FirebaseProviders
import com.example.util.FirestoreOperation
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VoiceClubRepository(
    private val context: Context,
    private val firestoreOverride: FirebaseFirestore? = null,
    private val authOverride: FirebaseAuth? = null
) {
    private val firestore: FirebaseFirestore? get() = firestoreOverride ?: FirebaseProviders.getFirestoreOrNull(context)
    private val auth: FirebaseAuth? get() = authOverride ?: FirebaseProviders.getAuthOrNull(context)

    fun observePublicClubs(): Flow<List<VoiceClubItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg: ListenerRegistration = db.collection("voice_clubs")
            .whereEqualTo("isPrivate", false)
            .limit(40)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "voice_clubs", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { parseClub(it) } ?: emptyList()
                trySend(list.sortedByDescending { it.clubXp })
            }
        awaitClose { reg.remove() }
    }

    fun observeMyClubs(): Flow<List<VoiceClubItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("voice_clubs")
            .whereArrayContains("memberUids", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "voice_clubs/my", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { parseClub(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeClubMembers(clubId: String): Flow<List<VoiceClubMemberItem>> = callbackFlow {
        val db = firestore
        if (db == null || auth?.currentUser == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("voice_clubs").document(clubId)
            .collection("members")
            .whereEqualTo("clubId", clubId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "voice_clubs/$clubId/members", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    VoiceClubMemberItem(
                        uid = doc.getString("uid") ?: doc.id,
                        clubId = doc.getString("clubId") ?: clubId,
                        displayName = doc.getString("displayName") ?: "Player",
                        role = VoiceClubRole.fromWire(doc.getString("role")),
                        isOnline = doc.getBoolean("isOnline") ?: false,
                        isMutedByMod = doc.getBoolean("isMutedByMod") ?: false
                    )
                } ?: emptyList()
                trySend(list.sortedBy { it.role.ordinal })
            }
        awaitClose { reg.remove() }
    }

    fun observeClubMessages(clubId: String, isPublicClub: Boolean): Flow<List<ClubChatMessageItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val col = db.collection("voice_clubs").document(clubId).collection("messages")
        val query = if (isPublicClub) {
            col.whereEqualTo("isPublicClub", true).limit(80)
        } else {
            col.whereArrayContains("memberUids", uid).limit(80)
        }
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                handleFirestoreError(err, FirestoreOperation.READ, "voice_clubs/$clubId/messages", auth)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { doc ->
                val ts = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ClubChatMessageItem(
                    messageId = doc.getString("messageId") ?: doc.id,
                    clubId = doc.getString("clubId") ?: clubId,
                    senderUid = doc.getString("senderUid") ?: "",
                    senderName = doc.getString("senderName") ?: "Player",
                    text = doc.getString("text") ?: "",
                    isAnnouncement = doc.getBoolean("isAnnouncement") ?: false,
                    createdAtMillis = ts?.toDate()?.time ?: System.currentTimeMillis()
                )
            }?.sortedBy { it.createdAtMillis } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    fun observeClubVoiceRooms(clubId: String, isPublicClub: Boolean): Flow<List<ClubVoiceRoomItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val col = db.collection("voice_clubs").document(clubId).collection("voice_rooms")
        val query = if (isPublicClub) col.whereEqualTo("isPublic", true) else col.whereArrayContains("allowedUids", uid)
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                handleFirestoreError(err, FirestoreOperation.READ, "voice_clubs/$clubId/voice_rooms", auth)
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val allowed = (doc.get("allowedUids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                ClubVoiceRoomItem(
                    roomId = doc.getString("roomId") ?: doc.id,
                    clubId = doc.getString("clubId") ?: clubId,
                    title = doc.getString("title") ?: "Voice Lounge",
                    hostUid = doc.getString("hostUid") ?: "",
                    isPublic = doc.getBoolean("isPublic") ?: true,
                    allowedUids = allowed,
                    isActive = doc.getBoolean("isActive") ?: true
                )
            }?.filter { it.isActive } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    fun observeMyClubInvitations(): Flow<List<ClubInvitationItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collectionGroup("invitations")
            .whereEqualTo("toUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "club_invitations", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    val status = doc.getString("status") ?: "PENDING"
                    if (status != "PENDING") return@mapNotNull null
                    ClubInvitationItem(
                        inviteId = doc.getString("inviteId") ?: doc.id,
                        clubId = doc.getString("clubId") ?: "",
                        clubName = doc.getString("clubName") ?: "Voice Club",
                        fromUid = doc.getString("fromUid") ?: "",
                        fromName = doc.getString("fromName") ?: "Player",
                        toUid = doc.getString("toUid") ?: uid,
                        status = status
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun createClub(
        name: String,
        description: String,
        avatarEmoji: String,
        isPrivate: Boolean,
        ownerDisplayName: String
    ): Result<VoiceClubItem> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val clubId = "club_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val inviteCode = "MG${(1000..9999).random()}"

        return try {
            val clubRef = db.collection("voice_clubs").document(clubId)
            val memberRef = clubRef.collection("members").document(user.uid)
            val batch = db.batch()
            batch.set(
                clubRef,
                mapOf(
                    "clubId" to clubId,
                    "name" to name.trim().take(60),
                    "description" to description.trim().take(240),
                    "avatarEmoji" to avatarEmoji.ifBlank { "👑" },
                    "ownerId" to user.uid,
                    "ownerName" to ownerDisplayName.ifBlank { "Royal Host" }.take(40),
                    "isPrivate" to isPrivate,
                    "inviteCode" to inviteCode,
                    "announcement" to "Welcome to ${name.trim()}!",
                    "memberCount" to 1,
                    "clubXp" to 100,
                    "memberUids" to listOf(user.uid),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                memberRef,
                mapOf(
                    "uid" to user.uid,
                    "clubId" to clubId,
                    "displayName" to ownerDisplayName.ifBlank { "Royal Host" }.take(40),
                    "role" to VoiceClubRole.OWNER.wire,
                    "isOnline" to true,
                    "isMutedByMod" to false,
                    "joinedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(
                VoiceClubItem(
                    clubId = clubId,
                    name = name.trim(),
                    description = description.trim(),
                    avatarEmoji = avatarEmoji.ifBlank { "👑" },
                    ownerId = user.uid,
                    ownerName = ownerDisplayName,
                    isPrivate = isPrivate,
                    inviteCode = inviteCode,
                    announcement = "Welcome to ${name.trim()}!",
                    memberCount = 1,
                    clubXp = 100,
                    memberUids = listOf(user.uid)
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "voice_clubs/$clubId", auth)))
        }
    }

    suspend fun joinClub(club: VoiceClubItem, displayName: String, enteredInviteCode: String? = null): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        if (club.isPrivate && club.inviteCode != enteredInviteCode && user.uid !in club.memberUids) {
            return Result.failure(IllegalStateException("Invalid invite code for private Voice Club."))
        }
        return try {
            val clubRef = db.collection("voice_clubs").document(club.clubId)
            val memberRef = clubRef.collection("members").document(user.uid)
            val newMemberUids = (club.memberUids + user.uid).distinct().take(50)
            val batch = db.batch()
            batch.set(
                memberRef,
                mapOf(
                    "uid" to user.uid,
                    "clubId" to club.clubId,
                    "displayName" to displayName.ifBlank { "Player" }.take(40),
                    "role" to if (club.ownerId == user.uid) VoiceClubRole.OWNER.wire else VoiceClubRole.MEMBER.wire,
                    "isOnline" to true,
                    "isMutedByMod" to false,
                    "joinedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                clubRef,
                buildMap<String, Any> {
                    put("memberUids", newMemberUids)
                    put("memberCount", newMemberUids.size)
                    put("clubXp", club.clubXp + 15)
                    put("updatedAt", FieldValue.serverTimestamp())
                    if (club.isPrivate) put("inviteCode", club.inviteCode)
                }
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "voice_clubs/${club.clubId}/join", auth)))
        }
    }

    suspend fun sendClubMessage(
        club: VoiceClubItem,
        senderName: String,
        text: String,
        isAnnouncement: Boolean = false
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val msgId = "msg_${UUID.randomUUID().toString().take(12)}"
        return try {
            val clubRef = db.collection("voice_clubs").document(club.clubId)
            clubRef.collection("messages").document(msgId).set(
                mapOf(
                    "messageId" to msgId,
                    "clubId" to club.clubId,
                    "senderUid" to user.uid,
                    "senderName" to senderName.take(40),
                    "text" to text.trim().take(500),
                    "isAnnouncement" to isAnnouncement,
                    "isPublicClub" to !club.isPrivate,
                    "memberUids" to club.memberUids.take(50),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (isAnnouncement) {
                clubRef.update(
                    mapOf(
                        "announcement" to text.trim().take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "voice_clubs/${club.clubId}/messages", auth)))
        }
    }

    suspend fun createVoiceRoom(club: VoiceClubItem, title: String): Result<ClubVoiceRoomItem> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val roomId = "vr_${UUID.randomUUID().toString().take(10)}"
        return try {
            val allowed = (club.memberUids + user.uid).distinct().take(50)
            db.collection("voice_clubs").document(club.clubId)
                .collection("voice_rooms").document(roomId)
                .set(
                    mapOf(
                        "roomId" to roomId,
                        "clubId" to club.clubId,
                        "title" to title.trim().ifBlank { "Royal Voice Stage" }.take(80),
                        "hostUid" to user.uid,
                        "isPublic" to !club.isPrivate,
                        "allowedUids" to allowed,
                        "isActive" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(ClubVoiceRoomItem(roomId, club.clubId, title, user.uid, !club.isPrivate, allowed, true))
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "voice_rooms/$roomId", auth)))
        }
    }

    suspend fun invitePlayerToClub(club: VoiceClubItem, fromName: String, toUid: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val inviteId = "inv_${UUID.randomUUID().toString().take(10)}"
        return try {
            db.collection("voice_clubs").document(club.clubId)
                .collection("invitations").document(inviteId)
                .set(
                    mapOf(
                        "inviteId" to inviteId,
                        "clubId" to club.clubId,
                        "clubName" to club.name,
                        "fromUid" to user.uid,
                        "fromName" to fromName.take(40),
                        "toUid" to toUid,
                        "status" to "PENDING",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "invitations/$inviteId", auth)))
        }
    }

    suspend fun respondToClubInvite(invite: ClubInvitationItem, accept: Boolean, displayName: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        return try {
            val invRef = db.collection("voice_clubs").document(invite.clubId)
                .collection("invitations").document(invite.inviteId)
            invRef.update("status", if (accept) "ACCEPTED" else "DECLINED").await()
            if (accept) {
                val clubDoc = db.collection("voice_clubs").document(invite.clubId).get().await()
                val club = parseClub(clubDoc)
                if (club != null) {
                    joinClub(club, displayName, club.inviteCode)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "invitations/${invite.inviteId}", auth)))
        }
    }

    suspend fun updateMemberRole(clubId: String, targetUid: String, newRole: VoiceClubRole): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            db.collection("voice_clubs").document(clubId)
                .collection("members").document(targetUid)
                .update(mapOf("role" to newRole.wire, "updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "members/$targetUid", auth)))
        }
    }

    suspend fun kickAndBlockMember(club: VoiceClubItem, targetUid: String, reason: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val mod = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        return try {
            val clubRef = db.collection("voice_clubs").document(club.clubId)
            val batch = db.batch()
            batch.set(
                clubRef.collection("banned").document(targetUid),
                mapOf(
                    "uid" to targetUid,
                    "clubId" to club.clubId,
                    "bannedByUid" to mod.uid,
                    "reason" to reason.ifBlank { "Removed by moderator" }.take(200),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.delete(clubRef.collection("members").document(targetUid))
            val updatedUids = club.memberUids.filter { it != targetUid }
            batch.update(
                clubRef,
                mapOf(
                    "memberUids" to updatedUids,
                    "memberCount" to updatedUids.size,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "banned/$targetUid", auth)))
        }
    }

    private fun parseClub(doc: DocumentSnapshot): VoiceClubItem? {
        if (!doc.exists()) return null
        @Suppress("UNCHECKED_CAST")
        val memberUids = (doc.get("memberUids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        return VoiceClubItem(
            clubId = doc.getString("clubId") ?: doc.id,
            name = doc.getString("name") ?: "Royal Club",
            description = doc.getString("description") ?: "",
            avatarEmoji = doc.getString("avatarEmoji") ?: "👑",
            ownerId = doc.getString("ownerId") ?: "",
            ownerName = doc.getString("ownerName") ?: "Owner",
            isPrivate = doc.getBoolean("isPrivate") ?: false,
            inviteCode = doc.getString("inviteCode") ?: "",
            announcement = doc.getString("announcement") ?: "",
            memberCount = (doc.getLong("memberCount") ?: memberUids.size.toLong()).toInt(),
            clubXp = (doc.getLong("clubXp") ?: 0L).toInt(),
            memberUids = memberUids
        )
    }
}
