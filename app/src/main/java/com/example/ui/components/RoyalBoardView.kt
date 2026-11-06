package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor
import com.example.model.Pawn
import com.example.model.PawnState
import com.example.model.Player
import com.example.model.RegalBoardTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic 2D Royal Ludo Board View crafted to exactly match the MG Ludo reference screenshot.
 * Features:
 * - 4 Gradient Corner Quadrants (Top-Left: Blue, Top-Right: Red, Bottom-Left: Yellow, Bottom-Right: Green)
 * - Inset rounded white cards with 4 translucent frosted circular pawn sockets
 * - Clean white track cells with subtle borders and colored home runways
 * - 8 Embossed 3D Golden Star Medallions at exact standard positions
 * - 4-Color Center Finish triangles crowned with a central 3D golden sphere and star apex
 * - Jewel-glass crown pawns with golden pedestal bases, drop shadows, and active turn glow
 */
@Composable
fun RoyalBoardView(
    players: List<Player>,
    onPawnClick: (Pawn) -> Unit,
    pawnCrownEmoji: String = "👑",
    boardTheme: RegalBoardTheme,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("royal_board_2d_view"),
        contentAlignment = Alignment.Center
    ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        // 1. Canvas Layer: Board background, 15x15 grid, home stretches, safe stars, bases, apex
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = size.width / 15f
            val w = size.width
            val h = size.height

            // Board Canvas Base Background (Pure White)
            drawRect(color = Color.White)

            // 1A. Four Colored Home Quadrants with Inset White Cards and Frosted Sockets
            // Top-Left: Blue
            drawAuthenticHomeQuadrant(
                color = LudoColor.BLUE,
                topLeft = Offset(0f, 0f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                gradient = listOf(Color(0xFF2979FF), Color(0xFF1976D2), Color(0xFF0D47A1)),
                socketTint = Color(0xFF29B6F6),
                socketBorder = Color(0xFF64B5F6)
            )

            // Top-Right: Red
            drawAuthenticHomeQuadrant(
                color = LudoColor.RED,
                topLeft = Offset(cellPx * 9f, 0f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                gradient = listOf(Color(0xFFFF1744), Color(0xFFD50000), Color(0xFFB71C1C)),
                socketTint = Color(0xFFFF5252),
                socketBorder = Color(0xFFE57373)
            )

            // Bottom-Left: Yellow
            drawAuthenticHomeQuadrant(
                color = LudoColor.YELLOW,
                topLeft = Offset(0f, cellPx * 9f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                gradient = listOf(Color(0xFFFFD600), Color(0xFFFFAB00), Color(0xFFFF8F00)),
                socketTint = Color(0xFFFFD54F),
                socketBorder = Color(0xFFFFCA28)
            )

            // Bottom-Right: Green
            drawAuthenticHomeQuadrant(
                color = LudoColor.GREEN,
                topLeft = Offset(cellPx * 9f, cellPx * 9f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                gradient = listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF1B5E20)),
                socketTint = Color(0xFF69F0AE),
                socketBorder = Color(0xFF81C784)
            )

            // 1B. Draw 52 Outer Track Cell Outlines
            val trackGridColor = Color(0xFFD1D5DB)
            for (pos in StandardLudoBoardLayout.TRACK_COORDINATES) {
                val x = pos.first * cellPx
                val y = pos.second * cellPx
                drawRect(
                    color = trackGridColor,
                    topLeft = Offset(x, y),
                    size = Size(cellPx, cellPx),
                    style = Stroke(width = 1.2f)
                )
            }

            // 1C. Four Colored Home Runways (Home Stretches leading to Center)
            // Top: Red runway (col 7, rows 1..5)
            for (step in 1..5) {
                val x = 7 * cellPx
                val y = step * cellPx
                drawRect(
                    color = Color(0xFFEF5350),
                    topLeft = Offset(x + 0.8f, y + 0.8f),
                    size = Size(cellPx - 1.6f, cellPx - 1.6f)
                )
            }

            // Left: Blue runway (cols 1..5, row 7)
            for (step in 1..5) {
                val x = step * cellPx
                val y = 7 * cellPx
                drawRect(
                    color = Color(0xFF29B6F6),
                    topLeft = Offset(x + 0.8f, y + 0.8f),
                    size = Size(cellPx - 1.6f, cellPx - 1.6f)
                )
            }

            // Right: Green runway (cols 9..13, row 7)
            for (col in 9..13) {
                val x = col * cellPx
                val y = 7 * cellPx
                drawRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(x + 0.8f, y + 0.8f),
                    size = Size(cellPx - 1.6f, cellPx - 1.6f)
                )
            }

            // Bottom: Yellow runway (col 7, rows 9..13)
            for (row in 9..13) {
                val x = 7 * cellPx
                val y = row * cellPx
                drawRect(
                    color = Color(0xFFFDD835),
                    topLeft = Offset(x + 0.8f, y + 0.8f),
                    size = Size(cellPx - 1.6f, cellPx - 1.6f)
                )
            }

            // 1D. Central Finish Area with 4 Triangles
            val centerLeft = 6 * cellPx
            val centerTop = 6 * cellPx
            val centerRight = 9 * cellPx
            val centerBottom = 9 * cellPx
            val cx = 7.5f * cellPx
            val cy = 7.5f * cellPx

            // Top Triangle (Red)
            val redTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(centerRight, centerTop)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = redTriangle, color = Color(0xFFE53935))

            // Left Triangle (Blue)
            val blueTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(centerLeft, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = blueTriangle, color = Color(0xFF1E88E5))

            // Right Triangle (Green)
            val greenTriangle = Path().apply {
                moveTo(centerRight, centerTop)
                lineTo(centerRight, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = greenTriangle, color = Color(0xFF43A047))

            // Bottom Triangle (Yellow)
            val yellowTriangle = Path().apply {
                moveTo(centerLeft, centerBottom)
                lineTo(centerRight, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = yellowTriangle, color = Color(0xFFFBC02D))

            // Central Finish Separator Lines
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(centerLeft, centerTop),
                end = Offset(centerRight, centerBottom),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(centerRight, centerTop),
                end = Offset(centerLeft, centerBottom),
                strokeWidth = 1.5f
            )

            // 1E. Central 3D Golden Sphere Medallion with Star
            drawCentralGoldenSphere(cx, cy, cellPx * 0.78f)

            // 1F. Eight 3D Golden Safe Star Medallions
            val safeStarCoords = listOf(
                1 to 6,   // Blue Start
                6 to 2,   // Red approach star
                8 to 1,   // Red Start
                12 to 6,  // Green approach star
                13 to 8,  // Green Start
                8 to 12,  // Yellow approach star
                6 to 13,  // Yellow Start
                2 to 8    // Blue approach star
            )

            for ((sc, sr) in safeStarCoords) {
                val starCenterX = (sc + 0.5f) * cellPx
                val starCenterY = (sr + 0.5f) * cellPx
                drawAuthenticGoldenStarMedallion(starCenterX, starCenterY, cellPx * 0.38f)
            }
        }

        // 2. Interactive Pawns Layer matching Screenshot aesthetics
        val allPawns = players.flatMap { it.pawns }

        val pawnPositions = remember(allPawns) {
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
                Triple(pawn, gridX, gridY)
            }
        }

        pawnPositions.forEach { (pawn, colFloat, rowFloat) ->
            RoyalAuthenticPawn(
                pawn = pawn,
                colFloat = colFloat,
                rowFloat = rowFloat,
                cellSize = cellSize,
                pawnCrownEmoji = pawnCrownEmoji,
                onPawnClick = onPawnClick
            )
        }
    }
}

