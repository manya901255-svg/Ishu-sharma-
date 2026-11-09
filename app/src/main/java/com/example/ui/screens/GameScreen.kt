package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LudoColor
import com.example.model.TurnPhase
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel
) {
    var showQuitConfirmation by remember { mutableStateOf(false) }

    // Intercept back button to prevent accidental exit; game never auto-closes until explicitly left
    BackHandler(enabled = true) {
        showQuitConfirmation = true
    }

    val activePlayer = uiState.players.getOrNull(uiState.currentPlayerIndex)
    val humanPlayer = uiState.players.firstOrNull { !it.isAi } ?: uiState.players.firstOrNull()
    val isHumanTurn = activePlayer?.id == humanPlayer?.id

    // Runtime Permission Launcher for Real-Time In-Game Voice Chat
    val micPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onRecordPermissionGranted()
            viewModel.showPopUpNotice(
                title = "🎙️ Voice Chat Connected",
                message = "Microphone active! You can now talk live with players in the room.",
                icon = "🎙️",
                type = "VOICE"
            )
        } else {
            viewModel.showPopUpNotice(
                title = "Microphone Permission Required",
                message = "Allow microphone permission to speak with players during the match.",
                icon = "🎙️",
                type = "INFO"
            )
        }
    }

    Scaffold(
        topBar = {
            // Authentic Top Bar matching Screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A255C))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Light Blue Hamburger Menu Button [☰]
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF29B6F6))
                        .clickable { viewModel.toggleMainMenuOverlay(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 2. Trophy & Coin Pill [🏆 🪙 30 K ℹ️]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F3A7D))
                        .border(1.2.dp, Color(0xFF29B6F6), RoundedCornerShape(16.dp))
                        .clickable { viewModel.showPopUpNotice("Trophy Wager", "30K Pot Active! Win to double coins!", "🏆", "INFO") }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("🏆", fontSize = 11.sp)
                    Text("🪙", fontSize = 10.sp)
                    Text("30 K", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF29B6F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("i", color = Color(0xFF0A255C), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                // 3. Cyan Pill [YOUR BOARD]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF061838))
                        .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(AppScreen.SHOP) }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOUR", color = Color(0xFFE0F7FA), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                        Text("BOARD", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                // 4. Green 3D Toggle Button [3D]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (uiState.is3DBoardEnabled) Color(0xFF00E676) else Color(0xFF1E3A70))
                        .clickable { viewModel.toggle3DBoard(!uiState.is3DBoardEnabled) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.is3DBoardEnabled) "3D" else "2D",
                        color = if (uiState.is3DBoardEnabled) Color(0xFF003300) else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                // 4b. Purple Royal Music Player Button [🎵 / 🎶]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6A1B9A))
                        .clickable { viewModel.toggleMusicPlayerDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isMusicPlaying) "🎶" else "🎵",
                        fontSize = 16.sp
                    )
                }

                // 5. Orange Settings Button [⚙️]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFA000))
                        .clickable { viewModel.toggleSoundSettings(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 6. Light Blue Help Button [?]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00B0FF))
                        .clickable { viewModel.toggleFairProof(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }

                // 7. Green Share Button [🔗]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00C853))
                        .clickable { viewModel.toggleInviteDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFF071228)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B1938),
                            Color(0xFF071228),
                            Color(0xFF040A18)
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Upper Ad Banner matching Screenshot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎵", fontSize = 13.sp)
                            }
                            Text(
                                text = "A custom feed delivering you more of the videos you want to see...",
                                color = Color(0xFF263238),
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2979FF))
                                .clickable { viewModel.showPopUpNotice("Featured App", "Downloading trending viral content pack...", "🎵", "INFO") }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Install", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Top Player Slots with Individual Dice Per Player
                val bluePlayer = uiState.players.find { it.color == LudoColor.BLUE }
                val redPlayer = uiState.players.find { it.color == LudoColor.RED }
                val yellowPlayer = uiState.players.find { it.color == LudoColor.YELLOW }
                val greenPlayer = uiState.players.find { it.color == LudoColor.GREEN }
                val activeColor = activePlayer?.color

                val isBlueHuman = bluePlayer != null && bluePlayer.id == humanPlayer?.id
                val isRedHuman = redPlayer != null && redPlayer.id == humanPlayer?.id
                val isYellowHuman = yellowPlayer != null && yellowPlayer.id == humanPlayer?.id
                val isGreenHuman = greenPlayer != null && greenPlayer.id == humanPlayer?.id

                val youSlotName = if (uiState.playerNickname.isNotBlank() && uiState.playerNickname != "You") "You (${uiState.playerNickname})" else "You"
                val isTwoPlayer = uiState.players.size <= 2

                // In 2-player match: Top-left is opponent Computer 2 (Abdul) matching screenshot!
                val topOpponent = if (isTwoPlayer) {
                    uiState.players.firstOrNull { it.id != humanPlayer?.id } ?: bluePlayer ?: redPlayer
                } else null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTwoPlayer && topOpponent != null) {
                        // Top-Left: Opponent Computer 2 with its own dice widget beside card
                        val isOpponentTurn = topOpponent.color == activeColor
                        PlayerCornerSlotPanel(
                            player = topOpponent,
                            slotLabel = topOpponent.name,
                            isActiveTurn = isOpponentTurn,
                            isHuman = false,
                            isRolling = uiState.isRolling && isOpponentTurn,
                            currentDiceValue = uiState.currentDiceValue,
                            timeRemainingSec = uiState.turnTimeRemaining,
                            consecutiveSixes = if (isOpponentTurn) uiState.consecutiveSixes else 0,
                            canRoll = false,
                            onRollClick = {},
                            onProfileClick = {},
                            floatingEmoji = if (uiState.activeFloatingEmoji?.first == topOpponent.name) uiState.activeFloatingEmoji?.second else null,
                            alignRight = false
                        )
                    } else if (!isTwoPlayer && redPlayer != null) {
                        // 4-Player Top-Left: Red Player with individual dice
                        PlayerCornerSlotPanel(
                            player = redPlayer,
                            slotLabel = if (isRedHuman) youSlotName else redPlayer.name,
                            isActiveTurn = redPlayer.color == activeColor,
                            isHuman = isRedHuman,
                            isRolling = uiState.isRolling && (redPlayer.color == activeColor),
                            currentDiceValue = uiState.currentDiceValue,
                            timeRemainingSec = uiState.turnTimeRemaining,
                            consecutiveSixes = if (redPlayer.color == activeColor) uiState.consecutiveSixes else 0,
                            canRoll = isRedHuman && isHumanTurn && uiState.turnPhase == TurnPhase.ROLL_DICE && !uiState.isRolling,
                            onRollClick = if (isRedHuman) { { viewModel.rollDice() } } else { {} },
                            onProfileClick = if (isRedHuman) { { viewModel.navigateTo(AppScreen.PROFILE) } } else { {} },
                            floatingEmoji = if (uiState.activeFloatingEmoji?.first == redPlayer.name) uiState.activeFloatingEmoji?.second else null,
                            alignRight = false
                        )
                    } else {
                        Spacer(modifier = Modifier.width(40.dp))
                    }

                    // Top-Right: Green Player in 4-Player games
                    if (!isTwoPlayer && greenPlayer != null) {
                        PlayerCornerSlotPanel(
                            player = greenPlayer,
                            slotLabel = if (isGreenHuman) youSlotName else greenPlayer.name,
                            isActiveTurn = greenPlayer.color == activeColor,
                            isHuman = isGreenHuman,
                            isRolling = uiState.isRolling && (greenPlayer.color == activeColor),
                            currentDiceValue = uiState.currentDiceValue,
                            timeRemainingSec = uiState.turnTimeRemaining,
                            consecutiveSixes = if (greenPlayer.color == activeColor) uiState.consecutiveSixes else 0,
                            canRoll = isGreenHuman && isHumanTurn && uiState.turnPhase == TurnPhase.ROLL_DICE && !uiState.isRolling,
                            onRollClick = if (isGreenHuman) { { viewModel.rollDice() } } else { {} },
                            onProfileClick = if (isGreenHuman) { { viewModel.navigateTo(AppScreen.PROFILE) } } else { {} },
                            floatingEmoji = if (uiState.activeFloatingEmoji?.first == greenPlayer.name) uiState.activeFloatingEmoji?.second else null,
                            alignRight = true
                        )
                    }
                }

                // 3. Center Board
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.is3DBoardEnabled) {
                        LudoBoard3DView(
                            players = uiState.players,
                            onPawnClick = { pawn -> viewModel.onPawnClicked(pawn) },
                            pawnCrownEmoji = com.example.model.getPawnCrownEmoji(uiState.equippedPawnCrownAvatar),
                            boardTheme = com.example.model.getBoardTheme(uiState.equippedBoardTheme),
                            is3DMode = true,
                            tiltAngle = uiState.board3DAngle,
                            onCycleAngle = { viewModel.cycle3DAngle() },
                            modifier = Modifier.fillMaxWidth(0.99f)
                        )
                    } else {
                        RoyalBoardView(
                            players = uiState.players,
                            onPawnClick = { pawn -> viewModel.onPawnClicked(pawn) },
                            pawnCrownEmoji = com.example.model.getPawnCrownEmoji(uiState.equippedPawnCrownAvatar),
                            boardTheme = com.example.model.getBoardTheme(uiState.equippedBoardTheme),
                            modifier = Modifier.fillMaxWidth(0.99f)
                        )
                    }
                }

                // 4. Bottom Player Slots with Individual Dice Per Player
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bottom-Left: Human Player 'You (Ishu)' in 2-Player or Yellow player in 4-Player
                    val bottomUserPlayer = if (isTwoPlayer) humanPlayer ?: yellowPlayer else yellowPlayer
                    if (bottomUserPlayer != null) {
                        val isBottomActive = bottomUserPlayer.color == activeColor
                        val isBottomHuman = bottomUserPlayer.id == humanPlayer?.id
                        PlayerCornerSlotPanel(
                            player = bottomUserPlayer,
                            slotLabel = if (isBottomHuman) youSlotName else bottomUserPlayer.name,
                            isActiveTurn = isBottomActive,
                            isHuman = isBottomHuman,
                            isRolling = uiState.isRolling && isBottomActive,
                            currentDiceValue = uiState.currentDiceValue,
                            timeRemainingSec = uiState.turnTimeRemaining,
                            consecutiveSixes = if (isBottomActive) uiState.consecutiveSixes else 0,
                            canRoll = isBottomHuman && isHumanTurn && uiState.turnPhase == TurnPhase.ROLL_DICE && !uiState.isRolling,
                            onRollClick = if (isBottomHuman) { { viewModel.rollDice() } } else { {} },
                            onProfileClick = if (isBottomHuman) { { viewModel.navigateTo(AppScreen.PROFILE) } } else { {} },
                            onLightningClick = { viewModel.activatePowerUp("LIGHTNING") },
                            floatingEmoji = if (uiState.activeFloatingEmoji?.first == bottomUserPlayer.name) uiState.activeFloatingEmoji?.second else null,
                            alignRight = false
                        )
                    }

                    // Bottom-Right: Blue Player in 4-Player games
                    if (!isTwoPlayer && bluePlayer != null) {
                        PlayerCornerSlotPanel(
                            player = bluePlayer,
                            slotLabel = if (isBlueHuman) youSlotName else bluePlayer.name,
                            isActiveTurn = bluePlayer.color == activeColor,
                            isHuman = isBlueHuman,
                            isRolling = uiState.isRolling && (bluePlayer.color == activeColor),
                            currentDiceValue = uiState.currentDiceValue,
                            timeRemainingSec = uiState.turnTimeRemaining,
                            consecutiveSixes = if (bluePlayer.color == activeColor) uiState.consecutiveSixes else 0,
                            canRoll = isBlueHuman && isHumanTurn && uiState.turnPhase == TurnPhase.ROLL_DICE && !uiState.isRolling,
                            onRollClick = if (isBlueHuman) { { viewModel.rollDice() } } else { {} },
                            onProfileClick = if (isBlueHuman) { { viewModel.navigateTo(AppScreen.PROFILE) } } else { {} },
                            floatingEmoji = if (uiState.activeFloatingEmoji?.first == bluePlayer.name) uiState.activeFloatingEmoji?.second else null,
                            alignRight = true
                        )
                    } else {
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                }

                // 5. Authentic Bottom Section matching Screenshot
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Match Status Notice Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF091C3E))
                            .border(1.dp, Color(0xFF1E3A70), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🎲", fontSize = 12.sp)
                            Text(
                                text = uiState.bannerNotice ?: "Royal Online Match begins! Wager: 🪙 ${formatCompactCoins(uiState.currentWager)} • 2X Double Prize Active!",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isHumanTurn && uiState.turnPhase == TurnPhase.ROLL_DICE) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF00E676))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "YOUR TURN",
                                    color = Color(0xFF003300),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Action Buttons Row matching Screenshot + Voice Chat Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // [ ROLL DICE ] Button
                        Box(
                            modifier = Modifier
                                .weight(2.0f)
                                .height(42.dp)
                                .shadow(3.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
                                    )
                                )
                                .clickable(enabled = isHumanTurn && uiState.turnPhase == TurnPhase.ROLL_DICE && !uiState.isRolling) {
                                    viewModel.rollDice()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🎲", fontSize = 15.sp)
                                Text(
                                    text = "ROLL DICE",
                                    color = Color(0xFF3E2723),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // [ ⚡ 6! ] Power-up
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1976D2))
                                .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(8.dp))
                                .clickable { viewModel.activatePowerUp("LIGHTNING") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚡", fontSize = 11.sp)
                                Text("6!", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // [ EVEN 2,4,6 ]
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1976D2))
                                .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(8.dp))
                                .clickable { viewModel.activatePowerUp("EVEN") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EVEN", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                Text("2,4,6", color = Color(0xFFBBDEFB), fontSize = 7.sp)
                            }
                        }

                        // [ ODD 1,3,5 ]
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1976D2))
                                .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(8.dp))
                                .clickable { viewModel.activatePowerUp("ODD") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ODD", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                Text("1,3,5", color = Color(0xFFBBDEFB), fontSize = 7.sp)
                            }
                        }

                        // [ 🎙️ / 🔇 ] Voice Chat Mic Toggle Button
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isMicMuted) Color(0xFF37474F) else Color(0xFF00897B))
                                .border(1.dp, if (uiState.isMicMuted) Color.Gray else Color(0xFF80CBC4), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (viewModel.voiceChatManager.hasRecordPermission()) {
                                        viewModel.toggleMic()
                                    } else {
                                        micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (uiState.isMicMuted) "🔇" else "🎙️", fontSize = 12.sp)
                                Text(
                                    text = if (uiState.isMicMuted) "MUTE" else "MIC",
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // [ 🔊 / 🔈 ] Room Audio Speaker Toggle Button
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isSpeakerMuted) Color(0xFF37474F) else Color(0xFF0277BD))
                                .border(1.dp, if (uiState.isSpeakerMuted) Color.Gray else Color(0xFF81D4FA), RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleSpeaker() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (uiState.isSpeakerMuted) "🔈" else "🔊", fontSize = 14.sp)
                        }

                        // [ 💬 ] Chat Button
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1976D2))
                                .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleChatDialog(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 16.sp)
                        }

                        // [ 🚪 LEAVE ] Button
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .height(42.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFE53935), Color(0xFFC62828))
                                    )
                                )
                                .border(1.dp, Color(0xFFFF8A80), RoundedCornerShape(8.dp))
                                .clickable { showQuitConfirmation = true }
                                .testTag("action_leave_game_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚪", fontSize = 11.sp)
                                Text("LEAVE", color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    // Prominent Dedicated Leave Game Option as explicitly requested
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF8B0000), Color(0xFFC62828), Color(0xFF8B0000))
                                )
                            )
                            .border(1.2.dp, Color(0xFFFF8A80), RoundedCornerShape(8.dp))
                            .clickable { showQuitConfirmation = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("leave_game_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🚪", fontSize = 14.sp)
                            Text(
                                text = "LEAVE GAME",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "🪙 ${formatCompactCoins(uiState.currentWager)} coins will be lost",
                            color = Color(0xFFFFCDD2),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Low On Cash Green Bar matching Screenshot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B5E20))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .clickable { viewModel.navigateTo(AppScreen.SHOP) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Low On Cash?", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("💵 180", color = Color(0xFFFFEB3B), fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "120",
                                color = Color(0xFFA5D6A7),
                                fontSize = 8.5.sp,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF00E676))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("₹68.29", color = Color(0xFF003300), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Transient In-Game Floating Chat Response Banner
            val bannerSenderColor = uiState.players.firstOrNull { it.name == uiState.lastChatSenderName }?.color
            InGameChatBannerOverlay(
                message = uiState.lastChatMessage,
                senderName = uiState.lastChatSenderName,
                senderColor = bannerSenderColor,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )

            // Celebratory Particle Effects (6 Rolled, Pawn Captured, Home Entry, Match Won)
            RegalCelebrationOverlay(
                celebrationType = uiState.activeCelebration,
                modifier = Modifier.fillMaxSize(),
                onAnimationEnd = { viewModel.clearCelebration() }
            )
        }

        // Invite Friends Dialog (WhatsApp, Instagram, Facebook)
        if (uiState.showInviteDialog) {
            InviteFriendsDialog(
                roomCode = uiState.roomCode,
                onRewardEarned = { coins, gems -> viewModel.claimDailySpinReward(coins, gems) },
                onDismiss = { viewModel.toggleInviteDialog(false) }
            )
        }

        // Dialogs
        if (uiState.showFairProofDialog) {
            FairDiceDialog(
                proof = uiState.fairDiceProof,
                onDismiss = { viewModel.toggleFairProof(false) }
            )
        }

        if (uiState.showChatDialog) {
            val currentSender = if (uiState.gameMode == com.example.model.GameMode.PASS_AND_PLAY) {
                uiState.players.getOrNull(uiState.currentPlayerIndex) ?: uiState.players.firstOrNull()
            } else {
                uiState.players.firstOrNull { !it.isAi } ?: uiState.players.firstOrNull()
            }
            RoyalChatEmojiDialog(
                messages = uiState.chatMessages,
                onSendMessage = { viewModel.sendChatMessage(it) },
                onSendPhrase = { viewModel.sendQuickChat(it) },
                onSendEmoji = { viewModel.sendRoyalEmoji(it) },
                currentSenderName = currentSender?.name ?: "You",
                currentSenderColor = currentSender?.color ?: com.example.model.LudoColor.RED,
                onDismiss = { viewModel.toggleChatDialog(false) }
            )
        }

        if (showQuitConfirmation) {
            QuitConfirmationDialog(
                wager = uiState.currentWager,
                onConfirm = {
                    showQuitConfirmation = false
                    viewModel.leaveCurrentGame()
                },
                onDismiss = { showQuitConfirmation = false }
            )
        }


        // Regal Settings & Atmosphere Dialog (BGM, SFX, Haptics, Regal Themes)
        if (uiState.showSoundSettingsDialog) {
            RegalSettingsDialog(
                isBgmEnabled = uiState.isBgmEnabled,
                bgmVolume = uiState.bgmVolume,
                isSfxEnabled = uiState.isSfxEnabled,
                sfxVolume = uiState.sfxVolume,
                isHapticsEnabled = uiState.isHapticsEnabled,
                selectedBoardThemeId = uiState.equippedBoardTheme,
                selectedSfxPreset = uiState.sfxPreset,
                currentBgmTrack = uiState.currentBgmTrack,
                currentBgmTrackIndex = uiState.currentBgmTrackIndex,
                bgmPlaylist = uiState.bgmPlaylist,
                onToggleBgm = { viewModel.toggleBgm(it) },
                onBgmVolumeChange = { viewModel.setBgmVolume(it) },
                onNextBgmTrack = { viewModel.playNextBgmTrack() },
                onPrevBgmTrack = { viewModel.playPreviousBgmTrack() },
                onSelectBgmTrack = { viewModel.selectBgmTrack(it) },
                onToggleSfx = { viewModel.toggleSfx(it) },
                onSfxVolumeChange = { viewModel.setSfxVolume(it) },
                onToggleHaptics = { viewModel.toggleHaptics(it) },
                onSelectBoardTheme = { viewModel.selectBoardTheme(it) },
                onSelectSfxPreset = { viewModel.setSfxPreset(it) },
                onTestDice = { viewModel.soundAndHaptics.playDiceRoll() },
                onTestPawn = { viewModel.soundAndHaptics.playPawnMove() },
                onTestFanfare = { viewModel.soundAndHaptics.soundManager.playVictoryFanfare() },
                onTestCapture = { viewModel.soundAndHaptics.soundManager.playPawnCapture() },
                onTestStarSafe = { viewModel.soundAndHaptics.playStarSafe() },
                onTestSixRolled = { viewModel.soundAndHaptics.playSixRolled() },
                onOpenMusicPlayer = { viewModel.toggleMusicPlayerDialog(true) },
                onDismiss = { viewModel.toggleSoundSettings(false) }
            )
        }

        // Royal Indian Music Player & Online Streaming Dialog
        if (uiState.showMusicPlayerDialog) {
            LudoMusicPlayerDialog(
                currentTrack = uiState.currentBgmTrack,
                isPlaying = uiState.isMusicPlaying,
                repeatMode = uiState.musicRepeatMode,
                isShuffleEnabled = uiState.isMusicShuffleEnabled,
                volume = uiState.bgmVolume,
                favoriteSongIds = uiState.favoriteSongIds,
                isBuffering = uiState.isMusicBuffering,
                playbackPositionMs = uiState.playbackPositionMs,
                playbackDurationMs = uiState.playbackDurationMs,
                selectedCategory = uiState.selectedMusicCategory,
                categoryTracks = uiState.categoryTracks,
                isLoadingCategory = uiState.isLoadingCategoryMusic,
                searchResults = uiState.onlineSearchResults,
                isSearching = uiState.isSearchingMusic,
                searchQuery = uiState.onlineSearchQuery,
                recentlyPlayed = uiState.recentlyPlayedTracks,
                userPlaylists = uiState.userPlaylists,
                favoriteTracks = uiState.favoriteTracks,
                onPlayPauseToggle = { viewModel.toggleMusicPlayPause() },
                onNextTrack = { viewModel.playNextBgmTrack() },
                onPreviousTrack = { viewModel.playPreviousBgmTrack() },
                onSelectTrack = { viewModel.selectBgmTrack(it) },
                onPlayTrackDirect = { track, list -> viewModel.playTrackDirect(track, list) },
                onSeekPosition = { viewModel.seekMusicPosition(it) },
                onSearchQueryChange = { viewModel.searchOnlineMusic(it) },
                onSelectCategory = { viewModel.selectMusicCategory(it) },
                onCreatePlaylist = { viewModel.createUserPlaylist(it) },
                onAddTrackToPlaylist = { plId, track -> viewModel.addTrackToUserPlaylist(plId, track) },
                onRemoveTrackFromPlaylist = { plId, trackId -> viewModel.removeTrackFromUserPlaylist(plId, trackId) },
                onRepeatModeCycle = { viewModel.cycleMusicRepeatMode() },
                onToggleShuffle = { viewModel.toggleMusicShuffle() },
                onVolumeChange = { viewModel.setBgmVolume(it) },
                onToggleFavorite = { viewModel.toggleFavoriteSong(it) },
                onToggleFavoriteTrack = { viewModel.toggleFavoriteTrack(it) },
                onDismiss = { viewModel.toggleMusicPlayerDialog(false) }
            )
        }

        // Royal Victory Dialog / Game Over Screen Overlay
        if (uiState.winner != null) {
            val userPlayer = uiState.players.firstOrNull { !it.isAi } ?: uiState.players.firstOrNull()
            val doubleWinningPrize = uiState.currentWager * uiState.players.size * 2
            RoyalVictoryDialog(
                winner = uiState.winner,
                players = uiState.players,
                userPlayer = userPlayer,
                gameMode = uiState.gameMode,
                wager = uiState.currentWager,
                totalPot = doubleWinningPrize,
                wonChestBoxName = uiState.wonChestBoxName,
                onOpenChestBox = { viewModel.openWonChestBox() },
                onPlayAgain = { viewModel.startNewGame(uiState.gameMode, uiState.players.size, uiState.userChosenColor, uiState.currentWager) },
                onReturnHome = { viewModel.leaveCurrentGame() }
            )
        }

        // Real-time Game Pop-Up Notice (Friend Online with Green Tick, Key, Chest, or Season Pass)
        GamePopUpNoticeDialog(
            notice = uiState.activePopUpNotice,
            onActionClick = {
                val notice = uiState.activePopUpNotice
                if (notice?.type == "FRIEND_ONLINE") {
                    viewModel.inviteFriendToMatch(com.example.model.MOCK_ROYAL_FRIENDS.first())
                } else if (notice?.type == "KEY") {
                    viewModel.navigateTo(AppScreen.HOME)
                } else if (notice?.type == "SEASON_PASS") {
                    viewModel.toggleSeasonPassDialog(true)
                }
            },
            onDismiss = { viewModel.dismissPopUpNotice() }
        )
    }
}

