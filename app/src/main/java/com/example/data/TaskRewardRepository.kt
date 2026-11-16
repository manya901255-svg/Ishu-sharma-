package com.example.data

import android.content.Context
import com.example.model.DailyActivitySummary
import com.example.model.RewardHistoryEntry
import com.example.model.TaskCategory
import com.example.model.TaskStatus
import com.example.model.VerifiedTaskItem
import com.example.util.FirebaseProviders
import com.example.util.FirestoreOperation
import com.example.util.LudoPeriodKeys
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class TaskCatalogSpec(
    val taskId: String,
    val category: TaskCategory,
    val title: String,
    val description: String,
    val target: Int,
    val rewardCoins: Int,
    val rewardCash: Int = 0,
    val rewardXp: Int = 25
)

data class ClaimOutcome(
    val success: Boolean,
    val message: String,
    val coinsGranted: Int = 0,
    val cashGranted: Int = 0,
    val xpGranted: Int = 0,
    val txId: String? = null
)

/**
 * Authoritative, cheat-proof Task, Achievement, Streak, and Reward Engine.
 * - Ties every task & achievement strictly to real gameplay or verified social events.
 * - Blocks claims when progress < target or status == CLAIMED.
 * - Deduplicates every eventId and every reward transaction ID (txId).
 * - Persists state both locally (survives offline & restarts) and atomically in Firestore
 *   (validated by firestore.rules `existsAfter(reward_history/{txId})` and `progress >= target`).
 */
