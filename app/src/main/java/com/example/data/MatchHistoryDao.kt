package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 10")
    fun getLast10Matches(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistoryEntity): Long

    @Query("SELECT COUNT(*) FROM match_history")
    suspend fun getMatchCount(): Int

    @Query("DELETE FROM match_history")
    suspend fun clearHistory()
}
