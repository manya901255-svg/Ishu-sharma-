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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Realistic 3D Dice Component with Custom Procedural Artwork per Dice ID:
 * - Bespoke art motifs inspired by Classic Premium, VIP Zodiac/Crystal, and Special Themed Dice
 * - 3D Perspective Projection (cameraDistance = 16 * density)
 * - 7 distinct DiceAnimationStyle trajectories & bounce physics
 * - Dynamic Particle Burst Overlay (Embers, Leaves, Snowflakes, Lightning, Confetti, Petals, Balloons)
 * - Optional smooth 3D idle rotation preview for Store Cards & Detail Popups
 */
@Composable
fun Realistic3DDice(
    diceValue: Int,
    isRolling: Boolean,
    playerColor: LudoColor,
    isTurnActive: Boolean,
    modifier: Modifier = Modifier,
    diceSize: Dp = 68.dp,
    diceSkinId: String = "dice_imperial_gold",
    diceItemOverride: PremiumDiceItem? = null,
    animationStyleOverride: DiceAnimationStyle? = null,
    particleEffectOverride: DiceParticleEffect? = null,
    autoRotatePreview: Boolean = false,
    onRollClick: () -> Unit
) {
    val density = LocalDensity.current.density
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val resolvedDice = remember(diceSkinId, diceItemOverride) {
        diceItemOverride ?: PremiumDiceCatalog.buildDefaultSeedCatalog().firstOrNull { it.id == diceSkinId }
    }
    val animationStyle = animationStyleOverride ?: resolvedDice?.benefits?.animationStyle ?: DiceAnimationStyle.ROYAL_TUMBLE
    val particleEffect = particleEffectOverride ?: resolvedDice?.benefits?.particleEffect ?: DiceParticleEffect.GOLD_SPARKLE

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )

    // Smooth idle rotation for store preview cards / popup showcase
    val previewTransition = rememberInfiniteTransition(label = "previewSpin")
    val previewPhase by previewTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "previewPhase"
    )

    // Physics Animation Driver for the 3D Roll
    val rollAnim = remember { Animatable(0f) }
    var displayedPip by remember { mutableIntStateOf(diceValue) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            rollAnim.snapTo(0f)
            rollAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 820, easing = LinearEasing)
            )
            displayedPip = diceValue
        } else {
            rollAnim.snapTo(1f)
            displayedPip = diceValue
        }
    }

    LaunchedEffect(isRolling, rollAnim.value) {
        if (isRolling && rollAnim.value < 0.85f) {
            displayedPip = ((rollAnim.value * 17f).toInt() % 6) + 1
        } else if (!isRolling || rollAnim.value >= 0.85f) {
            displayedPip = diceValue
        }
    }

    val progress = rollAnim.value
    val (altitudeDp, squashX, squashY) = calculateBouncePhysics(isRolling, progress, animationStyle)

    val (rotX, rotY, rotZ) = if (isRolling) {
        when (animationStyle) {
            DiceAnimationStyle.METEOR_SPIN -> Triple((1f - progress) * 1080f, (1f - progress) * 1440f, (1f - progress) * 720f)
            DiceAnimationStyle.LIGHTNING_SNAP -> Triple(
                sin(progress * 18f) * 35f * (1f - progress),
                (1f - progress) * 900f,
                sin(progress * 24f) * 28f * (1f - progress)
            )
            DiceAnimationStyle.CRYSTAL_FLOAT -> Triple(sin(progress * 6.28f) * 24f, (1f - progress) * 720f, (1f - progress) * 360f)
            DiceAnimationStyle.WHIRLWIND -> Triple((1f - progress) * 720f, (1f - progress) * 1080f, (1f - progress) * 1080f)
            DiceAnimationStyle.BOUNCE_FESTIVAL -> Triple((1f - progress) * 540f, sin(progress * 12f) * 40f, (1f - progress) * 720f)
            DiceAnimationStyle.ROAR_SLAM -> Triple((1f - progress) * 360f, (1f - progress) * 720f, 0f)
            DiceAnimationStyle.ROYAL_TUMBLE -> Triple((1f - progress) * 720f + (progress * 15f), (1f - progress) * 1080f, (1f - progress) * 540f)
        }
    } else if (autoRotatePreview) {
        val rad = previewPhase * (PI.toFloat() / 180f)
        Triple(sin(rad) * 14f, cos(rad) * 18f, sin(rad * 0.5f) * 6f)
    } else {
        Triple(0f, 0f, 0f)
    }

    val shadowAlpha = if (isRolling) (0.75f - (altitudeDp / 80f) * 0.45f).coerceIn(0.2f, 0.75f) else 0.55f
    val shadowScaleX = if (isRolling) (1f + (altitudeDp / 80f) * 0.45f) * squashX else 1f
    val shadowScaleY = if (isRolling) (1f + (altitudeDp / 80f) * 0.35f) * squashY else 1f

    val bgGradientColors = resolveDiceSurfaceColors(diceSkinId, resolvedDice)
    val borderColors = resolveDiceBorderColors(diceSkinId, resolvedDice)
    val pipColor = resolvedDice?.pipColor ?: playerColor.primary

    Box(
        modifier = modifier
            .size(diceSize + 32.dp)
            .testTag("realistic_3d_dice"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Dynamic Cast Shadow
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
                    colors = listOf(Color(0x99000000), Color(0x55000000), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width / 2f
                )
            )
        }

        // 2. Impact Shockwave Ring
        if (isRolling && (progress in 0.42f..0.52f || progress in 0.72f..0.82f)) {
            Canvas(
                modifier = Modifier
                    .size(diceSize * 1.38f)
                    .offset(y = diceSize * 0.36f)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(particleEffect.primaryColorHex).copy(alpha = 0.65f), Color.Transparent),
                        radius = size.width / 2f
                    ),
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }
        }

        // 3. Particle Effect Aura during Rolling or Lucky 6
        if (isRolling || (!isRolling && diceValue == 6)) {
            DiceParticleBurstOverlay(
                diceSize = diceSize,
                particleEffect = particleEffect,
                progress = if (isRolling) progress else 0.75f
            )
        }

        // 4. 3D Perspective Cube Body
        val cornerRadiusDp = if (resolvedDice?.rarity == DiceRarity.VIP) 14.dp else 16.dp
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
                    shape = RoundedCornerShape(cornerRadiusDp)
                )
                .clip(RoundedCornerShape(cornerRadiusDp))
                .background(
                    brush = Brush.radialGradient(
                        colors = bgGradientColors,
                        radius = 160f,
                        center = Offset(
                            x = if (isRolling) (diceSize.value * (0.3f + 0.4f * sin(progress * 6.28f))) else diceSize.value * 0.35f,
                            y = if (isRolling) (diceSize.value * (0.3f + 0.4f * cos(progress * 6.28f))) else diceSize.value * 0.35f
                        )
                    )
                )
                .border(
                    width = if (resolvedDice?.rarity == DiceRarity.VIP) 2.8.dp else 2.2.dp,
                    brush = Brush.linearGradient(colors = borderColors),
                    shape = RoundedCornerShape(cornerRadiusDp)
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
            // Custom Artwork Layer + 1..6 Pips
            RealisticDiceFaceCanvas(
                diceSkinId = diceSkinId,
                diceValue = displayedPip,
                pipColor = pipColor,
                accentColor = resolvedDice?.accentColor ?: RoyalGoldLight,
                rarity = resolvedDice?.rarity ?: DiceRarity.COMMON,
                modifier = Modifier.fillMaxSize()
            )

            // Specular Lighting Sheen across face during tumble
            if (isRolling) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sweepProgress = (progress * 2.5f) % 1f
                    val beamX = size.width * (sweepProgress * 1.6f - 0.3f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.38f), Color.Transparent),
                            start = Offset(beamX - 25f, 0f),
                            end = Offset(beamX + 25f, size.height)
                        )
                    )
                }
            }
        }
    }
}

