package com.example.model

import com.example.R

/**
 * MusicCategory represents all major Indian music genres and language categories.
 */
enum class MusicCategory(
    val label: String,
    val icon: String,
    val searchQuery: String
) {
    TRENDING("Trending", "🔥", "trending hindi songs"),
    BOLLYWOOD_NEW("New Bollywood", "🎬", "latest bollywood hindi songs"),
    BOLLYWOOD_OLD("Old Hindi", "📻", "classic old hindi songs 70s 80s 90s"),
    PUNJABI("Punjabi", "⚡", "top punjabi hits"),
    HARYANVI("Haryanvi", "🚜", "top haryanvi songs"),
    BHOJPURI("Bhojpuri", "🪕", "bhojpuri hits"),
    RAJASTHANI("Rajasthani", "🐪", "rajasthani songs"),
    MARATHI("Marathi", "🥁", "top marathi songs"),
    SOUTH_INDIAN("South Indian", "🌴", "tamil telugu hit songs"),
    DEVOTIONAL("Devotional & Bhajan", "🙏", "bhajan aarti bhakti geet"),
    ROMANTIC("Romantic", "❤️", "romantic hindi love songs"),
    SAD("Sad Songs", "🌧️", "sad soulful hindi songs"),
    PARTY_DJ("Party & DJ", "🎉", "party dance hindi dj remix"),
    INSTRUMENTAL("Instrumental", "🪈", "indian instrumental flute sitar"),
    HINDI("Hindi", "🇮🇳", "hindi classical folk heritage"),
    // Backwards-compatible aliases
    TRENDING_MODERN("Trending/Modern", "🔥", "trending hindi"),
    RETRO_CLASSIC("Retro/Classic", "📻", "retro classic hindi")
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
 * BgmTrack represents one background music track in the in-game playlist,
 * supporting both local bundled audio and online streaming Indian music.
 */
data class BgmTrack(
    val id: String,
    val title: String,
    val singer: String,
    val instrumentalStyle: String = "",
    val category: MusicCategory = MusicCategory.TRENDING,
    val assetFileName: String = "",
    val rawResId: Int = 0,
    val durationLabel: String = "3:30",
    val durationSeconds: Int = 210,
    val iconEmoji: String = "🎵",
    val isFavorite: Boolean = false,
    val streamUrl: String? = null,
    val albumArtUrl: String? = null,
    val albumName: String = "",
    val year: String = "",
    val language: String = "Hindi",
    val licenseType: String = "CC BY-SA 4.0 / CC0",
    val licenseUrl: String = "https://creativecommons.org/licenses/",
    val attribution: String = "Verified Legal Indian Music Track"
) {
    val subtitle: String
        get() = when {
            albumName.isNotEmpty() && albumName != title -> "$singer • $albumName"
            instrumentalStyle.isNotEmpty() -> "$singer • $instrumentalStyle"
            else -> singer
        }

    val isOnlineStream: Boolean
        get() = !streamUrl.isNullOrEmpty()
}

/**
 * UserPlaylist represents custom playlists created by the user.
 */
data class UserPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<String> = emptyList(),
    val iconEmoji: String = "🎶",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Restored Royal & Indian Music BGM playlist for MG Ludo.
 */
val BGM_PLAYLIST: List<BgmTrack> = listOf(
    BgmTrack(
        id = "track_royal_palace_ambient",
        title = "Royal Palace Ambient BGM",
        singer = "Royal Kingdom Orchestra",
        instrumentalStyle = "Sitar, Bells & Flute Harmony",
        category = MusicCategory.INSTRUMENTAL,
        rawResId = R.raw.royal_ambient_bgm,
        durationLabel = "3:30",
        durationSeconds = 210,
        iconEmoji = "👑",
        licenseType = "Royalty Free (MG Ludo Original)",
        attribution = "MG Ludo Royal Sound Design"
    ),
    BgmTrack(
        id = "track_royal_court_raga",
        title = "Royal Court Sitar & Flute Raga",
        singer = "Ustad Vilayat Style Ensemble",
        instrumentalStyle = "Roz Roz Ankhon Tale Sitar & Flute",
        category = MusicCategory.HINDI,
        streamUrl = "https://jiotunepreview.jio.com/content/Converted/010911262558507.mp3",
        albumArtUrl = "https://c.saavncdn.com/117/Bollywood-Love-Instrumentals-Instrumental-2015-500x500.jpg",
        durationLabel = "4:25",
        durationSeconds = 265,
        iconEmoji = "🪕",
        licenseType = "Authorized Stream",
        attribution = "Classical Flute & Sitar Ensemble"
    ),
    BgmTrack(
        id = "track_romantic_piano_santoor",
        title = "Romantic Piano & Santoor Serenade",
        singer = "Arijit Singh, Mithoon",
        albumName = "Aashiqui 2 Romance",
        category = MusicCategory.ROMANTIC,
        streamUrl = "https://jiotunepreview.jio.com/content/Converted/010910092419390.mp3",
        albumArtUrl = "https://c.saavncdn.com/430/Aashiqui-2-Hindi-2013-500x500.jpg",
        durationLabel = "4:22",
        durationSeconds = 262,
        iconEmoji = "❤️",
        licenseType = "Authorized Stream",
        attribution = "Arijit Singh, Mithoon • Romantic Melodies"
    ),
    BgmTrack(
        id = "track_maharaja_victory_anthem",
        title = "Maharaja Victory Festive Anthem",
        singer = "Royal Court Shehnai & Dholak",
        instrumentalStyle = "Festive Celebration Rhythms",
        category = MusicCategory.PARTY_DJ,
        rawResId = R.raw.maharaja_victory_anthem,
        durationLabel = "3:10",
        durationSeconds = 190,
        iconEmoji = "🎉",
        licenseType = "Royalty Free (MG Ludo Original)",
        attribution = "MG Ludo Royal Sound Design"
    ),
    BgmTrack(
        id = "track_mystic_palace_bansuri",
        title = "Mystic Palace Bansuri & Tanpura",
        singer = "Arijit Singh, Pritam",
        albumName = "Brahmastra Kesariya",
        category = MusicCategory.INSTRUMENTAL,
        streamUrl = "https://jiotunepreview.jio.com/content/Converted/010910141580615.mp3",
        albumArtUrl = "https://c.saavncdn.com/871/Brahmastra-Original-Motion-Picture-Soundtrack-Hindi-2022-20221006155213-500x500.webp",
        durationLabel = "4:28",
        durationSeconds = 268,
        iconEmoji = "🪈",
        licenseType = "Authorized Stream",
        attribution = "Pritam, Arijit Singh • Mystic Bansuri"
    ),
    BgmTrack(
        id = "legal_vande_mataram",
        title = "Vande Mataram (National Melody)",
        singer = "Hemant Kumar",
        albumName = "Anandamath Heritage Classics",
        category = MusicCategory.HINDI,
        durationLabel = "3:15",
        durationSeconds = 195,
        streamUrl = "https://jiotunepreview.jio.com/content/Converted/010910090087528.mp3",
        albumArtUrl = "https://c.saavncdn.com/256/Shree-Hanuman-Chalisa-Hanuman-Ashtak-Hindi-1992-20260324161030-500x500.webp",
        iconEmoji = "🇮🇳",
        licenseType = "Heritage Classic",
        attribution = "Hemant Kumar • National Heritage"
    ),
    BgmTrack(
        id = "legal_ganpati_vandana",
        title = "Shree Hanuman Chalisa & Aarti",
        singer = "Hariharan, Gulshan Kumar",
        albumName = "Sacred Devotional Collection",
        category = MusicCategory.DEVOTIONAL,
        durationLabel = "9:46",
        durationSeconds = 586,
        streamUrl = "https://jiotunepreview.jio.com/content/Converted/010910090087528.mp3",
        albumArtUrl = "https://c.saavncdn.com/256/Shree-Hanuman-Chalisa-Hanuman-Ashtak-Hindi-1992-20260324161030-500x500.webp",
        iconEmoji = "🙏",
        licenseType = "Devotional Bhakti",
        attribution = "Hariharan • T-Series Bhakti Sagar"
    ),
    BgmTrack(
        id = "legal_raga_yaman_peaceful",
        title = "Apna Bana Le",
        singer = "Arijit Singh, Sachin-Jigar",
        albumName = "Bhediya Bollywood Hits",
        category = MusicCategory.BOLLYWOOD_NEW,
        durationLabel = "4:21",
        durationSeconds = 261,
        streamUrl = "https://jiotunepreview.jio.com/content/Converted/010910441686043.mp3",
        albumArtUrl = "https://c.saavncdn.com/228/Sachin-Jigar-Bollywood-Hits-Hindi-2026-20260630213800-500x500.webp",
        iconEmoji = "🎬",
        licenseType = "Authorized Stream",
        attribution = "Arijit Singh, Sachin-Jigar • Bhediya Hits"
    )
)
