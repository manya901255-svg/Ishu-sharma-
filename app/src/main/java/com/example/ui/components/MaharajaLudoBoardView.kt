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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor
import com.example.model.Pawn
import com.example.model.PawnState
import com.example.model.Player
import kotlin.math.cos
import kotlin.math.sin

/**
 * Luxurious Maharaja-Inspired Custom Ludo Board Component.
 *
 * Implements a royal Indian aesthetic inspired by Rajasthan palace architecture,
 * Mughal jharokhas, carved teakwood frames, and 24K gold filigree:
 *
 * 1. Four Regal Home Quadrants:
 *    - Deep imperial jewel-tone velvet backgrounds (Mewar Ruby, Mughal Emerald, Jaipur Amber, Jodhpur Sapphire).
 *    - Intricate gold jharokha arch and lotus mandala inlays.
 *    - Polished Makrana marble inner courtyard cards with soft drop shadows.
 *    - Four recessed circular token pedestals with 24K gold fluted rims and jewel core studs.
 *
 * 2. Marble Paths & Gilded Home Runways:
 *    - Polished ivory marble tiles with antique brass grid borders.
 *    - Four jewel-paved home stretch runways with gold chevron rank markings.
 *    - Ornate golden ceremonial entrance daggers/arrows pointing into the palace.
 *    - Eight 8-pointed Imperial Maharaja Safe Stars with radiant starbursts.
 *
 * 3. Central Maharaja Durbar Finish Area:
 *    - Four converging royal velvet triangular pavilions.
 *    - Golden meridian lines with diamond studs.
 *    - Central multi-tiered 24K gold Durbar Medallion with lotus petal rim and Maharaja Crown.
 *
 * 4. Interactive Pawns Layer:
 *    - 3D royal crown tokens with multi-token fan-out stacking and turn halos.
 */
