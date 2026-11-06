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
        description = "Capture 500 opponent pawns in royal battles",
        category = "COMBAT",
        iconEmoji = "⚔️",
        currentProgress = 376,
        targetProgress = 500,
        xpReward = 50,
        coinReward = 10000L,
        medalsEarned = 1
    ),
    AchievementEntity(
        id = "ach_six_cliques",
        title = "Six Cliques",
        description = "Roll 1,000 sixes on your lucky royal dice",
        category = "ROLLING",
        iconEmoji = "🎲",
        currentProgress = 1000,
        targetProgress = 1000,
        xpReward = 150,
        coinReward = 25000L,
        isCompleted = true,
        isClaimed = true,
        medalsEarned = 3
    ),
    AchievementEntity(
        id = "ach_ludo_love",
        title = "Ludo Love",
        description = "Complete 500 online kingdom matches",
        category = "MATCHES",
        iconEmoji = "❤️",
        currentProgress = 113,
        targetProgress = 500,
        xpReward = 200,
        coinReward = 50000L,
        medalsEarned = 2
    ),
    AchievementEntity(
        id = "ach_home_runner",
        title = "Home Runner",
        description = "Take 300 pawns home to the central palace",
        category = "COMBAT",
        iconEmoji = "🏰",
        currentProgress = 224,
        targetProgress = 300,
        xpReward = 80,
        coinReward = 15000L,
        medalsEarned = 2
    ),
    AchievementEntity(
        id = "ach_streak_master",
        title = "Win Streak Sovereign",
        description = "Achieve a consecutive 10-match win streak",
        category = "WIN_STREAK",
        iconEmoji = "🔥",
        currentProgress = 6,
        targetProgress = 10,
        xpReward = 300,
        coinReward = 100000L,
        medalsEarned = 3
    ),
    AchievementEntity(
        id = "ach_high_roller",
        title = "High Roller Maharaja",
        description = "Win 5 matches with entry wager >= 50,000 Coins",
        category = "MATCHES",
        iconEmoji = "👑",
        currentProgress = 3,
        targetProgress = 5,
        xpReward = 250,
        coinReward = 75000L,
        medalsEarned = 2
    )
)
