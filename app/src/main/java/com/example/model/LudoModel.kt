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
