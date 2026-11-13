package com.example.engine

import com.example.model.*

object LudoEngine {

    val SAFE_POSITIONS = setOf(1, 9, 14, 22, 27, 35, 40, 48)

    fun isSafePosition(pos: Int): Boolean = pos in SAFE_POSITIONS

    /**
     * Checks if a pawn can make a move given a dice value.
     */
    fun canPawnMove(pawn: Pawn, dice: Int): Boolean {
        if (pawn.state == PawnState.FINISHED) return false

        if (pawn.state == PawnState.IN_BASE) {
            return dice == 6
        }

        if (pawn.state == PawnState.IN_HOME_STRETCH) {
            return (pawn.homeStep + dice) <= 6
        }

        if (pawn.state == PawnState.ON_BOARD) {
            val stepsToEntrance = distanceToHomeEntrance(pawn.color, pawn.trackPosition)
            if (dice > stepsToEntrance) {
                val excess = dice - stepsToEntrance
                return excess <= 6
            }
            return true
        }

        return false
    }

    /**
     * Calculates the track distance from a current position to the color's home entrance cell.
     */
    fun distanceToHomeEntrance(color: LudoColor, currentPos: Int): Int {
        val entrance = color.homeEntranceTrackPos
        return if (entrance >= currentPos) {
            entrance - currentPos
        } else {
            (52 - currentPos) + entrance
        }
    }

    /**
     * Generates the exact cell-by-cell intermediate Pawn states for smooth animated movement.
     * Each element represents a single tile hop along the track or home stretch.
     */
    fun buildStepPath(pawn: Pawn, dice: Int): List<Pawn> {
        if (dice !in 1..6 || !canPawnMove(pawn, dice)) return emptyList()

        if (pawn.state == PawnState.IN_BASE) {
            return listOf(
                pawn.copy(
                    state = PawnState.ON_BOARD,
                    trackPosition = pawn.color.startTrackPos,
                    homeStep = 0,
                    isMovable = false
                )
            )
        }

        val steps = mutableListOf<Pawn>()
        var current = pawn.copy(isMovable = false)

        repeat(dice) {
            current = when (current.state) {
                PawnState.ON_BOARD -> {
                    if (current.trackPosition == current.color.homeEntranceTrackPos) {
                        current.copy(
                            state = PawnState.IN_HOME_STRETCH,
                            trackPosition = -1,
                            homeStep = 1,
                            isMovable = false
                        )
                    } else {
                        current.copy(
                            state = PawnState.ON_BOARD,
                            trackPosition = (current.trackPosition + 1) % 52,
                            isMovable = false
                        )
                    }
                }
                PawnState.IN_HOME_STRETCH -> {
                    val nextStep = current.homeStep + 1
                    if (nextStep >= 6) {
                        current.copy(
                            state = PawnState.FINISHED,
                            trackPosition = -1,
                            homeStep = 6,
                            isMovable = false
                        )
                    } else {
                        current.copy(
                            state = PawnState.IN_HOME_STRETCH,
                            trackPosition = -1,
                            homeStep = nextStep,
                            isMovable = false
                        )
                    }
                }
                else -> current
            }
            steps.add(current)
        }
        return steps
    }

    /**
     * Simulates the new state of a pawn after moving.
     */
    fun calculateMoveResult(pawn: Pawn, dice: Int): Pawn {
        val path = buildStepPath(pawn, dice)
        return path.lastOrNull() ?: pawn
    }

