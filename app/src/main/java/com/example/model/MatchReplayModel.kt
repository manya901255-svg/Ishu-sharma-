package com.example.model

/**
 * Represents an individual recorded move in a Ludo game for Match Replay
 */
data class ReplayMove(
    val stepNumber: Int,
    val playerName: String,
    val playerColor: LudoColor,
    val playerAvatar: String,
    val diceRoll: Int,
    val pawnId: Int,
    val fromPosition: Int,
    val toPosition: Int,
    val isCapture: Boolean = false,
    val capturedPlayerName: String? = null,
    val isHome: Boolean = false,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Complete Match Replay session record with metadata and sequenced moves
 */
data class MatchReplaySession(
    val id: String,
    val matchTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String,
    val wager: Long,
    val winnerName: String,
    val winnerColor: LudoColor,
    val winnerAvatar: String,
    val totalMovesCount: Int,
    val durationFormatted: String = "3m 42s",
    val moves: List<ReplayMove>
)
