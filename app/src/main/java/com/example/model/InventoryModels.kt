package com.example.model

import androidx.compose.ui.graphics.Color

enum class InventoryCategory(
    val title: String,
    val shortTitle: String,
    val icon: String
) {
    DICE("Dice Collection", "Dice", "🎲"),
    FRAME("Frame Collection", "Frames", "🖼️"),
    EMOJI("Emoji Collection", "Emojis", "😀"),
    BOARD("Board Collection", "Boards", "🎮"),
    PAWN("Pawn Collection", "Pawns", "♟️"),
    POWERUP("Powerups", "Powerups", "🚀"),
    OWNED("Owned Items", "Owned", "✅")
}

enum class InventoryFilter(val label: String) {
    ALL("All"),
    OWNED("Owned"),
    UNOWNED("Unowned"),
    SPECIAL("Special"),
    EVENTS("Events"),
    ADS("Ads"),
    CHESTS("Chest")
}

enum class ItemRarity(
    val label: String,
    val badgeColorHex: Long,
    val glowColorHex: Long
) {
    COMMON("Common", 0xFF78909C, 0xFFB0BEC5),
    RARE("Rare", 0xFF1E88E5, 0xFF64B5F6),
    EPIC("Epic", 0xFF8E24AA, 0xFFCE93D8),
    LEGENDARY("Legendary", 0xFFFF8F00, 0xFFFFD54F),
    MYTHIC("Mythic", 0xFFD50000, 0xFFFF8A80)
}

enum class CurrencyType(val symbol: String, val label: String) {
    COINS("🪙", "Coins"),
    CASH("💵", "Cash"),
    AD_REWARD("🎬", "Watch Ad"),
    EVENT("🏆", "Event"),
    CHEST("📦", "Chest"),
    FREE("🎁", "Free")
}

data class InventoryItem(
    val id: String,
    val name: String,
    val description: String,
    val category: InventoryCategory,
    val iconEmoji: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val price: Long,
    val currencyType: CurrencyType,
    val rarity: ItemRarity,
    val unlockRequirement: String,
    val progressTarget: Int = 0,
    val eventId: String? = null,
    val isSpecial: Boolean = false,
    val isEventOnly: Boolean = false,
    val isConsumable: Boolean = false,
    val defaultOwned: Boolean = false,
    val pawnColorHexes: List<Long> = emptyList(),
    val trailName: String = "",
    val powerupEffectType: String? = null,
    val cooldownSeconds: Long = 0L,
    val dailyLimit: Int = 0,
    val durationMillis: Long = 0L
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)
    val accentColor: Color get() = Color(accentColorHex)
}

// ======================================================
// SECTION 14: DATABASE & PERSISTENCE SCHEMA MODELS
// ======================================================

data class PlayerCurrency(
    val playerId: String,
    val goldCoins: Long,
    val cashNotes: Int
)

data class OwnedItems(
    val playerId: String,
    val itemId: String,
    val quantity: Int = 1,
    val unlockedAt: Long = System.currentTimeMillis(),
    val source: String = "PURCHASE"
)

data class EquippedItems(
    val playerId: String,
    val diceId: String = "dice_imperial_gold",
    val frameId: String = "frame_royal_crest",
    val boardId: String = "imperial_navy",
    val pawnId: String = "crown_maharaja",
    val pawnTrailEnabled: Boolean = true
)

