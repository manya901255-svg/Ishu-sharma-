package com.example

import com.example.engine.FairDiceEngine
import com.example.engine.LudoEngine
import com.example.model.LudoColor
import com.example.model.Pawn
import com.example.model.PawnState
import com.example.model.Player
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun fairDice_producesValidRollsAndCryptographicProof() {
        for (i in 1..50) {
            val (roll, proof) = FairDiceEngine.rollDice()
            assertTrue("Roll must be between 1 and 6", roll in 1..6)
            assertEquals(roll, proof.roll)
            assertTrue(proof.serverSeedHash.isNotEmpty())
            assertTrue(proof.clientSeed.isNotEmpty())
            assertTrue(proof.nonce > 0)
        }
    }

    @Test
    fun ludoEngine_pawnInBaseOnlyMovesOnSix() {
        val pawn = Pawn(id = 0, color = LudoColor.RED, state = PawnState.IN_BASE)
        assertFalse(LudoEngine.canPawnMove(pawn, 1))
        assertFalse(LudoEngine.canPawnMove(pawn, 5))
        assertTrue(LudoEngine.canPawnMove(pawn, 6))

        val moved = LudoEngine.calculateMoveResult(pawn, 6)
        assertEquals(PawnState.ON_BOARD, moved.state)
        assertEquals(LudoColor.RED.startTrackPos, moved.trackPosition)
    }

    @Test
    fun ludoEngine_safePositionsDoNotAllowCapture() {
        assertTrue(LudoEngine.isSafePosition(1))
        assertTrue(LudoEngine.isSafePosition(14))
        assertTrue(LudoEngine.isSafePosition(27))
        assertTrue(LudoEngine.isSafePosition(40))

        val redPawn = Pawn(id = 0, color = LudoColor.RED, state = PawnState.ON_BOARD, trackPosition = 1)
        val greenPawn = Pawn(id = 0, color = LudoColor.GREEN, state = PawnState.ON_BOARD, trackPosition = 1)

        val playerRed = Player("1", "Red", LudoColor.RED, pawns = listOf(redPawn))
        val playerGreen = Player("2", "Green", LudoColor.GREEN, pawns = listOf(greenPawn))

        val captures = LudoEngine.checkForCaptures(redPawn, listOf(playerRed, playerGreen))
        assertTrue("Safe tile 1 should prevent capture", captures.isEmpty())
    }

    @Test
    fun ludoEngine_capturesOnNormalSquare() {
        // Square 5 is not safe
        assertFalse(LudoEngine.isSafePosition(5))

        val redPawn = Pawn(id = 0, color = LudoColor.RED, state = PawnState.ON_BOARD, trackPosition = 5)
        val greenPawn = Pawn(id = 0, color = LudoColor.GREEN, state = PawnState.ON_BOARD, trackPosition = 5)

        val playerRed = Player("1", "Red", LudoColor.RED, pawns = listOf(redPawn))
        val playerGreen = Player("2", "Green", LudoColor.GREEN, pawns = listOf(greenPawn))

        val captures = LudoEngine.checkForCaptures(redPawn, listOf(playerRed, playerGreen))
        assertEquals(1, captures.size)
        assertEquals(LudoColor.GREEN, captures.first().first.color)
    }

    @Test
    fun ludoEngine_exactHomeRollRequired() {
        val pawn = Pawn(id = 0, color = LudoColor.RED, state = PawnState.IN_HOME_STRETCH, homeStep = 4)
        // 4 + 3 = 7 > 6 -> cannot move
        assertFalse(LudoEngine.canPawnMove(pawn, 3))
        // 4 + 2 = 6 -> finishes!
        assertTrue(LudoEngine.canPawnMove(pawn, 2))

        val finishedPawn = LudoEngine.calculateMoveResult(pawn, 2)
        assertEquals(PawnState.FINISHED, finishedPawn.state)
        assertEquals(6, finishedPawn.homeStep)
    }
}
