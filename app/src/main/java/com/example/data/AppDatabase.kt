package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MatchHistoryEntity::class,
        DailyRewardEntity::class,
        UserProfileEntity::class,
        AchievementEntity::class,
        PlayerWalletEntity::class,
        OwnedItemEntity::class,
        EquippedLoadoutEntity::class,
        PurchaseTransactionEntity::class,
        RewardTransactionEntity::class,
        EmojiDeckSlotEntity::class,
        ActivePowerupEntity::class,
        EventProgressEntity::class,
        DiceCatalogEntity::class,
        DiceAssetEntity::class,
        DiceBenefitEntity::class,
        PlayerOwnedDiceEntity::class,
        PlayerEquippedDiceEntity::class,
        CoinTransactionEntity::class,
        RealMoneyTransactionEntity::class,
        DicePurchaseHistoryEntity::class,
        DiceEventRewardEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun dailyRewardDao(): DailyRewardDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun achievementDao(): AchievementDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun diceStoreDao(): DiceStoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "royal_ludo_database.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