/**
 * Draws an authentic Home Quadrant matching the reference screenshot:
 * Rich gradient background, rounded corners, inner white card, and 4 frosted circular sockets.
 */
private fun DrawScope.drawAuthenticHomeQuadrant(
    color: LudoColor,
    topLeft: Offset,
    size: Size,
    cellPx: Float,
    gradient: List<Color>,
    socketTint: Color,
    socketBorder: Color
) {
    val cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())

    // 1. Quadrant Base with Gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = gradient,
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius
    )

    // 2. Inner White Card
    val cardInset = cellPx * 0.72f
    val cardWidth = size.width - (cardInset * 2f)
    val cardHeight = size.height - (cardInset * 2f)
    val cardTopLeft = Offset(topLeft.x + cardInset, topLeft.y + cardInset)
    val cardCornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())

    // Subtle drop shadow under white card
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(cardTopLeft.x + 1f, cardTopLeft.y + 2f),
        size = Size(cardWidth, cardHeight),
        cornerRadius = cardCornerRadius
    )

    // White Card Surface
    drawRoundRect(
        color = Color.White,
        topLeft = cardTopLeft,
        size = Size(cardWidth, cardHeight),
        cornerRadius = cardCornerRadius
    )

    // 3. Four Frosted Circular Sockets
    val socketRadius = cellPx * 0.68f
    val colOffset1 = cardTopLeft.x + (cardWidth * 0.28f)
    val colOffset2 = cardTopLeft.x + (cardWidth * 0.72f)
    val rowOffset1 = cardTopLeft.y + (cardHeight * 0.28f)
    val rowOffset2 = cardTopLeft.y + (cardHeight * 0.72f)

    val centers = listOf(
        Offset(colOffset1, rowOffset1),
        Offset(colOffset2, rowOffset1),
        Offset(colOffset1, rowOffset2),
        Offset(colOffset2, rowOffset2)
    )

    for (center in centers) {
        // Soft outer shadow ring
        drawCircle(
            color = Color(0x18000000),
            radius = socketRadius + 1.5f,
            center = Offset(center.x, center.y + 1f)
        )

        // Frosted radial gradient filling
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    socketTint.copy(alpha = 0.22f),
                    socketTint.copy(alpha = 0.45f)
                ),
                center = center,
                radius = socketRadius
            ),
            radius = socketRadius,
            center = center
        )

        // Inner rim stroke
        drawCircle(
            color = socketBorder.copy(alpha = 0.75f),
            radius = socketRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Draws the prominent 3D Central Golden Sphere Medallion with star apex.
 */
private fun DrawScope.drawCentralGoldenSphere(
    cx: Float,
    cy: Float,
    radius: Float
) {
    // Drop shadow
    drawCircle(
        color = Color(0x55000000),
        radius = radius + 3f,
        center = Offset(cx, cy + 2.5f)
    )

    // 3D Sphere Radial Gradient (convex orb appearance)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFDE7),
                Color(0xFFFFEE58),
                Color(0xFFFFB300),
                Color(0xFFF57F17),
                Color(0xFF8D6E63)
            ),
            center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
            radius = radius * 1.15f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // Gold Outer Rim
    drawCircle(
        color = Color(0xFFFFA000),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.dp.toPx())
    )

    // Embossed Center Star
    draw5PointedStar(cx, cy, radius * 0.46f, Color(0xFF6B4500), Color.White)
}

