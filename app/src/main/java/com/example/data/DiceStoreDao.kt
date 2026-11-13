package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DiceStoreDao {

    // 1. DiceCatalog
    @Query("SELECT * FROM dice_catalog ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllDiceCatalog(): List<DiceCatalogEntity>

    @Query("SELECT * FROM dice_catalog WHERE diceId = :diceId")
    suspend fun getDiceCatalogById(diceId: String): DiceCatalogEntity?

    @Query("SELECT * FROM dice_catalog WHERE skuId = :skuId LIMIT 1")
    suspend fun getDiceCatalogBySku(skuId: String): DiceCatalogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceCatalog(item: DiceCatalogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceCatalogAll(items: List<DiceCatalogEntity>)

    @Query("DELETE FROM dice_catalog WHERE diceId = :diceId")
    suspend fun deleteDiceCatalog(diceId: String)

    // 2. DiceAssets
    @Query("SELECT * FROM dice_assets")
    suspend fun getAllDiceAssets(): List<DiceAssetEntity>

    @Query("SELECT * FROM dice_assets WHERE diceId = :diceId")
    suspend fun getDiceAssetById(diceId: String): DiceAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceAsset(asset: DiceAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceAssetsAll(assets: List<DiceAssetEntity>)

    @Query("DELETE FROM dice_assets WHERE diceId = :diceId")
    suspend fun deleteDiceAsset(diceId: String)

    // 3. DiceBenefits
    @Query("SELECT * FROM dice_benefits")
    suspend fun getAllDiceBenefits(): List<DiceBenefitEntity>

    @Query("SELECT * FROM dice_benefits WHERE diceId = :diceId")
    suspend fun getDiceBenefitById(diceId: String): DiceBenefitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceBenefit(benefit: DiceBenefitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceBenefitsAll(benefits: List<DiceBenefitEntity>)

    @Query("DELETE FROM dice_benefits WHERE diceId = :diceId")
    suspend fun deleteDiceBenefit(diceId: String)

    // 4. PlayerOwnedDice
    @Query("SELECT * FROM player_owned_dice WHERE playerId = :playerId AND isRevoked = 0")
    suspend fun getOwnedDice(playerId: String): List<PlayerOwnedDiceEntity>

    @Query("SELECT * FROM player_owned_dice WHERE compositeId = :compositeId")
    suspend fun getOwnedDiceByComposite(compositeId: String): PlayerOwnedDiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOwnedDice(owned: PlayerOwnedDiceEntity)

    @Query("DELETE FROM player_owned_dice WHERE compositeId = :compositeId")
    suspend fun deleteOwnedDice(compositeId: String)

    // 5. PlayerEquippedDice
    @Query("SELECT * FROM player_equipped_dice WHERE playerId = :playerId")
    suspend fun getEquippedDice(playerId: String): PlayerEquippedDiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEquippedDice(equipped: PlayerEquippedDiceEntity)

    // 6. CoinTransactions
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCoinTransaction(tx: CoinTransactionEntity)

    @Query("SELECT * FROM coin_transactions WHERE playerId = :playerId ORDER BY timestamp DESC")
    suspend fun getCoinTransactions(playerId: String): List<CoinTransactionEntity>

    // 7. RealMoneyTransactions
    @Query("SELECT * FROM real_money_transactions WHERE purchaseToken = :purchaseToken LIMIT 1")
    suspend fun getRealMoneyTxByToken(purchaseToken: String): RealMoneyTransactionEntity?

    @Query("SELECT * FROM real_money_transactions WHERE playerId = :playerId ORDER BY verifiedAt DESC")
    suspend fun getRealMoneyTransactions(playerId: String): List<RealMoneyTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRealMoneyTransaction(tx: RealMoneyTransactionEntity)

    // 8. DicePurchaseHistory
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDicePurchaseHistory(history: DicePurchaseHistoryEntity)

    @Query("SELECT * FROM dice_purchase_history WHERE playerId = :playerId ORDER BY timestamp DESC")
    suspend fun getDicePurchaseHistory(playerId: String): List<DicePurchaseHistoryEntity>

    // 9. DiceEventRewards
    @Query("SELECT * FROM dice_event_rewards WHERE playerId = :playerId")
    suspend fun getDiceEventRewards(playerId: String): List<DiceEventRewardEntity>

    @Query("SELECT * FROM dice_event_rewards WHERE compositeId = :compositeId")
    suspend fun getDiceEventRewardByComposite(compositeId: String): DiceEventRewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiceEventReward(reward: DiceEventRewardEntity)
}