private fun resolveDiceSurfaceColors(diceSkinId: String, resolved: PremiumDiceItem?): List<Color> {
    return when (diceSkinId) {
        "dice_maharaja_ruby" -> listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF880E4F))
        "dice_emerald_empire" -> listOf(Color(0xFF69F0AE), Color(0xFF00C853), Color(0xFF1B5E20))
        "dice_cyber_neon" -> listOf(Color(0xFF18FFFF), Color(0xFF00B0FF), Color(0xFF651FFF))
        "dice_royal_platinum" -> listOf(Color(0xFFFFFFFF), Color(0xFFCFD8DC), Color(0xFF78909C))
        "dice_classic_ivory" -> listOf(Color(0xFFFFF9C4), Color(0xFFFFF176), Color(0xFFFFD54F))
        "dice_medusa" -> listOf(Color(0xFF69F0AE), Color(0xFF00E676), Color(0xFF1B5E20))
        "dice_scorpius" -> listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF4A0010))
        "dice_wilder" -> listOf(Color(0xFFFFCA28), Color(0xFFFF9100), Color(0xFF4E342E))
        "dice_eaglor" -> listOf(Color(0xFF80D8FF), Color(0xFF2979FF), Color(0xFF0D47A1))
        "dice_mysticat" -> listOf(Color(0xFFEA80FC), Color(0xFFE040FB), Color(0xFF4A148C))
        "dice_bonfire" -> listOf(Color(0xFFFFEA00), Color(0xFFFF6D00), Color(0xFFBF360C))
        "dice_jewel" -> listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF006064))
        "dice_wanderly" -> listOf(Color(0xFF80CBC4), Color(0xFF26A69A), Color(0xFF004D40))
        "dice_captaino" -> listOf(Color(0xFF3F51B5), Color(0xFF1A237E), Color(0xFF000A33))
        else -> {
            if (resolved != null) {
                listOf(resolved.accentColor.copy(alpha = 0.85f), resolved.primaryColor, resolved.secondaryColor)
            } else {
                val catalogDice = ItemCatalog.findById(diceSkinId)
                if (catalogDice != null) {
                    listOf(catalogDice.accentColor, catalogDice.primaryColor, catalogDice.secondaryColor)
                } else {
                    listOf(RoyalMarbleBase, RoyalMarbleTile, RoyalMarbleShadow)
                }
            }
        }
    }
}