@Composable
fun MaharajaLudoBoardView(
    players: List<Player>,
    onPawnClick: (Pawn) -> Unit,
    modifier: Modifier = Modifier,
    pawnCrownEmoji: String = "👑",
    pawnSkinId: String = "crown_maharaja",
    isPawnTrailEnabled: Boolean = true
) {
    // Shimmer animation for golden filigree accents
    val infiniteTransition = rememberInfiniteTransition(label = "maharajaBoardShimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(20.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF2E1708), // Dark royal teakwood
                        Color(0xFF1B0C04),
                        Color(0xFF0F0602)
                    )
                )
            )
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFE082),
                        Color(0xFFFFB300),
                        Color(0xFFFFE082),
                        Color(0xFFFF8F00),
                        Color(0xFFFFE082)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(6.dp)
            .testTag("maharaja_ludo_board_view"),
        contentAlignment = Alignment.TopStart
    ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        // 1. Canvas Layer: Board architecture, quadrants, tracks, home runways, stars, and durbar
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = size.width / 15f

            // Polished Makrana marble floor base
            drawRect(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFFDF9), Color(0xFFFBF7F0), Color(0xFFF4ECE0))
                )
            )

            // Inner gilded border separating frame from board
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color(0xFFFFD54F), Color(0xFFFF8F00), Color(0xFFFFD54F))
                ),
                style = Stroke(width = 2.dp.toPx())
            )

            // 1A. Four Regal Home Quadrants (6x6 each)
            // Top-Left: Red (Mewar Ruby Palace)
            drawMaharajaHomeQuadrant(
                topLeft = Offset(0f, 0f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFFB71C1C),
                deepVelvet = Color(0xFF560000),
                accentGold = Color(0xFFFFD54F),
                emblemEmoji = "👑",
                emblemTitle = "RUBY PALACE"
            )

            // Top-Right: Green (Mughal Emerald Palace)
            drawMaharajaHomeQuadrant(
                topLeft = Offset(cellPx * 9f, 0f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFF1B5E20),
                deepVelvet = Color(0xFF003300),
                accentGold = Color(0xFFFFD54F),
                emblemEmoji = "🪷",
                emblemTitle = "EMERALD MAHAL"
            )

            // Bottom-Left: Yellow (Jaipur Amber Palace)
            drawMaharajaHomeQuadrant(
                topLeft = Offset(0f, cellPx * 9f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFFF57F17),
                deepVelvet = Color(0xFF6A3800),
                accentGold = Color(0xFFFFECB3),
                emblemEmoji = "☀️",
                emblemTitle = "AMBER FORT"
            )

            // Bottom-Right: Blue (Jodhpur Sapphire Palace)
            drawMaharajaHomeQuadrant(
                topLeft = Offset(cellPx * 9f, cellPx * 9f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFF0D47A1),
                deepVelvet = Color(0xFF001A4E),
                accentGold = Color(0xFFFFD54F),
                emblemEmoji = "🦚",
                emblemTitle = "SAPPHIRE COURT"
            )

            // 1B. Four Colored Starting Cells with Royal Inlay
            // Red start at (1, 6)
            drawMaharajaStartCell(1, 6, cellPx, Color(0xFFB71C1C), Color(0xFFFFD54F))
            // Green start at (8, 1)
            drawMaharajaStartCell(8, 1, cellPx, Color(0xFF1B5E20), Color(0xFFFFD54F))
            // Blue start at (13, 8)
            drawMaharajaStartCell(13, 8, cellPx, Color(0xFF0D47A1), Color(0xFFFFD54F))
            // Yellow start at (6, 13)
            drawMaharajaStartCell(6, 13, cellPx, Color(0xFFF57F17), Color(0xFFFFD54F))

            // 1C. Four Gilded Home Runways (5 steps into palace)
            // Left: Red runway (cols 1..5, row 7)
            for (col in 1..5) {
                drawMaharajaRunwayStep(col, 7, cellPx, Color(0xFFB71C1C), col)
            }

            // Top: Green runway (col 7, rows 1..5)
            for (row in 1..5) {
                drawMaharajaRunwayStep(7, row, cellPx, Color(0xFF1B5E20), row)
            }

            // Right: Blue runway (cols 13..9, row 7)
            for (col in 9..13) {
                val step = 14 - col
                drawMaharajaRunwayStep(col, 7, cellPx, Color(0xFF0D47A1), step)
            }

            // Bottom: Yellow runway (col 7, rows 13..9)
            for (row in 9..13) {
                val step = 14 - row
                drawMaharajaRunwayStep(7, row, cellPx, Color(0xFFF57F17), step)
            }

            // 1D. Track Grid Lines over all 52 track cells
            val brassBorder = Color(0xFFC5A059).copy(alpha = 0.75f)
            for (pos in StandardLudoBoardLayout.TRACK_COORDINATES) {
                drawRoundRect(
                    color = brassBorder,
                    topLeft = Offset(pos.first * cellPx, pos.second * cellPx),
                    size = Size(cellPx, cellPx),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // 1E. Directional Royal Katar/Arrows at Home Entrances
            // Red Entrance Arrow at (0, 7) pointing Right
            drawMaharajaArrow(
                center = Offset(0.5f * cellPx, 7.5f * cellPx),
                size = cellPx * 0.48f,
                direction = MaharajaArrowDirection.RIGHT,
                color = Color(0xFFB71C1C)
            )

            // Green Entrance Arrow at (7, 0) pointing Down
            drawMaharajaArrow(
                center = Offset(7.5f * cellPx, 0.5f * cellPx),
                size = cellPx * 0.48f,
                direction = MaharajaArrowDirection.DOWN,
                color = Color(0xFF1B5E20)
            )

            // Blue Entrance Arrow at (14, 7) pointing Left
            drawMaharajaArrow(
                center = Offset(14.5f * cellPx, 7.5f * cellPx),
                size = cellPx * 0.48f,
                direction = MaharajaArrowDirection.LEFT,
                color = Color(0xFF0D47A1)
            )

            // Yellow Entrance Arrow at (7, 14) pointing Up
            drawMaharajaArrow(
                center = Offset(7.5f * cellPx, 14.5f * cellPx),
                size = cellPx * 0.48f,
                direction = MaharajaArrowDirection.UP,
                color = Color(0xFFF57F17)
            )

            // 1F. Central Maharaja Durbar (Throne Room Finish Area: 3x3)
            val centerLeft = 6 * cellPx
            val centerTop = 6 * cellPx
            val centerRight = 9 * cellPx
            val centerBottom = 9 * cellPx
            val cx = 7.5f * cellPx
            val cy = 7.5f * cellPx

            // Top Triangle (Emerald Durbar)
            val topTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(centerRight, centerTop)
                lineTo(cx, cy)
                close()
            }
            drawPath(
                path = topTriangle,
                brush = Brush.radialGradient(
                    listOf(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF0A3311)),
                    center = Offset(cx, cy - cellPx * 0.7f),
                    radius = cellPx * 1.5f
                )
            )

            // Left Triangle (Ruby Durbar)
            val leftTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(centerLeft, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(
                path = leftTriangle,
                brush = Brush.radialGradient(
                    listOf(Color(0xFFD32F2F), Color(0xFFB71C1C), Color(0xFF560000)),
                    center = Offset(cx - cellPx * 0.7f, cy),
                    radius = cellPx * 1.5f
                )
            )

            // Right Triangle (Sapphire Durbar)
            val rightTriangle = Path().apply {
                moveTo(centerRight, centerTop)
                lineTo(centerRight, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(
                path = rightTriangle,
                brush = Brush.radialGradient(
                    listOf(Color(0xFF1976D2), Color(0xFF0D47A1), Color(0xFF001F5C)),
                    center = Offset(cx + cellPx * 0.7f, cy),
                    radius = cellPx * 1.5f
                )
            )

            // Bottom Triangle (Amber Durbar)
            val bottomTriangle = Path().apply {
                moveTo(centerLeft, centerBottom)
                lineTo(centerRight, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(
                path = bottomTriangle,
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFA000), Color(0xFFF57F17), Color(0xFF8A4600)),
                    center = Offset(cx, cy + cellPx * 0.7f),
                    radius = cellPx * 1.5f
                )
            )

            // Meridian Golden Filigree Lines
            val goldDividerBrush = Brush.linearGradient(
                listOf(Color(0xFFFFD54F), Color(0xFFFF8F00), Color(0xFFFFD54F))
            )
            drawLine(
                brush = goldDividerBrush,
                start = Offset(centerLeft, centerTop),
                end = Offset(centerRight, centerBottom),
                strokeWidth = 2.5.dp.toPx()
            )
            drawLine(
                brush = goldDividerBrush,
                start = Offset(centerRight, centerTop),
                end = Offset(centerLeft, centerBottom),
                strokeWidth = 2.5.dp.toPx()
            )

            // Outer Durbar Frame
            drawRect(
                brush = goldDividerBrush,
                topLeft = Offset(centerLeft, centerTop),
                size = Size(3 * cellPx, 3 * cellPx),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Multi-Tiered Central Maharaja Gold Medallion
            drawMaharajaDurbarMedallion(cx, cy, cellPx * 0.78f, shimmerAlpha)

            // 1G. Eight Imperial Safe Stars with Specular Diamond Core
            // 4 Safe track squares
            val safeCoords = listOf(
                6 to 2,   // Red approach star
                12 to 6,  // Green approach star
                8 to 12,  // Yellow approach star
                2 to 8    // Blue approach star
            )
            for ((sc, sr) in safeCoords) {
                val starX = (sc + 0.5f) * cellPx
                val starY = (sr + 0.5f) * cellPx
                drawMaharajaImperialStar(starX, starY, cellPx * 0.38f, isApproachStar = true)
            }

            // 4 Starting Safe Stars (on start tiles)
            drawMaharajaImperialStar((1 + 0.5f) * cellPx, (6 + 0.5f) * cellPx, cellPx * 0.34f, isApproachStar = false)
            drawMaharajaImperialStar((8 + 0.5f) * cellPx, (1 + 0.5f) * cellPx, cellPx * 0.34f, isApproachStar = false)
            drawMaharajaImperialStar((13 + 0.5f) * cellPx, (8 + 0.5f) * cellPx, cellPx * 0.34f, isApproachStar = false)
            drawMaharajaImperialStar((6 + 0.5f) * cellPx, (13 + 0.5f) * cellPx, cellPx * 0.34f, isApproachStar = false)
        }

        // 2. Interactive Pawns Layer with Multi-Token Stacking
        val allPawns = players.flatMap { it.pawns }

        val rawPawnPositions = remember(allPawns) {
            allPawns.map { pawn ->
                val (gridX, gridY) = when (pawn.state) {
                    PawnState.IN_BASE -> StandardLudoBoardLayout.getBasePocketFloatPosition(pawn.color, pawn.id)
                    PawnState.ON_BOARD -> {
                        val pos = pawn.trackPosition.coerceIn(0, 51)
                        val pair = StandardLudoBoardLayout.TRACK_COORDINATES[pos]
                        pair.first.toFloat() to pair.second.toFloat()
                    }
                    PawnState.IN_HOME_STRETCH -> {
                        val pair = StandardLudoBoardLayout.getHomeStretchGridPosition(pawn.color, pawn.homeStep)
                        pair.first.toFloat() to pair.second.toFloat()
                    }
                    PawnState.FINISHED -> 7.0f to 7.0f
                }
                pawn to (gridX to gridY)
            }
        }

        val pawnsByCell = remember(rawPawnPositions) {
            rawPawnPositions
                .filter { it.first.state != PawnState.IN_BASE }
                .groupBy { "${"%.2f".format(it.second.first)}_${"%.2f".format(it.second.second)}" }
        }

        rawPawnPositions.forEach { (pawn, basePos) ->
            val (baseX, baseY) = basePos

            val (stackOffsetX, stackOffsetY) = if (pawn.state != PawnState.IN_BASE && pawn.state != PawnState.FINISHED) {
                val key = "${"%.2f".format(baseX)}_${"%.2f".format(baseY)}"
                val cellGroup = pawnsByCell[key] ?: emptyList()
                val indexInCell = cellGroup.indexOfFirst { it.first.id == pawn.id && it.first.color == pawn.color }
                val totalInCell = cellGroup.size

                if (totalInCell > 1 && indexInCell >= 0) {
                    when (totalInCell) {
                        2 -> when (indexInCell) {
                            0 -> -4.5.dp to -4.5.dp
                            else -> 4.5.dp to 4.5.dp
                        }
                        3 -> when (indexInCell) {
                            0 -> -5.dp to -5.dp
                            1 -> 5.dp to -5.dp
                            else -> 0.dp to 5.dp
                        }
                        else -> when (indexInCell) {
                            0 -> -5.dp to -5.dp
                            1 -> 5.dp to -5.dp
                            2 -> -5.dp to 5.dp
                            else -> 5.dp to 5.dp
                        }
                    }
                } else 0.dp to 0.dp
            } else if (pawn.state == PawnState.FINISHED) {
                when (pawn.color) {
                    LudoColor.RED -> (-7).dp to 0.dp
                    LudoColor.GREEN -> 0.dp to (-7).dp
                    LudoColor.BLUE -> 7.dp to 0.dp
                    LudoColor.YELLOW -> 0.dp to 7.dp
                }
            } else 0.dp to 0.dp

            val pawnCenterLeft = (cellSize * baseX) + (cellSize / 2f) + stackOffsetX
            val pawnCenterTop = (cellSize * baseY) + (cellSize / 2f) + stackOffsetY
            val pawnRenderSize = if (pawn.state == PawnState.IN_BASE) cellSize * 1.15f else cellSize * 0.98f

            Box(
                modifier = Modifier
                    .offset(
                        x = pawnCenterLeft - (pawnRenderSize / 2f),
                        y = pawnCenterTop - (pawnRenderSize / 2f)
                    )
            ) {
                RoyalCrownPawn(
                    pawn = pawn,
                    size = pawnRenderSize,
                    crownEmoji = pawnCrownEmoji,
                    pawnSkin = pawnSkinId,
                    isTrailEnabled = isPawnTrailEnabled,
                    onClick = if (pawn.isMovable) { { onPawnClick(pawn) } } else null
                )
            }
        }
    }
}

