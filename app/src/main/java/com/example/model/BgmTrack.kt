package com.example.model

/**
 * MusicCategory represents the tabs/filters in the in-game Music Player.
 */
enum class MusicCategory(val label: String, val icon: String) {
    TRENDING_MODERN("Trending/Modern", "🔥"),
    RETRO_CLASSIC("Retro/Classic", "📻"),
    INSTRUMENTAL("Instrumental", "🪈")
}

/**
 * MusicRepeatMode represents playback looping options.
 */
enum class MusicRepeatMode(val label: String, val icon: String) {
    ALL("Repeat All", "🔁"),
    ONE("Repeat One", "🔂"),
    OFF("Repeat Off", "➡️")
}

/**
 * BgmTrack represents one background music track in the in-game playlist.
 */
data class BgmTrack(
    val id: String,
    val title: String,
    val singer: String,
    val instrumentalStyle: String,
    val category: MusicCategory,
    val assetFileName: String = "",
    val rawResId: Int = 0,
    val durationLabel: String = "3:45",
    val iconEmoji: String = "🎵",
    val isFavorite: Boolean = false
) {
    val subtitle: String get() = "$singer • $instrumentalStyle"
}

/**
 * All background music songs have been completely removed per user request.
 * The playlist is empty, while sound effects (SFX) and haptics remain active.
 */
val BGM_PLAYLIST: List<BgmTrack> = emptyList()