data class PurchaseTransactions(
    val transactionId: String,
    val playerId: String,
    val itemId: String,
    val amount: Long,
    val currencyType: CurrencyType,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RewardTransactions(
    val rewardId: String,
    val playerId: String,
    val adOrEventId: String,
    val itemId: String? = null,
    val coinsGranted: Long = 0L,
    val cashGranted: Int = 0,
    val verified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class EmojiDeck(
    val playerId: String,
    val slots: List<String?> = List(18) { null }
) {
    companion object {
        const val MAX_SLOTS = 18
    }
}

data class ActivePowerups(
    val playerId: String,
    val itemId: String,
    val powerupType: String,
    val quantity: Int,
    val activeUntilMillis: Long = 0L,
    val lastActivatedMillis: Long = 0L,
    val dailyUsedCount: Int = 0,
    val dailyResetDay: Long = 0L
) {
    fun isCurrentlyActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        activeUntilMillis > nowMillis
}

data class EventProgress(
    val playerId: String,
    val eventOrItemId: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val expiresAtMillis: Long = Long.MAX_VALUE
)

data class PlayerInventory(
    val playerId: String,
    val currency: PlayerCurrency,
    val ownedItems: List<OwnedItems>,
    val equippedItems: EquippedItems,
    val emojiDeck: EmojiDeck,
    val activePowerups: List<ActivePowerups>,
    val eventProgress: List<EventProgress>
) {
    val ownedItemIds: Set<String> get() = ownedItems.filter { it.quantity > 0 }.map { it.itemId }.toSet()

    fun isOwned(itemId: String): Boolean = ownedItems.any { it.itemId == itemId && it.quantity > 0 }

    fun getQuantity(itemId: String): Int = ownedItems.firstOrNull { it.itemId == itemId }?.quantity ?: 0

    fun uniqueOwnedCountFor(category: InventoryCategory, catalog: List<InventoryItem> = ItemCatalog.ALL_ITEMS): Int {
        val catIds = catalog.filter { it.category == category }.map { it.id }.toSet()
        return ownedItemIds.count { it in catIds }
    }
}

sealed class PurchaseResult {
    data class Success(val item: InventoryItem, val transaction: PurchaseTransactions) : PurchaseResult()
    data class AlreadyOwned(val item: InventoryItem) : PurchaseResult()
    data class InsufficientBalance(val required: Long, val available: Long, val currencyType: CurrencyType) : PurchaseResult()
    data class RequirementNotMet(val message: String) : PurchaseResult()
    data class EventExpired(val eventId: String) : PurchaseResult()
    data class NotFound(val itemId: String) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

sealed class AdVerificationResult {
    data class Verified(
        val transactionId: String,
        val coinsGranted: Long,
        val cashGranted: Int,
        val unlockedItemIds: List<String>
    ) : AdVerificationResult()
    data class DuplicateTransaction(val transactionId: String) : AdVerificationResult()
    data class NotVerified(val reason: String) : AdVerificationResult()
}

sealed class PowerupActivationResult {
    data class Activated(val item: InventoryItem, val state: ActivePowerups) : PowerupActivationResult()
    data class NotOwned(val itemId: String) : PowerupActivationResult()
    data class OnCooldown(val remainingSeconds: Long) : PowerupActivationResult()
    data class DailyLimitReached(val limit: Int) : PowerupActivationResult()
    data class Error(val message: String) : PowerupActivationResult()
}

object ItemCatalog {

    val DICE_ITEMS: List<InventoryItem> = listOf(
        InventoryItem(
            id = "dice_imperial_gold",
            name = "Imperial Gold",
            description = "24K Jaipur bullion gold dice with ruby pips",
            category = InventoryCategory.DICE,
            iconEmoji = "👑",
            primaryColorHex = 0xFFFFD700,
            secondaryColorHex = 0xFFFFA000,
            accentColorHex = 0xFFC62828,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Starter Royal Dice",
            defaultOwned = true
        ),
        InventoryItem(
            id = "dice_classic_ivory",
            name = "Classic Ivory",
            description = "Hand-carved vintage ivory bone with ebony pips",
            category = InventoryCategory.DICE,
            iconEmoji = "🎲",
            primaryColorHex = 0xFFFFF8E1,
            secondaryColorHex = 0xFFFFE082,
            accentColorHex = 0xFF212121,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Starter Classic Dice",
            defaultOwned = true
        ),
        InventoryItem(
            id = "dice_medusa",
            name = "Medusa",
            description = "Mythic serpent emerald dice with petrifying jade gaze",
            category = InventoryCategory.DICE,
            iconEmoji = "🐍",
            primaryColorHex = 0xFF00E676,
            secondaryColorHex = 0xFF1B5E20,
            accentColorHex = 0xFFFFD54F,
            price = 4500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 4,500 Coins"
        ),
        InventoryItem(
            id = "dice_scorpius",
            name = "Scorpius",
            description = "Venomous desert scorpion crimson obsidian cube",
            category = InventoryCategory.DICE,
            iconEmoji = "🦂",
            primaryColorHex = 0xFFFF1744,
            secondaryColorHex = 0xFF4A0010,
            accentColorHex = 0xFFFFAB00,
            price = 35L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Unlock for 35 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "dice_wilder",
            name = "Wilder",
            description = "Untamed safari timber & amber wildcat dice",
            category = InventoryCategory.DICE,
            iconEmoji = "🐆",
            primaryColorHex = 0xFFFF9100,
            secondaryColorHex = 0xFF4E342E,
            accentColorHex = 0xFF76FF03,
            price = 3000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 3,000 Coins"
        ),
        InventoryItem(
            id = "dice_eaglor",
            name = "Eaglor",
            description = "Soaring golden eagle crest forged in sky sapphire",
            category = InventoryCategory.DICE,
            iconEmoji = "🦅",
            primaryColorHex = 0xFF2979FF,
            secondaryColorHex = 0xFF0D47A1,
            accentColorHex = 0xFFFFD740,
            price = 55L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Unlock for 55 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "dice_mysticat",
            name = "Mysticat",
            description = "Arcane feline crystal dice radiating neon violet",
            category = InventoryCategory.DICE,
            iconEmoji = "🐱",
            primaryColorHex = 0xFFE040FB,
            secondaryColorHex = 0xFF4A148C,
            accentColorHex = 0xFF00E5FF,
            price = 5000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 5,000 Coins"
        ),
        InventoryItem(
            id = "dice_bonfire",
            name = "Bonfire",
            description = "Crackling campfire ember dice with warm flame pips",
            category = InventoryCategory.DICE,
            iconEmoji = "🔥",
            primaryColorHex = 0xFFFF6D00,
            secondaryColorHex = 0xFFBF360C,
            accentColorHex = 0xFFFFEA00,
            price = 2800L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 2,800 Coins"
        ),
        InventoryItem(
            id = "dice_jewel",
            name = "Jewel",
            description = "Prism-cut diamond & royal sapphire luxury cube",
            category = InventoryCategory.DICE,
            iconEmoji = "💎",
            primaryColorHex = 0xFF00E5FF,
            secondaryColorHex = 0xFF006064,
            accentColorHex = 0xFFFFFFFF,
            price = 65L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Unlock for 65 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "dice_wanderly",
            name = "Wanderly",
            description = "Voyager brass compass dice from the Royal Expedition",
            category = InventoryCategory.DICE,
            iconEmoji = "🧭",
            primaryColorHex = 0xFF26A69A,
            secondaryColorHex = 0xFF004D40,
            accentColorHex = 0xFFFFD54F,
            price = 3200L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Expedition Event or 3,200 Coins",
            eventId = "event_wanderlust",
            isSpecial = true,
            isEventOnly = false
        ),
        InventoryItem(
            id = "dice_captaino",
            name = "Captaino",
            description = "Admiral treasure galleon dice trimmed in mythic gold",
            category = InventoryCategory.DICE,
            iconEmoji = "🏴‍☠️",
            primaryColorHex = 0xFF1A237E,
            secondaryColorHex = 0xFF000A33,
            accentColorHex = 0xFFFFD700,
            price = 8500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.MYTHIC,
            unlockRequirement = "Chest Drop or 8,500 Coins",
            isSpecial = true
        ),
        InventoryItem(
            id = "dice_maharaja_ruby",
            name = "Maharaja Ruby",
            description = "Faceted crimson gemstone with Mughal gold pips",
            category = InventoryCategory.DICE,
            iconEmoji = "♦️",
            primaryColorHex = 0xFFD50000,
            secondaryColorHex = 0xFF880E4F,
            accentColorHex = 0xFFFFD700,
            price = 6000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Purchase for 6,000 Coins"
        ),
        InventoryItem(
            id = "dice_cyber_neon",
            name = "Cyber Neon",
            description = "Electrified synthwave cyan with magenta pips",
            category = InventoryCategory.DICE,
            iconEmoji = "⚡",
            primaryColorHex = 0xFF00E5FF,
            secondaryColorHex = 0xFF651FFF,
            accentColorHex = 0xFFFF007F,
            price = 3500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 3,500 Coins"
        ),
        InventoryItem(
            id = "dice_emerald_empire",
            name = "Emerald Empire",
            description = "Royal Mughal emerald with diamond glint pips",
            category = InventoryCategory.DICE,
            iconEmoji = "🟢",
            primaryColorHex = 0xFF00C853,
            secondaryColorHex = 0xFF1B5E20,
            accentColorHex = 0xFFE0F7FA,
            price = 7000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 7,000 Coins"
        )
    ) + PremiumDiceCatalog.buildDefaultSeedCatalog()
        .filter { it.id != "dice_imperial_gold" }
        .map { PremiumDiceCatalog.toInventoryItem(it) }

    val FRAME_ITEMS: List<InventoryItem> = listOf(
        InventoryItem(
            id = "frame_royal_crest",
            name = "Royal Crest",
            description = "Classic engraved gold border of the Maharaja court",
            category = InventoryCategory.FRAME,
            iconEmoji = "👑",
            primaryColorHex = 0xFFFFD54F,
            secondaryColorHex = 0xFFB8860B,
            accentColorHex = 0xFFFFF8E1,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Starter Profile Frame",
            defaultOwned = true
        ),
        InventoryItem(
            id = "frame_campfire",
            name = "Campfire",
            description = "Warm crackling blaze border with glowing ember sparks",
            category = InventoryCategory.FRAME,
            iconEmoji = "🔥",
            primaryColorHex = 0xFFFF6D00,
            secondaryColorHex = 0xFFDD2C00,
            accentColorHex = 0xFFFFEB3B,
            price = 2500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 2,500 Coins"
        ),
        InventoryItem(
            id = "frame_volt",
            name = "Volt",
            description = "High-voltage electric cyan lightning arc frame",
            category = InventoryCategory.FRAME,
            iconEmoji = "⚡",
            primaryColorHex = 0xFF00E5FF,
            secondaryColorHex = 0xFF2979FF,
            accentColorHex = 0xFFFFEA00,
            price = 4500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 4,500 Coins"
        ),
        InventoryItem(
            id = "frame_jackpot",
            name = "Jackpot",
            description = "Spinning 777 casino gold lights & emerald gems",
            category = InventoryCategory.FRAME,
            iconEmoji = "🎰",
            primaryColorHex = 0xFFFFD700,
            secondaryColorHex = 0xFFD50000,
            accentColorHex = 0xFF00E676,
            price = 45L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Unlock for 45 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "frame_window",
            name = "Window Frame",
            description = "Ornate palace jharokha stained-glass arch frame",
            category = InventoryCategory.FRAME,
            iconEmoji = "🪟",
            primaryColorHex = 0xFF42A5F5,
            secondaryColorHex = 0xFF1A237E,
            accentColorHex = 0xFFFFD54F,
            price = 3200L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 3,200 Coins"
        ),
        InventoryItem(
            id = "frame_pixie",
            name = "Pixie Frame",
            description = "Enchanted shimmering stardust & fairy blossom border",
            category = InventoryCategory.FRAME,
            iconEmoji = "🧚",
            primaryColorHex = 0xFFFF4081,
            secondaryColorHex = 0xFF8E24AA,
            accentColorHex = 0xFF80D8FF,
            price = 30L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Unlock for 30 Cash"
        ),
        InventoryItem(
            id = "frame_stage",
            name = "Stage Frame",
            description = "Grand championship spotlight curtains & star bulbs",
            category = InventoryCategory.FRAME,
            iconEmoji = "🎭",
            primaryColorHex = 0xFFAB47BC,
            secondaryColorHex = 0xFF4A148C,
            accentColorHex = 0xFFFFD700,
            price = 5500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Spotlight Event or 5,500 Coins",
            eventId = "event_spotlight",
            isSpecial = true
        ),
        InventoryItem(
            id = "frame_fiesta",
            name = "Fiesta Frame",
            description = "Festive carnival confetti ribbons & vibrant marigold",
            category = InventoryCategory.FRAME,
            iconEmoji = "🎊",
            primaryColorHex = 0xFFFF9100,
            secondaryColorHex = 0xFFE91E63,
            accentColorHex = 0xFF00E676,
            price = 25L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Fiesta Event or 25 Cash",
            eventId = "event_fiesta",
            isSpecial = true
        ),
        InventoryItem(
            id = "frame_comic",
            name = "Comic Frame",
            description = "Bold superhero halftone ink & POW burst corners",
            category = InventoryCategory.FRAME,
            iconEmoji = "💥",
            primaryColorHex = 0xFFFFEA00,
            secondaryColorHex = 0xFFD50000,
            accentColorHex = 0xFF2979FF,
            price = 2200L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 2,200 Coins"
        ),
        InventoryItem(
            id = "frame_bounty",
            name = "Bounty",
            description = "Imperial Outlaw bounty crest with crimson wax seal",
            category = InventoryCategory.FRAME,
            iconEmoji = "🎯",
            primaryColorHex = 0xFFB71C1C,
            secondaryColorHex = 0xFF3E2723,
            accentColorHex = 0xFFFFD54F,
            price = 60L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.MYTHIC,
            unlockRequirement = "Unlock for 60 Cash",
            isSpecial = true
        )
    )

    val EMOJI_ITEMS: List<InventoryItem> = listOf(
        InventoryItem(
            id = "emoji_royal_crown",
            name = "Royal Crown",
            description = "Show your sovereign pride in the arena",
            category = InventoryCategory.EMOJI,
            iconEmoji = "👑",
            primaryColorHex = 0xFFFFD700,
            secondaryColorHex = 0xFF1A237E,
            accentColorHex = 0xFFFFF8E1,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Starter Emoji",
            defaultOwned = true
        ),
        InventoryItem(
            id = "emoji_fire_streak",
            name = "Royal Fire",
            description = "Celebrate an unstoppable dice roll streak",
            category = InventoryCategory.EMOJI,
            iconEmoji = "🔥",
            primaryColorHex = 0xFFFF6D00,
            secondaryColorHex = 0xFF4E1500,
            accentColorHex = 0xFFFFEA00,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Starter Emoji",
            defaultOwned = true
        ),
        InventoryItem(
            id = "emoji_charmed",
            name = "Charmed",
            description = "Heart-eyes admiration for a brilliant move",
            category = InventoryCategory.EMOJI,
            iconEmoji = "😍",
            primaryColorHex = 0xFFFF4081,
            secondaryColorHex = 0xFF880E4F,
            accentColorHex = 0xFFFFD54F,
            price = 1200L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 1,200 Coins"
        ),
        InventoryItem(
            id = "emoji_applause",
            name = "Applause",
            description = "Royal standing ovation for worthy opponents",
            category = InventoryCategory.EMOJI,
            iconEmoji = "👏",
            primaryColorHex = 0xFFFFB300,
            secondaryColorHex = 0xFF1E3A8A,
            accentColorHex = 0xFFFFFFFF,
            price = 800L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Purchase for 800 Coins"
        ),
        InventoryItem(
            id = "emoji_rage",
            name = "Rage",
            description = "Furious reaction when your token gets captured",
            category = InventoryCategory.EMOJI,
            iconEmoji = "😡",
            primaryColorHex = 0xFFFF1744,
            secondaryColorHex = 0xFF4A0000,
            accentColorHex = 0xFFFF9100,
            price = 1500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 1,500 Coins"
        ),
        InventoryItem(
            id = "emoji_goofy",
            name = "Goofy",
            description = "Silly playful wink after a lucky escape",
            category = InventoryCategory.EMOJI,
            iconEmoji = "🤪",
            primaryColorHex = 0xFFFFEB3B,
            secondaryColorHex = 0xFF4A148C,
            accentColorHex = 0xFF00E5FF,
            price = 900L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Purchase for 900 Coins"
        ),
        InventoryItem(
            id = "emoji_cat_angry",
            name = "Cat Angry",
            description = "Grumpy royal cat glaring across the board",
            category = InventoryCategory.EMOJI,
            iconEmoji = "😾",
            primaryColorHex = 0xFFFF7043,
            secondaryColorHex = 0xFF3E2723,
            accentColorHex = 0xFFFFD54F,
            price = 15L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Unlock for 15 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "emoji_cat_crying",
            name = "Cat Crying",
            description = "Dramatic teary kitty begging for mercy",
            category = InventoryCategory.EMOJI,
            iconEmoji = "😿",
            primaryColorHex = 0xFF29B6F6,
            secondaryColorHex = 0xFF0D47A1,
            accentColorHex = 0xFFE1F5FE,
            price = 2000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 2,000 Coins"
        ),
        InventoryItem(
            id = "emoji_clasher",
            name = "Clasher",
            description = "Crossed swords challenge to duel for the center",
            category = InventoryCategory.EMOJI,
            iconEmoji = "⚔️",
            primaryColorHex = 0xFFD50000,
            secondaryColorHex = 0xFF1A237E,
            accentColorHex = 0xFFFFD700,
            price = 25L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Unlock for 25 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "emoji_squall",
            name = "Squall",
            description = "Tempest whirlwind emoji earned through Storm Event",
            category = InventoryCategory.EMOJI,
            iconEmoji = "🌪️",
            primaryColorHex = 0xFF00B0FF,
            secondaryColorHex = 0xFF263238,
            accentColorHex = 0xFF80D8FF,
            price = 0L,
            currencyType = CurrencyType.EVENT,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Complete 50 Event Actions (0/50)",
            progressTarget = 50,
            eventId = "event_storm",
            isSpecial = true,
            isEventOnly = true
        ),
        InventoryItem(
            id = "emoji_guy_crying",
            name = "Guy Crying",
            description = "Hilarious waterfall tears after a triple-6 forfeit",
            category = InventoryCategory.EMOJI,
            iconEmoji = "😭",
            primaryColorHex = 0xFF42A5F5,
            secondaryColorHex = 0xFF1565C0,
            accentColorHex = 0xFFFFD54F,
            price = 0L,
            currencyType = CurrencyType.AD_REWARD,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Watch 50 Rewarded Ads (0/50)",
            progressTarget = 50
        ),
        InventoryItem(
            id = "emoji_trophy_wink",
            name = "Champion",
            description = "Golden victory cup flex",
            category = InventoryCategory.EMOJI,
            iconEmoji = "🏆",
            primaryColorHex = 0xFFFFC107,
            secondaryColorHex = 0xFF311B92,
            accentColorHex = 0xFFFFFFFF,
            price = 1800L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 1,800 Coins"
        )
    )

    val BOARD_ITEMS: List<InventoryItem> = listOf(
        InventoryItem(
            id = "imperial_navy",
            name = "Imperial Navy",
            description = "Classic deep sapphire court with crisp white tiles",
            category = InventoryCategory.BOARD,
            iconEmoji = "🏛️",
            primaryColorHex = 0xFF091F56,
            secondaryColorHex = 0xFF1565C0,
            accentColorHex = 0xFFFFD54F,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Starter Classic Board",
            defaultOwned = true
        ),
        InventoryItem(
            id = "maharaja_palace",
            name = "Maharaja Durbar",
            description = "Carved teakwood, Makrana marble & 24K gold Durbar inlays",
            category = InventoryCategory.BOARD,
            iconEmoji = "👑",
            primaryColorHex = 0xFF4A1808,
            secondaryColorHex = 0xFFB71C1C,
            accentColorHex = 0xFFFFD54F,
            price = 5000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Purchase for 5,000 Coins",
            isSpecial = true
        ),
        InventoryItem(
            id = "board_luxor",
            name = "Luxor",
            description = "Ancient Pharaoh sandstone gold & lapis lazuli temple",
            category = InventoryCategory.BOARD,
            iconEmoji = "🏺",
            primaryColorHex = 0xFFD4AF37,
            secondaryColorHex = 0xFF3E2723,
            accentColorHex = 0xFF00B0FF,
            price = 60L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Unlock for 60 Cash",
            isSpecial = true
        ),
        InventoryItem(
            id = "board_campout",
            name = "Campout",
            description = "Midnight pinewood trail illuminated by cozy lantern amber",
            category = InventoryCategory.BOARD,
            iconEmoji = "🏕️",
            primaryColorHex = 0xFF2E7D32,
            secondaryColorHex = 0xFF1B3B2B,
            accentColorHex = 0xFFFF8F00,
            price = 4000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 4,000 Coins"
        ),
        InventoryItem(
            id = "board_fiesta",
            name = "Fiesta",
            description = "Vibrant marigold, fiesta pink & confetti festival court",
            category = InventoryCategory.BOARD,
            iconEmoji = "🎉",
            primaryColorHex = 0xFFE91E63,
            secondaryColorHex = 0xFF6A1B9A,
            accentColorHex = 0xFFFFC107,
            price = 40L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Fiesta Event or 40 Cash",
            eventId = "event_fiesta",
            isSpecial = true
        ),
        InventoryItem(
            id = "board_ocean",
            name = "Ocean",
            description = "Crystal Atlantis turquoise tides with coral reef borders",
            category = InventoryCategory.BOARD,
            iconEmoji = "🌊",
            primaryColorHex = 0xFF00BCD4,
            secondaryColorHex = 0xFF002B49,
            accentColorHex = 0xFFFF4081,
            price = 6500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 6,500 Coins"
        ),
        InventoryItem(
            id = "board_garden",
            name = "Garden",
            description = "Lush botanical jade meadows with golden floral stars",
            category = InventoryCategory.BOARD,
            iconEmoji = "🌸",
            primaryColorHex = 0xFF4CAF50,
            secondaryColorHex = 0xFF13361E,
            accentColorHex = 0xFFFFD54F,
            price = 4500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 4,500 Coins"
        ),
        InventoryItem(
            id = "board_neon",
            name = "Neon",
            description = "Electrifying magenta & cyan cyber grid on dark quartz",
            category = InventoryCategory.BOARD,
            iconEmoji = "🌆",
            primaryColorHex = 0xFFE040FB,
            secondaryColorHex = 0xFF100726,
            accentColorHex = 0xFF00E5FF,
            price = 50L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Unlock for 50 Cash"
        )
    )

    val PAWN_ITEMS: List<InventoryItem> = listOf(
        InventoryItem(
            id = "crown_maharaja",
            name = "Maharaja Crown",
            description = "Imperial golden crown tokens with sovereign shimmer trail",
            category = InventoryCategory.PAWN,
            iconEmoji = "👑",
            primaryColorHex = 0xFFFFD700,
            secondaryColorHex = 0xFFB8860B,
            accentColorHex = 0xFFFFFFFF,
            price = 0L,
            currencyType = CurrencyType.FREE,
            rarity = ItemRarity.COMMON,
            unlockRequirement = "Starter Royal Pawns",
            defaultOwned = true,
            pawnColorHexes = listOf(0xFFE53935, 0xFF43A047, 0xFF1E88E5, 0xFFFDD835),
            trailName = "Golden Stardust Trail"
        ),
        InventoryItem(
            id = "pawn_roasty",
            name = "Roasty",
            description = "Blazing ember pawns leaving a glowing flame trail",
            category = InventoryCategory.PAWN,
            iconEmoji = "🔥",
            primaryColorHex = 0xFFFF5722,
            secondaryColorHex = 0xFFBF360C,
            accentColorHex = 0xFFFFEA00,
            price = 3000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 3,000 Coins",
            pawnColorHexes = listOf(0xFFFF3D00, 0xFF00E676, 0xFF00B0FF, 0xFFFFC400),
            trailName = "Blazing Flame Trail"
        ),
        InventoryItem(
            id = "pawn_swatty",
            name = "Swatty",
            description = "Armored tactical squad pawns with laser beam trails",
            category = InventoryCategory.PAWN,
            iconEmoji = "🛡️",
            primaryColorHex = 0xFF2979FF,
            secondaryColorHex = 0xFF102027,
            accentColorHex = 0xFF00E5FF,
            price = 4500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 4,500 Coins",
            pawnColorHexes = listOf(0xFFD50000, 0xFF2E7D32, 0xFF1565C0, 0xFFFFAB00),
            trailName = "Tactical Laser Trail"
        ),
        InventoryItem(
            id = "pawn_kicko",
            name = "Kicko",
            description = "High-voltage martial striker tokens with shockwave streaks",
            category = InventoryCategory.PAWN,
            iconEmoji = "⚡",
            primaryColorHex = 0xFFFFC107,
            secondaryColorHex = 0xFFE65100,
            accentColorHex = 0xFF00E5FF,
            price = 3500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 3,500 Coins",
            pawnColorHexes = listOf(0xFFFF1744, 0xFF00C853, 0xFF2979FF, 0xFFFFEA00),
            trailName = "Lightning Spark Trail"
        ),
        InventoryItem(
            id = "pawn_mambo",
            name = "Mambo",
            description = "Carnival rhythm dancer pawns with vibrant confetti trails",
            category = InventoryCategory.PAWN,
            iconEmoji = "💃",
            primaryColorHex = 0xFFE91E63,
            secondaryColorHex = 0xFF6A1B9A,
            accentColorHex = 0xFF00E676,
            price = 30L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Fiesta Event or 30 Cash",
            eventId = "event_fiesta",
            isSpecial = true,
            pawnColorHexes = listOf(0xFFFF4081, 0xFF00E676, 0xFF00E5FF, 0xFFFFD740),
            trailName = "Carnival Confetti Trail"
        ),
        InventoryItem(
            id = "pawn_oakor",
            name = "Oakor",
            description = "Ancient forest guardian totems with emerald leaf trails",
            category = InventoryCategory.PAWN,
            iconEmoji = "🌿",
            primaryColorHex = 0xFF4CAF50,
            secondaryColorHex = 0xFF1B5E20,
            accentColorHex = 0xFFA5D6A7,
            price = 5000L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 5,000 Coins",
            pawnColorHexes = listOf(0xFFE53935, 0xFF388E3C, 0xFF0288D1, 0xFFFBC02D),
            trailName = "Emerald Leaf Trail"
        ),
        InventoryItem(
            id = "pawn_rolf",
            name = "Rolf",
            description = "Arctic alpha wolf crests with icy aurora trails",
            category = InventoryCategory.PAWN,
            iconEmoji = "🐺",
            primaryColorHex = 0xFF80D8FF,
            secondaryColorHex = 0xFF263238,
            accentColorHex = 0xFFFFFFFF,
            price = 50L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Unlock for 50 Cash",
            isSpecial = true,
            pawnColorHexes = listOf(0xFFEF5350, 0xFF66BB6A, 0xFF42A5F5, 0xFFFFEE58),
            trailName = "Frost Aurora Trail"
        ),
        InventoryItem(
            id = "pawn_jack",
            name = "Jack",
            description = "Grandmaster Royal Ace tokens with golden card trails",
            category = InventoryCategory.PAWN,
            iconEmoji = "🃏",
            primaryColorHex = 0xFFD50000,
            secondaryColorHex = 0xFF1A1A2E,
            accentColorHex = 0xFFFFD700,
            price = 75L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.MYTHIC,
            unlockRequirement = "Unlock for 75 Cash",
            isSpecial = true,
            pawnColorHexes = listOf(0xFFD50000, 0xFF00C853, 0xFF2962FF, 0xFFFFD600),
            trailName = "Royal Ace Trail"
        )
    )

    val POWERUP_ITEMS: List<InventoryItem> = listOf(
        InventoryItem(
            id = "powerup_xp_booster",
            name = "XP Booster",
            description = "Doubles (2X) all XP earned in matches for 30 minutes",
            category = InventoryCategory.POWERUP,
            iconEmoji = "🚀",
            primaryColorHex = 0xFF00B0FF,
            secondaryColorHex = 0xFF0D47A1,
            accentColorHex = 0xFF00E676,
            price = 1500L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.EPIC,
            unlockRequirement = "Purchase for 1,500 Coins",
            isConsumable = true,
            powerupEffectType = "XP_BOOSTER",
            cooldownSeconds = 60L,
            dailyLimit = 5,
            durationMillis = 30L * 60L * 1000L
        ),
        InventoryItem(
            id = "powerup_recover_coins",
            name = "Recover Coins",
            description = "Protects your wager and recovers 50% Coins on match loss",
            category = InventoryCategory.POWERUP,
            iconEmoji = "🛡️",
            primaryColorHex = 0xFF00E676,
            secondaryColorHex = 0xFF1B5E20,
            accentColorHex = 0xFFFFD54F,
            price = 15L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.LEGENDARY,
            unlockRequirement = "Purchase for 15 Cash",
            isConsumable = true,
            isSpecial = true,
            powerupEffectType = "RECOVER_COINS",
            cooldownSeconds = 120L,
            dailyLimit = 3,
            durationMillis = 60L * 60L * 1000L
        ),
        InventoryItem(
            id = "powerup_extra_powerup",
            name = "Extra Powerup",
            description = "Grants +2 Lightning / Even / Odd Dice Charges for matches",
            category = InventoryCategory.POWERUP,
            iconEmoji = "⚡",
            primaryColorHex = 0xFFFFAB00,
            secondaryColorHex = 0xFFE65100,
            accentColorHex = 0xFFFFFFFF,
            price = 1200L,
            currencyType = CurrencyType.COINS,
            rarity = ItemRarity.RARE,
            unlockRequirement = "Purchase for 1,200 Coins",
            isConsumable = true,
            powerupEffectType = "EXTRA_POWERUP",
            cooldownSeconds = 30L,
            dailyLimit = 10,
            durationMillis = 15L * 60L * 1000L
        ),
        InventoryItem(
            id = "powerup_season_pass",
            name = "Ranking / Season Pass",
            description = "Unlocks +50% Trophy Ranking boost & VIP Season Pass rewards",
            category = InventoryCategory.POWERUP,
            iconEmoji = "🏆",
            primaryColorHex = 0xFFAA00FF,
            secondaryColorHex = 0xFF4A148C,
            accentColorHex = 0xFFFFD700,
            price = 80L,
            currencyType = CurrencyType.CASH,
            rarity = ItemRarity.MYTHIC,
            unlockRequirement = "Unlock for 80 Cash",
            isConsumable = true,
            isSpecial = true,
            powerupEffectType = "SEASON_PASS",
            cooldownSeconds = 300L,
            dailyLimit = 2,
            durationMillis = 7L * 24L * 60L * 60L * 1000L
        )
    )

    private val dynamicDiceRegistry = java.util.concurrent.CopyOnWriteArrayList<InventoryItem>()

    fun syncDynamicDiceCatalog(items: List<PremiumDiceItem>) {
        val staticIds = (DICE_ITEMS + FRAME_ITEMS + EMOJI_ITEMS + BOARD_ITEMS + PAWN_ITEMS + POWERUP_ITEMS).map { it.id }.toSet()
        dynamicDiceRegistry.clear()
        items.filter { it.isPublished && it.id !in staticIds }.forEach {
            dynamicDiceRegistry.add(PremiumDiceCatalog.toInventoryItem(it))
        }
    }

    val ALL_ITEMS: List<InventoryItem>
        get() = DICE_ITEMS + dynamicDiceRegistry + FRAME_ITEMS + EMOJI_ITEMS + BOARD_ITEMS + PAWN_ITEMS + POWERUP_ITEMS

    fun findById(id: String): InventoryItem? = ALL_ITEMS.firstOrNull { it.id == id }

    fun itemsForCategory(category: InventoryCategory): List<InventoryItem> =
        if (category == InventoryCategory.OWNED) ALL_ITEMS else ALL_ITEMS.filter { it.category == category }

    fun defaultOwnedItemIds(): Set<String> =
        ALL_ITEMS.filter { it.defaultOwned }.map { it.id }.toSet()
}
