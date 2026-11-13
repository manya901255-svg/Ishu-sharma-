package com.example.model

import androidx.compose.ui.graphics.Color

enum class DiceCollectionGroup(val title: String, val badge: String) {
    CLASSIC_PREMIUM("Classic Premium Dice", "CLASSIC"),
    VIP("VIP Dice Collection", "VIP"),
    SPECIAL_THEMED("Special Themed Dice", "SPECIAL"),
    DAILY_EVENT("Daily & Event Dice", "EVENT")
}

enum class DiceRarity(
    val label: String,
    val primaryBorderHex: Long,
    val secondaryBorderHex: Long,
    val glowHex: Long,
    val badgeBgHex: Long
) {
    COMMON("Common", 0xFF90A4AE, 0xFFCFD8DC, 0xFFB0BEC5, 0xFF455A64),
    RARE("Rare", 0xFF29B6F6, 0xFF0288D1, 0xFF4FC3F7, 0xFF01579B),
    EPIC("Epic", 0xFFBA68C8, 0xFF7B1FA2, 0xFFE040FB, 0xFF4A148C),
    LEGENDARY("Legendary", 0xFFFFD54F, 0xFFFF6F00, 0xFFFFEA00, 0xFFE65100),
    VIP("VIP", 0xFFFFD700, 0xFF00E5FF, 0xFFFFEA00, 0xFF311B92);

    val primaryBorderColor: Color get() = Color(primaryBorderHex)
    val secondaryBorderColor: Color get() = Color(secondaryBorderHex)
    val glowColor: Color get() = Color(glowHex)
    val badgeBgColor: Color get() = Color(badgeBgHex)
}

enum class DiceAnimationStyle(val displayName: String, val description: String) {
    ROYAL_TUMBLE("Royal Tumble", "Weighted sovereign multi-bounce flip"),
    METEOR_SPIN("Meteor Spin", "High-velocity blazing vortex spin"),
    LIGHTNING_SNAP("Lightning Snap", "Sharp electric zig-zag snap landing"),
    CRYSTAL_FLOAT("Crystal Float", "Levitating prism rotation with gentle settle"),
    WHIRLWIND("Whirlwind Gust", "Cyclonic 3-axis cyclone spiral"),
    BOUNCE_FESTIVAL("Festival Bounce", "Elastic rhythmic carnival rebound"),
    ROAR_SLAM("Apex Slam", "Heavy majestic impact shockwave drop")
}

enum class DiceParticleEffect(val displayName: String, val primaryColorHex: Long, val secondaryColorHex: Long) {
    NONE("Standard", 0xFFFFD700, 0xFFFFFFFF),
    GOLD_SPARKLE("Gold Filigree Sparkle", 0xFFFFD700, 0xFFFFF59D),
    DESERT_SAND("Desert Sun Ember", 0xFFFFA726, 0xFFD84315),
    CITRUS_SPLASH("Citrus Zest Mist", 0xFFFFEA00, 0xFF76FF03),
    BLUE_LIGHTNING("Sapphire Arc Sparks", 0xFF00E5FF, 0xFF2979FF),
    EMERALD_GLOW("Emerald Aura Shards", 0xFF00E676, 0xFFB9F6CA),
    SAPPHIRE_SHARD("Sapphire Prism Burst", 0xFF40C4FF, 0xFFFFD700),
    RUBY_SPARK("Ruby Rose Glints", 0xFFFF4081, 0xFFFFD54F),
    GOLDEN_LEAF("Golden Laurel Leaves", 0xFFFFD700, 0xFFFF8F00),
    FIRE_EMBERS("Blazing Flame Embers", 0xFFFF5722, 0xFFFFEA00),
    NATURE_PETALS("Jasmine Blossom Petals", 0xFFFFFFFF, 0xFF69F0AE),
    CHOCOLATE_SWIRL("Cocoa Cream Swirl", 0xFF8D6E63, 0xFFFFF8E1),
    SNOWFLAKES("Arctic Frost Snowflakes", 0xFFE0F7FA, 0xFF40C4FF),
    CONFETTI("Festival Confetti", 0xFFFF4081, 0xFFFFEA00),
    BALLOONS("Party Pop Confetti", 0xFF29B6F6, 0xFFFFEB3B),
    PAW_PRINTS("Golden Sparks & Stars", 0xFFFFCA28, 0xFFFF6D00)
}

