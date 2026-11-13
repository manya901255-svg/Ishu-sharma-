package com.example.model

import androidx.compose.ui.graphics.Color

data class RegalBoardTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val outerFrameColor: Color,
    val borderColor: Color,
    val trackBgColor: Color,
    val gridLineColor: Color,
    val accentStarColor: Color,
    val previewGradient: List<Color>
)

val REGAL_BOARD_THEMES = listOf(
    RegalBoardTheme(
        id = "maharaja_palace",
        name = "Maharaja Gold Durbar",
        subtitle = "Carved teakwood, Makrana marble & 24K gold Durbar inlays",
        outerFrameColor = Color(0xFF1B0C04),
        borderColor = Color(0xFFFFD54F),
        trackBgColor = Color(0xFFFBF7F0),
        gridLineColor = Color(0xFFC5A059),
        accentStarColor = Color(0xFFFFD54F),
        previewGradient = listOf(Color(0xFFB71C1C), Color(0xFFFFB300))
    ),
    RegalBoardTheme(
        id = "imperial_navy",
        name = "Imperial Royal Navy",
        subtitle = "Classic deep sapphire court with golden trim",
        outerFrameColor = Color(0xFF091F56),
        borderColor = Color(0xFF1565C0),
        trackBgColor = Color.White,
        gridLineColor = Color(0xFFCFD8DC),
        accentStarColor = Color(0xFFFFD54F),
        previewGradient = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
    ),
    RegalBoardTheme(
        id = "jaipur_ruby",
        name = "Jaipur Ruby Palace",
        subtitle = "Crimson palace border with warm ivory track",
        outerFrameColor = Color(0xFF4A0A17),
        borderColor = Color(0xFFD32F2F),
        trackBgColor = Color(0xFFFFFDE7),
        gridLineColor = Color(0xFFFFCDD2),
        accentStarColor = Color(0xFFFF5252),
        previewGradient = listOf(Color(0xFF880E4F), Color(0xFFC2185B))
    ),
    RegalBoardTheme(
        id = "mughal_emerald",
        name = "Mughal Emerald Garden",
        subtitle = "Deep emerald velvet with jade filigree",
        outerFrameColor = Color(0xFF072A1B),
        borderColor = Color(0xFF2E7D32),
        trackBgColor = Color(0xFFF1F8E9),
        gridLineColor = Color(0xFFC8E6C9),
        accentStarColor = Color(0xFF66BB6A),
        previewGradient = listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
    ),
    RegalBoardTheme(
        id = "kohinoor_marble",
        name = "Kohinoor Platinum Marble",
        subtitle = "Pristine white marble court with platinum borders",
        outerFrameColor = Color(0xFF263238),
        borderColor = Color(0xFF90A4AE),
        trackBgColor = Color(0xFFFAFAFA),
        gridLineColor = Color(0xFFECEFF1),
        accentStarColor = Color(0xFF00B0FF),
        previewGradient = listOf(Color(0xFF37474F), Color(0xFF78909C))
    ),
    RegalBoardTheme(
        id = "suryavanshi_amber",
        name = "Suryavanshi Sunset Amber",
        subtitle = "Warm sandalwood frame with radiant brass accents",
        outerFrameColor = Color(0xFF3E2723),
        borderColor = Color(0xFFFF8F00),
        trackBgColor = Color(0xFFFFF8E1),
        gridLineColor = Color(0xFFFFE082),
        accentStarColor = Color(0xFFFFB300),
        previewGradient = listOf(Color(0xFFE65100), Color(0xFFFFA000))
    ),
    RegalBoardTheme(
        id = "board_neon",
        name = "Neon Cyber Matrix",
        subtitle = "Electrifying magenta & cyan neon glow on midnight quartz",
        outerFrameColor = Color(0xFF100726),
        borderColor = Color(0xFFE040FB),
        trackBgColor = Color(0xFF191233),
        gridLineColor = Color(0xFF7C4DFF),
        accentStarColor = Color(0xFF00E5FF),
        previewGradient = listOf(Color(0xFF4A148C), Color(0xFF00E5FF))
    ),
    RegalBoardTheme(
        id = "board_garden",
        name = "Royal Blossom Garden",
        subtitle = "Lush botanical flora with jade & emerald garden pathways",
        outerFrameColor = Color(0xFF13361E),
        borderColor = Color(0xFF4CAF50),
        trackBgColor = Color(0xFFF1F8E9),
        gridLineColor = Color(0xFFA5D6A7),
        accentStarColor = Color(0xFFFFD54F),
        previewGradient = listOf(Color(0xFF2E7D32), Color(0xFF81C784))
    ),
    RegalBoardTheme(
        id = "board_ocean",
        name = "Deep Ocean Atlantis",
        subtitle = "Crystal turquoise tides with coral reef borders & aquatic shells",
        outerFrameColor = Color(0xFF002B49),
        borderColor = Color(0xFF00E5FF),
        trackBgColor = Color(0xFFE0F7FA),
        gridLineColor = Color(0xFF80DEEA),
        accentStarColor = Color(0xFFFF4081),
        previewGradient = listOf(Color(0xFF006064), Color(0xFF00BCD4))
    ),
    RegalBoardTheme(
        id = "board_luxor",
        name = "Luxor Pharaoh Gold",
        subtitle = "Ancient sandstone gold with royal lapis lazuli inlays",
        outerFrameColor = Color(0xFF3E2723),
        borderColor = Color(0xFFD4AF37),
        trackBgColor = Color(0xFFFFF8E1),
        gridLineColor = Color(0xFFD7CCC8),
        accentStarColor = Color(0xFF00B0FF),
        previewGradient = listOf(Color(0xFF5D4037), Color(0xFFD4AF37))
    ),
    RegalBoardTheme(
        id = "board_campout",
        name = "Midnight Campout Trail",
        subtitle = "Evergreen pine forest illuminated by cozy campfire amber",
        outerFrameColor = Color(0xFF1B3B2B),
        borderColor = Color(0xFFFF8F00),
        trackBgColor = Color(0xFFF1F8E9),
        gridLineColor = Color(0xFFA5D6A7),
        accentStarColor = Color(0xFFFF6D00),
        previewGradient = listOf(Color(0xFF1B5E20), Color(0xFFFF8F00))
    ),
    RegalBoardTheme(
        id = "board_fiesta",
        name = "Royal Carnival Fiesta",
        subtitle = "Vibrant marigold, magenta & turquoise festival celebration",
        outerFrameColor = Color(0xFF6A1B9A),
        borderColor = Color(0xFFFFC107),
        trackBgColor = Color(0xFFFFF3E0),
        gridLineColor = Color(0xFFF8BBD0),
        accentStarColor = Color(0xFFE91E63),
        previewGradient = listOf(Color(0xFFE91E63), Color(0xFFFFC107))
    )
)

fun getBoardTheme(id: String): RegalBoardTheme {
    return REGAL_BOARD_THEMES.firstOrNull { it.id == id } ?: REGAL_BOARD_THEMES.first()
}