/**
 * Draws an authentic Maharaja Palace Home Quadrant with Jharokha arch and marble card
 */
private fun DrawScope.drawMaharajaHomeQuadrant(
    topLeft: Offset,
    size: Size,
    cellPx: Float,
    primaryColor: Color,
    deepVelvet: Color,
    accentGold: Color,
    emblemEmoji: String,
    emblemTitle: String
) {
    // 1. Deep royal velvet background with rich gradient
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor, deepVelvet),
            center = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f),
            radius = size.width * 0.75f
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
    )

    // Gold filigree quadrant outer border
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(accentGold, Color(0xFFFF8F00), accentGold)
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // 2. Inner Polished Makrana Marble Courtyard Card
    val cardMargin = cellPx * 0.65f
    val cardTopLeft = Offset(topLeft.x + cardMargin, topLeft.y + cardMargin)
    val cardSize = Size(size.width - (cardMargin * 2f), size.height - (cardMargin * 2f))

    // Soft courtyard shadow
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(cardTopLeft.x + 2.dp.toPx(), cardTopLeft.y + 3.dp.toPx()),
        size = cardSize,
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )

    // Marble card base
    drawRoundRect(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFFDF8), Color(0xFFF9F5EC), Color(0xFFF1E8D9)),
            center = Offset(cardTopLeft.x + cardSize.width / 2f, cardTopLeft.y + cardSize.height / 2f),
            radius = cardSize.width * 0.7f
        ),
        topLeft = cardTopLeft,
        size = cardSize,
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )

    // 24K Gold lace border around marble card
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(accentGold, Color(0xFFFFB300), accentGold)
        ),
        topLeft = cardTopLeft,
        size = cardSize,
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
        style = Stroke(width = 1.8.dp.toPx())
    )

    // Center Palace Lotus Watermark
    val cardCenterX = cardTopLeft.x + cardSize.width / 2f
    val cardCenterY = cardTopLeft.y + cardSize.height / 2f
    drawCircle(
        color = primaryColor.copy(alpha = 0.08f),
        radius = cellPx * 0.85f,
        center = Offset(cardCenterX, cardCenterY)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(accentGold.copy(alpha = 0.4f), Color.Transparent)
        ),
        radius = cellPx * 1.1f,
        center = Offset(cardCenterX, cardCenterY)
    )

    // 3. Four Royal Token Pedestals (Sockets)
    val socketCenters = listOf(
        Offset(topLeft.x + cellPx * 2.25f, topLeft.y + cellPx * 2.25f),
        Offset(topLeft.x + cellPx * 4.25f, topLeft.y + cellPx * 2.25f),
        Offset(topLeft.x + cellPx * 2.25f, topLeft.y + cellPx * 4.25f),
        Offset(topLeft.x + cellPx * 4.25f, topLeft.y + cellPx * 4.25f)
    )

    val socketRadius = cellPx * 0.62f

    socketCenters.forEach { center ->
        // Drop shadow under socket
        drawCircle(
            color = Color(0x30000000),
            radius = socketRadius + 1.dp.toPx(),
            center = Offset(center.x + 1.dp.toPx(), center.y + 1.5.dp.toPx())
        )

        // Outer 24K Gold Scalloped Ring
        drawCircle(
            brush = Brush.linearGradient(
                listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFFF8F00))
            ),
            radius = socketRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Recessed Velvet Pedestal
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.85f),
                    deepVelvet
                ),
                center = center,
                radius = socketRadius
            ),
            radius = socketRadius - 1.dp.toPx(),
            center = center
        )

        // Center Jewel Stud in matching jewel color
        drawCircle(
            color = primaryColor,
            radius = socketRadius * 0.42f,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = socketRadius * 0.16f,
            center = Offset(center.x - 1.5.dp.toPx(), center.y - 1.5.dp.toPx())
        )
    }
}

