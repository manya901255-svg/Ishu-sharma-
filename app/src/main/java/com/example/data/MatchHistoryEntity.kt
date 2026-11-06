package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MatchHistoryEntity: Stores summary of completed Ludo matches
 * Tracks game mode, wager, outcome, duration, and full final leaderboard rankings.
 */
@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String,
    val wager: Long,
    val coinsWon: Long, // positive for winnings, negative/0 for losses
    val userRank: Int, // 1 = 1st Place (Winner), 2 = 2nd, etc.
    val winnerName: String,
    val winnerColor: String,
    val durationSeconds: Int = 180,
    // Structured format: "1:Name:Color:PawnsHome,2:Name:Color:PawnsHome,..."
    val leaderboardSummary: String
) {
    val isWin: Boolean get() = userRank == 1

    /**
     * Parse structured leaderboard items into individual player positions
     */
    fun parseLeaderboardPositions(): List<LeaderboardPositionItem> {
        if (leaderboardSummary.isBlank()) return emptyList()
        return leaderboardSummary.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 4) {
                LeaderboardPositionItem(
                    rank = parts[0].toIntOrNull() ?: 1,
                    playerName = parts[1],
                    colorName = parts[2],
                    pawnsHome = parts[3].toIntOrNull() ?: 0
                )
            } else null
        }
    }
}

data class LeaderboardPositionItem(
    val rank: Int,
    val playerName: String,
    val colorName: String,
    val pawnsHome: Int
)
