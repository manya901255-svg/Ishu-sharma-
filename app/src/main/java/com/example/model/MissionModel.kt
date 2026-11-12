package com.example.model

/**
 * DailyMission: Regal-themed daily quests that reward currency and exclusive crown cosmetics.
 */
data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardCoins: Long,
    val rewardGems: Int,
    val rewardCosmeticId: String? = null,
    val rewardCosmeticName: String? = null,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = currentProgress >= targetProgress
    val progressFraction: Float get() = (currentProgress.toFloat() / targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f)
}

val DEFAULT_DAILY_MISSIONS = listOf(
    DailyMission(
        id = "task_win_online",
        title = "Win 1 online match",
        description = "Defeat royal opponents in an online multiplayer match",
        icon = "🌐🏆",
        currentProgress = 0,
        targetProgress = 1,
        rewardCoins = 100L,
        rewardGems = 5,
        rewardCosmeticId = "avatar_vikramaditya",
        rewardCosmeticName = "Maharaja Vikramaditya 🤴"
    ),
    DailyMission(
        id = "task_complete_matches",
        title = "Complete 2 matches",
        description = "Play and finish 2 full matches in any Ludo game mode",
        icon = "⚔️🎮",
        currentProgress = 0,
        targetProgress = 2,
        rewardCoins = 150L,
        rewardGems = 10
    ),
    DailyMission(
        id = "task_roll_dice",
        title = "Roll the dice 10 times",
        description = "Roll the royal dice 10 times across your battles",
        icon = "🎲✨",
        currentProgress = 0,
        targetProgress = 10,
        rewardCoins = 50L,
        rewardGems = 5
    ),
    DailyMission(
        id = "task_token_safety",
        title = "Move a token to safety",
        description = "Move your royal pawn onto a safe star cell or into the home runway",
        icon = "⭐🛡️",
        currentProgress = 0,
        targetProgress = 1,
        rewardCoins = 100L,
        rewardGems = 5
    ),
    DailyMission(
        id = "task_quick_match",
        title = "Finish 1 quick match",
        description = "Complete 1 lightning-fast Quick Rush match",
        icon = "⚡🔥",
        currentProgress = 0,
        targetProgress = 1,
        rewardCoins = 200L,
        rewardGems = 15,
        rewardCosmeticId = "crown_kohinoor_diamond",
        rewardCosmeticName = "Kohinoor Diadem 💎"
    )
)