/**
 * Draws a royal starting cell with golden trim and 8-pointed star
 */
private fun DrawScope.drawMaharajaStartCell(
    col: Int,
    row: Int,
    cellPx: Float,
    baseColor: Color,
    goldColor: Color
) {
    val topLeft = Offset(col * cellPx, row * cellPx)
    val size = Size(cellPx, cellPx)

    drawRect(
        brush = Brush.radialGradient(
            listOf(baseColor, baseColor.copy(alpha = 0.85f)),
            center = Offset(topLeft.x + cellPx / 2f, topLeft.y + cellPx / 2f),
            radius = cellPx
        ),
        topLeft = topLeft,
        size = size
    )

    drawRect(
        brush = Brush.linearGradient(
            listOf(goldColor, Color(0xFFFF8F00), goldColor)
        ),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = 1.6.dp.toPx())
    )
}

/**
 * Draws an individual step in the home stretch runway with rank chevron
 */
private fun DrawScope.drawMaharajaRunwayStep(
    col: Int,
    row: Int,
    cellPx: Float,
    carpetColor: Color,
    stepNumber: Int
) {
    val topLeft = Offset(col * cellPx, row * cellPx)
    val size = Size(cellPx, cellPx)

    // Deep velvet carpet
    drawRect(
        brush = Brush.radialGradient(
            listOf(carpetColor, carpetColor.copy(alpha = 0.90f)),
            center = Offset(topLeft.x + cellPx / 2f, topLeft.y + cellPx / 2f),
            radius = cellPx * 0.8f
        ),
        topLeft = topLeft,
        size = size
    )

    // Gold borders
    drawRect(
        color = Color(0xFFFFD54F).copy(alpha = 0.8f),
        topLeft = topLeft,
        size = size,
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Inner subtle gold chevron indicating progression to center
    val centerX = topLeft.x + cellPx / 2f
    val centerY = topLeft.y + cellPx / 2f
    drawCircle(
        color = Color.White.copy(alpha = 0.22f),
        radius = cellPx * 0.22f,
        center = Offset(centerX, centerY)
    )
}

/**
 * Draws the central Maharaja Durbar Medallion with golden lotus petals and crown
 */
private fun DrawScope.drawMaharajaDurbarMedallion(
    cx: Float,
    cy: Float,
    radius: Float,
    shimmerAlpha: Float
) {
    // Drop shadow
    drawCircle(
        color = Color(0x66000000),
        radius = radius + 2.dp.toPx(),
        center = Offset(cx + 1.dp.toPx(), cy + 2.dp.toPx())
    )

    // Outer 24K Gold Lotus Medallion Disc
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color(0xFFFFF9C4),
                Color(0xFFFFD54F),
                Color(0xFFFF8F00),
                Color(0xFFB36B00)
            ),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // Gold Fluted Lotus Petal Rim
    val petalCount = 12
    for (i in 0 until petalCount) {
        val angle = Math.toRadians((i * (360.0 / petalCount)))
        val px = cx + (radius * 0.90f * cos(angle)).toFloat()
        val py = cy + (radius * 0.90f * sin(angle)).toFloat()
        drawCircle(
            color = Color(0xFFFFECB3),
            radius = 2.2.dp.toPx(),
            center = Offset(px, py)
        )
    }

    // Inner Royal Velvet Core (Imperial Ruby & Gold Sun)
    val innerRadius = radius * 0.68f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color(0xFFD32F2F),
                Color(0xFF880E4F),
                Color(0xFF3E001F)
            ),
            center = Offset(cx, cy),
            radius = innerRadius
        ),
        radius = innerRadius,
        center = Offset(cx, cy)
    )

    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = shimmerAlpha),
        radius = innerRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Central 8-Pointed Star of Jaipur
    drawStarShape(
        cx = cx,
        cy = cy,
        outerRadius = innerRadius * 0.65f,
        innerRadius = innerRadius * 0.32f,
        points = 8,
        color = Color(0xFFFFD54F)
    )

    // Center Jewel
    drawCircle(
        color = Color(0xFFFFFFFF),
        radius = 2.dp.toPx(),
        center = Offset(cx, cy)
    )
}

