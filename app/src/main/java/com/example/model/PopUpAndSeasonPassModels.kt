package com.example.model

data class GamePopUpNotice(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val icon: String = "🔔",
    val type: String = "INFO", // "FRIEND_ONLINE", "KEY", "REWARD", "CHEST", "SEASON_PASS"
    val timestamp: Long = System.currentTimeMillis()
)

data class SeasonPassTier(
    val level: Int,
    val requiredXp: Int,
    val freeRewardTitle: String,
    val freeRewardCoins: Long,
    val freeRewardGems: Int,
    val vipRewardTitle: String,
    val vipRewardCoins: Long,
    val vipRewardGems: Int,
    val isUnlocked: Boolean = true,
    val isClaimed: Boolean = false
)

val DEFAULT_SEASON_PASS_TIERS = listOf(
    SeasonPassTier(
        level = 1,
        requiredXp = 100,
        freeRewardTitle = "+5,000 Gold Coins",
        freeRewardCoins = 5000L,
        freeRewardGems = 0,
        vipRewardTitle = "Imperial Ruby Crest 👑",
        vipRewardCoins = 10000L,
        vipRewardGems = 15,
        isUnlocked = true
    ),
    SeasonPassTier(
        level = 2,
        requiredXp = 250,
        freeRewardTitle = "+25 Royal Gems",
        freeRewardCoins = 0L,
        freeRewardGems = 25,
        vipRewardTitle = "Maharaja Emerald Pawn Skin 🦚",
        vipRewardCoins = 8000L,
        vipRewardGems = 30,
        isUnlocked = true
    ),
    SeasonPassTier(
        level = 3,
        requiredXp = 450,
        freeRewardTitle = "+10,000 Gold Coins",
        freeRewardCoins = 10000L,
        freeRewardGems = 10,
        vipRewardTitle = "Royal Sapphire Dice Skin 🎲",
        vipRewardCoins = 15000L,
        vipRewardGems = 40,
        isUnlocked = true
    ),
    SeasonPassTier(
        level = 4,
        requiredXp = 700,
        freeRewardTitle = "Royal Ruby Mystery Chest 📦",
        freeRewardCoins = 8000L,
        freeRewardGems = 20,
        vipRewardTitle = "Jaipur Marble Board Theme 🏰",
        vipRewardCoins = 20000L,
        vipRewardGems = 50,
        isUnlocked = true
    ),
    SeasonPassTier(
        level = 5,
        requiredXp = 1000,
        freeRewardTitle = "+15,000 Gold Coins",
        freeRewardCoins = 15000L,
        freeRewardGems = 15,
        vipRewardTitle = "Kohinoor Platinum Crown 💎",
        vipRewardCoins = 25000L,
        vipRewardGems = 60,
        isUnlocked = true
    ),
    SeasonPassTier(
        level = 6,
        requiredXp = 1400,
        freeRewardTitle = "+35,000 Gold Coins & 50 Gems",
        freeRewardCoins = 35000L,
        freeRewardGems = 50,
        vipRewardTitle = "Maharaja Golden Throne Avatar 👑",
        vipRewardCoins = 50000L,
        vipRewardGems = 100,
        isUnlocked = true
    )
)
