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
     * Simulates the new state of a pawn after moving.
     */
    fun calculateMoveResult(pawn: Pawn, dice: Int): Pawn {
        if (pawn.state == PawnState.IN_BASE && dice == 6) {
            return pawn.copy(
                state = PawnState.ON_BOARD,
                trackPosition = pawn.color.startTrackPos,
                homeStep = 0,
                isMovable = false
            )
        }

        if (pawn.state == PawnState.IN_HOME_STRETCH) {
            val newStep = pawn.homeStep + dice
            return if (newStep == 6) {
                pawn.copy(state = PawnState.FINISHED, homeStep = 6, isMovable = false)
            } else {
                pawn.copy(homeStep = newStep, isMovable = false)
            }
        }

        if (pawn.state == PawnState.ON_BOARD) {
            val stepsToEntrance = distanceToHomeEntrance(pawn.color, pawn.trackPosition)
            if (dice > stepsToEntrance) {
                val excess = dice - stepsToEntrance
                return if (excess == 6) {
                    pawn.copy(state = PawnState.FINISHED, homeStep = 6, isMovable = false)
                } else {
                    pawn.copy(
                        state = PawnState.IN_HOME_STRETCH,
                        trackPosition = -1,
                        homeStep = excess,
                        isMovable = false
                    )
                }
            } else {
                val nextPos = (pawn.trackPosition + dice) % 52
                return pawn.copy(trackPosition = nextPos, isMovable = false)
            }
        }

        return pawn
    }

    /**
     * Checks if moving this pawn would capture any opponent pawn(s).
     * Returns the captured pawns if any. Safe squares prevent capture.
     */
    fun checkForCaptures(
        movingPawn: Pawn,
        allPlayers: List<Player>
    ): List<Pair<Player, Pawn>> {
        if (movingPawn.state != PawnState.ON_BOARD) return emptyList()
        val targetPos = movingPawn.trackPosition
        if (isSafePosition(targetPos)) return emptyList()

        val captured = mutableListOf<Pair<Player, Pawn>>()
        for (player in allPlayers) {
            if (player.color == movingPawn.color) continue
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
        allPlayers: List<Player>
    ): Pawn? {
        if (movablePawns.isEmpty()) return null
        if (movablePawns.size == 1) return movablePawns.first()

        // 1. Priority: Can capture an enemy pawn?
        for (pawn in movablePawns) {
            val simulated = calculateMoveResult(pawn, dice)
            val captures = checkForCaptures(simulated, allPlayers)
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
     * - RUSH / QUICK_RUSH: First player with 2 pawns home wins!
     * - BOLT: First player with 3 pawns home wins!
     * - CLASSIC / Others: All 4 pawns home to win!
     */
    fun hasPlayerWon(player: Player, mode: GameMode): Boolean {
        val finishedCount = player.pawns.count { it.state == PawnState.FINISHED }
        return when (mode) {
            GameMode.DASH -> finishedCount >= 1
            GameMode.RUSH, GameMode.QUICK_RUSH -> finishedCount >= 2
            GameMode.BOLT -> finishedCount >= 3
            else -> finishedCount == player.pawns.size
        }
    }
}
