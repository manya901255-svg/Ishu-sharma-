package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GameMode
import com.example.model.LudoColor
import com.example.ui.theme.*

@Composable
fun OnlineModeLobbyDialog(
    userCoins: Long,
    initialColor: LudoColor = LudoColor.BLUE,
    initialMode: GameMode = GameMode.ONLINE_MULTIPLAYER,
    titleText: String = "Online",
    onDismiss: () -> Unit,
    onStartGame: (playerCount: Int, wager: Long, chosenColor: LudoColor, mode: GameMode) -> Unit
) {
    var playerCount by remember { mutableIntStateOf(2) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedSubMode by remember { mutableStateOf("Classic") }
    var showNotEnoughCoinsAlert by remember { mutableStateOf(false) }

    // Wager tiers (500 to 250,000 coins)
    val wagers = listOf(500L, 1000L, 2500L, 5000L, 10000L, 25000L, 50000L, 100000L, 250000L)
    var wagerIndex by remember { mutableIntStateOf(6) } // default 50,000 matching screenshot
    val currentWager = wagers.getOrElse(wagerIndex) { 50000L }

    val winningCoins = when (playerCount) {
        2 -> (currentWager * 1.9f).toLong()
        3 -> (currentWager * 2.7f).toLong()
        else -> (currentWager * 3.6f).toLong()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lobby_anim")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_float"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5BA7FF),
                            Color(0xFF82C4FF),
                            Color(0xFFA5DBFF)
                        )
                    )
                )
                .systemBarsPadding()
                .testTag("online_mode_3d_entry_screen")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(Color.White.copy(alpha = 0.55f), radius = 90.dp.toPx(), center = Offset(30.dp.toPx(), 40.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.45f), radius = 120.dp.toPx(), center = Offset(w * 0.5f, 15.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.55f), radius = 80.dp.toPx(), center = Offset(w - 30.dp.toPx(), 45.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.40f), radius = 140.dp.toPx(), center = Offset(40.dp.toPx(), h - 20.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.50f), radius = 160.dp.toPx(), center = Offset(w * 0.55f, h + 10.dp.toPx()))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFEBF5FF),
                                Color(0xFFD6ECFF),
                                Color(0xFFC7E3FF)
                            )
                        )
                    )
                    .border(3.5.dp, Color.White, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. TOP HEADER: Back Arrow, 3D "Online", Bell
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFFE65100))
                                    )
                                )
                                .border(2.5.dp, Color.White, CircleShape)
                                .clickable { onDismiss() }
                                .testTag("lobby_3d_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier.offset(y = cloudOffset.dp * 0.3f),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(width = 160.dp, height = 52.dp)) {
                                val w = size.width
                                val h = size.height
                                drawCircle(Color.White.copy(alpha = 0.95f), radius = 22.dp.toPx(), center = Offset(30.dp.toPx(), h / 2f))
                                drawCircle(Color.White.copy(alpha = 0.95f), radius = 28.dp.toPx(), center = Offset(w / 2f, h / 2f - 4.dp.toPx()))
                                drawCircle(Color.White.copy(alpha = 0.95f), radius = 22.dp.toPx(), center = Offset(w - 30.dp.toPx(), h / 2f))
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.95f),
                                    topLeft = Offset(20.dp.toPx(), h / 2f - 12.dp.toPx()),
                                    size = Size(w - 40.dp.toPx(), 26.dp.toPx()),
                                    cornerRadius = CornerRadius(14.dp.toPx())
                                )
                            }

                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = titleText,
                                    color = Color(0xFF0D47A1),
                                    fontSize = 29.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.offset(y = 3.dp)
                                )
                                Text(
                                    text = titleText,
                                    color = Color(0xFF0288D1),
                                    fontSize = 29.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.offset(y = 1.5.dp)
                                )
                                Text(
                                    text = titleText,
                                    color = Color(0xFF29B6F6),
                                    fontSize = 29.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFF8F00))
                                    )
                                )
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 22.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 3D PILL TOGGLE: 2 PLAYERS vs 3 PLAYERS vs 4 PLAYERS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 2 PLAYERS
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { playerCount = 2 }
                                .testTag("select_2_players_3d")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.offset(y = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFE0B2))
                                            .border(1.5.dp, Color(0xFF5D4037), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👦", fontSize = 14.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0288D1))
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🐱", fontSize = 15.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .shadow(
                                            elevation = if (playerCount == 2) 8.dp else 3.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            if (playerCount == 2) {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF29B6F6), Color(0xFF0288D1), Color(0xFF01579B))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF90CAF9), Color(0xFF42A5F5), Color(0xFF1976D2))
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (playerCount == 2) 2.5.dp else 1.2.dp,
                                            color = if (playerCount == 2) Color.White else Color(0xFFBBDEFB),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "2 PLAYERS",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            if (playerCount == 2) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .shadow(4.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00C853)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3 PLAYERS
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { playerCount = 3 }
                                .testTag("select_3_players_3d")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.offset(y = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFE0B2))
                                            .border(1.5.dp, Color(0xFF5D4037), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👦", fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE1BEE7))
                                            .border(1.5.dp, Color(0xFF7B1FA2), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👧", fontSize = 14.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF80CBC4))
                                            .border(1.5.dp, Color(0xFF004D40), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🐱", fontSize = 13.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .shadow(
                                            elevation = if (playerCount == 3) 8.dp else 3.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            if (playerCount == 3) {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF26A69A), Color(0xFF00897B), Color(0xFF004D40))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF80CBC4), Color(0xFF4DB6AC), Color(0xFF00897B))
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (playerCount == 3) 2.5.dp else 1.2.dp,
                                            color = if (playerCount == 3) Color.White else Color(0xFFB2DFDB),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "3 PLAYERS",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            if (playerCount == 3) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .shadow(4.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00C853)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4 PLAYERS
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { playerCount = 4 }
                                .testTag("select_4_players_3d")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.offset(y = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFCCBC))
                                            .border(1.5.dp, Color(0xFFD84315), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🧔", fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF8BBD0))
                                            .border(1.5.dp, Color(0xFFC2185B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👧", fontSize = 14.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF9C4))
                                            .border(1.5.dp, Color(0xFFF57F17), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🧒", fontSize = 13.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .shadow(
                                            elevation = if (playerCount == 4) 8.dp else 3.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            if (playerCount == 4) {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFF8F00))
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFFFFE082), Color(0xFFFFCA28), Color(0xFFFFA000))
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (playerCount == 4) 2.5.dp else 1.2.dp,
                                            color = if (playerCount == 4) Color.White else Color(0xFFFFE082),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "4 PLAYERS",
                                        color = if (playerCount == 4) Color(0xFF3E2723) else Color(0xFF5D4037),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            if (playerCount == 4) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .shadow(4.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00C853)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 3D "MODES" BOARD WITH 4 TOKENS & FIGURINES
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFF8E7), Color(0xFFF7E7CD), Color(0xFFEED3B0))
                                )
                            )
                            .border(2.5.dp, Color(0xFFD7B387), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, end = 4.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFE0B2))
                                        .border(1.5.dp, Color(0xFFD84315), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⏱️", fontSize = 18.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "MODES",
                                        color = Color(0xFF6D3B16),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0288D1))
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("i", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0288D1))
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("😸", fontSize = 18.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val modeItems = listOf(
                                    Triple("Classic", "Classic\nMode", "token_classic"),
                                    Triple("Speed", "Speed\nDash", "token_speed"),
                                    Triple("Bolt", "Bolt\nRush", "token_bolt"),
                                    Triple("Sprint", "Ludo\nSprint", "token_sprint")
                                )

                                modeItems.forEach { (subKey, label, tokenType) ->
                                    val isSelected = selectedSubMode == subKey
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { selectedSubMode = subKey }
                                            .padding(2.dp)
                                            .testTag("mode_token_$subKey")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (isSelected) 60.dp else 54.dp)
                                                .shadow(
                                                    elevation = if (isSelected) 8.dp else 3.dp,
                                                    shape = CircleShape
                                                )
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) {
                                                        Brush.radialGradient(
                                                            listOf(Color(0xFFFFECB3), Color(0xFFFFB74D), Color(0xFFE65100))
                                                        )
                                                    } else {
                                                        Brush.radialGradient(
                                                            listOf(Color(0xFFFBE9E7), Color(0xFFD7CCC8), Color(0xFFA1887F))
                                                        )
                                                    }
                                                )
                                                .border(
                                                    width = if (isSelected) 3.dp else 2.dp,
                                                    color = if (isSelected) Color(0xFFFFB300) else Color(0xFF8D6E63),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (tokenType) {
                                                "token_classic" -> Text("♟️", fontSize = 22.sp)
                                                "token_speed" -> Text("👟", fontSize = 24.sp)
                                                "token_bolt" -> Text("⚡", fontSize = 24.sp)
                                                else -> Text("🚪", fontSize = 24.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = label,
                                            color = if (isSelected) Color(0xFFBF360C) else Color(0xFF5D4037),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val pawnThemes = listOf(
                                    Pair(LudoColor.BLUE, "Doraemon" to "🐱"),
                                    Pair(LudoColor.RED, "Shizuka" to "🎀"),
                                    Pair(LudoColor.GREEN, "Gian" to "👑"),
                                    Pair(LudoColor.YELLOW, "Nobita" to "🛟")
                                )

                                pawnThemes.forEach { (colorKey, details) ->
                                    val (_, emoji) = details
                                    val isColorSelected = selectedColor == colorKey
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { selectedColor = colorKey }
                                            .testTag("entry_pawn_color_${colorKey.name.lowercase()}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (isColorSelected) 38.dp else 32.dp)
                                                .shadow(if (isColorSelected) 6.dp else 2.dp, CircleShape)
                                                .clip(CircleShape)
                                                .background(colorKey.primary)
                                                .border(
                                                    width = if (isColorSelected) 2.5.dp else 1.dp,
                                                    color = if (isColorSelected) Color.White else colorKey.dark,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(emoji, fontSize = if (isColorSelected) 18.sp else 15.sp)
                                        }

                                        if (isColorSelected) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .height(3.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color(0xFFE65100))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 3D WOODEN FRAMED "REWARDS" BOARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8D4E19), Color(0xFF63320B), Color(0xFF4E2606))
                                )
                            )
                            .border(3.dp, Color(0xFFB57039), RoundedCornerShape(20.dp))
                            .padding(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF9EAD2))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFB4682A), Color(0xFF8A4613))
                                        )
                                    )
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🚁👧", fontSize = 14.sp)
                                Text(
                                    text = "REWARDS",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text("🧔🌴", fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .shadow(4.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color(0xFFFF6F00))
                                            )
                                        )
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🏆", fontSize = 24.sp)
                                        Text(
                                            "1st",
                                            color = Color(0xFF4E342E),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 30.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatCompactCoins(winningCoins),
                                        color = Color(0xFF0D47A1),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1DEBF))
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Pair("☁️", "x4"),
                                    Pair("⭐", "x4K"),
                                    Pair("🔥", "x1"),
                                    Pair("🌴", "x1"),
                                    Pair("🎟️", "x1")
                                ).forEach { (icon, count) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(icon, fontSize = 16.sp)
                                        Text(
                                            text = count,
                                            color = Color(0xFF4E342E),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. CONVEYOR BELT WITH 5 GOLDEN DORAYAKI PANCAKES
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF37474F), Color(0xFF263238), Color(0xFF102027))
                                )
                            )
                            .border(1.5.dp, Color(0xFF78909C), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 34.dp, height = 20.dp)
                                        .shadow(3.dp, RoundedCornerShape(10.dp))
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFD78235), Color(0xFFA55314), Color(0xFF5D2802))
                                            )
                                        )
                                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(2.dp)
                                            .background(Color(0xFF3E1E04))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. 3D METALLIC ENTRY CONSOLE: "Entry: 🪙 50,000"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(46.dp)
                            .shadow(6.dp, RoundedCornerShape(23.dp))
                            .clip(RoundedCornerShape(23.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E3A70), Color(0xFF0E2554), Color(0xFF081636))
                                )
                            )
                            .border(2.dp, Color(0xFF64B5F6), RoundedCornerShape(23.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF90A4AE))
                                    .border(1.dp, Color.White, CircleShape)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Entry: ",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("🪙", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCompactCoins(currentWager),
                                    color = Color(0xFFFFD54F),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF90A4AE))
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    if (showNotEnoughCoinsAlert) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Aapke paas 🪙 " + formatCompactCoins(userCoins) + " coins hain. Wager kam karein ya free spin lein!",
                            color = Color(0xFFD50000),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7. BOTTOM CONTROLS: Left [+], Center [GAME START!], Right [-]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF29B6F6), Color(0xFF0288D1), Color(0xFF01579B))
                                    )
                                )
                                .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                                .clickable(enabled = wagerIndex < wagers.lastIndex) {
                                    if (wagerIndex < wagers.lastIndex) {
                                        wagerIndex++
                                        showNotEnoughCoinsAlert = false
                                    }
                                }
                                .testTag("lobby_wager_plus_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase Entry",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .shadow(10.dp, RoundedCornerShape(32.dp))
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF76FF03),
                                            Color(0xFF00E676),
                                            Color(0xFF00C853),
                                            Color(0xFF00701A)
                                        )
                                    )
                                )
                                .border(3.dp, Color.White, RoundedCornerShape(32.dp))
                                .clickable {
                                    if (userCoins < currentWager) {
                                        showNotEnoughCoinsAlert = true
                                    } else {
                                        val mappedMode = when (selectedSubMode) {
                                            "Speed" -> GameMode.QUICK_RUSH
                                            "Bolt" -> GameMode.ONLINE_MULTIPLAYER
                                            "Sprint" -> GameMode.QUICK_RUSH
                                            else -> initialMode
                                        }
                                        onStartGame(playerCount, currentWager, selectedColor, mappedMode)
                                    }
                                }
                                .testTag("lobby_3d_game_start_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GAME",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color(0xFF004D20),
                                            offset = Offset(0f, 3f),
                                            blurRadius = 4f
                                        )
                                    )
                                )
                                Text(
                                    text = "START!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color(0xFF004D20),
                                            offset = Offset(0f, 3f),
                                            blurRadius = 4f
                                        )
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFF5252), Color(0xFFE53935), Color(0xFFB71C1C))
                                    )
                                )
                                .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                                .clickable(enabled = wagerIndex > 0) {
                                    if (wagerIndex > 0) {
                                        wagerIndex--
                                        showNotEnoughCoinsAlert = false
                                    }
                                }
                                .testTag("lobby_wager_minus_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(5.dp)
                                    .background(Color.White, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