private fun resolveDiceBorderColors(diceSkinId: String, resolved: PremiumDiceItem?): List<Color> {
    if (resolved != null) {
        return listOf(resolved.rarity.primaryBorderColor, resolved.accentColor, resolved.rarity.secondaryBorderColor)
    }
    return when (diceSkinId) {
        "dice_maharaja_ruby" -> listOf(Color(0xFFFFD700), Color(0xFFFF5252), Color(0xFFFFD700))
        "dice_emerald_empire" -> listOf(Color(0xFFB9F6CA), Color(0xFFFFD700), Color(0xFF00E676))
        "dice_cyber_neon" -> listOf(Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFF00E5FF))
        else -> listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark, RoyalAmber)
    }
}

private fun calculateBouncePhysics(
    isRolling: Boolean,
    progress: Float,
    style: DiceAnimationStyle
): Triple<Float, Float, Float> {
    if (!isRolling || progress >= 1f) return Triple(0f, 1f, 1f)

    val peakMultiplier = when (style) {
        DiceAnimationStyle.BOUNCE_FESTIVAL -> 1.2f
        DiceAnimationStyle.ROAR_SLAM -> 1.35f
        DiceAnimationStyle.CRYSTAL_FLOAT -> 0.85f
        else -> 1.0f
    }

    return when {
        progress < 0.45f -> {
            val t = progress / 0.45f
            val altitude = 4f * 68f * peakMultiplier * t * (1f - t)
            Triple(altitude, 1f, 1f)
        }
        progress < 0.48f -> {
            val t = (progress - 0.45f) / 0.03f
            val squash = 0.84f + (1f - 0.84f) * t
            val stretch = 1.16f - (1.16f - 1f) * t
            Triple(0f, stretch, squash)
        }
        progress < 0.75f -> {
            val t = (progress - 0.48f) / 0.27f
            val altitude = 4f * 26f * peakMultiplier * t * (1f - t)
            Triple(altitude, 1f, 1f)
        }
        progress < 0.78f -> {
            val t = (progress - 0.75f) / 0.03f
            val squash = 0.92f + (1f - 0.92f) * t
            val stretch = 1.08f - (1.08f - 1f) * t
            Triple(0f, stretch, squash)
        }
        progress < 0.92f -> {
            val t = (progress - 0.78f) / 0.14f
            val altitude = 4f * 8f * t * (1f - t)
            Triple(altitude, 1f, 1f)
        }
        else -> Triple(0f, 1f, 1f)
    }
}