enum class DiceSoundEffect(val displayName: String) {
    ROYAL_FANFARE("Royal Filigree Tumble"),
    DESERT_WIND("Desert Canyon Whoosh"),
    CITRUS_POP("Crisp Citrus Snap"),
    CRYSTAL_CHIME("Sapphire Mandala Chime"),
    GOLDEN_CHIME("Golden Palace Bell"),
    EMERALD_RESONANCE("Emerald Vault Tone"),
    SAPPHIRE_PRISM("Sapphire Prism Chord"),
    RUBY_BELL("Imperial Ruby Resonance"),
    GOLDEN_LEAF_CHIME("Sovereign Laurel Harp"),
    FIRE_CRACKLE("Inferno Flame Crackle"),
    NATURE_BREEZE("Jasmine Breeze Chime"),
    COCOA_THUMP("Velvet Cocoa Clack"),
    PANDA_PLAYFUL("Bamboo Wood Tap"),
    CARNIVAL_DRUM("Carnival Dholak Beat"),
    ICE_CHIME("Frost Crystal Tinkle"),
    LION_ROAR("Lion Apex Bass Impact"),
    SUNSET_HARP("Twilight Temple Harp"),
    CAT_CHIRP("Playful Kitty Spark"),
    BALLOON_POP("Festive Balloon Pop"),
    CLASSIC_WOOD("Classic Marble Clack")
}

enum class DiceAvailabilityType(val label: String) {
    ALWAYS_AVAILABLE("Store Permanent"),
    DAILY_OFFER("Daily Offer"),
    LIMITED_TIME("Limited Time"),
    EVENT_EXCLUSIVE("Event Reward"),
    COLLECTION_CHALLENGE("Collection Challenge"),
    ADMIN_DISABLED("Disabled")
}

enum class BillingPurchaseStatus {
    PURCHASED,
    PENDING,
    FAILED,
    CANCELLED,
    REFUNDED
}

data class DiceBenefits(
    val animationStyle: DiceAnimationStyle,
    val particleEffect: DiceParticleEffect,
    val soundEffect: DiceSoundEffect,
    val visualEffectSummary: String,
    val vipStatusLabel: String? = null,
    val progressionNote: String = "Fair Play Certified: Uniform 1-6 RNG"
)

data class PremiumDiceItem(
    val id: String,
    val name: String,
    val description: String,
    val collectionGroup: DiceCollectionGroup,
    val rarity: DiceRarity,
    val coinPrice: Long,
    val inrPrice: Int,
    val skuId: String,
    val canBuyWithCoins: Boolean = true,
    val canBuyWithInr: Boolean = true,
    val isAvailable: Boolean = true,
    val availabilityType: DiceAvailabilityType = DiceAvailabilityType.ALWAYS_AVAILABLE,
    val eventEndTimeMillis: Long = 0L,
    val challengeRequirementText: String = "",
    val challengeTargetCount: Int = 0,
    val benefits: DiceBenefits,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val pipColorHex: Long,
    val iconEmoji: String,
    val isStarterDefault: Boolean = false,
    val isPublished: Boolean = true,
    val sortOrder: Int = 0,
    val updatedAt: Long = 0L
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)
    val accentColor: Color get() = Color(accentColorHex)
    val pipColor: Color get() = Color(pipColorHex)
    val formattedInrPrice: String get() = if (inrPrice > 0) "₹$inrPrice" else "Free"

    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
        (availabilityType == DiceAvailabilityType.LIMITED_TIME || availabilityType == DiceAvailabilityType.DAILY_OFFER) &&
            eventEndTimeMillis > 0L &&
            nowMillis > eventEndTimeMillis
}

data class OwnedDiceRecord(
    val playerId: String,
    val diceId: String,
    val source: String,
    val purchasedAt: Long,
    val isRevoked: Boolean = false
)

data class DiceEventRewardProgress(
    val playerId: String,
    val diceId: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val expiresAtMillis: Long
)

