package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

enum class TaskCategory(val wire: String, val label: String) {
    DAILY("DAILY", "Daily Tasks"),
    WEEKLY("WEEKLY", "Weekly Challenges"),
    CHALLENGE("CHALLENGE", "Special Challenges"),
    LOGIN_STREAK("LOGIN_STREAK", "Login Streak"),
    WIN_STREAK("WIN_STREAK", "Win Streak"),
    CLUB("CLUB", "Club Rewards"),
    TOURNAMENT("TOURNAMENT", "Tournament"),
    LEVEL("LEVEL", "Level Milestones"),
    EVENT("EVENT", "Event Missions"),
    ACHIEVEMENT("ACHIEVEMENT", "Achievements");

    companion object {
        fun fromWire(value: String?): TaskCategory =
            entries.firstOrNull { it.wire == value } ?: DAILY
    }
}

enum class TaskStatus(val wire: String, val label: String) {
    IN_PROGRESS("IN_PROGRESS", "In Progress"),
    COMPLETED("COMPLETED", "Completed"),
    CLAIMED("CLAIMED", "Claimed");

    companion object {
        fun fromWire(value: String?): TaskStatus =
            entries.firstOrNull { it.wire == value } ?: IN_PROGRESS
    }
}

@IgnoreExtraProperties
data class VerifiedTaskItem(
    val taskId: String = "",
    val userId: String = "",
    val category: TaskCategory = TaskCategory.DAILY,
    val title: String = "",
    val description: String = "",
    val progress: Int = 0,
    val target: Int = 1,
    val rewardCoins: Int = 0,
    val rewardCash: Int = 0,
    val rewardXp: Int = 0,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    val periodKey: String = "",
    val verifiedEventIds: List<String> = emptyList(),
    val rewardTxId: String? = null,
    val claimedAtMillis: Long? = null,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val isCompletedOrClaimed: Boolean get() = progress >= target
    val canClaim: Boolean get() = progress >= target && status == TaskStatus.COMPLETED
    val progressFraction: Float get() = if (target <= 0) 0f else (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    fun toFirestoreMap(includeServerTimestamp: Boolean = true): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "taskId" to taskId,
            "userId" to userId,
            "category" to category.wire,
            "title" to title,
            "progress" to progress,
            "target" to target,
            "rewardCoins" to rewardCoins,
            "rewardCash" to rewardCash,
            "rewardXp" to rewardXp,
            "status" to status.wire,
            "periodKey" to periodKey,
            "verifiedEventIds" to verifiedEventIds.takeLast(100),
            "rewardTxId" to rewardTxId,
            "updatedAt" to if (includeServerTimestamp) FieldValue.serverTimestamp() else Timestamp.now()
        )
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot, descriptionFallback: String = ""): VerifiedTaskItem? {
            if (!doc.exists()) return null
            val progress = (doc.getLong("progress") ?: 0L).toInt()
            val target = (doc.getLong("target") ?: 1L).toInt().coerceAtLeast(1)
            val status = TaskStatus.fromWire(doc.getString("status"))
            val ts = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val claimedTs = doc.getTimestamp("claimedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            @Suppress("UNCHECKED_CAST")
            val events = (doc.get("verifiedEventIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            return VerifiedTaskItem(
                taskId = doc.getString("taskId") ?: doc.id,
                userId = doc.getString("userId") ?: "",
                category = TaskCategory.fromWire(doc.getString("category")),
                title = doc.getString("title") ?: doc.id,
                description = descriptionFallback,
                progress = progress,
                target = target,
                rewardCoins = (doc.getLong("rewardCoins") ?: 0L).toInt(),
                rewardCash = (doc.getLong("rewardCash") ?: 0L).toInt(),
                rewardXp = (doc.getLong("rewardXp") ?: 0L).toInt(),
                status = status,
                periodKey = doc.getString("periodKey") ?: "",
                verifiedEventIds = events,
                rewardTxId = doc.getString("rewardTxId"),
                claimedAtMillis = claimedTs?.toDate()?.time,
                updatedAtMillis = ts?.toDate()?.time ?: System.currentTimeMillis()
            )
        }
    }
}

