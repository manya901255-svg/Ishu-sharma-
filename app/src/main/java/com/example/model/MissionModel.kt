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
        id = "quest_gold_pawns",
        title = "Win 3 games with Gold Pawns",
        description = "Command the royal yellow gold battalion to victory 3 times in battle",
        icon = "🟡👑",
        currentProgress = 0,
        targetProgress = 3,
        rewardCoins = 5000L,
        rewardGems = 25,
        rewardCosmeticId = "avatar_vikramaditya",
        rewardCosmeticName = "Maharaja Vikramaditya Avatar 🤴"
    ),
    DailyMission(
        id = "quest_capture_pawns",
        title = "Capture 5 Opponent Pawns in Battle",
        description = "Strike down 5 enemy pawns and send them back to their base",
        icon = "⚔️💥",
        currentProgress = 0,
        targetProgress = 5,
        rewardCoins = 3500L,
        rewardGems = 15,
        rewardCosmeticId = "avatar_rajput_king",
        rewardCosmeticName = "Maharana Pratap Avatar ⚔️"
    ),
    DailyMission(
        id = "quest_roll_sixes",
        title = "Roll three 6s in Royal Court Matches",
        description = "Summon royal luck by rolling lucky sixes during live games",
        icon = "🎲✨",
        currentProgress = 0,
        targetProgress = 3,
        rewardCoins = 2500L,
        rewardGems = 10,
        rewardCosmeticId = "crown_kohinoor_diamond",
        rewardCosmeticName = "Kohinoor Diamond Diadem 💎"
    ),
    DailyMission(
        id = "quest_online_matches",
        title = "Complete 2 Online Multiplayer Matches",
        description = "Compete with players across kingdoms in online rooms",
        icon = "🌐🏆",
        currentProgress = 0,
        targetProgress = 2,
        rewardCoins = 4000L,
        rewardGems = 20,
        rewardCosmeticId = "avatar_ashoka",
        rewardCosmeticName = "Samrat Ashoka Avatar 🦁"
    ),
    DailyMission(
        id = "quest_home_run",
        title = "Reach Home Palace with 4 Pawns",
        description = "Guide all 4 royal pawns safely to the center finish triangle",
        icon = "🏛️⭐",
        currentProgress = 0,
        targetProgress = 4,
        rewardCoins = 6000L,
        rewardGems = 30,
        rewardCosmeticId = "avatar_rani_padmini",
        rewardCosmeticName = "Rani Padmini Avatar 👸"
    )
)
