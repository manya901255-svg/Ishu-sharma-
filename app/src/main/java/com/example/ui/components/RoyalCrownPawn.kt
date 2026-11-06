package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Pawn
import com.example.ui.theme.*

/**
 * Modern 3D Conical Pin Token Pawn matching the classic mobile Ludo board aesthetic:
 * Spherical head with specular highlight, tapered neck, conical body, flared base,
 * drop shadow, and active pulsing glow when movable.
 */
@Composable
fun RoyalCrownPawn(
    pawn: Pawn,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    crownEmoji: String? = null,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pawnGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (pawn.isMovable) -4.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("pawn_${pawn.color}_${pawn.id}")
            .then(
                if (onClick != null && pawn.isMovable) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cy = h * 0.46f + bounceOffset

            // 1. Soft Oval Drop Shadow on ground
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66000000), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.88f),
                    radius = w * 0.44f
                ),
                topLeft = Offset(w * 0.12f, h * 0.76f),
                size = Size(w * 0.76f, h * 0.22f)
            )

            // 2. Movable Golden / White Active Turn Halo Ring
            if (pawn.isMovable) {
                drawCircle(
                    color = Color.White.copy(alpha = glowAlpha * 0.85f),
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, cy + h * 0.08f),
                    style = Stroke(width = 2.2.dp.toPx())
                )
                drawCircle(
                    color = RoyalGold.copy(alpha = glowAlpha * 0.4f),
                    radius = w * 0.54f,
                    center = Offset(w * 0.5f, cy + h * 0.08f)
                )
            }

            // 3. Flared Round Base Pedestal Rim
            val baseBrush = Brush.verticalGradient(
                colors = listOf(pawn.color.light, pawn.color.primary, pawn.color.dark),
                startY = cy + h * 0.20f,
                endY = cy + h * 0.42f
            )
            drawOval(
                brush = baseBrush,
                topLeft = Offset(w * 0.14f, cy + h * 0.22f),
                size = Size(w * 0.72f, h * 0.18f)
            )
            drawOval(
                color = pawn.color.dark,
                topLeft = Offset(w * 0.14f, cy + h * 0.22f),
                size = Size(w * 0.72f, h * 0.18f),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // 4. Conical Body (Tapering from wide base up to collar)
            val bodyPath = Path().apply {
                moveTo(w * 0.22f, cy + h * 0.24f)
                cubicTo(w * 0.28f, cy + h * 0.05f, w * 0.36f, cy - h * 0.06f, w * 0.38f, cy - h * 0.12f)
                lineTo(w * 0.62f, cy - h * 0.12f)
                cubicTo(w * 0.64f, cy - h * 0.06f, w * 0.72f, cy + h * 0.05f, w * 0.78f, cy + h * 0.24f)
                close()
            }
            val bodyBrush = Brush.horizontalGradient(
                colors = listOf(pawn.color.light, pawn.color.primary, pawn.color.dark),
                startX = w * 0.2f,
                endX = w * 0.8f
            )
            drawPath(path = bodyPath, brush = bodyBrush, style = Fill)

            // 5. Collar Ring
            drawOval(
                color = pawn.color.light,
                topLeft = Offset(w * 0.36f, cy - h * 0.15f),
                size = Size(w * 0.28f, h * 0.07f)
            )

            // 6. Spherical Head (Round 3D sphere)
            val headRadius = w * 0.24f
            val headCenter = Offset(w * 0.5f, cy - h * 0.22f)
            val headBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    pawn.color.light,
                    pawn.color.primary,
                    pawn.color.dark
                ),
                center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
                radius = headRadius * 1.3f
            )
            drawCircle(
                brush = headBrush,
                radius = headRadius,
                center = headCenter
            )
            drawCircle(
                color = pawn.color.dark.copy(alpha = 0.5f),
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = 0.6.dp.toPx())
            )

            // 7. Glossy Specular Light Reflection Arc
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = headRadius * 0.22f,
                center = Offset(headCenter.x - headRadius * 0.32f, headCenter.y - headRadius * 0.35f)
            )
        }

        // 8. Custom Crown Avatar Topper (when equipped)
        if (crownEmoji != null) {
            Text(
                text = crownEmoji,
                fontSize = (size.value * 0.38f).sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-size * 0.14f) + (bounceOffset.dp * 0.5f))
            )
        }

        // 9. Floating 'X Left' Step Counter Badge (Authentic Ludo Club feature shown in video)
        if (pawn.isMovable && (pawn.state == com.example.model.PawnState.ON_BOARD || pawn.state == com.example.model.PawnState.IN_HOME_STRETCH)) {
            val stepsLeft = when (pawn.state) {
                com.example.model.PawnState.ON_BOARD -> {
                    com.example.engine.LudoEngine.distanceToHomeEntrance(pawn.color, pawn.trackPosition) + 6
                }
                com.example.model.PawnState.IN_HOME_STRETCH -> (6 - pawn.homeStep).coerceAtLeast(1)
                else -> 0
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-size * 0.38f) + (bounceOffset.dp * 0.7f))
                    .shadow(4.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE53935))
                    .border(0.8.dp, Color.White, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepsLeft Left",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    lineHeight = 8.sp
                )
            }
        }
    }
}
