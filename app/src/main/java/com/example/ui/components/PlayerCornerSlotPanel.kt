package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor
import com.example.model.PawnState
import com.example.model.Player
import com.example.model.getCrownAvatarEmoji
import com.example.ui.theme.*

/**
 * PlayerCornerSlotPanel: Clean designated slot panel for players ('You', 'Computer 2',
 * 'Computer 3', 'Computer 4') with player avatar, score/status indicators (pawns in base,
 * on board, and home), and an interactive 3D-style animated dice rolling mechanism
 * located right near the board corners.
 */
@Composable
fun PlayerCornerSlotPanel(
    player: Player?,
    slotLabel: String,
    isActiveTurn: Boolean,
    isHuman: Boolean,
    isRolling: Boolean,
    currentDiceValue: Int,
    timeRemainingSec: Int,
    consecutiveSixes: Int,
    canRoll: Boolean,
    onRollClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onLightningClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    floatingEmoji: String? = null,
    alignRight: Boolean = false
) {
    if (player == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "slotTurn")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val baseCount = player.pawns.count { it.state == PawnState.IN_BASE }
    val boardCount = player.pawns.count { it.state == PawnState.ON_BOARD || it.state == PawnState.IN_HOME_STRETCH }
    val homeCount = player.pawns.count { it.state == PawnState.FINISHED }
    val individualDiceValue = if (isActiveTurn) currentDiceValue else player.lastDiceValue

    Row(
        modifier = modifier
            .testTag("player_slot_${player.color.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start
    ) {
        if (!alignRight) {
            // Left-aligned: Avatar & Info first, then 3D Dice, then Lightning Power-Up Card (as seen in video)
            PlayerInfoBox(
                player = player,
                slotLabel = slotLabel,
                isActiveTurn = isActiveTurn,
                isHuman = isHuman,
                pulseGlow = pulseGlow,
                timeRemainingSec = timeRemainingSec,
                baseCount = baseCount,
                boardCount = boardCount,
                homeCount = homeCount,
                onProfileClick = onProfileClick,
                floatingEmoji = floatingEmoji
            )

            Spacer(modifier = Modifier.width(6.dp))

            // 3D Corner Dice Rolling Mechanism with Individual Dice Value
            CornerDiceWidget(
                diceValue = individualDiceValue,
                isRolling = isRolling && isActiveTurn,
                playerColor = player.color,
                isActiveTurn = isActiveTurn,
                canRoll = canRoll && isHuman,
                consecutiveSixes = consecutiveSixes,
                timeRemainingSec = timeRemainingSec,
                onRollClick = onRollClick
            )

            if (isHuman) {
                Spacer(modifier = Modifier.width(6.dp))

                // Lightning Power-Up Card Widget (15 left, ⚡, 💵 2, ?)
                LightningPowerUpCardWidget(
                    countLeft = 15,
                    costCash = 2,
                    onClick = onLightningClick
                )
            }
        } else {
            // Right-aligned (Opponent): 3D Dice first, then Avatar & Info with circular timer ring
            CornerDiceWidget(
                diceValue = individualDiceValue,
                isRolling = isRolling && isActiveTurn,
                playerColor = player.color,
                isActiveTurn = isActiveTurn,
                canRoll = canRoll && isHuman,
                consecutiveSixes = consecutiveSixes,
                timeRemainingSec = timeRemainingSec,
                onRollClick = onRollClick
            )

            Spacer(modifier = Modifier.width(6.dp))

            PlayerInfoBox(
                player = player,
                slotLabel = slotLabel,
                isActiveTurn = isActiveTurn,
                isHuman = isHuman,
                pulseGlow = pulseGlow,
                timeRemainingSec = timeRemainingSec,
                baseCount = baseCount,
                boardCount = boardCount,
                homeCount = homeCount,
                onProfileClick = onProfileClick,
                floatingEmoji = floatingEmoji
            )
        }
    }
}

@Composable
private fun PlayerInfoBox(
    player: Player,
    slotLabel: String,
    isActiveTurn: Boolean,
    isHuman: Boolean,
    pulseGlow: Float,
    timeRemainingSec: Int,
    baseCount: Int,
    boardCount: Int,
    homeCount: Int,
    onProfileClick: () -> Unit,
    floatingEmoji: String?
) {
    Box(
        modifier = Modifier
            .shadow(if (isActiveTurn) 8.dp else 3.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isActiveTurn) {
                    Brush.verticalGradient(listOf(Color(0xFF0F3B7A), Color(0xFF08224D)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF0A1E40), Color(0xFF06142C)))
                }
            )
            .border(
                width = if (isActiveTurn) 2.dp else 1.2.dp,
                brush = if (isActiveTurn) {
                    Brush.linearGradient(listOf(player.color.primary, Color.White, player.color.dark))
                } else {
                    Brush.linearGradient(listOf(player.color.primary.copy(alpha = 0.6f), Color(0xFF204279)))
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = isHuman) { onProfileClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Avatar with Corner Turn/Level Badge and Circular Countdown Timer Ring
            Box(contentAlignment = Alignment.Center) {
                // Circular Countdown Timer Ring (Exact match to video gameplay)
                if (isActiveTurn) {
                    val progress = (timeRemainingSec / 25f).coerceIn(0f, 1f)
                    Canvas(modifier = Modifier.size(46.dp)) {
                        drawArc(
                            color = if (progress > 0.35f) Color(0xFF00E676) else Color(0xFFE91E63),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(player.color.dark)
                        .border(1.5.dp, if (isActiveTurn) Color.White else player.color.light, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!player.customPhotoUri.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = player.customPhotoUri,
                            contentDescription = "Player photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(9.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = getCrownAvatarEmoji(player.avatar),
                            fontSize = 20.sp
                        )
                    }
                }

                // Turn Pulse Ring
                if (isActiveTurn) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.5.dp, Color(0xFF00E676).copy(alpha = pulseGlow), RoundedCornerShape(12.dp))
                    )
                }

                // Voice Speaking Indicator (Audio waves around avatar)
                if (player.isSpeaking) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(2.dp, Color(0xFF00E5FF).copy(alpha = pulseGlow), RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-10).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E5FF))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("${getCrownAvatarEmoji(player.avatar)} 🗣️ ılılı", fontSize = 7.5.sp, color = Color(0xFF001E3D), fontWeight = FontWeight.Black)
                    }
                }

                // Star Level Badge (Matching video)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(if (isHuman) Color(0xFFFFB300) else Color(0xFF7B1FA2))
                        .border(1.dp, Color.White, CircleShape)
                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = "⭐${player.level}",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Gift 🎁 badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63))
                        .border(1.dp, Color.White, CircleShape)
                        .padding(2.dp)
                ) {
                    Text("🎁", fontSize = 8.sp)
                }

                // Floating In-game Reaction Emoji
                if (floatingEmoji != null) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, RoyalGold, CircleShape)
                            .padding(2.dp)
                    ) {
                        Text(floatingEmoji, fontSize = 16.sp)
                    }
                }
            }

            // Name & Status Indicators
            Column(
                modifier = Modifier.widthIn(min = 72.dp, max = 100.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Designated Slot Name ('You', 'Computer 2', etc.) with Ancient Kingdom Avatar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${getCrownAvatarEmoji(player.avatar)} $slotLabel",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isHuman) "🇮🇳" else "🤖",
                        fontSize = 9.sp
                    )
                }

                // Active Turn Status Indicator
                if (isActiveTurn) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isHuman) "YOUR TURN" else "ROLLING...",
                            color = Color(0xFF00E676),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    // Pawn Counter Pills: Base / Board / Home
                    Row(
                        modifier = Modifier.padding(top = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PawnStatusBadge(icon = "🏠", count = baseCount, color = player.color.primary)
                        PawnStatusBadge(icon = "🎯", count = boardCount, color = Color(0xFF42A5F5))
                        PawnStatusBadge(icon = "👑", count = homeCount, color = Color(0xFFFFD54F))
                    }
                }
            }
        }
    }
}

