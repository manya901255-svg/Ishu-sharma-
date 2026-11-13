package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ItemCatalog

/**
 * Renders the player's equipped Profile Frame (Campfire, Volt, Jackpot, Window, Pixie, Stage, Fiesta, Comic, Bounty)
 * around any Avatar composable. Used across Inventory previews, ProfileScreen, HomeScreen header, and Match HUD.
 */
@Composable
fun FramedAvatarBox(
    frameId: String,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    showCornerBadge: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val frameItem = ItemCatalog.findById(frameId)
    val primary = frameItem?.primaryColor ?: Color(0xFFFFD54F)
    val secondary = frameItem?.secondaryColor ?: Color(0xFFB8860B)
    val accent = frameItem?.accentColor ?: Color(0xFFFFF8E1)

    val infiniteTransition = rememberInfiniteTransition(label = "frameShimmer")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "framePulse"
    )

    val borderWidth = when (frameId) {
        "frame_jackpot", "frame_bounty", "frame_volt" -> 3.5.dp
        "frame_campfire", "frame_stage", "frame_fiesta" -> 3.dp
        else -> 2.5.dp
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing halo for special/animated frames
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = if (frameId != "frame_royal_crest") 8.dp else 3.dp,
                    shape = shape,
                    ambientColor = primary.copy(alpha = pulse),
                    spotColor = accent.copy(alpha = pulse)
                )
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            primary.copy(alpha = 0.35f * pulse),
                            secondary.copy(alpha = 0.45f),
                            accent.copy(alpha = 0.30f * pulse)
                        )
                    )
                )
                .border(
                    width = borderWidth,
                    brush = Brush.sweepGradient(
                        listOf(
                            primary.copy(alpha = pulse),
                            accent,
                            secondary,
                            primary.copy(alpha = pulse)
                        )
                    ),
                    shape = shape
                )
                .padding(borderWidth + 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size - (borderWidth * 2 + 2.dp))
                    .clip(shape),
                contentAlignment = Alignment.Center,
                content = content
            )
        }

        if (showCornerBadge && frameItem != null && frameId != "frame_royal_crest") {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-3).dp, y = (-3).dp)
                    .size((size.value * 0.32f).coerceIn(14f, 22f).dp)
                    .clip(CircleShape)
                    .background(secondary)
                    .border(1.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = frameItem.iconEmoji,
                    fontSize = (size.value * 0.16f).coerceIn(8f, 12f).sp
                )
            }
        }
    }
}