@IgnoreExtraProperties
data class RewardHistoryEntry(
    val txId: String = "",
    val userId: String = "",
    val sourceType: String = "",
    val sourceId: String = "",
    val periodKey: String = "",
    val coinsDelta: Int = 0,
    val cashDelta: Int = 0,
    val xpDelta: Int = 0,
    val description: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "txId" to txId,
        "userId" to userId,
        "sourceType" to sourceType,
        "sourceId" to sourceId,
        "periodKey" to periodKey,
        "coinsDelta" to coinsDelta,
        "cashDelta" to cashDelta,
        "xpDelta" to xpDelta,
        "description" to description,
        "createdAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): RewardHistoryEntry? {
            if (!doc.exists()) return null
            val ts = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            return RewardHistoryEntry(
                txId = doc.getString("txId") ?: doc.id,
                userId = doc.getString("userId") ?: "",
                sourceType = doc.getString("sourceType") ?: "",
                sourceId = doc.getString("sourceId") ?: "",
                periodKey = doc.getString("periodKey") ?: "",
                coinsDelta = (doc.getLong("coinsDelta") ?: 0L).toInt(),
                cashDelta = (doc.getLong("cashDelta") ?: 0L).toInt(),
                xpDelta = (doc.getLong("xpDelta") ?: 0L).toInt(),
                description = doc.getString("description") ?: "",
                createdAtMillis = ts?.toDate()?.time ?: System.currentTimeMillis()
            )
        }
    }
}

data class DailyActivitySummary(
    val dayKey: String = "",
    val userId: String = "",
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val diceRolls: Int = 0,
    val captures: Int = 0,
    val minutesPlayed: Int = 0
)

enum class NotificationCategory(val wire: String, val label: String) {
    FRIEND_REQUEST("FRIEND_REQUEST", "Friend Requests"),
    CLUB_INVITE("CLUB_INVITE", "Club Invitations"),
    CLUB_MESSAGE("CLUB_MESSAGE", "Club Messages"),
    MATCH_INVITE("MATCH_INVITE", "Match Invites"),
    TASK_COMPLETE("TASK_COMPLETE", "Task Completed"),
    REWARD_AVAILABLE("REWARD_AVAILABLE", "Rewards Available"),
    ACHIEVEMENT("ACHIEVEMENT", "Achievements Unlocked");

    companion object {
        fun fromWire(value: String?): NotificationCategory =
            entries.firstOrNull { it.wire == value } ?: REWARD_AVAILABLE
    }
}

data class NotificationPreferences(
    val friendRequests: Boolean = true,
    val clubInvites: Boolean = true,
    val clubMessages: Boolean = true,
    val matchInvites: Boolean = true,
    val taskComplete: Boolean = true,
    val rewardsAvailable: Boolean = true,
    val achievements: Boolean = true,
    val systemNotificationsEnabled: Boolean = true
)

