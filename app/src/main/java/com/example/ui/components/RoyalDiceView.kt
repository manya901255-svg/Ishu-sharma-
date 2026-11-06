package com.example.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

/**
 * High-fidelity 2D Royal Dice View with animated tumble physics,
 * ivory/gold surface textures, embossed pips, and glowing turn indicator.
 */
@Composable
fun RoyalDiceView(
    value: Int,
    isRolling: Boolean,
    canRoll: Boolean,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier,
    diceSize: Dp = 56.dp,
    playerColor: LudoColor = LudoColor.BLUE
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_wobble")

    val rollingRotation by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble_rotation"
    )

    val rollingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble_scale"
    )

    val bounceAnim = remember { Animatable(1f) }
    LaunchedEffect(value) {
        bounceAnim.animateTo(1.18f, animationSpec = tween(90))
        bounceAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f))
    }

    val currentRotation = if (isRolling) rollingRotation else 0f
    val currentScale = if (isRolling) rollingScale else bounceAnim.value

    Box(
        modifier = modifier
            .size(diceSize)
            .scale(currentScale)
            .rotate(currentRotation)
            .shadow(if (canRoll) 8.dp else 4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFF176))
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = canRoll && !isRolling) {
                onRollClick()
            }
            .testTag("royal_dice_view"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(7.dp)) {
            val pipRadius = size.width * 0.11f
            val left = size.width * 0.22f
            val center = size.width * 0.5f
            val right = size.width * 0.78f

            val top = size.height * 0.22f
            val middle = size.height * 0.5f
            val bottom = size.height * 0.78f

            val pipColor = if (value == 6) Color(0xFFD32F2F) else Color(0xFF1A237E)

            fun drawPip(x: Float, y: Float) {
                drawCircle(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = pipRadius + 1f,
                    center = Offset(x, y + 1.5f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(pipColor.copy(alpha = 0.85f), pipColor)
                    ),
                    radius = pipRadius,
                    center = Offset(x, y)
                )
            }

            when (value.coerceIn(1, 6)) {
                1 -> drawPip(center, middle)
                2 -> {
                    drawPip(left, top)
                    drawPip(right, bottom)
                }
                3 -> {
                    drawPip(left, top)
                    drawPip(center, middle)
                    drawPip(right, bottom)
                }
                4 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                5 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(center, middle)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                6 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, middle)
                    drawPip(right, middle)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
            }
        }

        // Shimmer shine reflection in top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .size(diceSize * 0.25f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.6f))
        )
    }
}
