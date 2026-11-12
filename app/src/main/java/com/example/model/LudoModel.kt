package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class LudoColor(
    val title: String,
    val primary: Color,
    val dark: Color,
    val light: Color,
    val startTrackPos: Int,
    val homeEntranceTrackPos: Int,
    val safeStarPos: Int
) {
    RED(
        title = "Red",
        primary = Color(0xFFE53935),
        dark = Color(0xFFC62828),
        light = Color(0xFFFFCDD2),
        startTrackPos = 1,
        homeEntranceTrackPos = 50,
        safeStarPos = 1
    ),
    GREEN(
        title = "Green",
        primary = Color(0xFF43A047),
        dark = Color(0xFF2E7D32),
        light = Color(0xFFC8E6C9),
        startTrackPos = 14,
        homeEntranceTrackPos = 11,
        safeStarPos = 14
    ),
    BLUE(
        title = "Blue",
        primary = Color(0xFF039BE5),
        dark = Color(0xFF0277BD),
        light = Color(0xFFE1F5FE),
        startTrackPos = 27,
        homeEntranceTrackPos = 24,
        safeStarPos = 27
    ),
    YELLOW(
        title = "Yellow",
        primary = Color(0xFFFDD835),
        dark = Color(0xFFF57F17),
        light = Color(0xFFFFF9C4),
        startTrackPos = 40,
        homeEntranceTrackPos = 37,
        safeStarPos = 40
    )
}

enum class PawnState {
    IN_BASE,
    ON_BOARD,
    IN_HOME_STRETCH,
    FINISHED
}

data class Pawn(
    val id: Int,
    val color: LudoColor,
    val state: PawnState = PawnState.IN_BASE,
    val trackPosition: Int = -1, // 0..51
    val homeStep: Int = 0, // 1..5, 6 is FINISHED
    val isMovable: Boolean = false
) {
    val isHome: Boolean get() = state == PawnState.FINISHED
}

data class Player(
    val id: String,
    val name: String,
    val color: LudoColor,
    val isAi: Boolean = false,
    val isOnline: Boolean = false,
    val avatar: String = "crown_imperial_gold",
    val customPhotoUri: String? = null,
    val title: String = "Maharaja",
    val countryFlag: String = "🇮🇳",
    val level: Int = 18,
    val rating: Int = 1200,
    val coins: Long = 10000,
    val pawns: List<Pawn> = (0..3).map { Pawn(id = it, color = color) },
    val rank: Int = 0,
    val hasWon: Boolean = false,
    val isConnected: Boolean = true,
    val lastDiceValue: Int = 1,
    val isSpeaking: Boolean = false
) {
    val finishedPawnsCount: Int get() = pawns.count { it.isHome }
    val isPlayerFinished: Boolean get() = finishedPawnsCount == pawns.size
}

enum class GameMode(val title: String, val subtitle: String, val defaultWager: Long) {
    CLASSIC("Classic Mode", "Traditional 4-pawn royal rules. First to home all 4 pawns wins!", 1500),
    RUSH("Rush Mode", "Blitz action! First player to get any 2 pawns home wins!", 2500),
    BOLT("Bolt Mode", "High speed! First player to get 3 pawns home wins!", 5000),
    DASH("Dash Mode", "Instant knockout! First single pawn to reach the center wins!", 10000),
    VS_AI("vs Royal AI", "Battle smart royal bots with 3 difficulty levels", 1500),
    PASS_AND_PLAY("Pass & Play", "Offline royal court match with 2-4 players", 0),
    ONLINE_MULTIPLAYER("Online Match", "Real-time match with players across kingdoms", 1500),
    PRIVATE_ROOM("Private Royal Room", "Create or join a room with royal invitation code", 1500),
    QUICK_RUSH("Quick Rush Mode", "Blitz mode: First pawn to palace wins!", 1500),
    TEAM_2VS2("Team Ludo (2 vs 2)", "Team up with a partner! Protect partner tokens & win together!", 2000),
    FRIENDS_CHALLENGE("Friends Challenge", "1-on-1 direct duel with royal friends", 1500),
    PRACTICE("Practice Mode", "Solo training ground with tactical hints & undo moves", 0),
    TOURNAMENT("Royal Tournament", "Championship bracket for massive jackpot", 5000)
}

enum class TurnPhase {
    ROLL_DICE,
    SELECT_PAWN,
    ANIMATING_MOVE,
    ROUND_OVER
}

