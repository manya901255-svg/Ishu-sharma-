package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing Player Achievements:
 * Tracks milestone progression (pawns captured, sixes rolled, matches played, win streaks),
 * target levels, XP rewards, coin rewards, and claimed status.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "COMBAT", "ROLLING", "WIN_STREAK", "MATCHES"
    val iconEmoji: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val xpReward: Int,
    val coinReward: Long,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val medalsEarned: Int = 1
)

val DEFAULT_ACHIEVEMENTS = listOf(
    AchievementEntity(
        id = "ach_defender",
        title = "The Defender",
        description = "Capture 50 opponent pawns in royal battles",
        category = "COMBAT",
        iconEmoji = "⚔️",
        currentProgress = 0,
        targetProgress = 50,
        xpReward = 150,
        coinReward = 2500L,
        isCompleted = false,
        isClaimed = false,
        medalsEarned = 1
    ),
    AchievementEntity(
        id = "ach_six_cliques",
        title = "Six Cliques",
        description = "Roll 100 sixes on your lucky royal dice",
        category = "ROLLING",
        iconEmoji = "🎲",
        currentProgress = 0,
        targetProgress = 100,
        xpReward = 150,
        coinReward = 1500L,
        isCompleted = false,
        isClaimed = false,
        medalsEarned = 2
    ),
    AchievementEntity(
        id = "ach_ludo_love",
        title = "Ludo Love",
        description = "Complete 25 matches across any game mode",
        category = "MATCHES",
        iconEmoji = "❤️",
        currentProgress = 0,
        targetProgress = 25,
        xpReward = 200,
        coinReward = 3000L,
        isCompleted = false,
        isClaimed = false,
        medalsEarned = 2
    ),
    AchievementEntity(
        id = "ach_home_runner",
        title = "Home Runner",
        description = "Take 20 pawns home to the central palace",
        category = "COMBAT",
        iconEmoji = "🏰",
        currentProgress = 0,
        targetProgress = 20,
        xpReward = 120,
        coinReward = 2000L,
        isCompleted = false,
        isClaimed = false,
        medalsEarned = 1
    ),
    AchievementEntity(
        id = "ach_streak_master",
        title = "Win Streak Sovereign",
        description = "Achieve a consecutive 3-match win streak",
        category = "WIN_STREAK",
        iconEmoji = "🔥",
        currentProgress = 0,
        targetProgress = 3,
        xpReward = 300,
        coinReward = 5000L,
        isCompleted = false,
        isClaimed = false,
        medalsEarned = 3
    ),
    AchievementEntity(
        id = "ach_high_roller",
        title = "High Roller Maharaja",
        description = "Win 3 matches with entry wager >= 5,000 Coins",
        category = "MATCHES",
        iconEmoji = "👑",
        currentProgress = 0,
        targetProgress = 3,
        xpReward = 250,
        coinReward = 4000L,
        isCompleted = false,
        isClaimed = false,
        medalsEarned = 2
    )
)
