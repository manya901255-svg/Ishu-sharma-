package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- Wallet ---
    @Query("SELECT * FROM player_wallets WHERE playerId = :playerId")
    suspend fun getWallet(playerId: String): PlayerWalletEntity?

    @Query("SELECT * FROM player_wallets WHERE playerId = :playerId")
    fun observeWallet(playerId: String): Flow<PlayerWalletEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWallet(wallet: PlayerWalletEntity)

    // --- Owned Items ---
    @Query("SELECT * FROM owned_items WHERE playerId = :playerId")
    suspend fun getOwnedItems(playerId: String): List<OwnedItemEntity>

    @Query("SELECT * FROM owned_items WHERE playerId = :playerId")
    fun observeOwnedItems(playerId: String): Flow<List<OwnedItemEntity>>

    @Query("SELECT * FROM owned_items WHERE compositeId = :compositeId")
    suspend fun getOwnedItem(compositeId: String): OwnedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOwnedItem(item: OwnedItemEntity)

    // --- Equipped Loadout ---
    @Query("SELECT * FROM equipped_loadouts WHERE playerId = :playerId")
    suspend fun getEquippedLoadout(playerId: String): EquippedLoadoutEntity?

    @Query("SELECT * FROM equipped_loadouts WHERE playerId = :playerId")
    fun observeEquippedLoadout(playerId: String): Flow<EquippedLoadoutEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEquippedLoadout(loadout: EquippedLoadoutEntity)

    // --- Purchase Transactions ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPurchaseTransaction(tx: PurchaseTransactionEntity)

    @Query("SELECT * FROM purchase_transactions WHERE playerId = :playerId ORDER BY timestamp DESC")
    suspend fun getPurchaseTransactions(playerId: String): List<PurchaseTransactionEntity>

    // --- Reward Transactions (Ad & Event deduplication) ---
    @Query("SELECT * FROM reward_transactions WHERE rewardId = :rewardId")
    suspend fun getRewardTransaction(rewardId: String): RewardTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRewardTransaction(tx: RewardTransactionEntity)

    @Query("SELECT * FROM reward_transactions WHERE playerId = :playerId ORDER BY timestamp DESC")
    suspend fun getRewardTransactions(playerId: String): List<RewardTransactionEntity>

    // --- Emoji Deck (18 Slots) ---
    @Query("SELECT * FROM emoji_deck_slots WHERE playerId = :playerId ORDER BY slotIndex ASC")
    suspend fun getEmojiDeckSlots(playerId: String): List<EmojiDeckSlotEntity>

    @Query("SELECT * FROM emoji_deck_slots WHERE playerId = :playerId ORDER BY slotIndex ASC")
    fun observeEmojiDeckSlots(playerId: String): Flow<List<EmojiDeckSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmojiDeckSlot(slot: EmojiDeckSlotEntity)

    // --- Active Powerups ---
    @Query("SELECT * FROM active_powerups WHERE playerId = :playerId")
    suspend fun getActivePowerups(playerId: String): List<ActivePowerupEntity>

    @Query("SELECT * FROM active_powerups WHERE playerId = :playerId")
    fun observeActivePowerups(playerId: String): Flow<List<ActivePowerupEntity>>

    @Query("SELECT * FROM active_powerups WHERE compositeId = :compositeId")
    suspend fun getActivePowerup(compositeId: String): ActivePowerupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivePowerup(powerup: ActivePowerupEntity)

    // --- Event & Rewarded Item Progress ---
    @Query("SELECT * FROM event_progress WHERE playerId = :playerId")
    suspend fun getEventProgressList(playerId: String): List<EventProgressEntity>

    @Query("SELECT * FROM event_progress WHERE playerId = :playerId")
    fun observeEventProgressList(playerId: String): Flow<List<EventProgressEntity>>

    @Query("SELECT * FROM event_progress WHERE compositeId = :compositeId")
    suspend fun getEventProgress(compositeId: String): EventProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEventProgress(progress: EventProgressEntity)
}
