package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Realistic 3D Dice Component with Compose Graphics:
 * - 3D Perspective Projection (cameraDistance = 16 * density)
 * - Tri-axial tumbling rotation (Yaw, Pitch, Roll)
 * - Parabolic lift, multi-stage bounce physics & squash-stretch
 * - Dynamic 3D decoupled drop shadow with altitude-based blur/scale
 * - Real-time tumbling pips with specular lighting sheen
 * - Golden impact shockwave & royal spark particles
 */
@Composable
fun Realistic3DDice(
    diceValue: Int,
    isRolling: Boolean,
    playerColor: LudoColor,
    isTurnActive: Boolean,
    modifier: Modifier = Modifier,
    diceSize: Dp = 68.dp,
    onRollClick: () -> Unit
) {
    val density = LocalDensity.current.density
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press-down spring response for immediate tactile feedback
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )

    // Physics Animation Driver for the 3D Roll
    val rollAnim = remember { Animatable(0f) }
    var displayedPip by remember { mutableIntStateOf(diceValue) }

    // When isRolling triggers, execute realistic 3D bounce trajectory
    LaunchedEffect(isRolling) {
        if (isRolling) {
            rollAnim.snapTo(0f)
            rollAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
            displayedPip = diceValue
        } else {
            rollAnim.snapTo(1f)
            displayedPip = diceValue
        }
    }

    // Rapidly randomize visible pip during tumble phase
    LaunchedEffect(isRolling, rollAnim.value) {
        if (isRolling && rollAnim.value < 0.85f) {
            displayedPip = (1..6).random()
        } else if (!isRolling || rollAnim.value >= 0.85f) {
            displayedPip = diceValue
        }
    }

    val progress = rollAnim.value

    // Parabolic Bounce Physics & Elevation
    // 0.0 -> 0.45: First large toss & bounce
    // 0.45 -> 0.75: Second medium bounce
    // 0.75 -> 0.92: Third small micro-bounce
    // 0.92 -> 1.0: Settle firmly into ground
    val (altitudeDp, squashX, squashY) = calculateBouncePhysics(isRolling, progress)

    // 3D Rotational Angles (degrees)
    val rotX = if (isRolling) {
        (1f - progress) * 720f + (progress * 15f)
    } else 0f

    val rotY = if (isRolling) {
        (1f - progress) * 1080f
    } else 0f

    val rotZ = if (isRolling) {
        (1f - progress) * 540f
    } else 0f

    // Dynamic 3D Drop Shadow properties based on altitude
    val shadowAlpha = if (isRolling) {
        (0.75f - (altitudeDp / 80f) * 0.45f).coerceIn(0.2f, 0.75f)
    } else 0.55f

    val shadowScaleX = if (isRolling) {
        (1f + (altitudeDp / 80f) * 0.45f) * squashX
    } else 1f

    val shadowScaleY = if (isRolling) {
        (1f + (altitudeDp / 80f) * 0.35f) * squashY
    } else 1f

    Box(
        modifier = modifier
            .size(diceSize + 32.dp)
            .testTag("realistic_3d_dice"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Dynamic Decoupled 3D Cast Shadow (projected on floor)
        Canvas(
            modifier = Modifier
                .size(diceSize * 1.1f, diceSize * 0.45f)
                .offset(y = (diceSize * 0.42f) + (altitudeDp * 0.15f).dp)
                .graphicsLayer {
                    scaleX = shadowScaleX
                    scaleY = shadowScaleY
                    alpha = shadowAlpha
                }
        ) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x99000000),
                        Color(0x55000000),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width / 2f
                )
            )
        }

        // 2. Landing Shockwave Ring (triggers when landing)
        if (isRolling && progress in 0.42f..0.52f || progress in 0.72f..0.82f) {
            Canvas(
                modifier = Modifier
                    .size(diceSize * 1.35f)
                    .offset(y = diceSize * 0.38f)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RoyalGoldLight.copy(alpha = 0.6f), Color.Transparent),
                        radius = size.width / 2f
                    ),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 3. 3D Cube Container with RenderNode 3D Perspective Projection
        Box(
            modifier = Modifier
                .offset(y = (-altitudeDp).dp)
                .size(diceSize)
                .graphicsLayer {
                    this.cameraDistance = 16f * density
                    this.rotationX = rotX
                    this.rotationY = rotY
                    this.rotationZ = rotZ
                    this.scaleX = pressScale * squashX
                    this.scaleY = pressScale * squashY
                }
                .shadow(
                    elevation = if (altitudeDp > 10f) (altitudeDp * 0.25f).dp else 6.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RoyalMarbleBase,
                            RoyalMarbleTile,
                            RoyalMarbleShadow
                        ),
                        radius = 160f,
                        center = Offset(
                            x = if (isRolling) (diceSize.value * (0.3f + 0.4f * sin(progress * 6.28f))) else diceSize.value * 0.35f,
                            y = if (isRolling) (diceSize.value * (0.3f + 0.4f * cos(progress * 6.28f))) else diceSize.value * 0.35f
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            RoyalGoldLight,
                            RoyalGold,
                            RoyalGoldDark,
                            RoyalAmber
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    enabled = isTurnActive && !isRolling,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onRollClick()
                }
                .testTag("realistic_dice_clickable"),
            contentAlignment = Alignment.Center
        ) {
            // Realistic 3D Cube Face Canvas with Corner Filigree & Dynamic Pips
            RealisticDiceFaceCanvas(
                diceValue = displayedPip,
                pipColor = playerColor.primary,
                rollProgress = progress,
                isRolling = isRolling,
                modifier = Modifier.fillMaxSize()
            )

            // Specular Lighting Sheen across face during 3D tumble
            if (isRolling) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sweepProgress = (progress * 2.5f) % 1f
                    val beamX = size.width * (sweepProgress * 1.6f - 0.3f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(beamX - 25f, 0f),
                            end = Offset(beamX + 25f, size.height)
                        )
                    )
                }
            }
        }

        // 4. Sparkle Particles on final landing
        if (!isRolling && diceValue == 6) {
            LandingSparkles(diceSize = diceSize)
        }
    }
}

