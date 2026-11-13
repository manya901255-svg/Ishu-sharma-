package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1. DiceCatalogEntity - Mutable Admin/Remote-configurable Dice Store Catalog
 */
@Entity(tableName = "dice_catalog")
data class DiceCatalogEntity(
    @PrimaryKey val diceId: String,
    val name: String,
    val description: String,
    val collectionGroup: String,
    val rarity: String,
    val coinPrice: Long,
    val inrPrice: Int,
    val skuId: String,
    val canBuyWithCoins: Boolean,
    val canBuyWithInr: Boolean,
    val isAvailable: Boolean,
    val availabilityType: String,
    val eventEndTimeMillis: Long,
    val challengeRequirementText: String,
    val challengeTargetCount: Int,
    val iconEmoji: String,
    val isStarterDefault: Boolean,
    val isPublished: Boolean,
    val sortOrder: Int,
    val updatedAt: Long
)

/**
 * 2. DiceAssetEntity - Visual rendering palette & procedural 3D shader/texture parameters
 */
@Entity(tableName = "dice_assets")
data class DiceAssetEntity(
    @PrimaryKey val diceId: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val pipColorHex: Long,
    val animationStyle: String,
    val particleEffect: String,
    val soundEffect: String
)

/**
 * 3. DiceBenefitEntity - Fair cosmetic & progression benefits metadata per dice
 */
@Entity(tableName = "dice_benefits")
data class DiceBenefitEntity(
    @PrimaryKey val diceId: String,
    val visualEffectSummary: String,
    val vipStatusLabel: String?,
    val progressionNote: String
)

/**
 * 4. PlayerOwnedDiceEntity - Permanent ownership ledger per player (supports refund revocation)
 */
@Entity(tableName = "player_owned_dice")
data class PlayerOwnedDiceEntity(
    @PrimaryKey val compositeId: String, // "${playerId}_${diceId}"
    val playerId: String,
    val diceId: String,
    val source: String, // STARTER, COINS, PLAY_BILLING, EVENT_CHALLENGE, ADMIN
    val purchaseToken: String? = null,
    val purchasedAt: Long = System.currentTimeMillis(),
    val isRevoked: Boolean = false
)

/**
 * 5. PlayerEquippedDiceEntity - Active equipped dice slot per player
 */
@Entity(tableName = "player_equipped_dice")
data class PlayerEquippedDiceEntity(
    @PrimaryKey val playerId: String,
    val diceId: String,
    val equippedAt: Long = System.currentTimeMillis()
)

/**
 * 6. CoinTransactionEntity - Double-entry style audit log for Gold Coin debits & credits
 */
@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey val transactionId: String,
    val playerId: String,
    val itemId: String,
    val amountDelta: Long, // negative for debit, positive for credit
    val balanceAfter: Long,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 7. RealMoneyTransactionEntity - Google Play Billing transaction & token verification ledger
 */
@Entity(tableName = "real_money_transactions")
data class RealMoneyTransactionEntity(
    @PrimaryKey val transactionId: String,
    val playerId: String,
    val diceId: String,
    val skuId: String,
    val purchaseToken: String,
    val orderId: String,
    val amountInr: Int,
    val status: String, // VERIFIED, PENDING, FAILED, CANCELLED, REFUNDED
    val acknowledged: Boolean = false,
    val verifiedAt: Long = System.currentTimeMillis()
)

/**
 * 8. DicePurchaseHistoryEntity - Unified human-readable purchase ledger
 */
@Entity(tableName = "dice_purchase_history")
data class DicePurchaseHistoryEntity(
    @PrimaryKey val historyId: String,
    val playerId: String,
    val diceId: String,
    val diceName: String,
    val paymentMethod: String, // COINS, INR_PLAY_BILLING, EVENT
    val amountDisplay: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 9. DiceEventRewardEntity - Daily offer & collection challenge progress per dice
 */
@Entity(tableName = "dice_event_rewards")
data class DiceEventRewardEntity(
    @PrimaryKey val compositeId: String, // "${playerId}_${diceId}"
    val playerId: String,
    val diceId: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val expiresAtMillis: Long
)