/**
 * Draws an 8-pointed Maharaja Imperial Star with dual-diamond overlapping geometry
 */
private fun DrawScope.drawMaharajaImperialStar(
    cx: Float,
    cy: Float,
    size: Float,
    isApproachStar: Boolean
) {
    val outerColor = if (isApproachStar) Color(0xFFFFD54F) else Color.White
    val coreColor = if (isApproachStar) Color(0xFFE65100) else Color(0xFF212121)

    // Star aura glow
    drawCircle(
        color = outerColor.copy(alpha = 0.25f),
        radius = size * 0.85f,
        center = Offset(cx, cy)
    )

    // Outer 8-pointed star
    drawStarShape(
        cx = cx,
        cy = cy,
        outerRadius = size * 0.70f,
        innerRadius = size * 0.35f,
        points = 8,
        color = outerColor
    )

    // Inner diamond core
    drawStarShape(
        cx = cx,
        cy = cy,
        outerRadius = size * 0.38f,
        innerRadius = size * 0.18f,
        points = 8,
        color = coreColor
    )

    // Center star glint
    drawCircle(
        color = Color.White,
        radius = size * 0.10f,
        center = Offset(cx, cy)
    )
}

private enum class MaharajaArrowDirection { UP, DOWN, LEFT, RIGHT }

