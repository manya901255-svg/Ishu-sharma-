package com.example

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.engine.LudoEngine
import com.example.model.*
import com.example.ui.components.MaharajaLudoBoardView
import com.example.ui.components.RoyalBoardView
import com.example.ui.components.StandardLudoBoardLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ProfilePreferences
import com.example.viewmodel.LudoViewModel
import com.example.viewmodel.MatchConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LudoTokenSystemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var prefs: ProfilePreferences

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        application.getSharedPreferences("mg_ludo_profile_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        runBlocking(Dispatchers.IO) {
            AppDatabase.getDatabase(application).clearAllTables()
        }
        prefs = ProfilePreferences(application)
        prefs.setCurrentPlayerId(ProfilePreferences.DEFAULT_PLAYER_ID)
        prefs.setUserCoins(25_000L)
        prefs.setUserGems(120)
    }

    private fun buildStandard4PlayerRoster(): List<Player> {
        return listOf(
            Player(id = "p_0", name = "You", color = LudoColor.YELLOW, isAi = false),
            Player(id = "p_1", name = "Red Bot", color = LudoColor.RED, isAi = true),
            Player(id = "p_2", name = "Green Bot", color = LudoColor.GREEN, isAi = true),
            Player(id = "p_3", name = "Blue Bot", color = LudoColor.BLUE, isAi = true)
        )
    }

    @Test
    fun `01 all 16 tokens render inside RoyalBoardView bounds with non-overlapping base pockets`() {
        val players = buildStandard4PlayerRoster()
        composeRule.setContent {
            MyApplicationTheme {
                RoyalBoardView(
                    players = players,
                    onPawnClick = {},
                    boardTheme = getBoardTheme("imperial_navy"),
                    modifier = Modifier.size(360.dp)
                )
            }
        }

        val boardBounds = composeRule.onNodeWithTag("royal_board_2d_view")
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        val colorQuadrantCenters = mutableMapOf<LudoColor, List<Rect>>()

        for (color in LudoColor.entries) {
            val rects = mutableListOf<Rect>()
            for (id in 0..3) {
                val tag = "pawn_${color.name.lowercase()}_$id"
                val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
                node.assertExists("Missing token $tag")
                node.assertIsDisplayed()
                val bounds = node.fetchSemanticsNode().boundsInRoot
                assertTrue("Token $tag width must be > 0", bounds.width > 0f)
                assertTrue("Token $tag height must be > 0", bounds.height > 0f)
                assertTrue(
                    "Token $tag left (${bounds.left}) must be >= board left (${boardBounds.left})",
                    bounds.left >= boardBounds.left - 1f
                )
                assertTrue(
                    "Token $tag right (${bounds.right}) must be <= board right (${boardBounds.right})",
                    bounds.right <= boardBounds.right + 1f
                )
                assertTrue(
                    "Token $tag top (${bounds.top}) must be >= board top (${boardBounds.top})",
                    bounds.top >= boardBounds.top - 1f
                )
                assertTrue(
                    "Token $tag bottom (${bounds.bottom}) must be <= board bottom (${boardBounds.bottom})",
                    bounds.bottom <= boardBounds.bottom + 1f
                )
                rects.add(bounds)
            }
            colorQuadrantCenters[color] = rects

            // Verify all 4 tokens of the same color sit in distinct non-overlapping positions
            for (i in rects.indices) {
                for (j in i + 1 until rects.size) {
                    val c1 = rects[i].center
                    val c2 = rects[j].center
                    val dist = kotlin.math.hypot((c1.x - c2.x).toDouble(), (c1.y - c2.y).toDouble())
                    assertTrue(
                        "Tokens $color#$i and $color#$j overlap (dist=$dist)",
                        dist > rects[i].width * 0.9
                    )
                }
            }
        }

        // Verify Red top-left, Green top-right, Yellow bottom-left, Blue bottom-right placement
        val redCenter = colorQuadrantCenters[LudoColor.RED]!!.first().center
        val greenCenter = colorQuadrantCenters[LudoColor.GREEN]!!.first().center
        val yellowCenter = colorQuadrantCenters[LudoColor.YELLOW]!!.first().center
        val blueCenter = colorQuadrantCenters[LudoColor.BLUE]!!.first().center

        assertTrue("Red must be left of Green", redCenter.x < greenCenter.x)
        assertTrue("Red must be above Yellow", redCenter.y < yellowCenter.y)
        assertTrue("Blue must be right of Yellow", blueCenter.x > yellowCenter.x)
        assertTrue("Blue must be below Green", blueCenter.y > greenCenter.y)
    }

    @Test
    fun `02 all 16 tokens render inside MaharajaLudoBoardView bounds cleanly`() {
        val players = buildStandard4PlayerRoster()
        composeRule.setContent {
            MyApplicationTheme {
                MaharajaLudoBoardView(
                    players = players,
                    onPawnClick = {},
                    modifier = Modifier.size(360.dp)
                )
            }
        }

        val boardBounds = composeRule.onNodeWithTag("maharaja_ludo_board_view")
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        for (color in LudoColor.entries) {
            for (id in 0..3) {
                val tag = "pawn_${color.name.lowercase()}_$id"
                val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
                node.assertExists("Missing token $tag in MaharajaLudoBoardView")
                node.assertIsDisplayed()
                val bounds = node.fetchSemanticsNode().boundsInRoot
                assertTrue(
                    "Maharaja token $tag must stay inside board bounds ($bounds vs $boardBounds)",
                    bounds.left >= boardBounds.left - 2f &&
                        bounds.right <= boardBounds.right + 2f &&
                        bounds.top >= boardBounds.top - 2f &&
                        bounds.bottom <= boardBounds.bottom + 2f
                )
            }
        }
    }

    @Test
    fun `03 all 4 colors follow exact 52-cell path, entrance arrows, home stretch, and exact finish`() {
        for (color in LudoColor.entries) {
            // Base release strictly requires 6
            var pawn = Pawn(id = 0, color = color, state = PawnState.IN_BASE)
            for (d in 1..5) {
                assertFalse("$color IN_BASE should not move on $d", LudoEngine.canPawnMove(pawn, d))
            }
            assertTrue("$color IN_BASE must move on 6", LudoEngine.canPawnMove(pawn, 6))

            // Deploy onto board start tile
            val deploySteps = LudoEngine.buildStepPath(pawn, 6)
            assertEquals(1, deploySteps.size)
            pawn = deploySteps.first()
            assertEquals(PawnState.ON_BOARD, pawn.state)
            assertEquals(color.startTrackPos, pawn.trackPosition)

            // Walk 50 steps one cell at a time to land exactly on the colored entrance arrow square
            val visitedTrack = mutableListOf(pawn.trackPosition)
            repeat(10) {
                val steps = LudoEngine.buildStepPath(pawn, 5)
                assertEquals(5, steps.size)
                assertTrue("All intermediate steps must stay ON_BOARD", steps.all { it.state == PawnState.ON_BOARD })
                visitedTrack.addAll(steps.map { it.trackPosition })
                pawn = steps.last()
            }
            assertEquals(51, visitedTrack.size)
            assertEquals("$color should land on homeEntranceTrackPos after 50 perimeter moves", color.homeEntranceTrackPos, pawn.trackPosition)

            // Roll 3 from home entrance -> enters home stretch step 3, cell by cell!
            val enterHomeSteps = LudoEngine.buildStepPath(pawn, 3)
            assertEquals(3, enterHomeSteps.size)
            assertEquals(1, enterHomeSteps[0].homeStep)
            assertEquals(2, enterHomeSteps[1].homeStep)
            assertEquals(3, enterHomeSteps[2].homeStep)
            pawn = enterHomeSteps.last()
            assertEquals(PawnState.IN_HOME_STRETCH, pawn.state)

            // Verify grid mapping stays inside 15x15 arm runway
            val (hx, hy) = StandardLudoBoardLayout.getHomeStretchGridPosition(color, pawn.homeStep)
            assertTrue("Home stretch coord ($hx,$hy) must be in 0..14", hx in 0..14 && hy in 0..14)

            // Overshooting finish (>6) must be blocked; exact 3 (3+3=6) must finish!
            assertFalse("Step 3 + 4 = 7 must be invalid", LudoEngine.canPawnMove(pawn, 4))
            assertTrue("Step 3 + 3 = 6 must finish", LudoEngine.canPawnMove(pawn, 3))
            val finishSteps = LudoEngine.buildStepPath(pawn, 3)
            assertEquals(3, finishSteps.size)
            assertEquals(4, finishSteps[0].homeStep)
            assertEquals(5, finishSteps[1].homeStep)
            assertEquals(PawnState.FINISHED, finishSteps[2].state)
            assertEquals(6, finishSteps[2].homeStep)
        }
    }

    @Test
    fun `04 safe star cells protect tokens while non-safe squares cut opponents back to base`() {
        // Check all 8 safe stars prevent captures
        for (safePos in LudoEngine.SAFE_POSITIONS) {
            val attacker = Pawn(id = 0, color = LudoColor.RED, state = PawnState.ON_BOARD, trackPosition = safePos)
            val defender = Pawn(id = 0, color = LudoColor.BLUE, state = PawnState.ON_BOARD, trackPosition = safePos)
            val captures = LudoEngine.checkForCaptures(
                movingPawn = attacker,
                allPlayers = listOf(
                    Player("p0", "Red", LudoColor.RED, pawns = listOf(attacker)),
                    Player("p1", "Blue", LudoColor.BLUE, pawns = listOf(defender))
                ),
                mode = GameMode.CLASSIC
            )
            assertTrue("Safe star $safePos must prevent capture", captures.isEmpty())
        }

        // Non-safe cell 19 captures Blue in Classic mode, but protects teammate in TEAM_2VS2 (Red & Blue = Team Moon)
        val redAttacker = Pawn(id = 0, color = LudoColor.RED, state = PawnState.ON_BOARD, trackPosition = 19)
        val blueTarget = Pawn(id = 1, color = LudoColor.BLUE, state = PawnState.ON_BOARD, trackPosition = 19)
        val players = listOf(
            Player("p0", "Red", LudoColor.RED, pawns = listOf(redAttacker)),
            Player("p1", "Blue", LudoColor.BLUE, pawns = listOf(blueTarget))
        )

        val classicCaptures = LudoEngine.checkForCaptures(redAttacker, players, GameMode.CLASSIC)
        assertEquals(1, classicCaptures.size)

        val coopCaptures = LudoEngine.checkForCaptures(redAttacker, players, GameMode.TEAM_2VS2)
        assertTrue("Team 2v2 teammates must never capture each other", coopCaptures.isEmpty())
    }

    private fun pumpMainLooperUntilMoveDone(viewModel: LudoViewModel) {
        val mainShadow = org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
        repeat(40) {
            mainShadow.idleFor(java.time.Duration.ofMillis(100))
            if (viewModel.uiState.value.turnPhase != TurnPhase.ANIMATING_MOVE) {
                mainShadow.idleFor(java.time.Duration.ofMillis(50))
                return
            }
        }
    }

    @Test
    fun `05 clicking movable token executes smooth step move, rejects opponent tokens, and prevents duplicate moves`() {
        val viewModel = LudoViewModel(application)
        viewModel.startNewGame(mode = GameMode.VS_AI, playerCount = 4, chosenColor = LudoColor.RED, wager = 0L)

        // Ensure all 16 tokens exist across 4 players
        val startState = viewModel.uiState.value
        assertEquals(4, startState.players.size)
        assertEquals(16, startState.players.sumOf { it.pawns.size })
        assertEquals(LudoColor.RED, startState.players[0].color)

        // Try clicking Green's (opponent's) token -> must be ignored cleanly
        val greenPawn = startState.players.first { it.color == LudoColor.GREEN }.pawns.first()
        viewModel.onPawnClicked(greenPawn)
        assertEquals(0L, viewModel.uiState.value.moveSequenceNumber)

        // Set up an active dice=6 move for Red via syncRemoteMoveIfAuthorized
        val seqBefore = viewModel.uiState.value.moveSequenceNumber
        val acceptedFirst = viewModel.syncRemoteMoveIfAuthorized(
            senderColor = LudoColor.RED,
            pawnId = 0,
            diceRoll = 6,
            expectedSequenceNumber = seqBefore
        )
        assertTrue("First valid authorized move must be accepted", acceptedFirst)

        // Duplicate move submission with same sequence must be blocked immediately
        val acceptedDuplicate = viewModel.syncRemoteMoveIfAuthorized(
            senderColor = LudoColor.RED,
            pawnId = 0,
            diceRoll = 6,
            expectedSequenceNumber = seqBefore
        )
        assertFalse("Duplicate move for same turn sequence must be rejected", acceptedDuplicate)

        // Pump Robolectric main looper so step animation & finally release complete cleanly
        pumpMainLooperUntilMoveDone(viewModel)

        val movedRedPawn = viewModel.uiState.value.players.first { it.color == LudoColor.RED }.pawns.first { it.id == 0 }
        assertEquals(PawnState.ON_BOARD, movedRedPawn.state)
        assertEquals(LudoColor.RED.startTrackPos, movedRedPawn.trackPosition)
        assertEquals(seqBefore + 1L, viewModel.uiState.value.moveSequenceNumber)

        // Subsequent advance by 4 steps moves Red pawn cell-by-cell from 1 -> 5
        val seqSecond = viewModel.uiState.value.moveSequenceNumber
        val advanceAccepted = viewModel.syncRemoteMoveIfAuthorized(
            senderColor = LudoColor.RED,
            pawnId = 0,
            diceRoll = 4,
            expectedSequenceNumber = seqSecond
        )
        assertTrue("Second move by 4 steps must be accepted", advanceAccepted)

        pumpMainLooperUntilMoveDone(viewModel)

        val finalRedPawn = viewModel.uiState.value.players.first { it.color == LudoColor.RED }.pawns.first { it.id == 0 }
        assertEquals(PawnState.ON_BOARD, finalRedPawn.state)
        assertEquals(LudoColor.RED.startTrackPos + 4, finalRedPawn.trackPosition)
        viewModel.leaveCurrentGame()
    }

    @Test
    fun `06 online multiplayer token positions persist across network disconnect, reconnect, ViewModel recreation, and restart`() {
        val viewModel1 = LudoViewModel(application)
        viewModel1.startNewGame(
            mode = GameMode.ONLINE_MULTIPLAYER,
            playerCount = 4,
            chosenColor = LudoColor.YELLOW,
            wager = 0L
        )
        assertEquals(16, viewModel1.uiState.value.players.sumOf { it.pawns.size })

        // Move Yellow pawn #0 out onto start square (40), then advance 5 steps -> square 45
        assertTrue(viewModel1.syncRemoteMoveIfAuthorized(LudoColor.YELLOW, 0, 6, viewModel1.uiState.value.moveSequenceNumber))
        pumpMainLooperUntilMoveDone(viewModel1)

        assertTrue(viewModel1.syncRemoteMoveIfAuthorized(LudoColor.YELLOW, 0, 5, viewModel1.uiState.value.moveSequenceNumber))
        pumpMainLooperUntilMoveDone(viewModel1)

        val expectedChecksum = viewModel1.uiState.value.lastSyncedBoardChecksum
        assertTrue(expectedChecksum.isNotBlank())

        // Simulate disconnect & reconnect on active ViewModel
        viewModel1.simulateNetworkDisconnect()
        assertEquals(MatchConnectionState.DISCONNECTED, viewModel1.uiState.value.connectionState)
        assertTrue(viewModel1.reconnectActiveMatch())
        assertEquals(MatchConnectionState.CONNECTED, viewModel1.uiState.value.connectionState)
        assertEquals(expectedChecksum, viewModel1.uiState.value.lastSyncedBoardChecksum)

        // Simulate full screen/process recreation by instantiating a new LudoViewModel
        val recreatedVm = LudoViewModel(application)
        val restoredYellowPawn0 = recreatedVm.uiState.value.players.first { it.color == LudoColor.YELLOW }.pawns.first { it.id == 0 }
        assertEquals(PawnState.ON_BOARD, restoredYellowPawn0.state)
        assertEquals(45, restoredYellowPawn0.trackPosition)
        assertEquals(expectedChecksum, recreatedVm.uiState.value.lastSyncedBoardChecksum)

        // Verify RoyalBoardView still renders all 16 tokens cleanly after restore (15 in base + 1 at track 45)
        composeRule.setContent {
            val s by recreatedVm.uiState.collectAsState()
            MyApplicationTheme {
                RoyalBoardView(
                    players = s.players,
                    onPawnClick = {},
                    boardTheme = getBoardTheme(s.equippedBoardTheme),
                    modifier = Modifier.size(360.dp)
                )
            }
        }
        val boardBounds = composeRule.onNodeWithTag("royal_board_2d_view").fetchSemanticsNode().boundsInRoot
        for (color in LudoColor.entries) {
            for (id in 0..3) {
                val bounds = composeRule.onNodeWithTag("pawn_${color.name.lowercase()}_$id", useUnmergedTree = true)
                    .assertIsDisplayed()
                    .fetchSemanticsNode()
                    .boundsInRoot
                assertTrue(bounds.left >= boardBounds.left - 1f && bounds.right <= boardBounds.right + 1f)
            }
        }

        // Verify restartMatch resets all 16 tokens cleanly back to IN_BASE
        recreatedVm.restartMatch()
        val restartedPawns = recreatedVm.uiState.value.players.flatMap { it.pawns }
        assertEquals(16, restartedPawns.size)
        assertTrue("After match restart, all 16 tokens must be IN_BASE", restartedPawns.all { it.state == PawnState.IN_BASE })
        recreatedVm.leaveCurrentGame()
        viewModel1.leaveCurrentGame()
    }
}
