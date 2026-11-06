package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class CelebrationType {
    SIX_ROLLED,
    PAWN_CAPTURED,
    PAWN_FINISHED,
    MATCH_WON
}

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    var angularVelocity: Float,
    var size: Float,
    var color: Color,
    var shapeType: Int, // 0: rect ribbon, 1: star, 2: gold coin circle
    var alpha: Float = 1.0f
)

/**
 * RegalCelebrationOverlay: Renders GPU-accelerated celebratory particle systems
 * for turn completion (rolling a 6, pawn captures, victory palace entry) and match wins.
 */
@Composable
fun RegalCelebrationOverlay(
    celebrationType: CelebrationType?,
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    if (celebrationType == null) return

    when (celebrationType) {
        CelebrationType.SIX_ROLLED,
        CelebrationType.PAWN_CAPTURED,
        CelebrationType.PAWN_FINISHED -> {
            TurnMilestoneCelebration(
                type = celebrationType,
                modifier = modifier,
                onDismiss = onAnimationEnd
            )
        }
        CelebrationType.MATCH_WON -> {
            MatchWonFullConfetti(
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TurnMilestoneCelebration(
    type: CelebrationType,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(type) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
        )
        onDismiss()
    }

    val t = progress.value
    val particles = remember(type) {
        List(40) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 450f + 150f
            ConfettiParticle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 120f,
                angle = Random.nextFloat() * 360f,
                angularVelocity = Random.nextFloat() * 400f - 200f,
                size = Random.nextFloat() * 12f + 8f,
                color = listOf(
                    RoyalGold,
                    RoyalGoldLight,
                    RubyRed,
                    Color(0xFF00E676),
                    Color(0xFF00B0FF),
                    Color(0xFFFFD54F)
                ).random(),
                shapeType = Random.nextInt(3)
            )
        }
    }

    val labelText = when (type) {
        CelebrationType.SIX_ROLLED -> "👑 ROYAL 6! ROLL AGAIN! 🎲"
        CelebrationType.PAWN_CAPTURED -> "⚔️ ROYAL CAPTURE! +1 TURN! 🛡️"
        CelebrationType.PAWN_FINISHED -> "🏰 PAWN REACHED PALACE! 👑"
        CelebrationType.MATCH_WON -> "🏆 ROYAL TRIUMPH! 👑"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("turn_milestone_celebration"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Shockwave Expanding Halo Ring
            val ringRadius = (t * size.minDimension * 0.55f)
            val ringAlpha = (1f - t).coerceIn(0f, 1f)
            drawCircle(
                color = RoyalGold.copy(alpha = ringAlpha * 0.7f),
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = (4f * (1f - t)).dp.toPx())
            )

            // Sparkle Particles with gravity & rotation
            particles.forEach { p ->
                val px = cx + p.vx * t
                val py = cy + p.vy * t + 0.5f * 600f * (t * t)
                val pAlpha = (1f - t).coerceIn(0f, 1f)
                val currentAngle = p.angle + p.angularVelocity * t

                rotate(degrees = currentAngle, pivot = Offset(px, py)) {
                    when (p.shapeType) {
                        0 -> {
                            // Confetti rectangle ribbon
                            drawRect(
                                color = p.color.copy(alpha = pAlpha),
                                topLeft = Offset(px - p.size / 2f, py - p.size / 4f),
                                size = Size(p.size, p.size * 0.5f)
                            )
                        }
                        1 -> {
                            // Star sparkle
                            drawCelebrationStar(px, py, p.size * 0.7f, p.color.copy(alpha = pAlpha))
                        }
                        else -> {
                            // Gold Coin particle
                            drawCircle(
                                color = RoyalGold.copy(alpha = pAlpha),
                                radius = p.size * 0.45f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }
        }

        // Floating Celebratory Banner
        if (t < 0.95f) {
            val bannerScale = if (t < 0.2f) (t / 0.2f) else if (t > 0.7f) (1f - (t - 0.7f) / 0.25f) else 1f
            val bannerAlpha = if (t < 0.15f) (t / 0.15f) else if (t > 0.75f) (1f - (t - 0.75f) / 0.2f) else 1f

            Box(
                modifier = Modifier
                    .offset(y = (-40).dp)
                    .graphicsLayerAlpha(bannerAlpha)
                    .graphicsLayerScale(bannerScale)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0D47A1).copy(alpha = 0.95f),
                                Color(0xFF1565C0).copy(alpha = 0.95f),
                                Color(0xFF0D47A1).copy(alpha = 0.95f)
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)),
                        androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = labelText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
fun MatchWonFullConfetti(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "victoryConfetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiTime"
    )

    val confettiList = remember {
        List(80) { i ->
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f, // start above screen
                vx = Random.nextFloat() * 160f - 80f,
                vy = Random.nextFloat() * 220f + 160f,
                angle = Random.nextFloat() * 360f,
                angularVelocity = Random.nextFloat() * 280f - 140f,
                size = Random.nextFloat() * 14f + 8f,
                color = listOf(
                    RoyalGold,
                    RoyalGoldLight,
                    Color(0xFFFF1744),
                    Color(0xFF00E676),
                    Color(0xFF2979FF),
                    Color(0xFFFFD54F),
                    Color(0xFFE040FB)
                ).random(),
                shapeType = i % 3
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize().testTag("match_won_confetti")) {
        val w = size.width
        val h = size.height

        confettiList.forEachIndexed { idx, p ->
            val elapsed = (time * 0.8f + idx * 0.12f)
            val currentY = (p.y * h + (p.vy * elapsed)) % (h + 80f) - 40f
            val wobbleX = sin(elapsed * 2.5f + idx) * 35f
            val currentX = (p.x * w + p.vx * elapsed * 0.3f + wobbleX) % w
            val currentAngle = p.angle + p.angularVelocity * elapsed

            rotate(degrees = currentAngle, pivot = Offset(currentX, currentY)) {
                when (p.shapeType) {
                    0 -> {
                        // Ribbon rectangle
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - p.size / 2f, currentY - p.size / 4f),
                            size = Size(p.size, p.size * 0.55f)
                        )
                    }
                    1 -> {
                        // Gold star
                        drawCelebrationStar(currentX, currentY, p.size * 0.6f, p.color)
                    }
                    else -> {
                        // Diamond or coin disc
                        drawCircle(
                            color = p.color,
                            radius = p.size * 0.4f,
                            center = Offset(currentX, currentY)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCelebrationStar(
    cx: Float,
    cy: Float,
    radius: Float,
    color: Color
) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.42f
    val step = PI / points

    for (i in 0 until (2 * points)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * step - PI / 2
        val x = (cx + r * cos(angle)).toFloat()
        val y = (cy + r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier = this.then(
    Modifier.graphicsLayer { this.alpha = alpha }
)

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale
    }
)
