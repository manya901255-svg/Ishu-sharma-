package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Splash & Welcome Overlay Screen for MG Ludo.
 * Displays "Welcome to the MG World" with regal typography (Cinzel Decorative),
 * fade-in animation, glowing crown, sparkling stardust particles, and a pulsing gold loading bar.
 */
@Composable
fun WelcomeOverlayScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fade-in animation states
    var startFadeIn by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isReadyToProceed by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startFadeIn) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "welcome_alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startFadeIn) 1f else 0.94f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "welcome_scale"
    )

    // Pulsing glowing crown effect
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_pulse")
    val crownGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crown_pulse"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gold_shimmer"
    )

    // Stardust floating particles
    val particles = remember {
        List(28) {
            ParticleData(
                xNorm = Random.nextFloat(),
                yNorm = Random.nextFloat(),
                size = Random.nextFloat() * 4.5f + 1.5f,
                speed = Random.nextFloat() * 0.0015f + 0.0008f,
                alpha = Random.nextFloat() * 0.7f + 0.3f
            )
        }
    }

    // Trigger smooth fade-in and progress bar
    LaunchedEffect(Unit) {
        startFadeIn = true
        val duration = 2600L
        val step = 40L
        val increments = duration / step
        for (i in 1..increments) {
            delay(step)
            progress = (i.toFloat() / increments).coerceIn(0f, 1f)
        }
        isReadyToProceed = true
        delay(400)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040B18))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allow tapping to skip/proceed instantly once loaded or after a moment
                if (progress > 0.4f) {
                    onFinished()
                }
            }
            .testTag("welcome_overlay_screen"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Artwork or Majestic Deep Royal Blue Vignette Background
        Image(
            painter = painterResource(id = R.drawable.welcome_mg_world),
            contentDescription = "Welcome to the MG World background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient vignette overlay to guarantee perfect contrast and glowing depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x77040A18),
                            Color(0x22040A18),
                            Color(0x88040A18),
                            Color(0xDD030814)
                        )
                    )
                )
        )

        // 2. Animated Floating Stardust Particle Layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim)
        ) {
            val width = size.width
            val height = size.height
            particles.forEach { p ->
                val currentY = ((p.yNorm - (shimmerOffset * 0.2f)) % 1f + 1f) % 1f
                drawCircle(
                    color = Color(0xFFFFE082).copy(alpha = p.alpha * 0.85f),
                    radius = p.size.dp.toPx() / 2f,
                    center = Offset(p.xNorm * width, currentY * height)
                )
            }
        }

        // 3. Ornate Corner Brackets (Royal Indian Maharaja Borders)
        RoyalCornerFiligree(Modifier.align(Alignment.TopStart), isTop = true, isLeft = true)
        RoyalCornerFiligree(Modifier.align(Alignment.TopEnd), isTop = true, isLeft = false)
        RoyalCornerFiligree(Modifier.align(Alignment.BottomStart), isTop = false, isLeft = true)
        RoyalCornerFiligree(Modifier.align(Alignment.BottomEnd), isTop = false, isLeft = false)

        // 4. Center Composition: Glowing Crown, Regal Typography, and Pulsing Gold Bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .alpha(alphaAnim)
                .scale(scaleAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Spacing & Game Branding Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Left Emblem
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.linearGradient(listOf(RoyalGold, Color(0xFFB78103))))
                            .border(1.dp, Color(0xFFFFF3B0), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MG LUDO",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RoyalGoldLight,
                        letterSpacing = 2.sp
                    )
                }

                // Skip button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x55000000))
                        .border(1.dp, Color(0x66FFD54F), RoundedCornerShape(14.dp))
                        .clickable { onFinished() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SKIP >",
                        fontFamily = CinzelFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFECB3),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Center Content: Golden Crown with Aura & Regal Typography Frame
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Glowing Crown Aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(crownGlowScale)
                ) {
                    // Outer Soft Halo
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x99FFD700),
                                        Color(0x44FFA000),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Majestic 3D Gold Crown Icon
                    Text(
                        text = "👑",
                        fontSize = 58.sp,
                        modifier = Modifier.shadow(16.dp, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ornate Royal Frame with Golden Borders
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .shadow(18.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xCC071530),
                                    Color(0xEE030A17),
                                    Color(0xDD081B3C)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFECB3),
                                    Color(0xFFFFB300),
                                    Color(0xFF8D6200),
                                    Color(0xFFFFD54F)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Royal Subtitle
                        Text(
                            text = "👑 MG LUDO 👑",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFFFE082),
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bold Stylized Golden Typography: "Welcome to the MG World"
                        Text(
                            text = "Welcome to the MG World",
                            fontFamily = CinzelDecorativeFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 25.sp,
                            color = Color(0xFFFFF8E1),
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                            modifier = Modifier
                                .shadow(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Regal filigree divider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color(0xFFFFD54F))
                                        )
                                    )
                            )
                            Text(
                                text = "  ⚜  ",
                                color = RoyalGold,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFFD54F), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tagline: "Embark on Your Ludo Adventure"
                        Text(
                            text = "Embark on Your Ludo Adventure",
                            fontFamily = CinzelFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFFFFECB3),
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Area: Smooth, Pulsing Glowing Gold Loading Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.88f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOADING...",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFFECB3),
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = RoyalGoldLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pulsing Gold Progress Bar Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(16.dp)
                        .shadow(12.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF09142B))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F),
                                    Color(0xFFFFA000),
                                    Color(0xFF7A5200),
                                    Color(0xFFFFECB3)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp)
                ) {
                    // Pulsing golden liquid fill
                    val fillFraction = progress.coerceIn(0.01f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillFraction)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF8F00),
                                        Color(0xFFFFD54F),
                                        Color(0xFFFFF8E1),
                                        Color(0xFFFFB300)
                                    )
                                )
                            )
                    ) {
                        // Light shimmer sweep across the bar
                        val sweepWidth = 40.dp
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(sweepWidth)
                                .offset(x = (shimmerOffset * 220).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.65f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Touch to enter hint
                Text(
                    text = if (isReadyToProceed) "TOUCH TO ENTER" else "Preparing Royal Arena...",
                    fontFamily = CinzelFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReadyToProceed) RoyalGoldLight else Color(0x99FFECB3),
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

/**
 * Regal golden corner filigree ornament for luxury framing
 */
@Composable
private fun RoyalCornerFiligree(
    modifier: Modifier = Modifier,
    isTop: Boolean,
    isLeft: Boolean
) {
    Canvas(
        modifier = modifier
            .size(46.dp)
            .padding(10.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val goldColor = Color(0xFFFFD54F)

        val path = Path().apply {
            if (isTop && isLeft) {
                moveTo(0f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                moveTo(size.width * 0.4f, 0f)
                lineTo(0f, size.height * 0.4f)
            } else if (isTop && !isLeft) {
                moveTo(size.width, size.height)
                lineTo(size.width, 0f)
                lineTo(0f, 0f)
                moveTo(size.width * 0.6f, 0f)
                lineTo(size.width, size.height * 0.4f)
            } else if (!isTop && isLeft) {
                moveTo(0f, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                moveTo(size.width * 0.4f, size.height)
                lineTo(0f, size.height * 0.6f)
            } else {
                moveTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                moveTo(size.width * 0.6f, size.height)
                lineTo(size.width, size.height * 0.6f)
            }
        }

        drawPath(
            path = path,
            color = goldColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

private data class ParticleData(
    val xNorm: Float,
    val yNorm: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)
