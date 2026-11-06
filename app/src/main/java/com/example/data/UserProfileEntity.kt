package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing the persisted Player Profile.
 * Supports display name, regal crown avatar, custom user display photo ("apni display profile"),
 * royal title, and player career statistics.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val nickname: String = "Ishu",
    val crownAvatar: String = "crown_imperial_gold",
    val customPhotoUri: String? = null,
    val title: String = "Maharaja",
    val level: Int = 18,
    val totalWins: Int = 28,
    val totalGamesPlayed: Int = 42,
    val winStreak: Int = 4,
    val totalCaptures: Int = 89,
    val equippedPawnCrownAvatar: String = "crown_imperial_gold",
    val lastSpinTimestampMillis: Long = 0L,
    val isGuestProfileConfigured: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
