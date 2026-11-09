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
        instrumentalStyle = "Raag Yaman Classical Sitar",
        category = MusicCategory.HINDI,
        rawResId = R.raw.royal_court_raga,
        durationLabel = "3:45",
        durationSeconds = 225,
        iconEmoji = "🪕",
        licenseType = "Royalty Free (MG Ludo Original)",
        attribution = "MG Ludo Royal Sound Design"
    ),
    BgmTrack(
        id = "track_romantic_piano_santoor",
        title = "Romantic Piano & Santoor Serenade",
        singer = "Kashmir Valley Melodies",
        instrumentalStyle = "Santoor & Acoustic Piano Chords",
        category = MusicCategory.ROMANTIC,
        rawResId = R.raw.romantic_piano_santoor,
        durationLabel = "3:20",
        durationSeconds = 200,
        iconEmoji = "❤️",
        licenseType = "Royalty Free (MG Ludo Original)",
        attribution = "MG Ludo Royal Sound Design"
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
        singer = "Pandit Hariprasad Style Flutist",
        instrumentalStyle = "Deep Bamboo Flute Meditation",
        category = MusicCategory.INSTRUMENTAL,
        rawResId = R.raw.mystic_palace_bansuri,
        durationLabel = "4:00",
        durationSeconds = 240,
        iconEmoji = "🪈",
        licenseType = "Royalty Free (MG Ludo Original)",
        attribution = "MG Ludo Royal Sound Design"
    ),
    BgmTrack(
        id = "legal_vande_mataram",
        title = "Vande Mataram (National Melody)",
        singer = "Hemant Kumar",
        albumName = "Anandamath / Indian Heritage Classics",
        category = MusicCategory.HINDI,
        durationLabel = "2:32",
        durationSeconds = 152,
        streamUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4b/Vande_Mataram_-_Hemant_Kumar.ogg",
        iconEmoji = "🇮🇳",
        licenseType = "Public Domain",
        licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
        attribution = "Hemant Kumar • Public Domain (Historical Indian Archive)"
    ),
    BgmTrack(
        id = "legal_ganpati_vandana",
        title = "Ganpati Vandana & Aarti",
        singer = "Shri Hari Om Sharan Ji",
        albumName = "Sacred Indian Devotional Collection",
        category = MusicCategory.DEVOTIONAL,
        durationLabel = "1:16",
        durationSeconds = 76,
        streamUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2d/Aarti_Puja%2C_Ganpati_Vandana.ogg",
        iconEmoji = "🙏",
        licenseType = "CC0 1.0 (Public Domain)",
        licenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/",
        attribution = "Wikimedia Commons CC0 Public Domain"
    ),
    BgmTrack(
        id = "legal_raga_yaman_peaceful",
        title = "Raag Yaman (Peaceful Sitar & Flute)",
        singer = "Pandit Ravi Shankar Style Ensemble",
        albumName = "Eternal Indian Evening Ragas",
        category = MusicCategory.INSTRUMENTAL,
        durationLabel = "2:45",
        durationSeconds = 165,
        streamUrl = "https://upload.wikimedia.org/wikipedia/commons/e/ec/Yaman_sitar.ogg",
        iconEmoji = "🪈",
        licenseType = "CC BY-SA 4.0",
        licenseUrl = "https://creativecommons.org/licenses/by-sa/4.0/",
        attribution = "Classical Indian Ragas • CC BY-SA 4.0"
    )
)
