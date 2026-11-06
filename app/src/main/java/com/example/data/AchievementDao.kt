package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET currentProgress = :progress, isCompleted = :completed WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, completed: Boolean)

    @Query("UPDATE achievements SET isClaimed = 1 WHERE id = :id")
    suspend fun claimAchievement(id: String)
}
