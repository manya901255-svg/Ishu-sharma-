package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.BUILT_IN_WALLPAPERS
import com.example.model.WallpaperItem
import com.example.model.WallpaperTarget
import java.io.File

/**
 * Universal Wallpaper Renderer for MG Ludo.
 * Supports custom uploaded photos, built-in gradients, textures and drawables.
 */
@Composable
fun WallpaperBackgroundView(
    wallpaperIdOrPath: String?,
    target: WallpaperTarget,
    modifier: Modifier = Modifier,
    dimOverlayAlpha: Float = 0.0f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val activeId = if (wallpaperIdOrPath.isNullOrBlank()) target.defaultWallpaperId else wallpaperIdOrPath
    val context = LocalContext.current

    // Check if it's a local file
    val isLocalFile = remember(activeId) {
        activeId.startsWith("/") ||
        activeId.startsWith("file:") ||
        activeId.startsWith("content:") ||
        (activeId.contains(File.separator) && File(activeId).exists())
    }

    val builtInPreset: WallpaperItem? = remember(activeId) {
        if (!isLocalFile) {
            BUILT_IN_WALLPAPERS.firstOrNull { it.id == activeId }
                ?: BUILT_IN_WALLPAPERS.firstOrNull { it.id == target.defaultWallpaperId }
        } else null
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // 1. User-uploaded custom image from phone gallery
            isLocalFile -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (activeId.startsWith("file:") || activeId.startsWith("content:")) activeId else File(activeId))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Custom Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Default Royal Deep Blue with full animated stardust canvas
            builtInPreset?.id == "default_royal_navy" -> {
                MgLudoRoyalBackground(modifier = Modifier.fillMaxSize())
            }

            // 3. Built-in texture drawable (e.g. Royal Purple Wood)
            builtInPreset?.drawableRes != null -> {
                Image(
                    painter = painterResource(id = builtInPreset.drawableRes),
                    contentDescription = builtInPreset.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. Built-in gradient theme
            builtInPreset?.gradientColors != null -> {
                val colors = remember(builtInPreset.gradientColors) {
                    builtInPreset.gradientColors.map { Color(it) }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(colors))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Ambient top glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.15f),
                                radius = w * 0.7f
                            ),
                            radius = w * 0.7f,
                            center = Offset(w * 0.5f, h * 0.15f)
                        )

                        // Ambient bottom glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.85f),
                                radius = w * 0.6f
                            ),
                            radius = w * 0.6f,
                            center = Offset(w * 0.5f, h * 0.85f)
                        )
                    }
                }
            }

            // 5. Fallback Default
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.bg_purple_wood),
                    contentDescription = "Default Royal Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Optional dimming scrim to ensure readable text/UI
        if (dimOverlayAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimOverlayAlpha))
            )
        }

        // Foreground content
        content()
    }
}