@Composable
private fun QuitConfirmationDialog(
    wager: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B38)),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, RubyRed),
            modifier = Modifier.testTag("leave_game_confirmation_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB71C1C).copy(alpha = 0.2f))
                        .border(2.dp, RubyRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚪", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Leave Game?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Agar aap abhi game leave karte hain ya haar jaate hain, to aapke entry kiye huye 🪙 ${formatCompactCoins(wager)} Coins wapas nahi milenge!\n\n(Warning: If you leave now, you will forfeit this match and your wager of 🪙 ${formatCompactCoins(wager)} coins is permanently lost. The game will not close until you confirm).",
                    color = Color(0xFFFFCDD2),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("cancel_leave_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF64B5F6))
                    ) {
                        Text("Stay & Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("confirm_leave_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RubyRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Forfeit & Leave", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoyalVictoryDialog(
    winner: com.example.model.Player,
    players: List<com.example.model.Player>,
    userPlayer: com.example.model.Player?,
    gameMode: com.example.model.GameMode,
    wager: Long,
    totalPot: Long,
    wonChestBoxName: String? = null,
    onOpenChestBox: () -> Unit = {},
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit
) {
    val isHumanWin = userPlayer != null && winner.id == userPlayer.id
    var isChestOpened by remember { mutableStateOf(false) }
    val sortedPlayers = remember(players, winner) {
        players.sortedWith(
            compareByDescending<com.example.model.Player> { it.id == winner.id }
                .thenByDescending { it.finishedPawnsCount }
        )
    }
    val userRank = sortedPlayers.indexOfFirst { it.id == userPlayer?.id } + 1
    val coinsEarned = if (isHumanWin) totalPot else -wager

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("royal_victory_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // Royal Confetti Falling System
            MatchWonFullConfetti(modifier = Modifier.fillMaxSize())

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF007132B)),
                border = androidx.compose.foundation.BorderStroke(
                    3.dp,
                    Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Arch Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (isHumanWin) listOf(Color(0xFFFFB300), Color(0xFFFF6F00))
                                    else listOf(Color(0xFFD32F2F), Color(0xFF7B0000))
                                )
                            )
                            .border(2.dp, RoyalGoldLight, RoundedCornerShape(20.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHumanWin) "👑 2X ROYAL VICTORY! 👑" else "⚔️ GAME OVER ⚔️",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Center Trophy / Crest
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(RoyalGoldLight, RoyalGoldDark)))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isHumanWin) "🏆" else "🎖️", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHumanWin) "Maharaja of the Court! 2X Winning Prize Awarded!" else "${winner.name} has claimed the Throne!",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Summary of Rank and Coins Earned
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F2653))
                            .border(1.dp, Color(0xFF28549C), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YOUR RANK", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "#$userRank ${if (isHumanWin) "👑 Winner" else ""}",
                                color = if (isHumanWin) RoyalGoldLight else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color(0xFF28549C))
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isHumanWin) "2X PRIZE WON" else "COINS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (coinsEarned >= 0) "+🪙 ${formatCompactCoins(coinsEarned)}" else "-🪙 ${formatCompactCoins(-coinsEarned)}",
                                color = if (coinsEarned >= 0) Color(0xFF00E676) else RubyRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Won Chest Box Section
                    if (isHumanWin && wonChestBoxName != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("won_chest_box_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D244D)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🎁", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = wonChestBoxName,
                                            color = RoyalGoldLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = if (isChestOpened) "Claimed bonus Coins & Gems! 💎" else "Unlocked with your 2X Victory!",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                if (!isChestOpened) {
                                    Button(
                                        onClick = {
                                            isChestOpened = true
                                            onOpenChestBox()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                            .testTag("open_chest_box_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("OPEN CHEST BOX 📦", color = RoyalNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1B3D2F))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("CHEST CLAIMED ✓", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Player Rankings Breakdown List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF091736))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sortedPlayers.forEachIndexed { idx, p ->
                            val rank = idx + 1
                            val isWinnerRow = rank == 1
                            val isUserRow = p.id == userPlayer?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isUserRow) Color(0xFF163773) else if (isWinnerRow) Color(0xFF1E2F52) else Color.Transparent
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when (rank) {
                                            1 -> "🥇"
                                            2 -> "🥈"
                                            3 -> "🥉"
                                            else -> "4️⃣"
                                        },
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = p.name + if (isUserRow) " (You)" else "",
                                        color = if (isUserRow) RoyalGoldLight else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isUserRow || isWinnerRow) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${p.finishedPawnsCount}/4 Home",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isWinnerRow) "+🪙${formatCompactCoins(totalPot)} (2X)" else "-🪙${formatCompactCoins(wager)}",
                                        color = if (isWinnerRow) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons: Lobby and Play Again
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReturnHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("victory_return_home_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF42A5F5))
                        ) {
                            Text("Lobby", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("victory_play_again_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Play Again", color = RoyalNavyDark, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoosterDiceButton(
    label: String,
    subtitle: String,
    cost: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) Color(0xFFFFB300) else Color(0xFF1976D2)
            )
            .border(
                1.5.dp,
                if (isSelected) Color.White else Color(0xFF64B5F6),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = if (label.length > 2) 9.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            if (cost != null) {
                Text(
                    text = "💵$cost",
                    color = Color(0xFF00E676),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