/**
 * Draws an ornate directional arrow for runway entrances
 */
private fun DrawScope.drawMaharajaArrow(
    center: Offset,
    size: Float,
    direction: MaharajaArrowDirection,
    color: Color
) {
    val path = Path()
    val half = size / 2f
    val tip = size * 0.6f

    when (direction) {
        MaharajaArrowDirection.RIGHT -> {
            path.moveTo(center.x + tip, center.y)
            path.lineTo(center.x - half, center.y - half)
            path.lineTo(center.x - half * 0.4f, center.y)
            path.lineTo(center.x - half, center.y + half)
            path.close()
        }
        MaharajaArrowDirection.DOWN -> {
            path.moveTo(center.x, center.y + tip)
            path.lineTo(center.x - half, center.y - half)
            path.lineTo(center.x, center.y - half * 0.4f)
            path.lineTo(center.x + half, center.y - half)
            path.close()
        }
        MaharajaArrowDirection.LEFT -> {
            path.moveTo(center.x - tip, center.y)
            path.lineTo(center.x + half, center.y - half)
            path.lineTo(center.x + half * 0.4f, center.y)
            path.lineTo(center.x + half, center.y + half)
            path.close()
        }
        MaharajaArrowDirection.UP -> {
            path.moveTo(center.x, center.y - tip)
            path.lineTo(center.x - half, center.y + half)
            path.lineTo(center.x, center.y + half * 0.4f)
            path.lineTo(center.x + half, center.y + half)
            path.close()
        }
    }

    // Shadow
    drawPath(
        path = path,
        color = Color(0x33000000)
    )
    // Main arrow body
    drawPath(
        path = path,
        color = color
    )
    // Gold trim
    drawPath(
        path = path,
        color = Color(0xFFFFD54F),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * Geometric star path utility
 */
private fun DrawScope.drawStarShape(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    innerRadius: Float,
    points: Int,
    color: Color
) {
    val path = Path()
    val totalSteps = points * 2
    val stepAngle = (2.0 * Math.PI) / totalSteps

    for (i in 0 until totalSteps) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * stepAngle - (Math.PI / 2.0)
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy + (r * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    drawPath(path = path, color = color, style = Fill)
}
