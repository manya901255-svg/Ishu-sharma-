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
 * Authentic Bollywood, Punjabi & Instrumental Custom Background Music Playlist
 * featuring all songs added for MG Ludo. Each track points 1:1 to an actual bundled
 * audio file in assets/bgm.
 */
val BGM_PLAYLIST: List<BgmTrack> = listOf(
    // --- 1. Trending & Modern Bollywood / Punjabi Hits ---
    BgmTrack(
        id = "track_guzarish",
        title = "Guzarish",
        singer = "Satinder Sartaaj",
        instrumentalStyle = "Sufi Acoustic & Poetry",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "guzarish.mp3",
        durationLabel = "4:48",
        iconEmoji = "📜"
    ),
    BgmTrack(
        id = "track_bacha",
        title = "Bacha",
        singer = "Prabh Gill",
        instrumentalStyle = "Punjabi Romantic Melodious",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "bacha_prabh_gill.mp3",
        durationLabel = "4:02",
        iconEmoji = "🧸"
    ),
    BgmTrack(
        id = "track_bach_ke_reh",
        title = "Bach Ke Reh",
        singer = "Rupinder Handa",
        instrumentalStyle = "Punjabi Desi Dhol Beats",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "bach_ke_reh.mp3",
        durationLabel = "3:37",
        iconEmoji = "🔥"
    ),
    BgmTrack(
        id = "track_ami_je_tomar",
        title = "Ami Je Tomar",
        singer = "Arijit Singh (Bhool Bhulaiyaa 2)",
        instrumentalStyle = "Classical Thumri & Raag",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "ami_je_tomar.mp3",
        durationLabel = "1:38",
        iconEmoji = "🪕"
    ),
    BgmTrack(
        id = "track_agar_tum_saath_ho",
        title = "Agar Tum Saath Ho",
        singer = "Arijit Singh & Alka Yagnik",
        instrumentalStyle = "Heartfelt Piano & Cello Melancholy",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "agar_tum_saath_ho.mp3",
        durationLabel = "5:41",
        iconEmoji = "🌧️"
    ),
    BgmTrack(
        id = "track_aavan_jaavan",
        title = "Aavan Jaavan",
        singer = "War 2 Soundtrack",
        instrumentalStyle = "Dynamic Cinematic Bass & Beats",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "aavan_jaavan.mp3",
        durationLabel = "3:45",
        iconEmoji = "🚀"
    ),
    BgmTrack(
        id = "track_aashiqui_aa_gayi",
        title = "Aashiqui Aa Gayi",
        singer = "Mithoon & Arijit Singh",
        instrumentalStyle = "Grand Piano Love Anthem",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "aashiqui_aa_gayi.mp3",
        durationLabel = "4:20",
        iconEmoji = "🎹"
    ),
    BgmTrack(
        id = "track_terre_pyaar_mein",
        title = "Terre Pyaar Mein",
        singer = "Himesh Reshammiya",
        instrumentalStyle = "Signature High-Pitch Romantic Pop",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "terre_pyaar_mein.mp3",
        durationLabel = "5:33",
        iconEmoji = "🔥"
    ),

    // --- 2. Retro & Classic Bollywood Evergreen Hits ---
    BgmTrack(
        id = "track_ajab_si",
        title = "Aankhon Mein Teri (Ajab Si)",
        singer = "KK (Om Shanti Om)",
        instrumentalStyle = "Acoustic Guitar Romance & Nostalgia",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "ajab_si.mp3",
        durationLabel = "4:06",
        iconEmoji = "👀"
    ),

    // --- 3. Instrumental Masterpieces & Flute/Harmonica Covers ---
    BgmTrack(
        id = "track_flute_ishq_hua",
        title = "Ishq Hua (Flute Cover)",
        singer = "Vijay Tambe",
        instrumentalStyle = "Mesmerizing Classical Bamboo Flute",
        category = MusicCategory.INSTRUMENTAL,
        assetFileName = "flute_ishq_hua.mp3",
        durationLabel = "4:21",
        iconEmoji = "🪈"
    ),
    BgmTrack(
        id = "track_flute_o_re_piya",
        title = "O Re Piya (Flute Cover)",
        singer = "Vijay Tambe",
        instrumentalStyle = "Soothing Meditative Bamboo Flute",
        category = MusicCategory.INSTRUMENTAL,
        assetFileName = "flute_o_re_piya.mp3",
        durationLabel = "4:50",
        iconEmoji = "🪈"
    ),
    BgmTrack(
        id = "track_harmonica_gulabi_aankhen",
        title = "Gulabi Aankhen (Harmonica)",
        singer = "Shailesh Mogre",
        instrumentalStyle = "Vintage Nostalgic Harmonica Vibrato",
        category = MusicCategory.INSTRUMENTAL,
        assetFileName = "harmonica_gulabi_aankhen.mp3",
        durationLabel = "3:20",
        iconEmoji = "🎷"
    ),
    BgmTrack(
        id = "track_harmonica_jab_koi_baat",
        title = "Jab Koi Baat (Harmonica)",
        singer = "Shailesh Mogre",
        instrumentalStyle = "Evergreen Warm Harmonica Melody",
        category = MusicCategory.INSTRUMENTAL,
        assetFileName = "harmonica_jab_koi_baat.mp3",
        durationLabel = "5:53",
        iconEmoji = "🎷"
    ),
    BgmTrack(
        id = "track_harmonica_mere_mehboob",
        title = "Mere Mehboob Qayamat Hogi (Harmonica)",
        singer = "Shailesh Mogre",
        instrumentalStyle = "Golden Era Retro Harmonica Soul",
        category = MusicCategory.INSTRUMENTAL,
        assetFileName = "harmonica_mere_mehboob.mp3",
        durationLabel = "3:26",
        iconEmoji = "🎷"
    )
)