/**
 * Calculates physical bounce trajectory with squash and stretch elasticity.
 */
private fun calculateBouncePhysics(isRolling: Boolean, progress: Float): Triple<Float, Float, Float> {
    if (!isRolling || progress >= 1f) {
        return Triple(0f, 1f, 1f)
    }

    return when {
        // First major toss arc: 0.0 -> 0.45
        progress < 0.45f -> {
            val t = progress / 0.45f
            val altitude = 4f * 70f * t * (1f - t) // Parabolic arc peak ~70dp
            Triple(altitude, 1f, 1f)
        }
        // First impact & squash: 0.45 -> 0.48
        progress < 0.48f -> {
            val t = (progress - 0.45f) / 0.03f
            val squash = 0.84f + (1f - 0.84f) * t
            val stretch = 1.16f - (1.16f - 1f) * t
            Triple(0f, stretch, squash)
        }
        // Second medium bounce: 0.48 -> 0.75
        progress < 0.75f -> {
            val t = (progress - 0.48f) / 0.27f
            val altitude = 4f * 28f * t * (1f - t) // Peak ~28dp
            Triple(altitude, 1f, 1f)
        }
        // Second impact & squash: 0.75 -> 0.78
        progress < 0.78f -> {
            val t = (progress - 0.75f) / 0.03f
            val squash = 0.92f + (1f - 0.92f) * t
            val stretch = 1.08f - (1.08f - 1f) * t
            Triple(0f, stretch, squash)
        }
        // Third small hop: 0.78 -> 0.92
        progress < 0.92f -> {
            val t = (progress - 0.78f) / 0.14f
            val altitude = 4f * 8f * t * (1f - t) // Peak ~8dp
            Triple(altitude, 1f, 1f)
        }
        // Settle into rest
        else -> {
            Triple(0f, 1f, 1f)
        }
    }
}

/**
 * Canvas drawing the beveled 3D dice face, gold corner brackets and embossed gemstone pips.
 */
