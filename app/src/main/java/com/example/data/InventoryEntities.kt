package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_wallets")
data class PlayerWalletEntity(
    @PrimaryKey val playerId: String,
    val goldCoins: Long,
    val cashNotes: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "owned_items")
data class OwnedItemEntity(
    @PrimaryKey val compositeId: String, // "${playerId}_${itemId}"
    val playerId: String,
    val itemId: String,
    val quantity: Int = 1,
    val unlockedAt: Long = System.currentTimeMillis(),
    val source: String = "PURCHASE"
)

@Entity(tableName = "equipped_loadouts")
data class EquippedLoadoutEntity(
    @PrimaryKey val playerId: String,
    val diceId: String = "dice_imperial_gold",
    val frameId: String = "frame_royal_crest",
    val boardId: String = "imperial_navy",
    val pawnId: String = "crown_maharaja",
    val pawnTrailEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_transactions")
data class PurchaseTransactionEntity(
    @PrimaryKey val transactionId: String,
    val playerId: String,
    val itemId: String,
    val amount: Long,
    val currencyType: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reward_transactions")
data class RewardTransactionEntity(
    @PrimaryKey val rewardId: String,
    val playerId: String,
    val adOrEventId: String,
    val itemId: String? = null,
    val coinsGranted: Long = 0L,
    val cashGranted: Int = 0,
    val verified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emoji_deck_slots")
data class EmojiDeckSlotEntity(
    @PrimaryKey val compositeId: String, // "${playerId}_${slotIndex}"
    val playerId: String,
    val slotIndex: Int, // 0..17
    val emojiItemId: String?
)

@Entity(tableName = "active_powerups")
data class ActivePowerupEntity(
    @PrimaryKey val compositeId: String, // "${playerId}_${itemId}"
    val playerId: String,
    val itemId: String,
    val powerupType: String,
    val quantity: Int,
    val activeUntilMillis: Long = 0L,
    val lastActivatedMillis: Long = 0L,
    val dailyUsedCount: Int = 0,
    val dailyResetDay: Long = 0L
)

@Entity(tableName = "event_progress")
data class EventProgressEntity(
    @PrimaryKey val compositeId: String, // "${playerId}_${eventOrItemId}"
    val playerId: String,
    val eventOrItemId: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val expiresAtMillis: Long = Long.MAX_VALUE
)
