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
 * Complete Bollywood, Punjabi & Instrumental Background Music Playlist
 * featuring all songs uploaded for MG Ludo.
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
        id = "track_soniye_je_tere_naal",
        title = "Soniye Je Tere Naal",
        singer = "TP Tejas",
        instrumentalStyle = "Punjabi Rhythmic Melody",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "soniye_je_tere_naal.mp3",
        durationLabel = "2:11",
        iconEmoji = "💫"
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
        id = "track_khuda_jaane",
        title = "Khuda Jaane / Khuda",
        singer = "KK & Shilpa Rao",
        instrumentalStyle = "Soulful Ballad & Acoustic Strings",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "khuda_jaane.mp3",
        durationLabel = "3:30",
        iconEmoji = "✨"
    ),
    BgmTrack(
        id = "track_chor_ninja",
        title = "Chor",
        singer = "Ninja",
        instrumentalStyle = "Punjabi Emotional Pop Ballad",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "chor_ninja.mp3",
        durationLabel = "3:45",
        iconEmoji = "💔"
    ),
    BgmTrack(
        id = "track_ki_keya",
        title = "Ki Keya",
        singer = "Ishqnama",
        instrumentalStyle = "Sufi Fusion & Rabab",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "ki_keya.mp3",
        durationLabel = "3:23",
        iconEmoji = "🕊️"
    ),
    BgmTrack(
        id = "track_gulabi_aankhen_sanam",
        title = "Gulabi Aankhen (SANAM)",
        singer = "SANAM Band",
        instrumentalStyle = "Modern Pop-Rock Retro Revival",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "gulabi_aankhen_sanam.mp3",
        durationLabel = "3:17",
        iconEmoji = "🎸"
    ),
    BgmTrack(
        id = "track_lut_gaye",
        title = "Lut Gaye",
        singer = "Jubin Nautiyal",
        instrumentalStyle = "Contemporary Qawwali Pop",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "lut_gaye.mp3",
        durationLabel = "3:48",
        iconEmoji = "🥀"
    ),
    BgmTrack(
        id = "track_gehra_hua",
        title = "Gehra Hua",
        singer = "Dhurandhar",
        instrumentalStyle = "Atmospheric Lo-Fi Synth & Ambient",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "gehra_hua.mp3",
        durationLabel = "6:02",
        iconEmoji = "🌊"
    ),
    BgmTrack(
        id = "track_pehli_baar",
        title = "Pehli Baar",
        singer = "Ajay Gogavale (Dhadak)",
        instrumentalStyle = "Grand Symphony Orchestral",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "pehli_baar.mp3",
        durationLabel = "5:15",
        iconEmoji = "💓"
    ),
    BgmTrack(
        id = "track_dhadak_title",
        title = "Dhadak (Title Track)",
        singer = "Ajay Gogavale & Shreya Ghoshal",
        instrumentalStyle = "Romantic Violin Symphony",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "dhadak_title.mp3",
        durationLabel = "4:03",
        iconEmoji = "🎻"
    ),
    BgmTrack(
        id = "track_channa_mereya",
        title = "Channa Mereya",
        singer = "Arijit Singh",
        instrumentalStyle = "Soulful Acoustic Anthem",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "channa_mereya.mp3",
        durationLabel = "4:49",
        iconEmoji = "🕯️"
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
        id = "track_matargashti",
        title = "Matargashti",
        singer = "Mohit Chauhan (Tamasha)",
        instrumentalStyle = "Playful Gypsy Accordion & Whistle",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "matargashti.mp3",
        durationLabel = "5:28",
        iconEmoji = "🎺"
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
        id = "track_hai_dil_ye_mera",
        title = "Hai Dil Ye Mera",
        singer = "Arijit Singh (Hate Story 2)",
        instrumentalStyle = "Passionate Acoustic Guitar & Strings",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "hai_dil_ye_mera.mp3",
        durationLabel = "4:57",
        iconEmoji = "💔"
    ),
    BgmTrack(
        id = "track_ishq_mubarak",
        title = "Ishq Mubarak",
        singer = "Arijit Singh (Tum Bin 2)",
        instrumentalStyle = "Poetic Ghazal-Flavored Ballad",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "ishq_mubarak.mp3",
        durationLabel = "4:56",
        iconEmoji = "🕊️"
    ),
    BgmTrack(
        id = "track_humnava_mere",
        title = "Humnava Mere",
        singer = "Jubin Nautiyal",
        instrumentalStyle = "Slow Melancholic Acoustic Ballad",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "humnava_mere.mp3",
        durationLabel = "5:29",
        iconEmoji = "🌙"
    ),
    BgmTrack(
        id = "track_suno_na_sangemarmar",
        title = "Suno Na Sangemarmar",
        singer = "Arijit Singh (Youngistaan)",
        instrumentalStyle = "Breezy Taj Mahal Romance & Flute",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "suno_na_sangemarmar.mp3",
        durationLabel = "3:22",
        iconEmoji = "🏛️"
    ),
    BgmTrack(
        id = "track_freeze",
        title = "Freeze",
        singer = "Urban Groove",
        instrumentalStyle = "Upbeat Modern Trap Bass & Synth",
        category = MusicCategory.TRENDING_MODERN,
        assetFileName = "freeze.mp3",
        durationLabel = "2:39",
        iconEmoji = "❄️"
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
        id = "track_meri_mehbooba",
        title = "Meri Mehbooba",
        singer = "Kumar Sanu & Alka Yagnik (Pardes)",
        instrumentalStyle = "Pardes 90s Classic Romance",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "meri_mehbooba.mp3",
        durationLabel = "6:55",
        iconEmoji = "🌹"
    ),
    BgmTrack(
        id = "track_yeh_dil_deewana",
        title = "Yeh Dil Deewana",
        singer = "Sonu Nigam (Pardes)",
        instrumentalStyle = "High Energy 90s Groove & Beats",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "yeh_dil_deewana.mp3",
        durationLabel = "7:06",
        iconEmoji = "⚡"
    ),
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
    BgmTrack(
        id = "track_main_agar_kahoon",
        title = "Main Agar Kahoon",
        singer = "Sonu Nigam & Shreya Ghoshal",
        instrumentalStyle = "Royal Orchestral Waltz & Strings",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "main_agar_kahoon.mp3",
        durationLabel = "5:12",
        iconEmoji = "👑"
    ),
    BgmTrack(
        id = "track_judai_jannat",
        title = "Lambi Judai",
        singer = "Kamran Ahmed (Jannat)",
        instrumentalStyle = "Intense Emotive Rock Ballad",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "judai_jannat.mp3",
        durationLabel = "5:02",
        iconEmoji = "🌧️"
    ),
    BgmTrack(
        id = "track_haan_tu_hain",
        title = "Haan Tu Hain",
        singer = "KK (Jannat)",
        instrumentalStyle = "Acoustic Soft Rock Romance",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "haan_tu_hain.mp3",
        durationLabel = "5:24",
        iconEmoji = "💖"
    ),
    BgmTrack(
        id = "track_ishq_hua_vocal",
        title = "Ishq Hua Hi Hua",
        singer = "Sonu Nigam & Shreya Ghoshal",
        instrumentalStyle = "Kathak Folk Fusion & Santoor",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "ishq_hua_vocal.mp3",
        durationLabel = "4:23",
        iconEmoji = "💃"
    ),
    BgmTrack(
        id = "track_o_re_piya_vocal",
        title = "O Re Piya",
        singer = "Rahat Fateh Ali Khan (Aaja Nachle)",
        instrumentalStyle = "Classical Sufi Masterpiece",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "o_re_piya_vocal.mp3",
        durationLabel = "6:19",
        iconEmoji = "🪷"
    ),
    BgmTrack(
        id = "track_madhubala",
        title = "Madhubala",
        singer = "Ali Zafar & Shweta Pandit",
        instrumentalStyle = "Festive Brass Band & Dholak",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "madhubala.mp3",
        durationLabel = "4:23",
        iconEmoji = "🎉"
    ),
    BgmTrack(
        id = "track_do_dhaari_talwaar",
        title = "Do Dhaari Talwaar",
        singer = "Shahid Mallya & Shweta Pandit",
        instrumentalStyle = "High Energy Punjabi Wedding Beat",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "do_dhaari_talwaar.mp3",
        durationLabel = "5:00",
        iconEmoji = "⚔️"
    ),
    BgmTrack(
        id = "track_isq_risk",
        title = "Isq Risk",
        singer = "Rahat Fateh Ali Khan",
        instrumentalStyle = "Light Sufi Pop & Acoustic Strum",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "isq_risk.mp3",
        durationLabel = "3:49",
        iconEmoji = "🎲"
    ),
    BgmTrack(
        id = "track_chalte_chalte",
        title = "Chalte Chalte",
        singer = "Mohabbatein Soundtrack",
        instrumentalStyle = "Mohabbatein Classic Youth Melody",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "chalte_chalte.mp3",
        durationLabel = "7:38",
        iconEmoji = "🍁"
    ),
    BgmTrack(
        id = "track_humko_humise_chura_lo",
        title = "Humko Humise Chura Lo",
        singer = "Lata Mangeshkar & Udit Narayan",
        instrumentalStyle = "Evergreen Violins & Flute Romance",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "humko_humise_chura_lo.mp3",
        durationLabel = "7:58",
        iconEmoji = "💐"
    ),
    BgmTrack(
        id = "track_tere_mast_mast_do_nain",
        title = "Tere Mast Mast Do Nain",
        singer = "Rahat Fateh Ali Khan (Dabangg)",
        instrumentalStyle = "Harmonium & Dholak Sufi Romance",
        category = MusicCategory.RETRO_CLASSIC,
        assetFileName = "tere_mast_mast_do_nain.mp3",
        durationLabel = "5:59",
        iconEmoji = "✨"
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
        id = "track_instrumental_main_agar_kahoon",
        title = "Main Agar Kahoon (Prelude)",
        singer = "Bollywood Chamber Strings",
        instrumentalStyle = "Soft Violin & Piano Theme",
        category = MusicCategory.INSTRUMENTAL,
        assetFileName = "instrumental_main_agar_kahoon.mp3",
        durationLabel = "0:29",
        iconEmoji = "🎻"
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