data class BillingVerificationPayload(
    val skuId: String,
    val diceId: String,
    val purchaseToken: String,
    val orderId: String,
    val packageName: String,
    val amountInr: Int,
    val purchaseState: BillingPurchaseStatus,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class DiceCoinPurchaseResult {
    data class Success(val dice: PremiumDiceItem, val transactionId: String, val remainingCoins: Long) : DiceCoinPurchaseResult()
    data class AlreadyOwned(val dice: PremiumDiceItem) : DiceCoinPurchaseResult()
    data class InsufficientCoins(val dice: PremiumDiceItem, val required: Long, val available: Long) : DiceCoinPurchaseResult()
    data class Unavailable(val reason: String) : DiceCoinPurchaseResult()
    data class Error(val message: String) : DiceCoinPurchaseResult()
}

sealed class DiceBillingResult {
    data class VerifiedGranted(val dice: PremiumDiceItem, val transactionId: String, val purchaseToken: String) : DiceBillingResult()
    data class Pending(val dice: PremiumDiceItem, val transactionId: String, val purchaseToken: String) : DiceBillingResult()
    data class DuplicateTokenRejected(val purchaseToken: String) : DiceBillingResult()
    data class Cancelled(val diceId: String) : DiceBillingResult()
    data class Failed(val reason: String) : DiceBillingResult()
    data class RefundedRevoked(val diceId: String, val purchaseToken: String) : DiceBillingResult()
}

data class DiceStoreSnapshot(
    val playerId: String,
    val goldCoins: Long,
    val cashNotes: Int,
    val catalog: List<PremiumDiceItem>,
    val ownedDiceIds: Set<String>,
    val equippedDiceId: String,
    val pendingBillingDiceIds: Set<String>,
    val eventRewards: Map<String, DiceEventRewardProgress>,
    val recentPurchaseHistory: List<String>
)

object PremiumDiceCatalog {

    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun buildDefaultSeedCatalog(referenceNowMillis: Long = System.currentTimeMillis()): List<PremiumDiceItem> = listOf(
        // Starter Imperial Gold Dice (Default Owned)
        PremiumDiceItem(
            id = "dice_imperial_gold",
            name = "Imperial Gold",
            description = "Signature 24K Jaipur royal bullion dice with ruby pips",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.COMMON,
            coinPrice = 0L,
            inrPrice = 0,
            skuId = "mg_dice_imperial_gold",
            canBuyWithCoins = false,
            canBuyWithInr = false,
            isAvailable = true,
            availabilityType = DiceAvailabilityType.ALWAYS_AVAILABLE,
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROYAL_TUMBLE,
                particleEffect = DiceParticleEffect.GOLD_SPARKLE,
                soundEffect = DiceSoundEffect.ROYAL_FANFARE,
                visualEffectSummary = "Classic Jaipur gold tumble with crimson ruby glint"
            ),
            primaryColorHex = 0xFFFFD700,
            secondaryColorHex = 0xFFFFA000,
            accentColorHex = 0xFFFFF59D,
            pipColorHex = 0xFFC62828,
            iconEmoji = "👑",
            isStarterDefault = true,
            sortOrder = 0
        ),

        // =====================================================
        // CLASSIC PREMIUM DICE (9 Required)
        // =====================================================
        PremiumDiceItem(
            id = "dice_royal_blue",
            name = "Royal Blue Dice",
            description = "Deep sapphire lacquer bevels framed with baroque golden filigree scrollwork",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.RARE,
            coinPrice = 5000L,
            inrPrice = 29,
            skuId = "mg_dice_royal_blue",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROYAL_TUMBLE,
                particleEffect = DiceParticleEffect.BLUE_LIGHTNING,
                soundEffect = DiceSoundEffect.CRYSTAL_CHIME,
                visualEffectSummary = "Baroque cobalt filigree glow + sapphire chime on roll"
            ),
            primaryColorHex = 0xFF0B3C74,
            secondaryColorHex = 0xFF041C3C,
            accentColorHex = 0xFFFFD54F,
            pipColorHex = 0xFF29B6F6,
            iconEmoji = "🔵",
            sortOrder = 1
        ),
        PremiumDiceItem(
            id = "dice_desert_warrior",
            name = "Desert Warrior Dice",
            description = "Sun-drenched canyon horizon & saguaro silhouettes trimmed in crimson lacquer",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.RARE,
            coinPrice = 8000L,
            inrPrice = 49,
            skuId = "mg_dice_desert_warrior",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROAR_SLAM,
                particleEffect = DiceParticleEffect.DESERT_SAND,
                soundEffect = DiceSoundEffect.DESERT_WIND,
                visualEffectSummary = "Blazing desert dust swirl + canyon wind acoustic roll"
            ),
            primaryColorHex = 0xFFA8201A,
            secondaryColorHex = 0xFF5D130E,
            accentColorHex = 0xFFFFE082,
            pipColorHex = 0xFFFFCA28,
            iconEmoji = "🌵",
            sortOrder = 2
        ),
        PremiumDiceItem(
            id = "dice_orange_citrus",
            name = "Orange Citrus Dice",
            description = "Vibrant sun-ripened citrus fruit slices framed by crisp orchard leaves",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.RARE,
            coinPrice = 10000L,
            inrPrice = 59,
            skuId = "mg_dice_orange_citrus",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.BOUNCE_FESTIVAL,
                particleEffect = DiceParticleEffect.CITRUS_SPLASH,
                soundEffect = DiceSoundEffect.CITRUS_POP,
                visualEffectSummary = "Refreshing citrus mist burst + crisp spring bounce"
            ),
            primaryColorHex = 0xFFFF9800,
            secondaryColorHex = 0xFFE65100,
            accentColorHex = 0xFFFFEB3B,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "🍊",
            sortOrder = 3
        ),
        PremiumDiceItem(
            id = "dice_blue_royal_pattern",
            name = "Blue Royal Pattern Dice",
            description = "Starry cobalt-blue Mughal lotus mandala engraved in fine gold thread",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.EPIC,
            coinPrice = 15000L,
            inrPrice = 79,
            skuId = "mg_dice_blue_royal_pattern",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.CRYSTAL_FLOAT,
                particleEffect = DiceParticleEffect.SAPPHIRE_SHARD,
                soundEffect = DiceSoundEffect.SAPPHIRE_PRISM,
                visualEffectSummary = "Floating lotus mandala projection + sapphire shard burst"
            ),
            primaryColorHex = 0xFF0A2463,
            secondaryColorHex = 0xFF031030,
            accentColorHex = 0xFF40C4FF,
            pipColorHex = 0xFFFFD740,
            iconEmoji = "💠",
            sortOrder = 4
        ),
        PremiumDiceItem(
            id = "dice_golden_palace",
            name = "Golden Palace Dice",
            description = "Hand-embossed 24K baroque palace wreath around an imperial garnet medallion",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.EPIC,
            coinPrice = 20000L,
            inrPrice = 99,
            skuId = "mg_dice_golden_palace",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROYAL_TUMBLE,
                particleEffect = DiceParticleEffect.GOLD_SPARKLE,
                soundEffect = DiceSoundEffect.GOLDEN_CHIME,
                visualEffectSummary = "24K palace bell chime + shimmering gold filigree halo"
            ),
            primaryColorHex = 0xFFD4AF37,
            secondaryColorHex = 0xFF7D5A00,
            accentColorHex = 0xFFFFF8E1,
            pipColorHex = 0xFF7F0000,
            iconEmoji = "🏛️",
            sortOrder = 5
        ),
        PremiumDiceItem(
            id = "dice_red_royal",
            name = "Red Royal Dice",
            description = "Deep velvet crimson body adorned with glowing magenta & marigold lotus symmetry",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.EPIC,
            coinPrice = 25000L,
            inrPrice = 119,
            skuId = "mg_dice_red_royal",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.METEOR_SPIN,
                particleEffect = DiceParticleEffect.RUBY_SPARK,
                soundEffect = DiceSoundEffect.RUBY_BELL,
                visualEffectSummary = "Crimson lotus spin + ruby spark cascade"
            ),
            primaryColorHex = 0xFF9E0B1C,
            secondaryColorHex = 0xFF4A0007,
            accentColorHex = 0xFFFF80AB,
            pipColorHex = 0xFFFFD54F,
            iconEmoji = "🌺",
            sortOrder = 6
        ),
        PremiumDiceItem(
            id = "dice_festival",
            name = "Festival Dice",
            description = "Diwali twilight purple cube lit with floating golden sky lanterns & scalloped medallion",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.LEGENDARY,
            coinPrice = 30000L,
            inrPrice = 149,
            skuId = "mg_dice_festival",
            availabilityType = DiceAvailabilityType.DAILY_OFFER,
            eventEndTimeMillis = referenceNowMillis + 2L * DAY_MS,
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.BOUNCE_FESTIVAL,
                particleEffect = DiceParticleEffect.CONFETTI,
                soundEffect = DiceSoundEffect.CARNIVAL_DRUM,
                visualEffectSummary = "Lantern glow burst + celebratory confetti rebound"
            ),
            primaryColorHex = 0xFF4A148C,
            secondaryColorHex = 0xFF1A0644,
            accentColorHex = 0xFFFF6D00,
            pipColorHex = 0xFFFFEA00,
            iconEmoji = "🏮",
            sortOrder = 7
        ),
        PremiumDiceItem(
            id = "dice_imperial_red",
            name = "Imperial Red Dice",
            description = "Scarlet imperial lacquer cube inlaid with ancient golden key-fret corners",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.LEGENDARY,
            coinPrice = 40000L,
            inrPrice = 199,
            skuId = "mg_dice_imperial_red",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROAR_SLAM,
                particleEffect = DiceParticleEffect.GOLD_SPARKLE,
                soundEffect = DiceSoundEffect.ROYAL_FANFARE,
                visualEffectSummary = "Emperor's lacquer shockwave + royal court resonance"
            ),
            primaryColorHex = 0xFFD50000,
            secondaryColorHex = 0xFF7F0000,
            accentColorHex = 0xFFFFD700,
            pipColorHex = 0xFFFFEB3B,
            iconEmoji = "🐉",
            sortOrder = 8
        ),
        PremiumDiceItem(
            id = "dice_royal_mosaic",
            name = "Royal Mosaic Dice",
            description = "Intricate Persian star mosaic bordering a luminous deep indigo gemstone core",
            collectionGroup = DiceCollectionGroup.CLASSIC_PREMIUM,
            rarity = DiceRarity.LEGENDARY,
            coinPrice = 50000L,
            inrPrice = 249,
            skuId = "mg_dice_royal_mosaic",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.CRYSTAL_FLOAT,
                particleEffect = DiceParticleEffect.SAPPHIRE_SHARD,
                soundEffect = DiceSoundEffect.SUNSET_HARP,
                visualEffectSummary = "Starlight tile shimmer + royal harp arpeggio"
            ),
            primaryColorHex = 0xFF1A237E,
            secondaryColorHex = 0xFF0A1045,
            accentColorHex = 0xFFFFD54F,
            pipColorHex = 0xFFFFEA00,
            iconEmoji = "✨",
            sortOrder = 9
        ),

        // =====================================================
        // VIP DICE COLLECTION (4 Required)
        // =====================================================
        PremiumDiceItem(
            id = "dice_emerald_vip",
            name = "Emerald VIP Dice",
            description = "Octagonal faceted emerald crystal ringed by golden royal crest medallions",
            collectionGroup = DiceCollectionGroup.VIP,
            rarity = DiceRarity.VIP,
            coinPrice = 75000L,
            inrPrice = 299,
            skuId = "mg_dice_emerald_vip",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.CRYSTAL_FLOAT,
                particleEffect = DiceParticleEffect.EMERALD_GLOW,
                soundEffect = DiceSoundEffect.EMERALD_RESONANCE,
                visualEffectSummary = "Faceted emerald prism spin + jade aura particles",
                vipStatusLabel = "VIP TIER I"
            ),
            primaryColorHex = 0xFF007E33,
            secondaryColorHex = 0xFF003D18,
            accentColorHex = 0xFFFFD700,
            pipColorHex = 0xFFB9F6CA,
            iconEmoji = "💚",
            sortOrder = 10
        ),
        PremiumDiceItem(
            id = "dice_sapphire_vip",
            name = "Sapphire VIP Dice",
            description = "Precision-cut sapphire VIP cube framed in double platinum & gold bevels",
            collectionGroup = DiceCollectionGroup.VIP,
            rarity = DiceRarity.VIP,
            coinPrice = 100000L,
            inrPrice = 399,
            skuId = "mg_dice_sapphire_vip",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.LIGHTNING_SNAP,
                particleEffect = DiceParticleEffect.SAPPHIRE_SHARD,
                soundEffect = DiceSoundEffect.SAPPHIRE_PRISM,
                visualEffectSummary = "Electric sapphire prism snap + crystal chime SFX",
                vipStatusLabel = "VIP TIER II"
            ),
            primaryColorHex = 0xFF0D47A1,
            secondaryColorHex = 0xFF051A44,
            accentColorHex = 0xFF40C4FF,
            pipColorHex = 0xFFFFE57F,
            iconEmoji = "💙",
            sortOrder = 11
        ),
        PremiumDiceItem(
            id = "dice_ruby_vip",
            name = "Ruby VIP Dice",
            description = "Imperial rose-ruby crystal inlaid with sculpted gold lace filigree",
            collectionGroup = DiceCollectionGroup.VIP,
            rarity = DiceRarity.VIP,
            coinPrice = 125000L,
            inrPrice = 499,
            skuId = "mg_dice_ruby_vip",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.METEOR_SPIN,
                particleEffect = DiceParticleEffect.RUBY_SPARK,
                soundEffect = DiceSoundEffect.RUBY_BELL,
                visualEffectSummary = "Royal ruby meteor vortex + shimmering crystal bell",
                vipStatusLabel = "VIP TIER III"
            ),
            primaryColorHex = 0xFFC2185B,
            secondaryColorHex = 0xFF560027,
            accentColorHex = 0xFFFF80AB,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "❤️",
            sortOrder = 12
        ),
        PremiumDiceItem(
            id = "dice_golden_leaf_vip",
            name = "Golden Leaf VIP Dice",
            description = "Sovereign obsidian & crimson cube crowned with hand-forged 24K golden laurel leaves",
            collectionGroup = DiceCollectionGroup.VIP,
            rarity = DiceRarity.VIP,
            coinPrice = 150000L,
            inrPrice = 599,
            skuId = "mg_dice_golden_leaf_vip",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROAR_SLAM,
                particleEffect = DiceParticleEffect.GOLDEN_LEAF,
                soundEffect = DiceSoundEffect.GOLDEN_LEAF_CHIME,
                visualEffectSummary = "Cascading 24K golden leaves + grand sovereign harp chord",
                vipStatusLabel = "ROYAL VIP APEX"
            ),
            primaryColorHex = 0xFF3E0B0B,
            secondaryColorHex = 0xFF140404,
            accentColorHex = 0xFFFFD700,
            pipColorHex = 0xFFFFD700,
            iconEmoji = "🌿",
            sortOrder = 13
        ),

        // =====================================================
        // SPECIAL THEMED DICE (10 Required + Celestial Bonus)
        // =====================================================
        PremiumDiceItem(
            id = "dice_fire",
            name = "Fire Dice",
            description = "Blazing grinning flame crest crackled with electric blue lightning streaks",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.EPIC,
            coinPrice = 12000L,
            inrPrice = 69,
            skuId = "mg_dice_fire",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.METEOR_SPIN,
                particleEffect = DiceParticleEffect.FIRE_EMBERS,
                soundEffect = DiceSoundEffect.FIRE_CRACKLE,
                visualEffectSummary = "Roaring flame embers + crackling fireburst audio"
            ),
            primaryColorHex = 0xFFFF5722,
            secondaryColorHex = 0xFF1A0904,
            accentColorHex = 0xFFFFEA00,
            pipColorHex = 0xFFFFAB00,
            iconEmoji = "🔥",
            sortOrder = 14
        ),
        PremiumDiceItem(
            id = "dice_nature",
            name = "Nature Dice",
            description = "Emerald botanical garden bursting with fresh white jasmine blossoms",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.EPIC,
            coinPrice = 14000L,
            inrPrice = 75,
            skuId = "mg_dice_nature",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.CRYSTAL_FLOAT,
                particleEffect = DiceParticleEffect.NATURE_PETALS,
                soundEffect = DiceSoundEffect.NATURE_BREEZE,
                visualEffectSummary = "Drifting white blossom petals + serene forest chime"
            ),
            primaryColorHex = 0xFF2E7D32,
            secondaryColorHex = 0xFF0B2E10,
            accentColorHex = 0xFFA5D6A7,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "🌸",
            sortOrder = 15
        ),
        PremiumDiceItem(
            id = "dice_chocolate",
            name = "Chocolate Dice",
            description = "Rich Belgian cocoa blocks swirling in a splash of fresh velvet cream",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.RARE,
            coinPrice = 16000L,
            inrPrice = 89,
            skuId = "mg_dice_chocolate",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.BOUNCE_FESTIVAL,
                particleEffect = DiceParticleEffect.CHOCOLATE_SWIRL,
                soundEffect = DiceSoundEffect.COCOA_THUMP,
                visualEffectSummary = "Creamy cocoa swirl splatter + velvet wooden clack"
            ),
            primaryColorHex = 0xFF5D4037,
            secondaryColorHex = 0xFF2B1810,
            accentColorHex = 0xFFFFF8E1,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "🍫",
            sortOrder = 16
        ),
        PremiumDiceItem(
            id = "dice_panda",
            name = "Panda Dice",
            description = "Cheerful crowned panda mascot nestled in crystal glacier-blue bamboo",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.EPIC,
            coinPrice = 18000L,
            inrPrice = 95,
            skuId = "mg_dice_panda",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.BOUNCE_FESTIVAL,
                particleEffect = DiceParticleEffect.SNOWFLAKES,
                soundEffect = DiceSoundEffect.PANDA_PLAYFUL,
                visualEffectSummary = "Playful bamboo bounce + sparkling frost stars"
            ),
            primaryColorHex = 0xFF0288D1,
            secondaryColorHex = 0xFF01386A,
            accentColorHex = 0xFFFFFFFF,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "🐼",
            sortOrder = 17
        ),
        PremiumDiceItem(
            id = "dice_carnival",
            name = "Carnival Dice",
            description = "Illuminated magenta festival cube glowing with golden earthen diyas",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.LEGENDARY,
            coinPrice = 22000L,
            inrPrice = 109,
            skuId = "mg_dice_carnival",
            availabilityType = DiceAvailabilityType.DAILY_OFFER,
            eventEndTimeMillis = referenceNowMillis + 3L * DAY_MS,
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.WHIRLWIND,
                particleEffect = DiceParticleEffect.CONFETTI,
                soundEffect = DiceSoundEffect.CARNIVAL_DRUM,
                visualEffectSummary = "Warm diya spark trail + celebratory dholak drumbeat"
            ),
            primaryColorHex = 0xFFC2185B,
            secondaryColorHex = 0xFF560027,
            accentColorHex = 0xFFFFD54F,
            pipColorHex = 0xFFFFEA00,
            iconEmoji = "🪔",
            sortOrder = 18
        ),
        PremiumDiceItem(
            id = "dice_snowman",
            name = "Snowman Dice",
            description = "Winter wonderland frosted cube featuring a jolly scarfed snowman",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.EPIC,
            coinPrice = 24000L,
            inrPrice = 119,
            skuId = "mg_dice_snowman",
            availabilityType = DiceAvailabilityType.LIMITED_TIME,
            eventEndTimeMillis = referenceNowMillis + 5L * DAY_MS,
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.CRYSTAL_FLOAT,
                particleEffect = DiceParticleEffect.SNOWFLAKES,
                soundEffect = DiceSoundEffect.ICE_CHIME,
                visualEffectSummary = "Falling winter snowflakes + crisp crystal ice chime"
            ),
            primaryColorHex = 0xFFB3E5FC,
            secondaryColorHex = 0xFF4FC3F7,
            accentColorHex = 0xFFFFFFFF,
            pipColorHex = 0xFF01579B,
            iconEmoji = "⛄",
            sortOrder = 19
        ),
        PremiumDiceItem(
            id = "dice_lion",
            name = "Lion Dice",
            description = "Fierce golden-maned apex lion emblem set on wild textured amber fur",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.LEGENDARY,
            coinPrice = 28000L,
            inrPrice = 139,
            skuId = "mg_dice_lion",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROAR_SLAM,
                particleEffect = DiceParticleEffect.PAW_PRINTS,
                soundEffect = DiceSoundEffect.LION_ROAR,
                visualEffectSummary = "Heavy apex impact slam + deep lion roar resonance"
            ),
            primaryColorHex = 0xFF795548,
            secondaryColorHex = 0xFF3E2723,
            accentColorHex = 0xFFFFB300,
            pipColorHex = 0xFFFFD54F,
            iconEmoji = "🦁",
            sortOrder = 20
        ),
        PremiumDiceItem(
            id = "dice_sunset",
            name = "Sunset Dice",
            description = "Royal stone palace archway overlooking warm twilight amber & starry skies",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.RARE,
            coinPrice = 20000L,
            inrPrice = 99,
            skuId = "mg_dice_sunset",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.ROYAL_TUMBLE,
                particleEffect = DiceParticleEffect.GOLD_SPARKLE,
                soundEffect = DiceSoundEffect.SUNSET_HARP,
                visualEffectSummary = "Warm dusk shimmer + melodic palace harp strum"
            ),
            primaryColorHex = 0xFFE65100,
            secondaryColorHex = 0xFF4A148C,
            accentColorHex = 0xFFFFCC80,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "🌅",
            sortOrder = 21
        ),
        PremiumDiceItem(
            id = "dice_cat",
            name = "Cat Dice",
            description = "Spotted wild-leopard pattern starring an adorable wide-eyed golden kitty",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.EPIC,
            coinPrice = 19000L,
            inrPrice = 89,
            skuId = "mg_dice_cat",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.LIGHTNING_SNAP,
                particleEffect = DiceParticleEffect.PAW_PRINTS,
                soundEffect = DiceSoundEffect.CAT_CHIRP,
                visualEffectSummary = "Agile feline pounce snap + sparkling star trail"
            ),
            primaryColorHex = 0xFFFFA726,
            secondaryColorHex = 0xFF4E342E,
            accentColorHex = 0xFFFFF59D,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "🐱",
            sortOrder = 22
        ),
        PremiumDiceItem(
            id = "dice_balloon",
            name = "Balloon Dice",
            description = "Violaceous celebration cube buoyant with primary party balloons & confetti",
            collectionGroup = DiceCollectionGroup.SPECIAL_THEMED,
            rarity = DiceRarity.RARE,
            coinPrice = 15000L,
            inrPrice = 79,
            skuId = "mg_dice_balloon",
            availabilityType = DiceAvailabilityType.COLLECTION_CHALLENGE,
            challengeRequirementText = "Roll Dice 15 Times in Matches to Unlock Free OR Buy Now",
            challengeTargetCount = 15,
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.BOUNCE_FESTIVAL,
                particleEffect = DiceParticleEffect.BALLOONS,
                soundEffect = DiceSoundEffect.BALLOON_POP,
                visualEffectSummary = "Buoyant party bounce + festive balloon confetti pop"
            ),
            primaryColorHex = 0xFF7B1FA2,
            secondaryColorHex = 0xFF311B92,
            accentColorHex = 0xFF29B6F6,
            pipColorHex = 0xFF00E5FF,
            iconEmoji = "🎈",
            sortOrder = 23
        ),

        // Original Bonus Zodiac Celestial VIP Design (inspired by Reference Image 2)
        PremiumDiceItem(
            id = "dice_celestial_zodiac",
            name = "Celestial Zodiac VIP Dice",
            description = "Astral midnight violet cube ringed with a glowing 12-constellation zodiac dial",
            collectionGroup = DiceCollectionGroup.DAILY_EVENT,
            rarity = DiceRarity.VIP,
            coinPrice = 60000L,
            inrPrice = 269,
            skuId = "mg_dice_celestial_zodiac",
            availabilityType = DiceAvailabilityType.LIMITED_TIME,
            eventEndTimeMillis = referenceNowMillis + 4L * DAY_MS,
            challengeRequirementText = "Limited Cosmic Event OR Collect Instant",
            benefits = DiceBenefits(
                animationStyle = DiceAnimationStyle.WHIRLWIND,
                particleEffect = DiceParticleEffect.SAPPHIRE_SHARD,
                soundEffect = DiceSoundEffect.SAPPHIRE_PRISM,
                visualEffectSummary = "Zodiac ring spiral + cosmic stardust detonation",
                vipStatusLabel = "EVENT VIP"
            ),
            primaryColorHex = 0xFF311B92,
            secondaryColorHex = 0xFF090428,
            accentColorHex = 0xFFFFD700,
            pipColorHex = 0xFFFFFFFF,
            iconEmoji = "♈",
            sortOrder = 24
        )
    )

    fun toInventoryItem(dice: PremiumDiceItem): InventoryItem {
        val mappedRarity = when (dice.rarity) {
            DiceRarity.COMMON -> ItemRarity.COMMON
            DiceRarity.RARE -> ItemRarity.RARE
            DiceRarity.EPIC -> ItemRarity.EPIC
            DiceRarity.LEGENDARY -> ItemRarity.LEGENDARY
            DiceRarity.VIP -> ItemRarity.MYTHIC
        }
        return InventoryItem(
            id = dice.id,
            name = dice.name,
            description = "${dice.description} • ${dice.benefits.visualEffectSummary}",
            category = InventoryCategory.DICE,
            iconEmoji = dice.iconEmoji,
            primaryColorHex = dice.primaryColorHex,
            secondaryColorHex = dice.secondaryColorHex,
            accentColorHex = dice.accentColorHex,
            price = dice.coinPrice,
            currencyType = if (dice.isStarterDefault) CurrencyType.FREE else CurrencyType.COINS,
            rarity = mappedRarity,
            unlockRequirement = if (dice.isStarterDefault) "Starter Royal Dice" else "${dice.coinPrice} Coins or ₹${dice.inrPrice}",
            isSpecial = dice.rarity == DiceRarity.VIP || dice.collectionGroup == DiceCollectionGroup.SPECIAL_THEMED,
            isEventOnly = dice.availabilityType == DiceAvailabilityType.EVENT_EXCLUSIVE,
            defaultOwned = dice.isStarterDefault
        )
    }
}
