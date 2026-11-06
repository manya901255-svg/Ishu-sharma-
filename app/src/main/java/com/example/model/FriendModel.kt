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

val MOCK_ROYAL_FRIENDS = listOf(
    RoyalFriend(
        id = "f_1",
        name = "Shahenshah Kabir",
        title = "Shahenshah",
        crownAvatarId = "crown_kohinoor_diamond",
        isOnline = true,
        statusText = "Online • In Lobby",
        level = 24,
        coins = 85000L,
        wins = 52,
        winStreak = 5,
        isFavorite = true
    ),
    RoyalFriend(
        id = "f_2",
        name = "Princess Ananya",
        title = "Maharani",
        crownAvatarId = "crown_maharani_pearl",
        isOnline = true,
        statusText = "Online • Ready to Play 🎲",
        level = 19,
        coins = 42000L,
        wins = 36,
        winStreak = 3,
        isFavorite = true
    ),
    RoyalFriend(
        id = "f_3",
        name = "Veer Rajput",
        title = "Senapati",
        crownAvatarId = "crown_warrior_rajput",
        isOnline = true,
        statusText = "In Match • Quick Rush ⚡",
        level = 21,
        coins = 61000L,
        wins = 44,
        winStreak = 2
    ),
    RoyalFriend(
        id = "f_4",
        name = "Raja Vikramaditya",
        title = "Raja",
        crownAvatarId = "crown_lion_emperor",
        isOnline = true,
        statusText = "Online • In Lobby",
        level = 28,
        coins = 120000L,
        wins = 78,
        winStreak = 7,
        isFavorite = true
    ),
    RoyalFriend(
        id = "f_5",
        name = "Sultan Amar",
        title = "Sultan",
        crownAvatarId = "crown_ruby_sun",
        isOnline = true,
        statusText = "In Match • 4-Player Court 👑",
        level = 16,
        coins = 28000L,
        wins = 25,
        winStreak = 1
    ),
    RoyalFriend(
        id = "f_6",
        name = "Queen Tara",
        title = "Maharani",
        crownAvatarId = "crown_lotus_blossom",
        isOnline = false,
        statusText = "Offline • 25m ago",
        level = 14,
        coins = 19500L,
        wins = 18,
        winStreak = 0
    ),
    RoyalFriend(
        id = "f_7",
        name = "Chhatrapati Samar",
        title = "Maharaja",
        crownAvatarId = "crown_imperial_gold",
        isOnline = false,
        statusText = "Offline • 2h ago",
        level = 22,
        coins = 73000L,
        wins = 49,
        winStreak = 4
    ),
    RoyalFriend(
        id = "f_8",
        name = "Royal Falcon Dev",
        title = "Yuvraj",
        crownAvatarId = "crown_falcon_crest",
        isOnline = false,
        statusText = "Offline • 1d ago",
        level = 11,
        coins = 14000L,
        wins = 12,
        winStreak = 0
    )
)