@Composable
private fun PawnStatusBadge(
    icon: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF06142C))
            .padding(horizontal = 2.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 7.sp)
        Text(
            text = "$count",
            color = if (count > 0) color else TextMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 3D-Style Animated Dice Rolling Mechanism near the board corner
 */
@Composable
private fun CornerDiceWidget(
    diceValue: Int,
    isRolling: Boolean,
    playerColor: LudoColor,
    isActiveTurn: Boolean,
    canRoll: Boolean,
    consecutiveSixes: Int,
    timeRemainingSec: Int,
    onRollClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dicePulse")
    val diceGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .testTag("corner_dice_${playerColor.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Authentic Ivory/Gold beveled tray matching screenshot
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFECB3))
                    )
                )
                .border(
                    width = if (canRoll) 2.dp else 1.2.dp,
                    color = if (canRoll) RoyalGold.copy(alpha = diceGlowAlpha) else Color(0xFFD4AF37),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 4 Corner Gold Rivet Dots matching screenshot
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                val dotRadius = 1.4.dp.toPx()
                val dotColor = Color(0xFFB8860B)
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(dotRadius + 1f, dotRadius + 1f))
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(size.width - dotRadius - 1f, dotRadius + 1f))
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(dotRadius + 1f, size.height - dotRadius - 1f))
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(size.width - dotRadius - 1f, size.height - dotRadius - 1f))
            }

            // Realistic 3D Dice with tumble and physics
            Realistic3DDice(
                diceValue = diceValue,
                isRolling = isRolling,
                playerColor = playerColor,
                isTurnActive = isActiveTurn,
                diceSize = 34.dp,
                onRollClick = {
                    if (canRoll) {
                        onRollClick()
                    }
                }
            )
        }

        // Streak indicator (e.g. rolled a 6)
        if (consecutiveSixes > 0 && !isRolling && isActiveTurn) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text("🔥", fontSize = 8.sp)
            }
        }

        // Animated pointing finger when ready to roll (as seen in gameplay video)
        if (canRoll && !isRolling) {
            val tapTransition = rememberInfiniteTransition(label = "diceTap")
            val tapOffsetY by tapTransition.animateFloat(
                initialValue = -18f,
                targetValue = -12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "tapY"
            )
            Text(
                text = "👆",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = tapOffsetY.dp)
            )
        }
    }
}

