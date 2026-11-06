package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyRewardStatus(
    val streakDay: Int, // 1..7 (the day to be claimed or just claimed)
    val isClaimedToday: Boolean,
    val rewardCoins: Long,
    val rewardGems: Int,
    val totalClaimedCoins: Long,
    val totalClaimedGems: Int
)

class LudoRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val matchHistoryDao = db.matchHistoryDao()
    private val dailyRewardDao = db.dailyRewardDao()
    private val userProfileDao = db.userProfileDao()
    private val achievementDao = db.achievementDao()

    val last10Matches: Flow<List<MatchHistoryEntity>> = matchHistoryDao.getLast10Matches()
    val dailyRewardFlow: Flow<DailyRewardEntity?> = dailyRewardDao.getDailyReward()
    val userProfileFlow: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val allAchievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    companion object {
        val REWARD_SCHEDULE = listOf(
            Pair(500L, 0),    // Day 1
            Pair(1000L, 0),   // Day 2
            Pair(2000L, 5),   // Day 3
            Pair(3500L, 0),   // Day 4
            Pair(5000L, 10),  // Day 5
            Pair(7500L, 0),   // Day 6
            Pair(15000L, 25)  // Day 7
        )
    }

    suspend fun getDailyRewardStatus(): DailyRewardStatus {
        val todayStr = dateFormat.format(Date())
        val record = dailyRewardDao.getDailyRewardDirect()

        if (record == null) {
            // First time opening app: ready for Day 1 reward
            return DailyRewardStatus(
                streakDay = 1,
                isClaimedToday = false,
                rewardCoins = REWARD_SCHEDULE[0].first,
                rewardGems = REWARD_SCHEDULE[0].second,
                totalClaimedCoins = 0L,
                totalClaimedGems = 0
            )
        }

        val isClaimedToday = record.lastClaimDate == todayStr

        // Check if last claim was yesterday to maintain streak
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        val nextStreakDay = when {
            isClaimedToday -> record.streakDay
            record.lastClaimDate == yesterdayStr -> {
                if (record.streakDay >= 7) 1 else record.streakDay + 1
            }
            else -> 1 // Streak broken, restart at Day 1
        }

        val scheduleIndex = (nextStreakDay - 1).coerceIn(0, 6)
        val schedule = REWARD_SCHEDULE[scheduleIndex]

        return DailyRewardStatus(
            streakDay = nextStreakDay,
            isClaimedToday = isClaimedToday,
            rewardCoins = schedule.first,
            rewardGems = schedule.second,
            totalClaimedCoins = record.totalClaimedCoins,
            totalClaimedGems = record.totalClaimedGems
        )
    }

    suspend fun claimTodayReward(): Pair<Long, Int> {
        val status = getDailyRewardStatus()
        if (status.isClaimedToday) return Pair(0L, 0)

        val todayStr = dateFormat.format(Date())
        val scheduleIndex = (status.streakDay - 1).coerceIn(0, 6)
        val (coins, gems) = REWARD_SCHEDULE[scheduleIndex]

        val newRecord = DailyRewardEntity(
            id = 1,
            lastClaimDate = todayStr,
            streakDay = status.streakDay,
            totalClaimedCoins = status.totalClaimedCoins + coins,
            totalClaimedGems = status.totalClaimedGems + gems,
            lastClaimTimestamp = System.currentTimeMillis()
        )
        dailyRewardDao.saveDailyReward(newRecord)
        return Pair(coins, gems)
    }

    suspend fun recordMatch(
        gameMode: String,
        wager: Long,
        coinsWon: Long,
        userRank: Int,
        winnerName: String,
        winnerColor: String,
        durationSeconds: Int,
        leaderboardSummary: String
    ) {
        val match = MatchHistoryEntity(
            gameMode = gameMode,
            wager = wager,
            coinsWon = coinsWon,
            userRank = userRank,
            winnerName = winnerName,
            winnerColor = winnerColor,
            durationSeconds = durationSeconds,
            leaderboardSummary = leaderboardSummary
        )
        matchHistoryDao.insertMatch(match)
    }

    suspend fun initializeSampleHistoryIfEmpty() {
        if (matchHistoryDao.getMatchCount() == 0) {
            val now = System.currentTimeMillis()
            val sampleMatches = listOf(
                MatchHistoryEntity(
                    timestamp = now - 1000 * 60 * 35, // 35 min ago
                    gameMode = "vs Royal AI",
                    wager = 500,
                    coinsWon = 1500,
                    userRank = 1,
                    winnerName = "Ishu",
                    winnerColor = "Yellow",
                    durationSeconds = 240,
                    leaderboardSummary = "1:Ishu:Yellow:4;2:Raja Vikram:Red:3;3:Maharani Tara:Green:2;4:Senapati Amar:Blue:1"
                ),
                MatchHistoryEntity(
                    timestamp = now - 1000 * 60 * 180, // 3 hours ago
                    gameMode = "Online Match",
                    wager = 2000,
                    coinsWon = -2000,
                    userRank = 2,
                    winnerName = "Crown_Kabir",
                    winnerColor = "Red",
                    durationSeconds = 310,
                    leaderboardSummary = "1:Crown_Kabir:Red:4;2:Ishu:Yellow:3;3:Delhi_Tiger:Green:1;4:RoyalKnight:Blue:0"
                ),
                MatchHistoryEntity(
                    timestamp = now - 1000 * 60 * 60 * 24, // 1 day ago
                    gameMode = "Quick Rush Mode",
                    wager = 5000,
                    coinsWon = 15000,
                    userRank = 1,
                    winnerName = "Ishu",
                    winnerColor = "Yellow",
                    durationSeconds = 125,
                    leaderboardSummary = "1:Ishu:Yellow:1;2:Raja Vikram:Red:0;3:Maharani Tara:Green:0;4:Senapati Amar:Blue:0"
                )
            )
            sampleMatches.forEach { matchHistoryDao.insertMatch(it) }
        }
    }

    suspend fun getUserProfile(): UserProfileEntity {
        return userProfileDao.getUserProfileDirect() ?: UserProfileEntity().also {
            userProfileDao.insertOrUpdateProfile(it)
        }
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun updateDailySpinTimestamp(timestamp: Long = System.currentTimeMillis()) {
        val current = getUserProfile()
        userProfileDao.insertOrUpdateProfile(
            current.copy(
                lastSpinTimestampMillis = timestamp,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun initializeDefaultProfileIfEmpty(
        defaultNickname: String,
        defaultAvatar: String,
        defaultTitle: String,
        defaultPawnCrown: String
    ): UserProfileEntity {
        val existing = userProfileDao.getUserProfileDirect()
        return if (existing != null) {
            existing
        } else {
            val initial = UserProfileEntity(
                id = 1,
                nickname = defaultNickname,
                crownAvatar = defaultAvatar,
                customPhotoUri = null,
                title = defaultTitle,
                equippedPawnCrownAvatar = defaultPawnCrown
            )
            userProfileDao.insertOrUpdateProfile(initial)
            initial
        }
    }

    suspend fun initializeAchievementsIfEmpty(): List<AchievementEntity> {
        val existing = achievementDao.getAllAchievements()
        return if (existing.isEmpty()) {
            achievementDao.insertAchievements(DEFAULT_ACHIEVEMENTS)
            DEFAULT_ACHIEVEMENTS
        } else {
            existing
        }
    }

    suspend fun claimAchievement(id: String) {
        achievementDao.claimAchievement(id)
    }

    suspend fun updateAchievementProgress(id: String, progress: Int, max: Int) {
        achievementDao.updateProgress(id, progress, progress >= max)
    }
}