    /**
     * Computes a deterministic board state checksum across all players and pawns
     * for multiplayer synchronization & anti-desync verification.
     */
    fun computeMatchStateHash(
        players: List<Player>,
        currentPlayerIndex: Int,
        moveSequence: Long
    ): String {
        val payload = buildString {
            append(currentPlayerIndex).append('|').append(moveSequence)
            for (p in players) {
                append('|').append(p.color.name).append(':')
                for (pawn in p.pawns.sortedBy { it.id }) {
                    append(pawn.id).append('=')
                        .append(pawn.state.ordinal).append(',')
                        .append(pawn.trackPosition).append(',')
                        .append(pawn.homeStep).append(';')
                }
            }
        }
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks if two colors belong to the same team in 2vs2 mode:
     * Team Sun: Yellow & Green
     * Team Moon: Red & Blue
     */
    fun areTeammates(color1: LudoColor, color2: LudoColor): Boolean {
        val teamSun = setOf(LudoColor.YELLOW, LudoColor.GREEN)
        val teamMoon = setOf(LudoColor.RED, LudoColor.BLUE)
        return (color1 in teamSun && color2 in teamSun) || (color1 in teamMoon && color2 in teamMoon)
    }

    /**
     * Checks if moving this pawn would capture any opponent pawn(s).
     * Returns the captured pawns if any. Safe squares prevent capture.
     * In TEAM_2VS2 mode, teammates do not capture each other.
     */
    fun checkForCaptures(
        movingPawn: Pawn,
        allPlayers: List<Player>,
        mode: GameMode = GameMode.CLASSIC
    ): List<Pair<Player, Pawn>> {
        if (movingPawn.state != PawnState.ON_BOARD) return emptyList()
        val targetPos = movingPawn.trackPosition
        if (isSafePosition(targetPos)) return emptyList()

        val captured = mutableListOf<Pair<Player, Pawn>>()
        for (player in allPlayers) {
            if (player.color == movingPawn.color) continue
            if (mode == GameMode.TEAM_2VS2 && areTeammates(player.color, movingPawn.color)) continue
            for (enemyPawn in player.pawns) {
                if (enemyPawn.state == PawnState.ON_BOARD && enemyPawn.trackPosition == targetPos) {
                    captured.add(player to enemyPawn)
                }
            }
        }
        return captured
    }

    /**
     * Evaluates smart AI move priority for bots.
     */
    fun pickBestAiMove(
        player: Player,
        movablePawns: List<Pawn>,
        dice: Int,
        allPlayers: List<Player>,
        mode: GameMode = GameMode.CLASSIC
    ): Pawn? {
        if (movablePawns.isEmpty()) return null
        if (movablePawns.size == 1) return movablePawns.first()

        // 1. Priority: Can capture an enemy pawn?
        for (pawn in movablePawns) {
            val simulated = calculateMoveResult(pawn, dice)
            val captures = checkForCaptures(simulated, allPlayers, mode)
            if (captures.isNotEmpty()) return pawn
        }

        // 2. Priority: Can reach home (finish)?
        for (pawn in movablePawns) {
            val simulated = calculateMoveResult(pawn, dice)
            if (simulated.state == PawnState.FINISHED) return pawn
        }

        // 3. Priority: Free a pawn from base on a 6?
        val basePawn = movablePawns.firstOrNull { it.state == PawnState.IN_BASE }
        if (dice == 6 && basePawn != null) {
            return basePawn
        }

        // 4. Priority: Enter home stretch safely?
        for (pawn in movablePawns) {
            val simulated = calculateMoveResult(pawn, dice)
            if (pawn.state == PawnState.ON_BOARD && simulated.state == PawnState.IN_HOME_STRETCH) {
                return pawn
            }
        }

        // 5. Priority: Move pawn closest to home
        return movablePawns.maxByOrNull { pawn ->
            when (pawn.state) {
                PawnState.IN_HOME_STRETCH -> 100 + pawn.homeStep
                PawnState.ON_BOARD -> 52 - distanceToHomeEntrance(pawn.color, pawn.trackPosition)
                PawnState.IN_BASE -> 0
                PawnState.FINISHED -> -1
            }
        }
    }

    /**
     * Checks if a player has met the winning condition based on the active GameMode:
     * - DASH: First player with 1 pawn home wins!
     * - RUSH / QUICK_RUSH / FRIENDS_CHALLENGE: First player with 2 pawns home wins!
     * - BOLT: First player with 3 pawns home wins!
     * - TEAM_2VS2: First team with 4 total pawns home wins together!
     * - CLASSIC / Others: All 4 pawns home to win!
     */
    fun hasPlayerWon(player: Player, mode: GameMode, allPlayers: List<Player> = emptyList()): Boolean {
        val finishedCount = player.pawns.count { it.state == PawnState.FINISHED }
        return when (mode) {
            GameMode.DASH -> finishedCount >= 1
            GameMode.RUSH, GameMode.QUICK_RUSH, GameMode.FRIENDS_CHALLENGE -> finishedCount >= 2
            GameMode.BOLT -> finishedCount >= 3
            GameMode.TEAM_2VS2 -> {
                val teamMembers = allPlayers.filter { areTeammates(it.color, player.color) }
                val totalTeamFinished = (if (teamMembers.isNotEmpty()) teamMembers else listOf(player))
                    .sumOf { p -> p.pawns.count { it.state == PawnState.FINISHED } }
                totalTeamFinished >= 4
            }
            else -> finishedCount == player.pawns.size
        }
    }
}