@Composable
private fun RealisticDiceFaceCanvas(
    diceValue: Int,
    pipColor: Color,
    rollProgress: Float,
    isRolling: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(8.dp)) {
        val w = size.width
        val h = size.height

        // 1. Ornate Golden Corner Filigree Brackets (Maharaja Theme)
        val bracketLen = w * 0.16f
        val strokeW = 1.8.dp.toPx()
        val goldBrush = Brush.linearGradient(listOf(RoyalGoldLight, RoyalGoldDark))

        // Top-left bracket
        drawLine(goldBrush, Offset(2f, 2f), Offset(2f + bracketLen, 2f), strokeW)
        drawLine(goldBrush, Offset(2f, 2f), Offset(2f, 2f + bracketLen), strokeW)
        // Top-right bracket
        drawLine(goldBrush, Offset(w - 2f, 2f), Offset(w - 2f - bracketLen, 2f), strokeW)
        drawLine(goldBrush, Offset(w - 2f, 2f), Offset(w - 2f, 2f + bracketLen), strokeW)
        // Bottom-left bracket
        drawLine(goldBrush, Offset(2f, h - 2f), Offset(2f + bracketLen, h - 2f), strokeW)
        drawLine(goldBrush, Offset(2f, h - 2f), Offset(2f, h - 2f - bracketLen), strokeW)
        // Bottom-right bracket
        drawLine(goldBrush, Offset(w - 2f, h - 2f), Offset(w - 2f - bracketLen, h - 2f), strokeW)
        drawLine(goldBrush, Offset(w - 2f, h - 2f), Offset(w - 2f, h - 2f - bracketLen), strokeW)

        // 2. Embossed Gemstone Pips
        val pipRadius = w * 0.105f

        val left = w * 0.25f
        val center = w * 0.50f
        val right = w * 0.75f

        val top = h * 0.25f
        val middle = h * 0.50f
        val bottom = h * 0.75f

        fun draw3DPip(cx: Float, cy: Float) {
            // Drop shadow behind pip inside dice bevel
            drawCircle(
                color = Color(0x44000000),
                radius = pipRadius * 1.15f,
                center = Offset(cx + 1.5f, cy + 2f)
            )
            // Golden outer bezel rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RoyalGoldLight, RoyalGoldDark),
                    center = Offset(cx - pipRadius * 0.2f, cy - pipRadius * 0.2f),
                    radius = pipRadius + 2f
                ),
                radius = pipRadius + 1.2f,
                center = Offset(cx, cy)
            )
            // Jewel Pip Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        pipColor,
                        pipColor,
                        Color(0xFF101010)
                    ),
                    center = Offset(cx - pipRadius * 0.35f, cy - pipRadius * 0.35f),
                    radius = pipRadius
                ),
                radius = pipRadius,
                center = Offset(cx, cy)
            )
            // Specular jewel glint
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = pipRadius * 0.25f,
                center = Offset(cx - pipRadius * 0.32f, cy - pipRadius * 0.32f)
            )
        }

        when (diceValue) {
            1 -> {
                draw3DPip(center, middle)
            }
            2 -> {
                draw3DPip(left, top)
                draw3DPip(right, bottom)
            }
            3 -> {
                draw3DPip(left, top)
                draw3DPip(center, middle)
                draw3DPip(right, bottom)
            }
            4 -> {
                draw3DPip(left, top)
                draw3DPip(right, top)
                draw3DPip(left, bottom)
                draw3DPip(right, bottom)
            }
            5 -> {
                draw3DPip(left, top)
                draw3DPip(right, top)
                draw3DPip(center, middle)
                draw3DPip(left, bottom)
                draw3DPip(right, bottom)
            }
            6 -> {
                draw3DPip(left, top)
                draw3DPip(right, top)
                draw3DPip(left, middle)
                draw3DPip(right, middle)
                draw3DPip(left, bottom)
                draw3DPip(right, bottom)
            }
        }
    }
}

/**
 * Royal golden sparkles bursting when landing on a lucky 6.
 */
@Composable
private fun LandingSparkles(diceSize: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")
    val sparkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkAlpha"
    )

    Canvas(modifier = Modifier.size(diceSize + 28.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val starCount = 8
        val radius = size.width * 0.48f

        for (i in 0 until starCount) {
            val angle = Math.toRadians((i * (360.0 / starCount)).toDouble())
            val sx = (cx + radius * cos(angle)).toFloat()
            val sy = (cy + radius * sin(angle)).toFloat()

            drawCircle(
                color = RoyalGoldLight.copy(alpha = sparkAlpha),
                radius = 3.dp.toPx(),
                center = Offset(sx, sy)
            )
        }
    }
}

/**
 * Center Court 3D Dice Throw Arena:
 * Pops up dramatically over the board during an active dice roll, giving the user
 * immediate, unmistakable, real-time 3D visual feedback as the dice flies and settles.
 */
@Composable
fun CenterCourt3DRollingOverlay(
    diceValue: Int,
    isRolling: Boolean,
    playerColor: LudoColor,
    modifier: Modifier = Modifier
) {
    if (!isRolling) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("center_court_3d_rolling_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Translucent court halo
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(RoyalNavyDark.copy(alpha = 0.5f))
                .border(1.5.dp, RoyalGold.copy(alpha = 0.4f), CircleShape)
        )

        // Large 3D Tumbling Dice
        Realistic3DDice(
            diceValue = diceValue,
            isRolling = isRolling,
            playerColor = playerColor,
            isTurnActive = false,
            diceSize = 84.dp,
            onRollClick = {}
        )
    }
}
