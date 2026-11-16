package com.example.data

import android.content.Context
import com.example.model.AppNotificationItem
import com.example.model.FriendRequestItem
import com.example.model.FriendshipItem
import com.example.model.ModerationReportItem
import com.example.model.NotificationCategory
import com.example.model.PrivateChatMessageItem
import com.example.model.PublicProfileItem
import com.example.util.FirebaseProviders
import com.example.util.FirestoreOperation
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SocialAndLeaderboardRepository(
    private val context: Context,
    private val firestoreOverride: FirebaseFirestore? = null,
    private val authOverride: FirebaseAuth? = null
) {
    private val firestore: FirebaseFirestore? get() = firestoreOverride ?: FirebaseProviders.getFirestoreOrNull(context)
    private val auth: FirebaseAuth? get() = authOverride ?: FirebaseProviders.getAuthOrNull(context)

    suspend fun syncPlayerProfiles(
        displayName: String,
        email: String?,
        avatarEmoji: String,
        goldCoins: Int,
        cashNotes: Int,
        xp: Int,
        level: Int,
        matchesPlayed: Int,
        matchesWon: Int,
        winStreak: Int,
        bestWinStreak: Int,
        totalDiceRolls: Int,
        totalCaptures: Int,
        totalEarnings: Int,
        referralCode: String,
        activeClubId: String?,
        clubName: String?,
        isOnline: Boolean
    ) {
        val db = firestore ?: return
        val user = auth?.currentUser ?: return
        val uid = user.uid
        try {
            val userRef = db.collection("users").document(uid)
            val snap = userRef.get().await()
            if (!snap.exists()) {
                val initData = buildMap<String, Any> {
                    put("userId", uid)
                    put("displayName", displayName.take(40))
                    if (!email.isNullOrBlank()) put("email", email.take(120))
                    put("avatarEmoji", avatarEmoji.ifBlank { "👑" })
                    put("goldCoins", goldCoins.coerceAtLeast(0))
                    put("cashNotes", cashNotes.coerceAtLeast(0))
                    put("xp", xp.coerceAtLeast(0))
                    put("level", level.coerceAtLeast(1))
                    put("matchesPlayed", matchesPlayed.coerceAtLeast(0))
                    put("matchesWon", matchesWon.coerceAtLeast(0))
                    put("winStreak", winStreak.coerceAtLeast(0))
                    put("bestWinStreak", bestWinStreak.coerceAtLeast(0))
                    put("totalDiceRolls", totalDiceRolls.coerceAtLeast(0))
                    put("totalCaptures", totalCaptures.coerceAtLeast(0))
                    put("totalEarnings", totalEarnings.coerceAtLeast(0))
                    put("referralCode", referralCode.take(24))
                    if (!activeClubId.isNullOrBlank()) put("activeClubId", activeClubId)
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                userRef.set(initData).await()
            } else {
                val updateData = buildMap<String, Any> {
                    put("displayName", displayName.take(40))
                    put("avatarEmoji", avatarEmoji.ifBlank { "👑" })
                    put("goldCoins", goldCoins.coerceAtLeast(0))
                    put("cashNotes", cashNotes.coerceAtLeast(0))
                    put("xp", xp.coerceAtLeast(0))
                    put("level", level.coerceAtLeast(1))
                    put("matchesPlayed", matchesPlayed.coerceAtLeast(0))
                    put("matchesWon", matchesWon.coerceAtLeast(0))
                    put("winStreak", winStreak.coerceAtLeast(0))
                    put("bestWinStreak", bestWinStreak.coerceAtLeast(0))
                    put("totalDiceRolls", totalDiceRolls.coerceAtLeast(0))
                    put("totalCaptures", totalCaptures.coerceAtLeast(0))
                    put("totalEarnings", totalEarnings.coerceAtLeast(0))
                    if (!activeClubId.isNullOrBlank()) put("activeClubId", activeClubId)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                userRef.update(updateData).await()
            }

            val publicData = buildMap<String, Any> {
                put("userId", uid)
                put("displayName", displayName.take(40))
                put("avatarEmoji", avatarEmoji.ifBlank { "👑" })
                put("level", level.coerceAtLeast(1))
                put("xp", xp.coerceAtLeast(0))
                put("matchesPlayed", matchesPlayed.coerceAtLeast(0))
                put("matchesWon", matchesWon.coerceAtLeast(0).coerceAtMost(matchesPlayed.coerceAtLeast(0)))
                put("winStreak", winStreak.coerceAtLeast(0))
                put("totalEarnings", totalEarnings.coerceAtLeast(0))
                put("referralCode", referralCode.take(24))
                if (!activeClubId.isNullOrBlank()) put("activeClubId", activeClubId)
                if (!clubName.isNullOrBlank()) put("clubName", clubName.take(60))
                put("isOnline", isOnline)
                put("updatedAt", FieldValue.serverTimestamp())
            }
            db.collection("public_profiles").document(uid).set(publicData, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, FirestoreOperation.WRITE, "users/$uid", auth)
        }
    }

    fun observePublicProfiles(): Flow<List<PublicProfileItem>> = callbackFlow {
        val db = firestore
        if (db == null || auth?.currentUser == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("public_profiles")
            .whereGreaterThanOrEqualTo("level", 1)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "public_profiles", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    PublicProfileItem(
                        userId = doc.getString("userId") ?: doc.id,
                        displayName = doc.getString("displayName") ?: "Player",
                        avatarEmoji = doc.getString("avatarEmoji") ?: "👑",
                        level = (doc.getLong("level") ?: 1L).toInt(),
                        xp = (doc.getLong("xp") ?: 0L).toInt(),
                        matchesPlayed = (doc.getLong("matchesPlayed") ?: 0L).toInt(),
                        matchesWon = (doc.getLong("matchesWon") ?: 0L).toInt(),
                        winStreak = (doc.getLong("winStreak") ?: 0L).toInt(),
                        totalEarnings = (doc.getLong("totalEarnings") ?: 0L).toInt(),
                        referralCode = doc.getString("referralCode") ?: "",
                        activeClubId = doc.getString("activeClubId"),
                        clubName = doc.getString("clubName"),
                        isOnline = doc.getBoolean("isOnline") ?: false
                    )
                } ?: emptyList()
                trySend(list.sortedByDescending { it.totalEarnings })
            }
        awaitClose { reg.remove() }
    }

    fun observeFriendRequests(): Flow<List<FriendRequestItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("friend_requests")
            .whereEqualTo("toUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "friend_requests", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    val status = doc.getString("status") ?: "PENDING"
                    if (status != "PENDING") return@mapNotNull null
                    val ts = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    FriendRequestItem(
                        requestId = doc.getString("requestId") ?: doc.id,
                        fromUid = doc.getString("fromUid") ?: "",
                        toUid = doc.getString("toUid") ?: uid,
                        fromName = doc.getString("fromName") ?: "Player",
                        toName = doc.getString("toName") ?: "You",
                        status = status,
                        createdAtMillis = ts?.toDate()?.time ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeFriends(): Flow<List<FriendshipItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("friendships")
            .whereArrayContains("memberUids", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "friendships", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    val userA = doc.getString("userAUid") ?: return@mapNotNull null
                    val userB = doc.getString("userBUid") ?: return@mapNotNull null
                    val nameA = doc.getString("userAName") ?: "Player"
                    val nameB = doc.getString("userBName") ?: "Player"
                    val isA = userA != uid
                    FriendshipItem(
                        friendshipId = doc.getString("friendshipId") ?: doc.id,
                        friendUid = if (isA) userA else userB,
                        friendName = if (isA) nameA else nameB
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeFollowedPlayerIds(): Flow<Set<String>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptySet())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("follows")
            .whereEqualTo("followerUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "follows", auth)
                    return@addSnapshotListener
                }
                val ids = snap?.documents?.mapNotNull { it.getString("followedUid") }?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { reg.remove() }
    }

    fun observeNotifications(): Flow<List<AppNotificationItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val reg = db.collection("users").document(uid)
            .collection("notifications")
            .limit(40)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "users/$uid/notifications", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    val ts = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    AppNotificationItem(
                        notifId = doc.getString("notifId") ?: doc.id,
                        recipientUid = doc.getString("recipientUid") ?: uid,
                        senderId = doc.getString("senderId") ?: "",
                        category = NotificationCategory.fromWire(doc.getString("category")),
                        title = doc.getString("title") ?: "",
                        body = doc.getString("body") ?: "",
                        targetId = doc.getString("targetId"),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAtMillis = ts?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }?.sortedByDescending { it.createdAtMillis } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observePrivateChatMessages(otherUid: String): Flow<List<PrivateChatMessageItem>> = callbackFlow {
        val db = firestore
        val uid = auth?.currentUser?.uid
        if (db == null || uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val chatId = pairId(uid, otherUid)
        val reg = db.collection("private_chats").document(chatId)
            .collection("messages")
            .whereArrayContains("memberUids", uid)
            .limit(80)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "private_chats/$chatId/messages", auth)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    val ts = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    PrivateChatMessageItem(
                        messageId = doc.getString("messageId") ?: doc.id,
                        chatId = doc.getString("chatId") ?: chatId,
                        senderUid = doc.getString("senderUid") ?: "",
                        senderName = doc.getString("senderName") ?: "Player",
                        text = doc.getString("text") ?: "",
                        createdAtMillis = ts?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }?.sortedBy { it.createdAtMillis } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendFriendRequest(fromName: String, toUid: String, toName: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        if (user.uid == toUid) return Result.failure(IllegalStateException("Cannot add yourself"))
        val reqId = pairId(user.uid, toUid)
        return try {
            db.collection("friend_requests").document(reqId).set(
                mapOf(
                    "requestId" to reqId,
                    "fromUid" to user.uid,
                    "toUid" to toUid,
                    "fromName" to fromName.take(40),
                    "toName" to toName.take(40),
                    "status" to "PENDING",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            sendUserNotification(
                recipientUid = toUid,
                category = NotificationCategory.FRIEND_REQUEST,
                title = "New Friend Request",
                body = "$fromName wants to be your Ludo friend!",
                targetId = user.uid
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "friend_requests/$reqId", auth)))
        }
    }

    suspend fun respondToFriendRequest(req: FriendRequestItem, accept: Boolean): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        return try {
            val reqRef = db.collection("friend_requests").document(req.requestId)
            reqRef.update(
                mapOf(
                    "status" to if (accept) "ACCEPTED" else "DECLINED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (accept) {
                val friendshipId = pairId(req.fromUid, req.toUid)
                db.collection("friendships").document(friendshipId).set(
                    mapOf(
                        "friendshipId" to friendshipId,
                        "memberUids" to listOf(req.fromUid, req.toUid).sorted(),
                        "userAUid" to req.fromUid,
                        "userBUid" to req.toUid,
                        "userAName" to req.fromName,
                        "userBName" to req.toName,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "friend_requests/${req.requestId}", auth)))
        }
    }

    suspend fun toggleFollow(targetUid: String, targetName: String, currentlyFollowing: Boolean): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val followId = "${user.uid}_$targetUid"
        return try {
            val ref = db.collection("follows").document(followId)
            if (currentlyFollowing) {
                ref.delete().await()
            } else {
                ref.set(
                    mapOf(
                        "followId" to followId,
                        "followerUid" to user.uid,
                        "followedUid" to targetUid,
                        "followedName" to targetName.take(40),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "follows/$followId", auth)))
        }
    }

    suspend fun sendPrivateMessage(otherUid: String, senderName: String, text: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val chatId = pairId(user.uid, otherUid)
        val members = listOf(user.uid, otherUid).sorted()
        val msgId = "pm_${UUID.randomUUID().toString().take(12)}"
        return try {
            val threadRef = db.collection("private_chats").document(chatId)
            threadRef.set(
                mapOf(
                    "chatId" to chatId,
                    "memberUids" to members,
                    "lastMessage" to text.trim().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            threadRef.collection("messages").document(msgId).set(
                mapOf(
                    "messageId" to msgId,
                    "chatId" to chatId,
                    "senderUid" to user.uid,
                    "senderName" to senderName.take(40),
                    "memberUids" to members,
                    "text" to text.trim().take(500),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "private_chats/$chatId", auth)))
        }
    }

    suspend fun redeemReferralCode(referralCode: String, myName: String): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        return try {
            val cleanCode = referralCode.trim().uppercase()
            val match = db.collection("public_profiles")
                .whereEqualTo("referralCode", cleanCode)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?: return Result.failure(IllegalStateException("Referral code not found."))
            val referrerUid = match.getString("userId") ?: return Result.failure(IllegalStateException("Invalid referrer"))
            if (referrerUid == user.uid) return Result.failure(IllegalStateException("Cannot redeem your own code."))
            db.collection("referrals").document(user.uid).set(
                mapOf(
                    "inviteeUid" to user.uid,
                    "inviteeName" to myName.take(40),
                    "referrerUid" to referrerUid,
                    "referralCode" to cleanCode,
                    "status" to "VERIFIED",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(referrerUid)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "referrals/${user.uid}", auth)))
        }
    }

    suspend fun getVerifiedReferralsCount(): Int {
        val db = firestore ?: return 0
        val uid = auth?.currentUser?.uid ?: return 0
        return try {
            db.collection("referrals").whereEqualTo("referrerUid", uid).get().await().size()
        } catch (_: Exception) {
            0
        }
    }

    suspend fun sendUserNotification(
        recipientUid: String,
        category: NotificationCategory,
        title: String,
        body: String,
        targetId: String? = null
    ) {
        val db = firestore ?: return
        val senderUid = auth?.currentUser?.uid ?: return
        val notifId = "n_${UUID.randomUUID().toString().take(12)}"
        try {
            val data = buildMap<String, Any> {
                put("notifId", notifId)
                put("recipientUid", recipientUid)
                put("senderId", senderUid)
                put("category", category.wire)
                put("title", title.take(100))
                put("body", body.take(300))
                if (!targetId.isNullOrBlank()) put("targetId", targetId.take(128))
                put("isRead", false)
                put("createdAt", FieldValue.serverTimestamp())
            }
            db.collection("users").document(recipientUid)
                .collection("notifications").document(notifId)
                .set(data)
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, FirestoreOperation.WRITE, "users/$recipientUid/notifications", auth)
        }
    }

    suspend fun markNotificationRead(notifId: String) {
        val db = firestore ?: return
        val uid = auth?.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid).collection("notifications").document(notifId)
                .update("isRead", true).await()
        } catch (_: Exception) {}
    }

    suspend fun submitModerationReport(report: ModerationReportItem): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val id = report.reportId.ifBlank { "rep_${UUID.randomUUID().toString().take(12)}" }
        return try {
            db.collection("moderation_reports").document(id).set(
                mapOf(
                    "reportId" to id,
                    "reporterUid" to user.uid,
                    "targetUid" to report.targetUid,
                    "contextType" to report.contextType,
                    "contextId" to report.contextId,
                    "reason" to report.reason.trim().take(250),
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "moderation_reports/$id", auth)))
        }
    }

    private fun pairId(a: String, b: String): String = listOf(a, b).sorted().joinToString("_")
}
