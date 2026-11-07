package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * MG Ludo Royal Deep Blue Background:
 * Rich multi-stop navy to royal sapphire gradient with subtle glowing ambient lights,
 * golden stardust particles, and regal geometric highlights.
 */
@Composable
fun MgLudoRoyalBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040A1A), // Ultra deep midnight navy
                        Color(0xFF091C3E), // Deep royal sapphire
                        Color(0xFF0D285A), // MG Ludo royal blue center
                        Color(0xFF081938), // Rich midnight blue
                        Color(0xFF030713)  // Bottom anchor
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top ambient cyan/blue glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3000E5FF), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.12f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.5f, h * 0.12f)
            )

            // Center golden ambient warmth behind game cards
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x25FFD700), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.5f, h * 0.42f)
            )

            // Bottom ambient sapphire glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x201565C0), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.85f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.5f, h * 0.85f)
            )

            // Delicate stardust and golden particles
            val particlePositions = listOf(
                Offset(w * 0.08f, h * 0.18f),
                Offset(w * 0.92f, h * 0.20f),
                Offset(w * 0.14f, h * 0.35f),
                Offset(w * 0.86f, h * 0.38f),
                Offset(w * 0.09f, h * 0.55f),
                Offset(w * 0.91f, h * 0.58f),
                Offset(w * 0.15f, h * 0.70f),
                Offset(w * 0.85f, h * 0.72f),
                Offset(w * 0.50f, h * 0.62f),
                Offset(w * 0.32f, h * 0.28f),
                Offset(w * 0.68f, h * 0.28f)
            )

            for (pos in particlePositions) {
                // Outer sparkle glow
                drawCircle(
                    color = Color(0x33FFD54F),
                    radius = 5f,
                    center = pos
                )
                // Center bright star core
                drawCircle(
                    color = Color(0x99FFF9C4),
                    radius = 2f,
                    center = pos
                )
            }

            // Subtle translucent 3D dice in upper background (matching reference)
            val diceColor = Color(0x1840C4FF)
            val diceDotColor = Color(0x180288D1)
            // Left upper dice
            drawRoundRect(
                color = diceColor,
                topLeft = Offset(w * 0.23f, h * 0.20f),
                size = androidx.compose.ui.geometry.Size(34f, 34f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawCircle(diceDotColor, 3f, Offset(w * 0.23f + 17f, h * 0.20f + 17f))
            drawCircle(diceDotColor, 3f, Offset(w * 0.23f + 8f, h * 0.20f + 8f))
            drawCircle(diceDotColor, 3f, Offset(w * 0.23f + 26f, h * 0.20f + 26f))

            // Right upper dice
            drawRoundRect(
                color = diceColor,
                topLeft = Offset(w * 0.71f, h * 0.20f),
                size = androidx.compose.ui.geometry.Size(36f, 36f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawCircle(diceDotColor, 3.5f, Offset(w * 0.71f + 10f, h * 0.20f + 10f))
            drawCircle(diceDotColor, 3.5f, Offset(w * 0.71f + 26f, h * 0.20f + 26f))
            drawCircle(diceDotColor, 3.5f, Offset(w * 0.71f + 10f, h * 0.20f + 26f))
            drawCircle(diceDotColor, 3.5f, Offset(w * 0.71f + 26f, h * 0.20f + 10f))

            // Tropical green leaves along edges (matching reference lower sides)
            val leafBrush = Brush.radialGradient(
                listOf(Color(0xFF2E7D32).copy(alpha = 0.55f), Color(0xFF1B5E20).copy(alpha = 0.25f), Color.Transparent),
                center = Offset(0f, h * 0.70f),
                radius = 80f
            )
            drawCircle(leafBrush, radius = 80f, center = Offset(-10f, h * 0.70f))
            drawCircle(leafBrush, radius = 70f, center = Offset(-5f, h * 0.78f))

            val leafBrushRight = Brush.radialGradient(
                listOf(Color(0xFF2E7D32).copy(alpha = 0.55f), Color(0xFF1B5E20).copy(alpha = 0.25f), Color.Transparent),
                center = Offset(w, h * 0.70f),
                radius = 80f
            )
            drawCircle(leafBrushRight, radius = 80f, center = Offset(w + 10f, h * 0.70f))
            drawCircle(leafBrushRight, radius = 70f, center = Offset(w + 5f, h * 0.78f))
        }
    }
}
