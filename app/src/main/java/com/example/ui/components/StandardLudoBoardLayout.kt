package com.example.ui.components

import com.example.model.LudoColor

/**
 * Standard 15x15 Ludo Board grid layout definitions and coordinate helpers.
 * Maps 0..51 track positions, home stretches, safe star cells, and base pockets.
 * Clockwise path matching official Ludo rules and authentic visual board screenshots.
 */
object StandardLudoBoardLayout {
    const val GRID_SIZE = 15

    // Standard 52 track coordinates (col, row) on a 15x15 grid (0-indexed).
    // Starting with Blue at track 0 (0, 6) moving clockwise.
    val TRACK_COORDINATES: List<Pair<Int, Int>> = listOf(
        // Left arm top lane (moving right): track 0..5
        0 to 6, 1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6,
        // Top arm left lane (moving up): track 6..11
        6 to 5, 6 to 4, 6 to 3, 6 to 2, 6 to 1, 6 to 0,
        // Top arm top cap: track 12
        7 to 0,
        // Top arm right lane (moving down): track 13..18
        8 to 0, 8 to 1, 8 to 2, 8 to 3, 8 to 4, 8 to 5,
        // Right arm top lane (moving right): track 19..24
        9 to 6, 10 to 6, 11 to 6, 12 to 6, 13 to 6, 14 to 6,
        // Right arm right cap: track 25
        14 to 7,
        // Right arm bottom lane (moving left): track 26..31
        14 to 8, 13 to 8, 12 to 8, 11 to 8, 10 to 8, 9 to 8,
        // Bottom arm right lane (moving down): track 32..37
        8 to 9, 8 to 10, 8 to 11, 8 to 12, 8 to 13, 8 to 14,
        // Bottom arm bottom cap: track 38
        7 to 14,
        // Bottom arm left lane (moving up): track 39..44
        6 to 14, 6 to 13, 6 to 12, 6 to 11, 6 to 10, 6 to 9,
        // Left arm bottom lane (moving left): track 45..50
        5 to 8, 4 to 8, 3 to 8, 2 to 8, 1 to 8, 0 to 8,
        // Left arm left cap: track 51
        0 to 7
    )

    // Safe squares by track index (standard international Ludo rules)
    // 1: Blue start (1, 6)
    // 9: Red approach star (6, 2)
    // 14: Red start (8, 1)
    // 22: Green approach star (12, 6)
    // 27: Green start (13, 8)
    // 35: Yellow approach star (8, 12)
    // 40: Yellow start (6, 13)
    // 48: Blue approach star (2, 8)
    val SAFE_TRACK_POSITIONS: Set<Int> = setOf(1, 9, 14, 22, 27, 35, 40, 48)

    // Safe grid positions (col, row)
    val SAFE_GRID_CELLS: Set<Pair<Int, Int>> = SAFE_TRACK_POSITIONS.map { TRACK_COORDINATES[it] }.toSet()

    // Home stretch columns (steps 1..5)
    // Step 6 is the center triumph apex (col 7, row 7)
    fun getHomeStretchGridPosition(color: LudoColor, step: Int): Pair<Int, Int> {
        val clampedStep = step.coerceIn(1, 5)
        return when (color) {
            LudoColor.RED -> clampedStep to 7              // Right from left (cols 1..5, row 7) - Left Arm
            LudoColor.GREEN -> 7 to clampedStep            // Down from top (col 7, rows 1..5) - Top Arm
            LudoColor.BLUE -> (14 - clampedStep) to 7     // Left from right (cols 13..9, row 7) - Right Arm
            LudoColor.YELLOW -> 7 to (14 - clampedStep)    // Up from bottom (col 7, rows 13..9) - Bottom Arm
        }
    }

    // Pawn base pocket float coordinates (col, row) for exact center of circular sockets inside white card
    fun getBasePocketFloatPosition(color: LudoColor, pocketIndex: Int): Pair<Float, Float> {
        val idx = pocketIndex.coerceIn(0, 3)
        return when (color) {
            LudoColor.RED -> when (idx) { // Top-Left (0..5, 0..5)
                0 -> 1.75f to 1.75f
                1 -> 3.75f to 1.75f
                2 -> 1.75f to 3.75f
                else -> 3.75f to 3.75f
            }
            LudoColor.GREEN -> when (idx) { // Top-Right (9..14, 0..5)
                0 -> 10.75f to 1.75f
                1 -> 12.75f to 1.75f
                2 -> 10.75f to 3.75f
                else -> 12.75f to 3.75f
            }
            LudoColor.YELLOW -> when (idx) { // Bottom-Left (0..5, 9..14)
                0 -> 1.75f to 10.75f
                1 -> 3.75f to 10.75f
                2 -> 1.75f to 12.75f
                else -> 3.75f to 12.75f
            }
            LudoColor.BLUE -> when (idx) { // Bottom-Right (9..14, 9..14)
                0 -> 10.75f to 10.75f
                1 -> 12.75f to 10.75f
                2 -> 10.75f to 12.75f
                else -> 12.75f to 12.75f
            }
        }
    }

    fun isSafeTrackPosition(trackPos: Int): Boolean = trackPos in SAFE_TRACK_POSITIONS
    fun isSafeGridCell(col: Int, row: Int): Boolean = (col to row) in SAFE_GRID_CELLS
}