/**
 * Draws an authentic 3D embossed Golden Star Medallion.
 */
private fun DrawScope.drawAuthenticGoldenStarMedallion(
    cx: Float,
    cy: Float,
    radius: Float
) {
    // Drop shadow
    drawCircle(
        color = Color(0x38000000),
        radius = radius + 1.5f,
        center = Offset(cx, cy + 1.2f)
    )

    // Golden Coin Base
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF9C4),
                Color(0xFFFFD54F),
                Color(0xFFFF8F00),
                Color(0xFFB78103)
            ),
            center = Offset(cx - radius * 0.28f, cy - radius * 0.28f),
            radius = radius * 1.15f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // Gold Rim
    drawCircle(
        color = Color(0xFFFFB300),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Crisp Center 5-Pointed Star
    draw5PointedStar(cx, cy, radius * 0.65f, Color(0x66B78103), Color.White)
}

/**
 * Helper to draw a 5-pointed star path.
 */
private fun DrawScope.draw5PointedStar(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    shadowColor: Color,
    starColor: Color
) {
    val innerRadius = outerRadius * 0.44f
    val points = 5
    val angleStep = Math.PI / points

    fun buildStarPath(offsetY: Float = 0f): Path {
        val path = Path()
        for (i in 0 until (2 * points)) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * angleStep - Math.PI / 2
            val px = (cx + r * cos(angle)).toFloat()
            val py = (cy + r * sin(angle) + offsetY).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        return path
    }

    // Shadow
    drawPath(path = buildStarPath(offsetY = 1f), color = shadowColor)
    // Star Body
    drawPath(path = buildStarPath(), color = starColor)
}

/**
 * High-fidelity Pawn Item matching the glass jewel crown pawns in the reference screenshot.
 */
@Composable
private fun RoyalAuthenticPawn(
    pawn: Pawn,
    colFloat: Float,
    rowFloat: Float,
    cellSize: Dp,
    pawnCrownEmoji: String,
    onPawnClick: (Pawn) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pawnGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (pawn.isMovable) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawnPulse"
    )

    val xOffset = cellSize * colFloat
    val yOffset = cellSize * rowFloat
    val pawnDiameter = cellSize * 0.92f

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(cellSize),
        contentAlignment = Alignment.Center
    ) {
        // Movable Turn Halo Glow
        if (pawn.isMovable) {
            Box(
                modifier = Modifier
                    .size(pawnDiameter * pulseScale)
                    .clip(CircleShape)
                    .background(Color(0x66FFD700))
                    .border(2.dp, Color(0xFFFFEB3B), CircleShape)
            )
        }

        // Authentic Glass Jewel Pawn Token
        Box(
            modifier = Modifier
                .size(pawnDiameter * 0.88f)
                .shadow(elevation = if (pawn.isMovable) 6.dp else 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when (pawn.color) {
                            LudoColor.BLUE -> listOf(
                                Color(0xFFE1F5FE),
                                Color(0xFF42A5F5),
                                Color(0xFF0288D1),
                                Color(0xFF01579B)
                            )
                            LudoColor.RED -> listOf(
                                Color(0xFFFFEBEE),
                                Color(0xFFFF5252),
                                Color(0xFFD50000),
                                Color(0xFF8E0000)
                            )
                            LudoColor.YELLOW -> listOf(
                                Color(0xFFFFFDE7),
                                Color(0xFFFFEE58),
                                Color(0xFFFFB300),
                                Color(0xFFF57F17)
                            )
                            LudoColor.GREEN -> listOf(
                                Color(0xFFE8F5E9),
                                Color(0xFF69F0AE),
                                Color(0xFF00C853),
                                Color(0xFF1B5E20)
                            )
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)
                    ),
                    shape = CircleShape
                )
                .clickable(enabled = pawn.isMovable) {
                    onPawnClick(pawn)
                }
                .testTag("pawn_${pawn.color.name.lowercase()}_${pawn.id}"),
            contentAlignment = Alignment.Center
        ) {
            // Top Gloss Specular Highlight
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .width(pawnDiameter * 0.44f)
                    .height(pawnDiameter * 0.18f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.65f))
            )

            // Center Golden Crown / Royal Emblem
            Text(
                text = pawnCrownEmoji,
                fontSize = (cellSize.value * 0.46f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