class TaskRewardRepository(
    private val context: Context,
    private val firestoreOverride: FirebaseFirestore? = null,
    private val authOverride: FirebaseAuth? = null
) {
    private val prefs = context.getSharedPreferences("mgludo_verified_tasks_v2", Context.MODE_PRIVATE)
    private val firestore: FirebaseFirestore? get() = firestoreOverride ?: FirebaseProviders.getFirestoreOrNull(context)
    private val auth: FirebaseAuth? get() = authOverride ?: FirebaseProviders.getAuthOrNull(context)

    private val _tasks = MutableStateFlow<List<VerifiedTaskItem>>(emptyList())
    val tasks: StateFlow<List<VerifiedTaskItem>> = _tasks.asStateFlow()

    private val _achievements = MutableStateFlow<List<VerifiedTaskItem>>(emptyList())
    val achievements: StateFlow<List<VerifiedTaskItem>> = _achievements.asStateFlow()

    private val _rewardHistory = MutableStateFlow<List<RewardHistoryEntry>>(emptyList())
    val rewardHistory: StateFlow<List<RewardHistoryEntry>> = _rewardHistory.asStateFlow()

    private val _todayActivity = MutableStateFlow(DailyActivitySummary())
    val todayActivity: StateFlow<DailyActivitySummary> = _todayActivity.asStateFlow()

    private val _loginStreak = MutableStateFlow(0)
    val loginStreak: StateFlow<Int> = _loginStreak.asStateFlow()

    private val _winStreak = MutableStateFlow(0)
    val winStreak: StateFlow<Int> = _winStreak.asStateFlow()

    val catalog: List<TaskCatalogSpec> = listOf(
        // Core Daily Tasks
        TaskCatalogSpec("daily_play_3", TaskCategory.DAILY, "Play 3 Ludo matches", "Finish 3 complete matches today", 3, 100, 0, 30),
        TaskCatalogSpec("daily_win_2", TaskCategory.DAILY, "Win 2 matches", "Achieve 2 victories today", 2, 150, 2, 50),
        TaskCatalogSpec("daily_roll_50", TaskCategory.DAILY, "Roll a total of 50 dice", "Roll 50 dice in active matches", 50, 50, 0, 20),
        TaskCatalogSpec("daily_capture_5", TaskCategory.DAILY, "Capture 5 opponent pawns", "Cut 5 opponent pawns in gameplay", 5, 120, 1, 35),
        TaskCatalogSpec("daily_invite_1", TaskCategory.DAILY, "Invite a friend who joins", "Friend must sign up with your invite code", 1, 200, 3, 80),
        // Weekly Challenges
        TaskCatalogSpec("weekly_matches_15", TaskCategory.WEEKLY, "Weekly Arena: 15 Matches", "Finish 15 matches this week", 15, 1200, 5, 150),
        TaskCatalogSpec("weekly_wins_7", TaskCategory.WEEKLY, "Weekly Champion: 7 Wins", "Win 7 matches this week", 7, 2000, 8, 250),
        TaskCatalogSpec("weekly_sixes_25", TaskCategory.WEEKLY, "Golden Sixes: Roll 25 Sixes", "Roll twenty-five 6s in matches this week", 25, 800, 2, 100),
        // Streaks, Club, Tournament, Level & Event Missions
        TaskCatalogSpec("streak_login_7", TaskCategory.LOGIN_STREAK, "7-Day Royal Login Streak", "Sign in 7 consecutive days", 7, 1000, 5, 120),
        TaskCatalogSpec("streak_win_3", TaskCategory.WIN_STREAK, "Unstoppable: 3 Win Streak", "Win 3 consecutive matches", 3, 500, 4, 90),
        TaskCatalogSpec("club_voice_3", TaskCategory.CLUB, "Voice Club Contributor", "Send 3 messages or host a club voice room", 3, 300, 2, 60),
        TaskCatalogSpec("tournament_wins_4", TaskCategory.TOURNAMENT, "Tournament Contender", "Win 4 multiplayer or Rush matches", 4, 1500, 6, 180),
        TaskCatalogSpec("level_milestone_5", TaskCategory.LEVEL, "Ascend to Level 5", "Reach Player Level 5 via real XP", 5, 1000, 5, 0),
        TaskCatalogSpec("event_captures_10", TaskCategory.EVENT, "Royal Festival Hunt", "Capture 10 opponent pawns during the event", 10, 900, 3, 100)
    )

    val achievementCatalog: List<TaskCatalogSpec> = listOf(
        TaskCatalogSpec("ach_first_blood", TaskCategory.ACHIEVEMENT, "First Royal Victory", "Win your very first Ludo match", 1, 250, 2, 50),
        TaskCatalogSpec("ach_ten_wins", TaskCategory.ACHIEVEMENT, "Grand Maharaja (10 Wins)", "Win 10 complete Ludo matches", 10, 1200, 5, 200),
        TaskCatalogSpec("ach_fifty_captures", TaskCategory.ACHIEVEMENT, "Cut Master (50 Captures)", "Capture 50 opponent pawns", 50, 1500, 6, 250),
        TaskCatalogSpec("ach_hundred_rolls", TaskCategory.ACHIEVEMENT, "High Roller (100 Rolls)", "Roll the dice 100 times in gameplay", 100, 400, 1, 75),
        TaskCatalogSpec("ach_ambassador_3", TaskCategory.ACHIEVEMENT, "Royal Ambassador", "Invite 3 friends who successfully join", 3, 1000, 10, 180)
    )

    private fun periodKeyFor(category: TaskCategory, nowMillis: Long): String {
        return when (category) {
            TaskCategory.DAILY -> LudoPeriodKeys.currentDayKey(nowMillis)
            TaskCategory.WEEKLY -> LudoPeriodKeys.currentWeekKey(nowMillis)
            TaskCategory.EVENT -> "EVENT-${LudoPeriodKeys.currentWeekKey(nowMillis)}"
            else -> "LIFETIME"
        }
    }

    private fun activeUid(explicitUid: String? = null): String {
        return explicitUid ?: auth?.currentUser?.uid ?: "local_device_user"
    }

    fun initializeForUser(uid: String? = null, nowMillis: Long = System.currentTimeMillis()) {
        val resolvedUid = activeUid(uid)
        val loadedTasks = catalog.map { spec ->
            loadOrResetTaskFromPrefs(resolvedUid, spec, nowMillis)
        }
        val loadedAchievements = achievementCatalog.map { spec ->
            loadOrResetTaskFromPrefs(resolvedUid, spec, nowMillis)
        }
        _tasks.value = loadedTasks
        _achievements.value = loadedAchievements
        _rewardHistory.value = loadHistoryFromPrefs(resolvedUid)
        _loginStreak.value = prefs.getInt("${resolvedUid}_login_streak", 0)
        _winStreak.value = prefs.getInt("${resolvedUid}_win_streak", 0)
        val dayKey = LudoPeriodKeys.currentDayKey(nowMillis)
        _todayActivity.value = DailyActivitySummary(
            dayKey = dayKey,
            userId = resolvedUid,
            matchesPlayed = prefs.getInt("${resolvedUid}_act_${dayKey}_played", 0),
            matchesWon = prefs.getInt("${resolvedUid}_act_${dayKey}_won", 0),
            diceRolls = prefs.getInt("${resolvedUid}_act_${dayKey}_rolls", 0),
            captures = prefs.getInt("${resolvedUid}_act_${dayKey}_captures", 0),
            minutesPlayed = prefs.getInt("${resolvedUid}_act_${dayKey}_mins", 0)
        )
    }

    suspend fun syncWithFirestore(uid: String? = null, nowMillis: Long = System.currentTimeMillis()) {
        val db = firestore ?: return
        val currentUser = auth?.currentUser ?: return
        val resolvedUid = uid ?: currentUser.uid
        initializeForUser(resolvedUid, nowMillis)

        try {
            val remoteTasks = db.collection("users").document(resolvedUid).collection("tasks").get().await()
            val mergedTasks = catalog.map { spec ->
                val expectedPeriod = periodKeyFor(spec.category, nowMillis)
                val remoteDoc = remoteTasks.documents.firstOrNull { it.id == spec.taskId }
                val remoteItem = remoteDoc?.let { VerifiedTaskItem.fromSnapshot(it, spec.description) }
                val localItem = _tasks.value.firstOrNull { it.taskId == spec.taskId }
                val chosen = when {
                    remoteItem != null && remoteItem.periodKey == expectedPeriod -> {
                        if (localItem != null && localItem.periodKey == expectedPeriod) {
                            if (remoteItem.status == TaskStatus.CLAIMED || remoteItem.progress > localItem.progress) {
                                remoteItem
                            } else localItem
                        } else remoteItem
                    }
                    localItem != null -> localItem
                    else -> createBlankTask(resolvedUid, spec, expectedPeriod)
                }
                if (remoteItem == null || remoteItem.periodKey != expectedPeriod) {
                    db.collection("users").document(resolvedUid).collection("tasks")
                        .document(spec.taskId)
                        .set(chosen.toFirestoreMap())
                        .await()
                }
                saveTaskToPrefs(resolvedUid, chosen)
                chosen
            }
            _tasks.value = mergedTasks

            val remoteAchs = db.collection("users").document(resolvedUid).collection("achievements").get().await()
            val mergedAchs = achievementCatalog.map { spec ->
                val remoteDoc = remoteAchs.documents.firstOrNull { it.id == spec.taskId }
                val remoteItem = remoteDoc?.let { VerifiedTaskItem.fromSnapshot(it, spec.description) }
                val localItem = _achievements.value.firstOrNull { it.taskId == spec.taskId }
                val chosen = when {
                    remoteItem != null && (localItem == null || remoteItem.status == TaskStatus.CLAIMED || remoteItem.progress > localItem.progress) -> remoteItem
                    localItem != null -> localItem
                    else -> createBlankTask(resolvedUid, spec, "LIFETIME")
                }
                if (remoteItem == null) {
                    db.collection("users").document(resolvedUid).collection("achievements")
                        .document(spec.taskId)
                        .set(chosen.toFirestoreMap())
                        .await()
                }
                saveTaskToPrefs(resolvedUid, chosen)
                chosen
            }
            _achievements.value = mergedAchs

            val historySnap = db.collection("users").document(resolvedUid)
                .collection("reward_history")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            val remoteHistory = historySnap.documents.mapNotNull { RewardHistoryEntry.fromSnapshot(it) }
            if (remoteHistory.isNotEmpty()) {
                _rewardHistory.value = remoteHistory
                saveHistoryToPrefs(resolvedUid, remoteHistory)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, FirestoreOperation.READ, "users/$resolvedUid/tasks", auth)
        }
    }

    suspend fun recordDailyLogin(uid: String? = null, nowMillis: Long = System.currentTimeMillis()): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        val today = LudoPeriodKeys.currentDayKey(nowMillis)
        val yesterday = LudoPeriodKeys.previousDayKey(nowMillis)
        val lastLoginDay = prefs.getString("${resolvedUid}_last_login_day", null)
        var streak = prefs.getInt("${resolvedUid}_login_streak", 0)
        if (lastLoginDay == today) {
            _loginStreak.value = streak
            return emptyList()
        }
        streak = if (lastLoginDay == yesterday) streak + 1 else 1
        prefs.edit()
            .putString("${resolvedUid}_last_login_day", today)
            .putInt("${resolvedUid}_login_streak", streak)
            .apply()
        _loginStreak.value = streak
        return advanceTaskAbsolute(resolvedUid, "streak_login_7", streak, "login_$today", nowMillis)
    }

    suspend fun recordDiceRolled(
        rolledValue: Int,
        eventId: String = "roll_${UUID.randomUUID()}",
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        incrementActivityStat(resolvedUid, nowMillis, rolls = 1)
        val newlyCompleted = mutableListOf<VerifiedTaskItem>()
        newlyCompleted += advanceTaskProgress(resolvedUid, "daily_roll_50", 1, eventId, nowMillis)
        newlyCompleted += advanceTaskProgress(resolvedUid, "ach_hundred_rolls", 1, eventId, nowMillis)
        if (rolledValue == 6) {
            newlyCompleted += advanceTaskProgress(resolvedUid, "weekly_sixes_25", 1, eventId, nowMillis)
        }
        return newlyCompleted
    }

    suspend fun recordPawnCaptured(
        eventId: String = "cap_${UUID.randomUUID()}",
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        incrementActivityStat(resolvedUid, nowMillis, captures = 1)
        val newlyCompleted = mutableListOf<VerifiedTaskItem>()
        newlyCompleted += advanceTaskProgress(resolvedUid, "daily_capture_5", 1, eventId, nowMillis)
        newlyCompleted += advanceTaskProgress(resolvedUid, "event_captures_10", 1, eventId, nowMillis)
        newlyCompleted += advanceTaskProgress(resolvedUid, "ach_fifty_captures", 1, eventId, nowMillis)
        return newlyCompleted
    }

    suspend fun recordMatchCompleted(
        didWin: Boolean,
        isOnlineOrTournament: Boolean,
        durationMinutes: Int = 3,
        matchId: String = "match_${UUID.randomUUID()}",
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        incrementActivityStat(
            resolvedUid,
            nowMillis,
            played = 1,
            won = if (didWin) 1 else 0,
            minutes = durationMinutes
        )
        val newlyCompleted = mutableListOf<VerifiedTaskItem>()
        newlyCompleted += advanceTaskProgress(resolvedUid, "daily_play_3", 1, "${matchId}_play", nowMillis)
        newlyCompleted += advanceTaskProgress(resolvedUid, "weekly_matches_15", 1, "${matchId}_wplay", nowMillis)

        if (didWin) {
            val newWinStreak = prefs.getInt("${resolvedUid}_win_streak", 0) + 1
            prefs.edit().putInt("${resolvedUid}_win_streak", newWinStreak).apply()
            _winStreak.value = newWinStreak
            newlyCompleted += advanceTaskProgress(resolvedUid, "daily_win_2", 1, "${matchId}_win", nowMillis)
            newlyCompleted += advanceTaskProgress(resolvedUid, "weekly_wins_7", 1, "${matchId}_wwin", nowMillis)
            newlyCompleted += advanceTaskAbsolute(resolvedUid, "streak_win_3", newWinStreak, "${matchId}_streak", nowMillis)
            newlyCompleted += advanceTaskProgress(resolvedUid, "ach_first_blood", 1, "${matchId}_fb", nowMillis)
            newlyCompleted += advanceTaskProgress(resolvedUid, "ach_ten_wins", 1, "${matchId}_10w", nowMillis)
            if (isOnlineOrTournament) {
                newlyCompleted += advanceTaskProgress(resolvedUid, "tournament_wins_4", 1, "${matchId}_tourn", nowMillis)
            }
        } else {
            prefs.edit().putInt("${resolvedUid}_win_streak", 0).apply()
            _winStreak.value = 0
        }
        return newlyCompleted
    }

    suspend fun recordVerifiedFriendInvite(
        referralId: String,
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        val newlyCompleted = mutableListOf<VerifiedTaskItem>()
        newlyCompleted += advanceTaskProgress(resolvedUid, "daily_invite_1", 1, "ref_$referralId", nowMillis)
        newlyCompleted += advanceTaskProgress(resolvedUid, "ach_ambassador_3", 1, "ref_$referralId", nowMillis)
        return newlyCompleted
    }

    suspend fun recordClubActivity(
        activityEventId: String = "club_${UUID.randomUUID()}",
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        return advanceTaskProgress(resolvedUid, "club_voice_3", 1, activityEventId, nowMillis)
    }

    suspend fun recordPlayerLevel(
        level: Int,
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VerifiedTaskItem> {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        return advanceTaskAbsolute(resolvedUid, "level_milestone_5", level, "lvl_$level", nowMillis)
    }

    /**
     * Strictly verifies completion, guards against duplicate claims & premature claims,
     * appends a unique transaction to reward_history, and updates Firestore atomically.
     */
    suspend fun claimTaskReward(
        taskId: String,
        uid: String? = null,
        explicitTxId: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): ClaimOutcome {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        val isAchievement = achievementCatalog.any { it.taskId == taskId }
        val currentList = if (isAchievement) _achievements.value else _tasks.value
        val current = currentList.firstOrNull { it.taskId == taskId }
            ?: return ClaimOutcome(false, "Task not found.")

        val expectedPeriod = periodKeyFor(current.category, nowMillis)
        if (current.periodKey != expectedPeriod) {
            initializeForUser(resolvedUid, nowMillis)
            return ClaimOutcome(false, "This task period has reset. Complete the new task first.")
        }
        if (current.status == TaskStatus.CLAIMED) {
            return ClaimOutcome(false, "Reward already claimed for this period.")
        }
        if (current.progress < current.target) {
            return ClaimOutcome(
                false,
                "Task incomplete (${current.progress}/${current.target}). Complete it in real gameplay first."
            )
        }

        val txId = explicitTxId ?: "tx_${taskId}_${expectedPeriod}_${UUID.randomUUID().toString().take(8)}"
        if (_rewardHistory.value.any { it.txId == txId } ||
            (_rewardHistory.value.any { it.sourceId == taskId && it.periodKey == expectedPeriod })
        ) {
            return ClaimOutcome(false, "Duplicate reward transaction blocked.")
        }

        val entry = RewardHistoryEntry(
            txId = txId,
            userId = resolvedUid,
            sourceType = current.category.wire,
            sourceId = current.taskId,
            periodKey = expectedPeriod,
            coinsDelta = current.rewardCoins,
            cashDelta = current.rewardCash,
            xpDelta = current.rewardXp,
            description = "Claimed: ${current.title}",
            createdAtMillis = nowMillis
        )
        val claimedTask = current.copy(
            status = TaskStatus.CLAIMED,
            rewardTxId = txId,
            claimedAtMillis = nowMillis,
            updatedAtMillis = nowMillis
        )

        val db = firestore
        val firebaseUser = auth?.currentUser
        if (db != null && firebaseUser != null && firebaseUser.uid == resolvedUid) {
            try {
                val subCol = if (isAchievement) "achievements" else "tasks"
                val userRef = db.collection("users").document(resolvedUid)
                val taskRef = userRef.collection(subCol).document(taskId)
                val historyRef = userRef.collection("reward_history").document(txId)

                val batch = db.batch()
                batch.set(historyRef, entry.toFirestoreMap())
                batch.update(
                    taskRef,
                    mapOf(
                        "status" to TaskStatus.CLAIMED.wire,
                        "rewardTxId" to txId,
                        "claimedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } catch (e: Exception) {
                val errMsg = handleFirestoreError(e, FirestoreOperation.WRITE, "users/$resolvedUid/tasks/$taskId", auth)
                return ClaimOutcome(false, errMsg)
            }
        }

        saveTaskToPrefs(resolvedUid, claimedTask)
        if (isAchievement) {
            _achievements.value = _achievements.value.map { if (it.taskId == taskId) claimedTask else it }
        } else {
            _tasks.value = _tasks.value.map { if (it.taskId == taskId) claimedTask else it }
        }
        val updatedHistory = listOf(entry) + _rewardHistory.value
        _rewardHistory.value = updatedHistory
        saveHistoryToPrefs(resolvedUid, updatedHistory)

        return ClaimOutcome(
            success = true,
            message = "Verified reward granted: +${current.rewardCoins} coins" +
                if (current.rewardCash > 0) ", +${current.rewardCash} cash" else "",
            coinsGranted = current.rewardCoins,
            cashGranted = current.rewardCash,
            xpGranted = current.rewardXp,
            txId = txId
        )
    }

    /**
     * Records a verified non-task reward (e.g., daily login wheel / spin) with unique txId + periodKey
     * so it can never be claimed twice in the same day/event.
     */
    suspend fun recordVerifiedBonusReward(
        sourceType: String,
        sourceId: String,
        periodKey: String,
        coinsDelta: Int,
        cashDelta: Int,
        description: String,
        uid: String? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): ClaimOutcome {
        val resolvedUid = activeUid(uid)
        ensureInitialized(resolvedUid, nowMillis)
        if (_rewardHistory.value.any { it.sourceId == sourceId && it.periodKey == periodKey }) {
            return ClaimOutcome(false, "Already claimed for $periodKey.")
        }
        val txId = "tx_${sourceId}_${periodKey}_${UUID.randomUUID().toString().take(6)}"
        val entry = RewardHistoryEntry(
            txId = txId,
            userId = resolvedUid,
            sourceType = sourceType,
            sourceId = sourceId,
            periodKey = periodKey,
            coinsDelta = coinsDelta,
            cashDelta = cashDelta,
            xpDelta = 15,
            description = description,
            createdAtMillis = nowMillis
        )
        val db = firestore
        val firebaseUser = auth?.currentUser
        if (db != null && firebaseUser != null && firebaseUser.uid == resolvedUid) {
            try {
                db.collection("users").document(resolvedUid)
                    .collection("reward_history").document(txId)
                    .set(entry.toFirestoreMap())
                    .await()
            } catch (e: Exception) {
                return ClaimOutcome(false, handleFirestoreError(e, FirestoreOperation.WRITE, "reward_history/$txId", auth))
            }
        }
        val updated = listOf(entry) + _rewardHistory.value
        _rewardHistory.value = updated
        saveHistoryToPrefs(resolvedUid, updated)
        return ClaimOutcome(true, description, coinsDelta, cashDelta, 15, txId)
    }

    private suspend fun advanceTaskProgress(
        uid: String,
        taskId: String,
        delta: Int,
        eventId: String,
        nowMillis: Long
    ): List<VerifiedTaskItem> {
        val isAchievement = achievementCatalog.any { it.taskId == taskId }
        val currentList = if (isAchievement) _achievements.value else _tasks.value
        val existing = currentList.firstOrNull { it.taskId == taskId } ?: return emptyList()
        if (existing.status == TaskStatus.CLAIMED) return emptyList()
        if (eventId in existing.verifiedEventIds) return emptyList()

        val newProgress = (existing.progress + delta).coerceAtMost(existing.target)
        val newStatus = if (newProgress >= existing.target) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS
        val updated = existing.copy(
            progress = newProgress,
            status = newStatus,
            verifiedEventIds = (existing.verifiedEventIds + eventId).takeLast(100),
            updatedAtMillis = nowMillis
        )
        persistUpdatedTask(uid, updated, isAchievement)
        return if (existing.status != TaskStatus.COMPLETED && newStatus == TaskStatus.COMPLETED) listOf(updated) else emptyList()
    }

    private suspend fun advanceTaskAbsolute(
        uid: String,
        taskId: String,
        absoluteValue: Int,
        eventId: String,
        nowMillis: Long
    ): List<VerifiedTaskItem> {
        val isAchievement = achievementCatalog.any { it.taskId == taskId }
        val currentList = if (isAchievement) _achievements.value else _tasks.value
        val existing = currentList.firstOrNull { it.taskId == taskId } ?: return emptyList()
        if (existing.status == TaskStatus.CLAIMED) return emptyList()
        if (absoluteValue <= existing.progress) return emptyList()

        val newProgress = absoluteValue.coerceAtMost(existing.target)
        val newStatus = if (newProgress >= existing.target) TaskStatus.COMPLETED else TaskStatus.IN_PROGRESS
        val updated = existing.copy(
            progress = newProgress,
            status = newStatus,
            verifiedEventIds = (existing.verifiedEventIds + eventId).takeLast(100),
            updatedAtMillis = nowMillis
        )
        persistUpdatedTask(uid, updated, isAchievement)
        return if (existing.status != TaskStatus.COMPLETED && newStatus == TaskStatus.COMPLETED) listOf(updated) else emptyList()
    }

    private suspend fun persistUpdatedTask(uid: String, updated: VerifiedTaskItem, isAchievement: Boolean) {
        saveTaskToPrefs(uid, updated)
        if (isAchievement) {
            _achievements.value = _achievements.value.map { if (it.taskId == updated.taskId) updated else it }
        } else {
            _tasks.value = _tasks.value.map { if (it.taskId == updated.taskId) updated else it }
        }
        val db = firestore
        val firebaseUser = auth?.currentUser
        if (db != null && firebaseUser != null && firebaseUser.uid == uid) {
            try {
                val subCol = if (isAchievement) "achievements" else "tasks"
                db.collection("users").document(uid).collection(subCol).document(updated.taskId)
                    .set(updated.toFirestoreMap())
                    .await()
            } catch (e: Exception) {
                handleFirestoreError(e, FirestoreOperation.WRITE, "users/$uid/tasks/${updated.taskId}", auth)
            }
        }
    }

    private fun incrementActivityStat(
        uid: String,
        nowMillis: Long,
        played: Int = 0,
        won: Int = 0,
        rolls: Int = 0,
        captures: Int = 0,
        minutes: Int = 0
    ) {
        val dayKey = LudoPeriodKeys.currentDayKey(nowMillis)
        val newPlayed = prefs.getInt("${uid}_act_${dayKey}_played", 0) + played
        val newWon = prefs.getInt("${uid}_act_${dayKey}_won", 0) + won
        val newRolls = prefs.getInt("${uid}_act_${dayKey}_rolls", 0) + rolls
        val newCaptures = prefs.getInt("${uid}_act_${dayKey}_captures", 0) + captures
        val newMins = prefs.getInt("${uid}_act_${dayKey}_mins", 0) + minutes
        prefs.edit()
            .putInt("${uid}_act_${dayKey}_played", newPlayed)
            .putInt("${uid}_act_${dayKey}_won", newWon)
            .putInt("${uid}_act_${dayKey}_rolls", newRolls)
            .putInt("${uid}_act_${dayKey}_captures", newCaptures)
            .putInt("${uid}_act_${dayKey}_mins", newMins)
            .apply()
        val summary = DailyActivitySummary(dayKey, uid, newPlayed, newWon, newRolls, newCaptures, newMins)
        _todayActivity.value = summary
    }

    private fun ensureInitialized(uid: String, nowMillis: Long) {
        if (_tasks.value.isEmpty() || _tasks.value.firstOrNull()?.userId != uid) {
            initializeForUser(uid, nowMillis)
        } else {
            // Check if any daily or weekly period expired!
            val needsReset = _tasks.value.any { it.periodKey != periodKeyFor(it.category, nowMillis) }
            if (needsReset) {
                initializeForUser(uid, nowMillis)
            }
        }
    }

    private fun createBlankTask(uid: String, spec: TaskCatalogSpec, periodKey: String): VerifiedTaskItem =
        VerifiedTaskItem(
            taskId = spec.taskId,
            userId = uid,
            category = spec.category,
            title = spec.title,
            description = spec.description,
            progress = 0,
            target = spec.target,
            rewardCoins = spec.rewardCoins,
            rewardCash = spec.rewardCash,
            rewardXp = spec.rewardXp,
            status = TaskStatus.IN_PROGRESS,
            periodKey = periodKey
        )

    private fun loadOrResetTaskFromPrefs(uid: String, spec: TaskCatalogSpec, nowMillis: Long): VerifiedTaskItem {
        val expectedPeriod = periodKeyFor(spec.category, nowMillis)
        val storedPeriod = prefs.getString("${uid}_t_${spec.taskId}_period", null)
        if (storedPeriod != expectedPeriod) {
            val fresh = createBlankTask(uid, spec, expectedPeriod)
            saveTaskToPrefs(uid, fresh)
            return fresh
        }
        val progress = prefs.getInt("${uid}_t_${spec.taskId}_prog", 0).coerceAtLeast(0)
        val statusStr = prefs.getString("${uid}_t_${spec.taskId}_status", TaskStatus.IN_PROGRESS.wire)
        var status = TaskStatus.fromWire(statusStr)
        // Hard anti-tamper invariant on load: never allow CLAIMED/COMPLETED if progress < target!
        if (progress < spec.target && status != TaskStatus.IN_PROGRESS) {
            status = TaskStatus.IN_PROGRESS
        }
        val eventsRaw = prefs.getString("${uid}_t_${spec.taskId}_events", "") ?: ""
        val events = if (eventsRaw.isBlank()) emptyList() else eventsRaw.split("|")
        val txId = prefs.getString("${uid}_t_${spec.taskId}_tx", null)
        val claimedAt = prefs.getLong("${uid}_t_${spec.taskId}_claimedAt", 0L).takeIf { it > 0L }
        return VerifiedTaskItem(
            taskId = spec.taskId,
            userId = uid,
            category = spec.category,
            title = spec.title,
            description = spec.description,
            progress = progress,
            target = spec.target,
            rewardCoins = spec.rewardCoins,
            rewardCash = spec.rewardCash,
            rewardXp = spec.rewardXp,
            status = status,
            periodKey = expectedPeriod,
            verifiedEventIds = events,
            rewardTxId = txId,
            claimedAtMillis = claimedAt
        )
    }

    private fun saveTaskToPrefs(uid: String, item: VerifiedTaskItem) {
        prefs.edit()
            .putString("${uid}_t_${item.taskId}_period", item.periodKey)
            .putInt("${uid}_t_${item.taskId}_prog", item.progress)
            .putString("${uid}_t_${item.taskId}_status", item.status.wire)
            .putString("${uid}_t_${item.taskId}_events", item.verifiedEventIds.takeLast(50).joinToString("|"))
            .apply {
                if (item.rewardTxId != null) putString("${uid}_t_${item.taskId}_tx", item.rewardTxId)
                else remove("${uid}_t_${item.taskId}_tx")
                if (item.claimedAtMillis != null) putLong("${uid}_t_${item.taskId}_claimedAt", item.claimedAtMillis)
                else remove("${uid}_t_${item.taskId}_claimedAt")
            }
            .apply()
    }

    private fun loadHistoryFromPrefs(uid: String): List<RewardHistoryEntry> {
        val count = prefs.getInt("${uid}_hist_count", 0)
        return (0 until count).mapNotNull { i ->
            val txId = prefs.getString("${uid}_hist_${i}_tx", null) ?: return@mapNotNull null
            RewardHistoryEntry(
                txId = txId,
                userId = uid,
                sourceType = prefs.getString("${uid}_hist_${i}_type", "") ?: "",
                sourceId = prefs.getString("${uid}_hist_${i}_src", "") ?: "",
                periodKey = prefs.getString("${uid}_hist_${i}_per", "") ?: "",
                coinsDelta = prefs.getInt("${uid}_hist_${i}_coins", 0),
                cashDelta = prefs.getInt("${uid}_hist_${i}_cash", 0),
                xpDelta = prefs.getInt("${uid}_hist_${i}_xp", 0),
                description = prefs.getString("${uid}_hist_${i}_desc", "") ?: "",
                createdAtMillis = prefs.getLong("${uid}_hist_${i}_ts", 0L)
            )
        }
    }

    private fun saveHistoryToPrefs(uid: String, list: List<RewardHistoryEntry>) {
        val trimmed = list.take(50)
        val editor = prefs.edit().putInt("${uid}_hist_count", trimmed.size)
        trimmed.forEachIndexed { i, e ->
            editor.putString("${uid}_hist_${i}_tx", e.txId)
            editor.putString("${uid}_hist_${i}_type", e.sourceType)
            editor.putString("${uid}_hist_${i}_src", e.sourceId)
            editor.putString("${uid}_hist_${i}_per", e.periodKey)
            editor.putInt("${uid}_hist_${i}_coins", e.coinsDelta)
            editor.putInt("${uid}_hist_${i}_cash", e.cashDelta)
            editor.putInt("${uid}_hist_${i}_xp", e.xpDelta)
            editor.putString("${uid}_hist_${i}_desc", e.description)
            editor.putLong("${uid}_hist_${i}_ts", e.createdAtMillis)
        }
        editor.apply()
    }
}
