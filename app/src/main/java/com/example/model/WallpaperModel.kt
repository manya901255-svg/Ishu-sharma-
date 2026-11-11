package com.example.model

import com.example.R

/**
 * Target areas in MG Ludo that can have distinct, independent wallpapers.
 */
enum class WallpaperTarget(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val defaultWallpaperId: String
) {
    HOME(
        id = "target_home",
        title = "Home Screen",
        subtitle = "Main lobby and carousel backdrop",
        iconEmoji = "🏠",
        defaultWallpaperId = "edison_amber_bulbs"
    ),
    BOARD(
        id = "target_board",
        title = "Ludo Board",
        subtitle = "In-game battleground background",
        iconEmoji = "🎲",
        defaultWallpaperId = "edison_amber_bulbs"
    ),
    LOBBY(
        id = "target_lobby",
        title = "Game Lobby",
        subtitle = "Online mode wager selection screen",
        iconEmoji = "⚔️",
        defaultWallpaperId = "default_sky_lobby"
    ),
    PROFILE(
        id = "target_profile",
        title = "Profile Background",
        subtitle = "Royal player profile and stats screen",
        iconEmoji = "👑",
        defaultWallpaperId = "default_royal_navy"
    ),
    PRIVATE_ROOM(
        id = "target_private_room",
        title = "Private Room",
        subtitle = "Chamber for games with friends",
        iconEmoji = "🏰",
        defaultWallpaperId = "edison_amber_bulbs"
    );

    companion object {
        fun fromId(id: String): WallpaperTarget {
            return entries.firstOrNull { it.id == id } ?: HOME
        }
    }
}

/**
 * Filter categories for browsing wallpapers.
 */
enum class WallpaperCategory(
    val id: String,
    val title: String,
    val iconEmoji: String
) {
    ALL("all", "All Wallpapers", "✨"),
    DEFAULT("default", "Presets", "🎨"),
    MY_UPLOADS("uploads", "My Wallpapers", "📁"),
    FAVORITES("favorites", "Favorites", "❤️"),
    RECENT("recent", "Recently Used", "⏱️")
}

/**
 * Represents a single wallpaper item (built-in preset or user-uploaded file).
 */
data class WallpaperItem(
    val id: String,
    val title: String,
    val description: String,
    val drawableRes: Int? = null,
    val gradientColors: List<Long>? = null,
    val localFilePath: String? = null,
    val isCustomUpload: Boolean = false,
    val tags: List<String> = emptyList()
) {
    val isLocalFile: Boolean get() = isCustomUpload && !localFilePath.isNullOrEmpty()
}

/**
 * Built-in collection of 12 premium themes matching royal Indian MG Ludo aesthetics.
 */
val BUILT_IN_WALLPAPERS: List<WallpaperItem> = listOf(
    WallpaperItem(
        id = "edison_amber_bulbs",
        title = "Vintage Edison Bulbs",
        description = "Warm glowing filaments & cozy dark brick ambiance",
        drawableRes = R.drawable.bg_vintage_amber_bulb,
        tags = listOf("featured", "vintage", "warm", "edison", "popular", "aesthetic")
    ),
    WallpaperItem(
        id = "default_purple_wood",
        title = "Royal Purple Wood",
        description = "Classic MG Ludo plank texture with golden trim",
        drawableRes = R.drawable.bg_purple_wood,
        tags = listOf("classic", "purple", "wood", "popular")
    ),
    WallpaperItem(
        id = "default_royal_navy",
        title = "Royal Deep Blue",
        description = "Midnight sapphire gradient with glowing ambient aura",
        gradientColors = listOf(0xFF040A1AL, 0xFF091C3EL, 0xFF0D285AL, 0xFF081938L, 0xFF030713L),
        tags = listOf("royal", "blue", "sapphire", "official")
    ),
    WallpaperItem(
        id = "default_midnight_board",
        title = "Dark Midnight Stealth",
        description = "Concentrated deep midnight blue for high-focus gameplay",
        gradientColors = listOf(0xFF0B1938L, 0xFF071228L, 0xFF040A18L),
        tags = listOf("dark", "stealth", "board", "focus")
    ),
    WallpaperItem(
        id = "default_sky_lobby",
        title = "Sky Cyan Arena",
        description = "Vibrant azure sky with celestial cloud wisps",
        gradientColors = listOf(0xFF5BA7FFL, 0xFF82C4FFL, 0xFFA5DBFFL),
        tags = listOf("sky", "cyan", "lobby", "bright")
    ),
    WallpaperItem(
        id = "luxury_gold_palace",
        title = "Luxury Gold Palace",
        description = "Imperial Rajasthani palace amber and warm candlelight",
        gradientColors = listOf(0xFF1F1200L, 0xFF4A3000L, 0xFF8A5D00L, 0xFF362000L, 0xFF140B00L),
        tags = listOf("gold", "luxury", "palace", "imperial")
    ),
    WallpaperItem(
        id = "neon_cyber_gaming",
        title = "Neon Gaming Cyber",
        description = "Electric ultraviolet and cyber pulse glow",
        gradientColors = listOf(0xFF0D0221L, 0xFF19053BL, 0xFF261447L, 0xFF0F084BL, 0xFF020014L),
        tags = listOf("neon", "cyber", "gaming", "party")
    ),
    WallpaperItem(
        id = "mystic_forest_nature",
        title = "Emerald Empire",
        description = "Lush jade greenery and mist from royal valley gardens",
        gradientColors = listOf(0xFF021B0FL, 0xFF073820L, 0xFF0D5230L, 0xFF08331EL, 0xFF01120AL),
        tags = listOf("nature", "emerald", "green", "peaceful")
    ),
    WallpaperItem(
        id = "cosmic_galaxy_space",
        title = "Cosmic Galaxy",
        description = "Deep violet starfield and distant stellar nebulae",
        gradientColors = listOf(0xFF08011AL, 0xFF160636L, 0xFF2A084EL, 0xFF100329L, 0xFF03000AL),
        tags = listOf("space", "galaxy", "stars", "cosmic")
    ),
    WallpaperItem(
        id = "ancient_crimson_ruby",
        title = "Royal Crimson Ruby",
        description = "Mughal velvet red and ruby stardust",
        gradientColors = listOf(0xFF2A050BL, 0xFF520B16L, 0xFF7A1322L, 0xFF420710L, 0xFF170206L),
        tags = listOf("crimson", "ruby", "red", "regal")
    ),
    WallpaperItem(
        id = "minimal_slate_sapphire",
        title = "Minimal Sapphire Slate",
        description = "Clean modern matte navy texture with zero distractions",
        gradientColors = listOf(0xFF101726L, 0xFF1B2438L, 0xFF25334DL, 0xFF182236L, 0xFF0D121EL),
        tags = listOf("minimal", "clean", "matte", "modern")
    ),
    WallpaperItem(
        id = "classical_teak_wood",
        title = "Classical Teak Wood",
        description = "Richly polished vintage timber board grain",
        gradientColors = listOf(0xFF2B1810L, 0xFF4A2A1BL, 0xFF693C27L, 0xFF3D2216L, 0xFF1F100BL),
        tags = listOf("wood", "vintage", "antique", "teak")
    ),
    WallpaperItem(
        id = "aurora_fantasy",
        title = "Aurora Fantasy",
        description = "Mystical arctic luminescence with turquoise ribbons",
        gradientColors = listOf(0xFF031926L, 0xFF0D3B66L, 0xFF235789L, 0xFF4682B4L, 0xFF0B1D28L),
        tags = listOf("fantasy", "aurora", "shimmer", "magic")
    )
)