data class AppNotificationItem(
    val notifId: String = "",
    val recipientUid: String = "",
    val senderId: String = "",
    val category: NotificationCategory = NotificationCategory.TASK_COMPLETE,
    val title: String = "",
    val body: String = "",
    val targetId: String? = null,
    val isRead: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class PublicProfileItem(
    val userId: String = "",
    val displayName: String = "",
    val avatarEmoji: String = "👑",
    val level: Int = 1,
    val xp: Int = 0,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val winStreak: Int = 0,
    val totalEarnings: Int = 0,
    val referralCode: String = "",
    val activeClubId: String? = null,
    val clubName: String? = null,
    val isOnline: Boolean = false
)

data class FriendRequestItem(
    val requestId: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val fromName: String = "",
    val toName: String = "",
    val status: String = "PENDING",
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class FriendshipItem(
    val friendshipId: String = "",
    val friendUid: String = "",
    val friendName: String = "",
    val isOnline: Boolean = false,
    val level: Int = 1,
    val avatarEmoji: String = "👑"
)

data class PrivateChatMessageItem(
    val messageId: String = "",
    val chatId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class VoiceClubRole(val wire: String, val badge: String) {
    OWNER("OWNER", "Owner"),
    ADMIN("ADMIN", "Admin"),
    MODERATOR("MODERATOR", "Mod"),
    MEMBER("MEMBER", "Member");

    fun canModerate(): Boolean = this == OWNER || this == ADMIN || this == MODERATOR
    fun canManageClub(): Boolean = this == OWNER || this == ADMIN

    companion object {
        fun fromWire(value: String?): VoiceClubRole =
            entries.firstOrNull { it.wire == value } ?: MEMBER
    }
}

data class VoiceClubItem(
    val clubId: String = "",
    val name: String = "",
    val description: String = "",
    val avatarEmoji: String = "👑",
    val ownerId: String = "",
    val ownerName: String = "",
    val isPrivate: Boolean = false,
    val inviteCode: String = "",
    val announcement: String = "",
    val memberCount: Int = 1,
    val clubXp: Int = 0,
    val memberUids: List<String> = emptyList()
)

data class VoiceClubMemberItem(
    val uid: String = "",
    val clubId: String = "",
    val displayName: String = "",
    val role: VoiceClubRole = VoiceClubRole.MEMBER,
    val isOnline: Boolean = true,
    val isMutedByMod: Boolean = false
)

data class ClubChatMessageItem(
    val messageId: String = "",
    val clubId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val isAnnouncement: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ClubInvitationItem(
    val inviteId: String = "",
    val clubId: String = "",
    val clubName: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val toUid: String = "",
    val status: String = "PENDING"
)

data class ClubVoiceRoomItem(
    val roomId: String = "",
    val clubId: String = "",
    val title: String = "",
    val hostUid: String = "",
    val isPublic: Boolean = true,
    val allowedUids: List<String> = emptyList(),
    val isActive: Boolean = true
)

enum class VoiceChannelMode(val wire: String, val label: String) {
    ROOM("ROOM", "All Players"),
    TEAM("TEAM", "Team Voice"),
    PRIVATE("PRIVATE", "Private Friend");

    companion object {
        fun fromWire(value: String?): VoiceChannelMode =
            entries.firstOrNull { it.wire == value } ?: ROOM
    }
}

data class VoiceParticipantState(
    val uid: String = "",
    val displayName: String = "",
    val teamId: Int = 0,
    val voiceChannel: VoiceChannelMode = VoiceChannelMode.ROOM,
    val isMicMuted: Boolean = false,
    val isSpeakerMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val voiceLevel: Float = 0f,
    val localVolume: Float = 1f,
    val isBlockedLocally: Boolean = false
)

data class MatchRoomItem(
    val roomId: String = "",
    val roomCode: String = "",
    val ownerId: String = "",
    val isPrivate: Boolean = false,
    val allowedUids: List<String> = emptyList(),
    val invitedUids: List<String> = emptyList(),
    val playerUids: List<String> = emptyList(),
    val maxPlayers: Int = 4,
    val gameMode: String = "ONLINE_MULTIPLAYER",
    val wagerCoins: Int = 500,
    val status: String = "WAITING",
    val currentTurnUid: String = "",
    val moveSequence: Int = 0,
    val stateHash: String = "",
    val winnerUid: String? = null,
    val turnDeadlineMillis: Long = 0L,
    val boardState: Map<String, Any> = emptyMap()
)

data class ModerationReportItem(
    val reportId: String = "",
    val reporterUid: String = "",
    val targetUid: String = "",
    val contextType: String = "VOICE_CLUB",
    val contextId: String = "",
    val reason: String = "",
    val status: String = "OPEN"
)