data class FairDiceProof(
    val roll: Int,
    val timestamp: Long,
    val serverSeedHash: String,
    val clientSeed: String,
    val nonce: Long,
    val verificationFormula: String = "HMAC_SHA256(serverSeed, clientSeed:nonce) % 6 + 1"
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderColor: LudoColor,
    val text: String,
    val isEmoji: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ShopItem(
    val id: String,
    val name: String,
    val category: String, // "Pawns", "Dice", "Boards"
    val priceCoins: Long,
    val priceGems: Int,
    val iconDescription: String,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val title: String,
    val avatar: String,
    val trophies: Int,
    val winRate: String,
    val totalEarnings: Long
)

data class DiceSkin(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val accentColor: Color,
    val pipColor: Color,
    val iconEmoji: String = "🎲",
    val priceCoins: Long = 0L,
    val isDefault: Boolean = false,
    val category: String = "Classic Dice"
)

val BUILT_IN_DICE_SKINS = listOf(
    // 1. Classic Dice
    DiceSkin("dice_classic_ivory", "Classic Ivory", "Hand-carved vintage ivory bone with deep ebony pips", Color(0xFFFFF8E1), Color(0xFFFFE082), Color(0xFF212121), "🎲", 0L, true, "Classic Dice"),
    DiceSkin("dice_royal_teak", "Royal Teak Wood", "Polished Rajasthan teak wood with antique brass pips", Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFFFFD54F), "🪵", 1500L, false, "Classic Dice"),
    DiceSkin("dice_makrana_marble", "Makrana Marble", "Pristine white Makrana marble with onyx pips", Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF263238), "🏛️", 2500L, false, "Classic Dice"),

    // 2. Neon Dice
    DiceSkin("dice_cyber_neon", "Cyber Neon", "Electrified synthwave cyan with radioactive magenta pips", Color(0xFF00E5FF), Color(0xFF00B0FF), Color(0xFFFF007F), "⚡", 3500L, false, "Neon Dice"),
    DiceSkin("dice_laser_pulse", "Laser Pulse", "Deep midnight purple with pulsing electric yellow pips", Color(0xFF7C4DFF), Color(0xFF651FFF), Color(0xFFFFFF00), "🟣", 4000L, false, "Neon Dice"),
    DiceSkin("dice_matrix_green", "Matrix Phosphor", "Arcade phosphor green with cybernetic emerald pips", Color(0xFF00E676), Color(0xFF00C853), Color(0xFF003300), "🟢", 4500L, false, "Neon Dice"),

    // 3. Fire Dice
    DiceSkin("dice_molten_lava", "Molten Lava", "Volcanic basalt stone with searing magma glow pips", Color(0xFFFF3D00), Color(0xFFDD2C00), Color(0xFFFFEB3B), "🌋", 4500L, false, "Fire Dice"),
    DiceSkin("dice_solar_flare", "Solar Flare", "Blazing solar flare core with coronal ruby pips", Color(0xFFFF6D00), Color(0xFFFFAB00), Color(0xFFD50000), "🔥", 5500L, false, "Fire Dice"),
    DiceSkin("dice_dragon_flame", "Dragon Flame", "Mythic crimson dragon scales with infernal ember pips", Color(0xFFC62828), Color(0xFF8E0000), Color(0xFFFFC107), "🐉", 6500L, false, "Fire Dice"),

    // 4. Ice Dice
    DiceSkin("dice_glacial_frost", "Glacial Frost", "Sub-zero crystalline iceberg with frostbite cyan pips", Color(0xFF80DEEA), Color(0xFF00ACC1), Color(0xFFFFFFFF), "❄️", 4500L, false, "Ice Dice"),
    DiceSkin("dice_arctic_blizzard", "Arctic Blizzard", "Chilled permafrost blizzard glass with diamond ice pips", Color(0xFFB2EBF2), Color(0xFF4DD0E1), Color(0xFF006064), "🧊", 5500L, false, "Ice Dice"),
    DiceSkin("dice_frozen_nebula", "Frozen Nebula", "Deep cryogenic violet frost with starlight pips", Color(0xFF9FA8DA), Color(0xFF5C6BC0), Color(0xFFE0F7FA), "🌌", 6500L, false, "Ice Dice"),

    // 5. Golden Dice
    DiceSkin("dice_imperial_gold", "Imperial Jaipur Gold", "Pure 24K bullion gold minted with royal ruby pips", Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFC62828), "👑", 5000L, false, "Golden Dice"),
    DiceSkin("dice_sun_temple_gold", "Sun Temple Gold", "Sacred carved solar disc gold with emerald crystal pips", Color(0xFFFFC107), Color(0xFFFF8F00), Color(0xFF00E676), "☀️", 7500L, false, "Golden Dice"),
    DiceSkin("dice_crown_sovereign", "Crown Sovereign", "Heavy royal treasury gold bullion with sapphire pips", Color(0xFFFFAB00), Color(0xFFFF6F00), Color(0xFF2979FF), "🪙", 10000L, false, "Golden Dice"),

    // 6. Diamond Dice
    DiceSkin("dice_kohinoor_diamond", "Kohinoor Diamond", "Faceted translucent diamond prism with rainbow refractions", Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF00B0FF), "💎", 8000L, false, "Diamond Dice"),
    DiceSkin("dice_star_sapphire", "Royal Star Sapphire", "Deep celestial midnight sapphire with diamond starlight pips", Color(0xFF1A237E), Color(0xFF283593), Color(0xFFFFFFFF), "⭐", 9500L, false, "Diamond Dice"),
    DiceSkin("dice_celestial_amethyst", "Celestial Amethyst", "Majestic cut royal amethyst crystal with gold pips", Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFFFFD700), "✨", 11000L, false, "Diamond Dice"),

    // 7. Cartoon Dice
    DiceSkin("dice_bubble_gum", "Bubble Gum Pop", "Sweet bubblegum pink with playful candy white pips", Color(0xFFFF4081), Color(0xFFF50057), Color(0xFFFFFFFF), "🍬", 2000L, false, "Cartoon Dice"),
    DiceSkin("dice_playful_emoji", "Playful Emoji", "Sunny yellow smiley dice with joyful heart pips", Color(0xFFFFEB3B), Color(0xFFFDD835), Color(0xFFFF1744), "😍", 2500L, false, "Cartoon Dice"),
    DiceSkin("dice_retro_arcade", "8-Bit Arcade", "Retro 1980s pixel block dice with rainbow pips", Color(0xFF00E5FF), Color(0xFFFF007F), Color(0xFFFFFF00), "👾", 3000L, false, "Cartoon Dice"),

    // 8. Royal Dice
    DiceSkin("dice_maharaja_ruby", "Maharaja Ruby", "Faceted royal crimson gem with Mughal gold pips", Color(0xFFD50000), Color(0xFF880E4F), Color(0xFFFFD700), "👑", 6000L, false, "Royal Dice"),
    DiceSkin("dice_emerald_empire", "Emerald Empire", "Royal Mughal emerald with brilliant diamond pips", Color(0xFF00C853), Color(0xFF1B5E20), Color(0xFFE0F7FA), "🟢", 7000L, false, "Royal Dice"),
    DiceSkin("dice_royal_platinum", "Royal Platinum", "Mirror-polished white platinum with royal sapphire pips", Color(0xFFECEFF1), Color(0xFF90A4AE), Color(0xFF0277BD), "✨", 8500L, false, "Royal Dice"),

    // 9. Animated Dice
    DiceSkin("dice_thunder_shock", "Thunderstorm Shock", "Arcing plasma lightning bolts with electrifying shockwave pips", Color(0xFFFFEA00), Color(0xFFFF9100), Color(0xFF00E5FF), "⚡", 9000L, false, "Animated Dice"),
    DiceSkin("dice_cosmic_void", "Cosmic Void", "Swirling black hole galaxy with supernova starburst pips", Color(0xFF12005E), Color(0xFF311B92), Color(0xFF00E5FF), "🌌", 12000L, false, "Animated Dice"),
    DiceSkin("dice_phoenix_rebirth", "Phoenix Rebirth", "Golden solar fire aura with immortal flame feather pips", Color(0xFFFF6D00), Color(0xFFDD2C00), Color(0xFFFFD700), "🪶", 15000L, false, "Animated Dice")
)

data class TokenSkin(
    val id: String,
    val name: String,
    val description: String,
    val symbolEmoji: String,
    val badgeTitle: String,
    val priceCoins: Long = 0L,
    val isDefault: Boolean = false
)

val BUILT_IN_TOKEN_SKINS = listOf(
    TokenSkin("crown_maharaja", "Maharaja Crown", "Imperial royal crown token", "👑", "IMPERIAL", 0L, true),
    TokenSkin("token_lion_shield", "Lion Shield", "Bravery emblem of royal warriors", "🛡️", "DEFENDER", 2500L),
    TokenSkin("token_lotus_gem", "Lotus Gem", "Sacred blooming lotus of prosperity", "🪷", "MYSTIC", 3000L),
    TokenSkin("token_dragon_crest", "Dragon Crest", "Fierce mythic dragon insignia", "🐉", "VALIANT", 4000L),
    TokenSkin("token_star_emperor", "Star Emperor", "Radiant galactic crown star", "⭐", "CHAMPION", 5000L)
)

data class PlayerReport(
    val id: String,
    val reportedPlayerName: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = true,
    val status: String = "UNDER_REVIEW"
)