/**
 * Authentic Lightning Power-Up Card Widget matching the gameplay recording:
 * Displays '15 left', lightning bolt '⚡', and cash cost '💵 2' with '?' tooltip.
 */
@Composable
fun LightningPowerUpCardWidget(
    countLeft: Int = 15,
    costCash: Int = 2,
    onClick: () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }

    LaunchedEffect(showTooltip) {
        if (showTooltip) {
            kotlinx.coroutines.delay(2800)
            showTooltip = false
        }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.testTag("lightning_powerup_card")
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(58.dp)
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0F265C))
                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                .clickable {
                    showTooltip = true
                    onClick()
                }
                .padding(vertical = 3.dp, horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Top: X left
                Text(
                    text = "$countLeft left",
                    color = Color.White,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )

                // Center: Lightning icon
                Text(
                    text = "⚡",
                    fontSize = 18.sp
                )

                // Bottom: Cash cost
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💵", fontSize = 8.sp)
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "$costCash",
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("?", color = Color.LightGray, fontSize = 7.5.sp)
                }
            }
        }

        if (showTooltip) {
            Box(
                modifier = Modifier
                    .offset(y = (-38).dp, x = 65.dp)
                    .shadow(8.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E3A8A))
                    .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Using Power-ups may kill an opponent",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
