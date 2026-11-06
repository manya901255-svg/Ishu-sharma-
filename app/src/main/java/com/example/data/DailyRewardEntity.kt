package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DailyRewardEntity: Tracks daily login streak and claimed gold coins & gems.
 */
@Entity(tableName = "daily_rewards")
data class DailyRewardEntity(
    @PrimaryKey
    val id: Int = 1,
    val lastClaimDate: String, // "YYYY-MM-DD"
    val streakDay: Int, // 1..7
    val totalClaimedCoins: Long = 0L,
    val totalClaimedGems: Int = 0,
    val lastClaimTimestamp: Long = 0L
)
