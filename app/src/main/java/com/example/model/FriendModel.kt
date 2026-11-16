package com.example.model

data class RoyalFriend(
    val id: String,
    val name: String,
    val title: String,
    val crownAvatarId: String,
    val isOnline: Boolean,
    val statusText: String,
    val level: Int,
    val coins: Long,
    val wins: Int,
    val winStreak: Int = 2,
    val isFavorite: Boolean = false
)

