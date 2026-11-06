package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRewardDao {
    @Query("SELECT * FROM daily_rewards WHERE id = 1 LIMIT 1")
    fun getDailyReward(): Flow<DailyRewardEntity?>

    @Query("SELECT * FROM daily_rewards WHERE id = 1 LIMIT 1")
    suspend fun getDailyRewardDirect(): DailyRewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyReward(reward: DailyRewardEntity)
}