@Composable
private fun RealisticDiceFaceCanvas(
    diceSkinId: String,
    diceValue: Int,
    pipColor: Color,
    accentColor: Color,
    rarity: DiceRarity,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(4.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // 1. Bespoke Original Procedural Artwork Layer per Dice ID
        drawThemeMotif(diceSkinId, w, h, cx, cy, accentColor, rarity)

        // 2. Ornate Corner Filigree Brackets
        val bracketLen = w * 0.16f
        val strokeW = 1.5.dp.toPx()
        val goldBrush = Brush.linearGradient(listOf(accentColor, RoyalGoldDark))
        drawLine(goldBrush, Offset(2f, 2f), Offset(2f + bracketLen, 2f), strokeW)
        drawLine(goldBrush, Offset(2f, 2f), Offset(2f, 2f + bracketLen), strokeW)
        drawLine(goldBrush, Offset(w - 2f, 2f), Offset(w - 2f - bracketLen, 2f), strokeW)
        drawLine(goldBrush, Offset(w - 2f, 2f), Offset(w - 2f, 2f + bracketLen), strokeW)
        drawLine(goldBrush, Offset(2f, h - 2f), Offset(2f + bracketLen, h - 2f), strokeW)
        drawLine(goldBrush, Offset(2f, h - 2f), Offset(2f, h - 2f - bracketLen), strokeW)
        drawLine(goldBrush, Offset(w - 2f, h - 2f), Offset(w - 2f - bracketLen, h - 2f), strokeW)
        drawLine(goldBrush, Offset(w - 2f, h - 2f), Offset(w - 2f, h - 2f - bracketLen), strokeW)

        // 3. Embossed Gemstone Pips (Strict 1..6 Accuracy)
        val pipRadius = w * 0.098f
        val left = w * 0.26f
        val center = w * 0.50f
        val right = w * 0.74f
        val top = h * 0.26f
        val middle = h * 0.50f
        val bottom = h * 0.74f

        fun draw3DPip(px: Float, py: Float) {
            drawCircle(
                color = Color(0x66000000),
                radius = pipRadius * 1.18f,
                center = Offset(px + 1.5f, py + 2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RoyalGoldLight, RoyalGoldDark),
                    center = Offset(px - pipRadius * 0.2f, py - pipRadius * 0.2f),
                    radius = pipRadius + 2f
                ),
                radius = pipRadius + 1.2f,
                center = Offset(px, py)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, pipColor, pipColor, Color(0xFF101010)),
                    center = Offset(px - pipRadius * 0.35f, py - pipRadius * 0.35f),
                    radius = pipRadius
                ),
                radius = pipRadius,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = pipRadius * 0.25f,
                center = Offset(px - pipRadius * 0.32f, py - pipRadius * 0.32f)
            )
        }

        when (diceValue) {
            1 -> draw3DPip(center, middle)
            2 -> { draw3DPip(left, top); draw3DPip(right, bottom) }
            3 -> { draw3DPip(left, top); draw3DPip(center, middle); draw3DPip(right, bottom) }
            4 -> { draw3DPip(left, top); draw3DPip(right, top); draw3DPip(left, bottom); draw3DPip(right, bottom) }
            5 -> { draw3DPip(left, top); draw3DPip(right, top); draw3DPip(center, middle); draw3DPip(left, bottom); draw3DPip(right, bottom) }
            6 -> {
                draw3DPip(left, top); draw3DPip(right, top)
                draw3DPip(left, middle); draw3DPip(right, middle)
                draw3DPip(left, bottom); draw3DPip(right, bottom)
            }
        }
    }
}

