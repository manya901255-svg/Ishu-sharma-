package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LudoColor
import com.example.model.MatchReplaySession
import com.example.model.ReplayMove
import com.example.model.getCrownAvatarEmoji
import kotlinx.coroutines.delay

@Composable
fun MatchReplayViewerDialog(
    replays: List<MatchReplaySession>,
    initialSelectedReplay: MatchReplaySession? = null,
    onDismiss: () -> Unit
) {
    var selectedReplay by remember {
        mutableStateOf(initialSelectedReplay ?: replays.firstOrNull())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("match_replay_viewer_dialog"),
            color = Color(0xFF07142A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
                            )
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Back/Close Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300))
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable {
                                if (selectedReplay != null && replays.size > 1) {
                                    selectedReplay = null
                                } else {
                                    onDismiss()
                                }
                            }
                            .testTag("match_replay_back_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A237E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎬 MATCH REPLAY THEATER",
                            color = Color(0xFFFFD54F),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (selectedReplay == null) "Select a match to watch summary" else selectedReplay!!.matchTitle,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (selectedReplay == null) {
                    // List of past matches to choose from
                    MatchSelectionList(
                        replays = replays,
                        onSelect = { selectedReplay = it }
                    )
                } else {
                    // Interactive Animated Replay Player
                    MatchReplayPlayerView(
                        session = selectedReplay!!,
                        onChangeMatch = { selectedReplay = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchSelectionList(
    replays: List<MatchReplaySession>,
    onSelect: (MatchReplaySession) -> Unit
) {
    if (replays.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📽️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No Recorded Matches Yet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Play matches in Online, AI, or Quick Rush to record replays!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Your Previous Matches (${replays.size})",
                    color = Color(0xFFFFD54F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(replays, key = { it.id }) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .clickable { onSelect(session) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10284D)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF29B6F6))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF071936))
                                    .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getCrownAvatarEmoji(session.winnerAvatar), fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = session.matchTitle,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Winner: ${session.winnerName} • ${session.durationFormatted}",
                                    color = Color(0xFF81D4FA),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Moves: ${session.totalMovesCount} • Pot: 🪙 ${session.wager * 2}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { onSelect(session) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Watch ▶", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchReplayPlayerView(
    session: MatchReplaySession,
    onChangeMatch: () -> Unit
) {
    var currentStepIndex by remember(session.id) { mutableIntStateOf(0) }
    var isPlaying by remember(session.id) { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    val moves = session.moves
    val currentMove = moves.getOrNull(currentStepIndex)

    // Playback loop
    LaunchedEffect(isPlaying, currentStepIndex, playbackSpeed) {
        if (isPlaying) {
            val delayMillis = (1200 / playbackSpeed).toLong().coerceAtLeast(300L)
            delay(delayMillis)
            if (currentStepIndex < moves.size - 1) {
                currentStepIndex++
            } else {
                isPlaying = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Match Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2648)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(getCrownAvatarEmoji(session.winnerAvatar), fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = session.matchTitle,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Champion: ${session.winnerName} 👑 • Pot: 🪙 ${session.wager * 2}",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onChangeMatch,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81D4FA)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Change", fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Board Action Stage Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF081933)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF29B6F6))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (currentMove != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Badge of move status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Step ${currentStepIndex + 1} of ${moves.size}",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            currentMove.isCapture -> Color(0xFFD50000)
                                            currentMove.isHome -> Color(0xFF00C853)
                                            else -> Color(0xFF1976D2)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when {
                                        currentMove.isCapture -> "⚔️ ROYAL CAPTURE!"
                                        currentMove.isHome -> "🏛️ PALACE FINISH!"
                                        else -> "🎲 MOVE STEP"
                                    },
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Player details & dice roll
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(getCrownAvatarEmoji(currentMove.playerAvatar), fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentMove.playerName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB300))
                                    .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${currentMove.diceRoll}",
                                    color = Color(0xFF212121),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentMove.description,
                            color = Color(0xFF81D4FA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text("No moves in this match", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Control Bar (Prev, Play/Pause, Next, Speed)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2648))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Slider
                Slider(
                    value = currentStepIndex.toFloat(),
                    onValueChange = {
                        isPlaying = false
                        currentStepIndex = it.toInt().coerceIn(0, (moves.size - 1).coerceAtLeast(0))
                    },
                    valueRange = 0f..(moves.size - 1).coerceAtLeast(0).toFloat(),
                    steps = if (moves.size > 1) moves.size - 2 else 0,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFD54F),
                        activeTrackColor = Color(0xFF00E676),
                        inactiveTrackColor = Color(0xFF1E3A5F)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Prev Move
                    IconButton(
                        onClick = {
                            isPlaying = false
                            if (currentStepIndex > 0) currentStepIndex--
                        },
                        enabled = currentStepIndex > 0
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                    }

                    // Play / Pause
                    Button(
                        onClick = {
                            if (currentStepIndex >= moves.size - 1) {
                                currentStepIndex = 0
                            }
                            isPlaying = !isPlaying
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) Color(0xFFFF9800) else Color(0xFF00C853)
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Next Move
                    IconButton(
                        onClick = {
                            isPlaying = false
                            if (currentStepIndex < moves.size - 1) currentStepIndex++
                        },
                        enabled = currentStepIndex < moves.size - 1
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                    }

                    // Speed Toggle (1x, 2x, 4x)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A3866))
                            .clickable {
                                playbackSpeed = when (playbackSpeed) {
                                    1f -> 2f
                                    2f -> 4f
                                    else -> 1f
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${playbackSpeed.toInt()}x Speed",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Move History Log (Scrollable timeline)
        Text(
            text = "Match Move Summary Timeline",
            color = Color(0xFFFFD54F),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(moves) { index, move ->
                val isSelected = index == currentStepIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF1E4C8A) else Color(0xFF0B1F3D))
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF1E3A5F),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            isPlaying = false
                            currentStepIndex = index
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${index + 1}",
                            color = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(getCrownAvatarEmoji(move.playerAvatar), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = move.playerName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🎲 ${move.diceRoll}",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = when {
                            move.isCapture -> "⚔️ Capture"
                            move.isHome -> "🏛️ Home"
                            else -> "Pawn #${move.pawnId + 1}"
                        },
                        color = when {
                            move.isCapture -> Color(0xFFFF5252)
                            move.isHome -> Color(0xFF00E676)
                            else -> Color(0xFF81D4FA)
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