private fun DrawScope.drawThemeMotif(
    diceSkinId: String,
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    accent: Color,
    rarity: DiceRarity
) {
    val radius = w * 0.36f
    when (diceSkinId) {
        "dice_royal_blue", "dice_golden_palace" -> {
            // Circular baroque filigree medallion
            drawCircle(
                color = accent.copy(alpha = 0.35f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.18f),
                radius = radius * 0.72f,
                center = Offset(cx, cy)
            )
        }
        "dice_desert_warrior", "dice_sunset" -> {
            // Glowing sun orb + desert mesa silhouette band
            drawCircle(
                color = Color(0xFFFFE082).copy(alpha = 0.45f),
                radius = w * 0.24f,
                center = Offset(cx, cy * 0.78f)
            )
            drawRoundRect(
                color = Color(0xFF3E1008).copy(alpha = 0.55f),
                topLeft = Offset(0f, h * 0.68f),
                size = Size(w, h * 0.32f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        "dice_orange_citrus" -> {
            // Citrus slice segments radiating from center
            drawCircle(color = Color(0xFFFFF59D).copy(alpha = 0.42f), radius = radius * 0.95f, center = Offset(cx, cy))
            for (i in 0 until 8) {
                val angle = i * (PI / 4.0)
                drawLine(
                    color = Color(0xFFFF6F00).copy(alpha = 0.45f),
                    start = Offset(cx, cy),
                    end = Offset((cx + radius * 0.9f * cos(angle)).toFloat(), (cy + radius * 0.9f * sin(angle)).toFloat()),
                    strokeWidth = 1.4.dp.toPx()
                )
            }
        }
        "dice_blue_royal_pattern", "dice_red_royal", "dice_royal_mosaic" -> {
            // 8-point Mughal lotus star mandala
            for (i in 0 until 8) {
                val angle = i * (PI / 4.0)
                val px = (cx + radius * 0.62f * cos(angle)).toFloat()
                val py = (cy + radius * 0.62f * sin(angle)).toFloat()
                drawCircle(color = accent.copy(alpha = 0.28f), radius = w * 0.13f, center = Offset(px, py))
            }
            drawCircle(color = accent.copy(alpha = 0.45f), radius = radius * 0.78f, center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
        }
        "dice_festival", "dice_carnival" -> {
            // Illuminated festive diya & glowing halo petals
            drawCircle(color = Color(0xFFFFEA00).copy(alpha = 0.22f), radius = radius * 0.92f, center = Offset(cx, cy))
            for (i in 0 until 6) {
                val angle = i * (PI / 3.0)
                drawCircle(
                    color = Color(0xFFFF6D00).copy(alpha = 0.40f),
                    radius = w * 0.08f,
                    center = Offset((cx + radius * 0.85f * cos(angle)).toFloat(), (cy + radius * 0.85f * sin(angle)).toFloat())
                )
            }
        }
        "dice_imperial_red" -> {
            // Imperial double key-fret frame
            drawRoundRect(
                color = Color(0xFFFFD700).copy(alpha = 0.55f),
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.76f, h * 0.76f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.8.dp.toPx())
            )
        }
        "dice_emerald_vip", "dice_sapphire_vip", "dice_ruby_vip", "dice_golden_leaf_vip", "dice_celestial_zodiac" -> {
            // VIP Beveled Zodiac Ring with 12 constellation notches
            drawCircle(
                color = accent.copy(alpha = 0.65f),
                radius = radius * 1.05f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.35f),
                radius = radius * 0.76f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2.dp.toPx())
            )
            for (i in 0 until 12) {
                val a = i * (PI / 6.0)
                drawCircle(
                    color = accent.copy(alpha = 0.75f),
                    radius = 1.6.dp.toPx(),
                    center = Offset((cx + radius * 0.91f * cos(a)).toFloat(), (cy + radius * 0.91f * sin(a)).toFloat())
                )
            }
        }
        "dice_fire" -> {
            // Blazing flame crest
            val flamePath = Path().apply {
                moveTo(cx, h * 0.16f)
                quadraticBezierTo(w * 0.78f, h * 0.45f, cx, h * 0.84f)
                quadraticBezierTo(w * 0.22f, h * 0.45f, cx, h * 0.16f)
                close()
            }
            drawPath(flamePath, color = Color(0xFFFFEA00).copy(alpha = 0.45f))
        }
        "dice_nature" -> {
            // 5-petal white jasmine flower silhouette
            for (i in 0 until 5) {
                val a = i * (2.0 * PI / 5.0)
                drawCircle(
                    color = Color.White.copy(alpha = 0.38f),
                    radius = w * 0.14f,
                    center = Offset((cx + w * 0.18f * cos(a)).toFloat(), (cy + h * 0.18f * sin(a)).toFloat())
                )
            }
        }
        "dice_chocolate" -> {
            // Creamy milk splash wave at bottom + cocoa squares
            drawRoundRect(
                color = Color(0xFFFFF8E1).copy(alpha = 0.45f),
                topLeft = Offset(w * 0.10f, h * 0.66f),
                size = Size(w * 0.80f, h * 0.22f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
        "dice_panda", "dice_cat" -> {
            // Playful ears silhouette + circular face backdrop
            drawCircle(color = accent.copy(alpha = 0.32f), radius = w * 0.13f, center = Offset(w * 0.28f, h * 0.26f))
            drawCircle(color = accent.copy(alpha = 0.32f), radius = w * 0.13f, center = Offset(w * 0.72f, h * 0.26f))
            drawCircle(color = accent.copy(alpha = 0.28f), radius = radius * 0.78f, center = Offset(cx, cy * 1.05f))
        }
        "dice_snowman" -> {
            // Frost crystal rim + snow drift
            drawCircle(color = Color.White.copy(alpha = 0.45f), radius = radius * 0.75f, center = Offset(cx, cy))
        }
        "dice_lion" -> {
            // Radiating golden mane triangles
            for (i in 0 until 12) {
                val a = i * (PI / 6.0)
                drawLine(
                    color = Color(0xFFFFCA28).copy(alpha = 0.55f),
                    start = Offset((cx + radius * 0.45f * cos(a)).toFloat(), (cy + radius * 0.45f * sin(a)).toFloat()),
                    end = Offset((cx + radius * 1.02f * cos(a)).toFloat(), (cy + radius * 1.02f * sin(a)).toFloat()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        "dice_balloon" -> {
            // Twin party balloon silhouettes
            drawCircle(color = Color(0xFF29B6F6).copy(alpha = 0.45f), radius = w * 0.16f, center = Offset(w * 0.36f, h * 0.36f))
            drawCircle(color = Color(0xFFFFEB3B).copy(alpha = 0.45f), radius = w * 0.16f, center = Offset(w * 0.64f, h * 0.34f))
        }
        else -> {
            if (rarity == DiceRarity.VIP || rarity == DiceRarity.LEGENDARY) {
                drawCircle(
                    color = accent.copy(alpha = 0.35f),
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun DiceParticleBurstOverlay(
    diceSize: Dp,
    particleEffect: DiceParticleEffect,
    progress: Float
) {
    if (particleEffect == DiceParticleEffect.NONE) return
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particlePulse"
    )

    Canvas(modifier = Modifier.size(diceSize + 30.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val count = 10
        val baseRadius = size.width * (0.42f + 0.08f * sin(progress * 6.28f))
        val c1 = Color(particleEffect.primaryColorHex)
        val c2 = Color(particleEffect.secondaryColorHex)

        for (i in 0 until count) {
            val angle = Math.toRadians((i * (360.0 / count)) + progress * 90.0)
            val dist = baseRadius * (0.85f + (i % 3) * 0.10f)
            val px = (cx + dist * cos(angle)).toFloat()
            val py = (cy + dist * sin(angle)).toFloat()
            drawCircle(
                color = (if (i % 2 == 0) c1 else c2).copy(alpha = pulse),
                radius = (2.2f + (i % 2) * 1.2f).dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

/**
 * Center Court 3D Dice Throw Arena:
 * Renders the equipped Premium Dice skin with its custom 3D rolling animation and particle burst.
 */
@Composable
fun CenterCourt3DRollingOverlay(
    diceValue: Int,
    isRolling: Boolean,
    playerColor: LudoColor,
    diceSkinId: String = "dice_imperial_gold",
    diceItemOverride: PremiumDiceItem? = null,
    modifier: Modifier = Modifier
) {
    if (!isRolling) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("center_court_3d_rolling_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(164.dp)
                .clip(CircleShape)
                .background(RoyalNavyDark.copy(alpha = 0.55f))
                .border(1.8.dp, RoyalGold.copy(alpha = 0.5f), CircleShape)
        )

        Realistic3DDice(
            diceValue = diceValue,
            isRolling = isRolling,
            playerColor = playerColor,
            isTurnActive = false,
            diceSize = 86.dp,
            diceSkinId = diceSkinId,
            diceItemOverride = diceItemOverride,
            onRollClick = {}
        )
    }
}
