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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
 * Authentic Normal Classic Ludo Board View.
 * Matches traditional physical Indian and international Ludo boards:
 * - 4 Classic corner home quadrants (Top-Left: Red, Top-Right: Green, Bottom-Left: Yellow, Bottom-Right: Blue)
 * - Inner white cards with 4 solid circular token bases
 * - Colored Starting Tiles (Red, Green, Yellow, Blue) with crisp stars
 * - 4 Colored Directional Entrance Arrows pointing into home runways
 * - 8 Safe Star positions clearly marked
 * - 4 Clean colored 5-step home stretch runways
 * - 4 Vibrant center finishing triangles (Red, Green, Yellow, Blue)
 * - Authentic 3D Ludo gotis (tokens) with multi-token stacking offsets, smooth movement animation & turn glow
 */
@Composable
fun RoyalBoardView(
    players: List<Player>,
    onPawnClick: (Pawn) -> Unit,
    pawnCrownEmoji: String = "👑",
    boardTheme: RegalBoardTheme,
    pawnSkinId: String = "crown_maharaja",
    isPawnTrailEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(boardTheme.outerFrameColor)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    listOf(boardTheme.borderColor, boardTheme.accentStarColor, boardTheme.borderColor)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("royal_board_2d_view"),
        contentAlignment = Alignment.TopStart
    ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        // 1. Canvas Layer: Board background, 15x15 grid, home stretches, safe stars, bases, center triangles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = size.width / 15f

            // Board Canvas Base Background (Uses selected Board Theme track color)
            drawRect(color = boardTheme.trackBgColor)

            // 1A. Four Classic Colored Home Quadrants (6x6 each)
            // Top-Left: Blue
            // 1A. Four Classic Colored Home Quadrants (6x6 each)
            // Top-Left: Red
            drawClassicHomeQuadrant(
                color = LudoColor.RED,
                topLeft = Offset(0f, 0f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFFE53935),
                darkColor = Color(0xFFC62828),
                socketColor = Color(0xFFE53935)
            )

            // Top-Right: Green
            drawClassicHomeQuadrant(
                color = LudoColor.GREEN,
                topLeft = Offset(cellPx * 9f, 0f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFF43A047),
                darkColor = Color(0xFF2E7D32),
                socketColor = Color(0xFF43A047)
            )

            // Bottom-Left: Yellow
            drawClassicHomeQuadrant(
                color = LudoColor.YELLOW,
                topLeft = Offset(0f, cellPx * 9f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFFFDD835),
                darkColor = Color(0xFFF9A825),
                socketColor = Color(0xFFFBC02D)
            )

            // Bottom-Right: Blue
            drawClassicHomeQuadrant(
                color = LudoColor.BLUE,
                topLeft = Offset(cellPx * 9f, cellPx * 9f),
                size = Size(cellPx * 6f, cellPx * 6f),
                cellPx = cellPx,
                primaryColor = Color(0xFF1E88E5),
                darkColor = Color(0xFF1565C0),
                socketColor = Color(0xFF1E88E5)
            )

            // 1B. Four Colored Starting Cells (The universal hallmark of classic normal Ludo)
            // Red start at (1, 6)
            drawRect(
                color = Color(0xFFE53935),
                topLeft = Offset(1 * cellPx, 6 * cellPx),
                size = Size(cellPx, cellPx)
            )
            // Green start at (8, 1)
            drawRect(
                color = Color(0xFF43A047),
                topLeft = Offset(8 * cellPx, 1 * cellPx),
                size = Size(cellPx, cellPx)
            )
            // Blue start at (13, 8)
            drawRect(
                color = Color(0xFF1E88E5),
                topLeft = Offset(13 * cellPx, 8 * cellPx),
                size = Size(cellPx, cellPx)
            )
            // Yellow start at (6, 13)
            drawRect(
                color = Color(0xFFFDD835),
                topLeft = Offset(6 * cellPx, 13 * cellPx),
                size = Size(cellPx, cellPx)
            )

            // 1C. Four Colored Home Runways (5 steps leading into center)
            // Left: Red runway (cols 1..5, row 7)
            for (step in 1..5) {
                drawRect(
                    color = Color(0xFFE53935),
                    topLeft = Offset(step * cellPx, 7 * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }

            // Top: Green runway (col 7, rows 1..5)
            for (step in 1..5) {
                drawRect(
                    color = Color(0xFF43A047),
                    topLeft = Offset(7 * cellPx, step * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }

            // Right: Blue runway (cols 9..13, row 7)
            for (col in 9..13) {
                drawRect(
                    color = Color(0xFF1E88E5),
                    topLeft = Offset(col * cellPx, 7 * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }

            // Bottom: Yellow runway (col 7, rows 9..13)
            for (row in 9..13) {
                drawRect(
                    color = Color(0xFFFDD835),
                    topLeft = Offset(7 * cellPx, row * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }

            // 1D. Crisp Classic Grid Lines over all 52 track cells + home runways
            val gridBorderColor = boardTheme.gridLineColor
            for (pos in StandardLudoBoardLayout.TRACK_COORDINATES) {
                drawRect(
                    color = gridBorderColor,
                    topLeft = Offset(pos.first * cellPx, pos.second * cellPx),
                    size = Size(cellPx, cellPx),
                    style = Stroke(width = 1.3f)
                )
            }

            // Home runway internal grid lines
            for (step in 1..5) {
                // Red (Left arm)
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(step * cellPx, 7 * cellPx),
                    size = Size(cellPx, cellPx),
                    style = Stroke(width = 1.2f)
                )
                // Green (Top arm)
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(7 * cellPx, step * cellPx),
                    size = Size(cellPx, cellPx),
                    style = Stroke(width = 1.2f)
                )
                // Blue (Right arm)
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset((8 + step) * cellPx, 7 * cellPx),
                    size = Size(cellPx, cellPx),
                    style = Stroke(width = 1.2f)
                )
                // Yellow (Bottom arm)
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(7 * cellPx, (8 + step) * cellPx),
                    size = Size(cellPx, cellPx),
                    style = Stroke(width = 1.2f)
                )
            }

            // 1E. Directional Arrows at Home Entrances (Classic Normal Ludo Feature)
            // Red Entrance Arrow at (0, 7) pointing Right
            drawDirectionalArrow(
                center = Offset(0.5f * cellPx, 7.5f * cellPx),
                size = cellPx * 0.45f,
                direction = ArrowDirection.RIGHT,
                color = Color(0xFFE53935)
            )

            // Green Entrance Arrow at (7, 0) pointing Down
            drawDirectionalArrow(
                center = Offset(7.5f * cellPx, 0.5f * cellPx),
                size = cellPx * 0.45f,
                direction = ArrowDirection.DOWN,
                color = Color(0xFF43A047)
            )

            // Blue Entrance Arrow at (14, 7) pointing Left
            drawDirectionalArrow(
                center = Offset(14.5f * cellPx, 7.5f * cellPx),
                size = cellPx * 0.45f,
                direction = ArrowDirection.LEFT,
                color = Color(0xFF1E88E5)
            )

            // Yellow Entrance Arrow at (7, 14) pointing Up
            drawDirectionalArrow(
                center = Offset(7.5f * cellPx, 14.5f * cellPx),
                size = cellPx * 0.45f,
                direction = ArrowDirection.UP,
                color = Color(0xFFF57F17)
            )

            // 1F. Central Finish Area with 4 Vibrant Triangles (Center 3x3 square)
            val centerLeft = 6 * cellPx
            val centerTop = 6 * cellPx
            val centerRight = 9 * cellPx
            val centerBottom = 9 * cellPx
            val cx = 7.5f * cellPx
            val cy = 7.5f * cellPx

            // Top Triangle (Green)
            val greenTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(centerRight, centerTop)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = greenTriangle, color = Color(0xFF43A047))

            // Left Triangle (Red)
            val redTriangle = Path().apply {
                moveTo(centerLeft, centerTop)
                lineTo(centerLeft, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = redTriangle, color = Color(0xFFE53935))

            // Right Triangle (Blue)
            val blueTriangle = Path().apply {
                moveTo(centerRight, centerTop)
                lineTo(centerRight, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = blueTriangle, color = Color(0xFF1E88E5))

            // Bottom Triangle (Yellow)
            val yellowTriangle = Path().apply {
                moveTo(centerLeft, centerBottom)
                lineTo(centerRight, centerBottom)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = yellowTriangle, color = Color(0xFFFDD835))

            // Center Diagonal Separator Lines
            drawLine(
                color = Color.White,
                start = Offset(centerLeft, centerTop),
                end = Offset(centerRight, centerBottom),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(centerRight, centerTop),
                end = Offset(centerLeft, centerBottom),
                strokeWidth = 2.dp.toPx()
            )

            // Center Finish Outer Border
            drawRect(
                color = gridBorderColor,
                topLeft = Offset(centerLeft, centerTop),
                size = Size(3 * cellPx, 3 * cellPx),
                style = Stroke(width = 2.dp.toPx())
            )

            // Elegant Compact Golden Center Star (Keeps triangles wide open and authentic)
            drawCompactCenterStar(cx, cy, cellPx * 0.40f)

            // 1G. Eight Standard Safe Stars
            // 4 Starting cells with white stars
            drawStarIcon((1 + 0.5f) * cellPx, (6 + 0.5f) * cellPx, cellPx * 0.32f, Color.White, Color(0x66000000))
            drawStarIcon((8 + 0.5f) * cellPx, (1 + 0.5f) * cellPx, cellPx * 0.32f, Color.White, Color(0x66000000))
            drawStarIcon((13 + 0.5f) * cellPx, (8 + 0.5f) * cellPx, cellPx * 0.32f, Color.White, Color(0x66000000))
            drawStarIcon((6 + 0.5f) * cellPx, (13 + 0.5f) * cellPx, cellPx * 0.32f, Color(0xFF424242), Color(0x33000000))

            // 4 Safe track squares with classic golden stars
            val safeSquareCoords = listOf(
                6 to 2,   // Red approach star
                12 to 6,  // Green approach star
                8 to 12,  // Yellow approach star
                2 to 8    // Blue approach star
            )
            for ((sc, sr) in safeSquareCoords) {
                val starCenterX = (sc + 0.5f) * cellPx
                val starCenterY = (sr + 0.5f) * cellPx
                drawStarIcon(starCenterX, starCenterY, cellPx * 0.35f, boardTheme.accentStarColor, Color(0xFFE65100))
            }
        }

        // 2. Interactive Pawns Layer with Multi-Token Stacking & Z-Ordering
        val allPawns = players.flatMap { it.pawns }

        // Compute base grid coordinates (including colored finishing triangles for FINISHED tokens)
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
                    PawnState.FINISHED -> when (pawn.color) {
                        LudoColor.RED -> 6.32f to 7.0f
                        LudoColor.GREEN -> 7.0f to 6.32f
                        LudoColor.BLUE -> 7.68f to 7.0f
                        LudoColor.YELLOW -> 7.0f to 7.68f
                    }
                }
                Triple(pawn, gridX, gridY)
            }
        }

        // Apply clean visual offsets when multiple tokens occupy the same track cell or finishing triangle
        val stackedPawns = remember(rawPawnPositions) {
            val cellGroups = rawPawnPositions.groupBy { (it.second * 10).toInt() to (it.third * 10).toInt() }
            rawPawnPositions.map { triple ->
                val pawn = triple.first
                val key = (triple.second * 10).toInt() to (triple.third * 10).toInt()
                val group = cellGroups[key] ?: listOf(triple)
                val indexInGroup = group.indexOf(triple)
                val (offsetX, offsetY) = if (group.size > 1 && pawn.state != PawnState.IN_BASE) {
                    if (pawn.state == PawnState.FINISHED) {
                        val shift = (indexInGroup - (group.size - 1) / 2f) * 0.24f
                        when (pawn.color) {
                            LudoColor.RED, LudoColor.BLUE -> 0f to shift
                            LudoColor.GREEN, LudoColor.YELLOW -> shift to 0f
                        }
                    } else {
                        when (group.size) {
                            2 -> if (indexInGroup == 0) -0.14f to -0.14f else 0.14f to 0.14f
                            3 -> when (indexInGroup) {
                                0 -> 0f to -0.16f
                                1 -> -0.16f to 0.14f
                                else -> 0.16f to 0.14f
                            }
                            else -> when (indexInGroup) {
                                0 -> -0.15f to -0.15f
                                1 -> 0.15f to -0.15f
                                2 -> -0.15f to 0.15f
                                else -> 0.15f to 0.15f
                            }
                        }
                    }
                } else 0f to 0f
                Triple(pawn, triple.second + offsetX, triple.third + offsetY)
            }
        }

        // Sort rendering so movable and active tokens always appear on top cleanly
        val orderedPawns = remember(stackedPawns) {
            stackedPawns.sortedBy { (pawn, _, _) ->
                when {
                    pawn.isMovable -> 3
                    pawn.state == PawnState.ON_BOARD || pawn.state == PawnState.IN_HOME_STRETCH -> 2
                    pawn.state == PawnState.FINISHED -> 1
                    else -> 0
                }
            }
        }

        orderedPawns.forEach { (pawn, colFloat, rowFloat) ->
            key("${pawn.color.name}_${pawn.id}") {
                ClassicLudoPawn(
                    pawn = pawn,
                    colFloat = colFloat,
                    rowFloat = rowFloat,
                    cellSize = cellSize,
                    pawnCrownEmoji = pawnCrownEmoji,
                    pawnSkinId = pawnSkinId,
                    isTrailEnabled = isPawnTrailEnabled,
                    onPawnClick = onPawnClick
                )
            }
        }
    }
}

private enum class ArrowDirection { UP, DOWN, LEFT, RIGHT }

/**
 * Draws a sharp directional entrance arrow inside a track cell.
 */
private fun DrawScope.drawDirectionalArrow(
    center: Offset,
    size: Float,
    direction: ArrowDirection,
    color: Color
) {
    val half = size * 0.5f
    val path = Path()
    when (direction) {
        ArrowDirection.RIGHT -> {
            path.moveTo(center.x - half, center.y - half * 0.7f)
            path.lineTo(center.x + half, center.y)
            path.lineTo(center.x - half, center.y + half * 0.7f)
        }
        ArrowDirection.DOWN -> {
            path.moveTo(center.x - half * 0.7f, center.y - half)
            path.lineTo(center.x, center.y + half)
            path.lineTo(center.x + half * 0.7f, center.y - half)
        }
        ArrowDirection.LEFT -> {
            path.moveTo(center.x + half, center.y - half * 0.7f)
            path.lineTo(center.x - half, center.y)
            path.lineTo(center.x + half, center.y + half * 0.7f)
        }
        ArrowDirection.UP -> {
            path.moveTo(center.x - half * 0.7f, center.y + half)
            path.lineTo(center.x, center.y - half)
            path.lineTo(center.x + half * 0.7f, center.y + half)
        }
    }
    path.close()
    drawPath(path = path, color = color)
}

/**
 * Draws a classic home quadrant (6x6):
 * Solid colored border, white inner card, and 4 circular colored token bases.
 */
private fun DrawScope.drawClassicHomeQuadrant(
    color: LudoColor,
    topLeft: Offset,
    size: Size,
    cellPx: Float,
    primaryColor: Color,
    darkColor: Color,
    socketColor: Color
) {
    val cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())

    // 1. Solid Outer Base
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(primaryColor, darkColor),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius
    )

    // 2. Inner White Card (positioned so pocket sockets sit symmetrically around token centers)
    val cardInset = cellPx * 0.62f
    val cardWidth = size.width - (cardInset * 2f)
    val cardHeight = size.height - (cardInset * 2f)
    val cardTopLeft = Offset(topLeft.x + cardInset, topLeft.y + cardInset)
    val cardCornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())

    // Subtle drop shadow
    drawRoundRect(
        color = Color(0x2A000000),
        topLeft = Offset(cardTopLeft.x + 1f, cardTopLeft.y + 2f),
        size = Size(cardWidth, cardHeight),
        cornerRadius = cardCornerRadius
    )

    // White Card
    drawRoundRect(
        color = Color.White,
        topLeft = cardTopLeft,
        size = Size(cardWidth, cardHeight),
        cornerRadius = cardCornerRadius
    )

    // Card border
    drawRoundRect(
        color = Color(0xFFCFD8DC),
        topLeft = cardTopLeft,
        size = Size(cardWidth, cardHeight),
        cornerRadius = cardCornerRadius,
        style = Stroke(width = 1.2f)
    )

    // 3. Four Classic Solid Circular Sockets aligned cleanly with getBasePocketFloatPosition (+0.5f cell center)
    val socketRadius = cellPx * 0.68f
    val centers = listOf(
        Offset(topLeft.x + cellPx * 2.25f, topLeft.y + cellPx * 2.25f),
        Offset(topLeft.x + cellPx * 4.25f, topLeft.y + cellPx * 2.25f),
        Offset(topLeft.x + cellPx * 2.25f, topLeft.y + cellPx * 4.25f),
        Offset(topLeft.x + cellPx * 4.25f, topLeft.y + cellPx * 4.25f)
    )

    for (center in centers) {
        // Outer dark ring
        drawCircle(
            color = Color(0x33000000),
            radius = socketRadius + 1.5f,
            center = Offset(center.x, center.y + 1f)
        )

        // Solid circular socket
        drawCircle(
            color = socketColor,
            radius = socketRadius,
            center = center
        )

        // Inner white concentric circle
        drawCircle(
            color = Color.White,
            radius = socketRadius * 0.65f,
            center = center
        )

        // Inner small colored center dot
        drawCircle(
            color = socketColor,
            radius = socketRadius * 0.28f,
            center = center
        )
    }
}

/**
 * Draws a compact center golden star leaving the 4 colored triangles visible.
 */
private fun DrawScope.drawCompactCenterStar(
    cx: Float,
    cy: Float,
    radius: Float
) {
    // Drop shadow
    drawCircle(
        color = Color(0x44000000),
        radius = radius + 2f,
        center = Offset(cx, cy + 1.5f)
    )

    // Golden circular base
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFE65100)),
            center = Offset(cx - radius * 0.2f, cy - radius * 0.2f),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    drawCircle(
        color = Color(0xFFFF8F00),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )

    // Center 5-pointed star
    draw5PointedStar(cx, cy, radius * 0.52f, Color(0xFF5D4037), Color.White)
}

/**
 * Draws a clean star icon inside safe cells.
 */
private fun DrawScope.drawStarIcon(
    cx: Float,
    cy: Float,
    radius: Float,
    starColor: Color,
    shadowColor: Color
) {
    draw5PointedStar(cx, cy, radius, shadowColor, starColor)
}

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

    drawPath(path = buildStarPath(offsetY = 1f), color = shadowColor)
    drawPath(path = buildStarPath(), color = starColor)
}

/**
 * Classic Normal Ludo Gotis (Tokens).
 * Renders a glossy sculpted 3D pin goti with bevel base, smooth grid interpolation animation,
 * proper z-ordering, and generous movable touch target.
 */
@Composable
private fun ClassicLudoPawn(
    pawn: Pawn,
    colFloat: Float,
    rowFloat: Float,
    cellSize: Dp,
    pawnCrownEmoji: String,
    pawnSkinId: String = "crown_maharaja",
    isTrailEnabled: Boolean = true,
    onPawnClick: (Pawn) -> Unit
) {
    val animatedCol by animateFloatAsState(
        targetValue = colFloat,
        animationSpec = tween(durationMillis = 95, easing = FastOutSlowInEasing),
        label = "pawnCol"
    )
    val animatedRow by animateFloatAsState(
        targetValue = rowFloat,
        animationSpec = tween(durationMillis = 95, easing = FastOutSlowInEasing),
        label = "pawnRow"
    )

    val pawnRenderSize = if (pawn.state == PawnState.IN_BASE) cellSize * 1.18f else cellSize * 1.02f
    val centerX = cellSize * (animatedCol + 0.5f)
    val centerY = cellSize * (animatedRow + 0.5f)
    val xOffset = centerX - (pawnRenderSize / 2f)
    val yOffset = centerY - (pawnRenderSize / 2f)

    val pawnZIndex = when {
        pawn.isMovable -> 10f
        pawn.state == PawnState.ON_BOARD || pawn.state == PawnState.IN_HOME_STRETCH -> 5f
        pawn.state == PawnState.FINISHED -> 3f
        else -> 2f
    }

    Box(
        modifier = Modifier
            .zIndex(pawnZIndex)
            .offset(x = xOffset, y = yOffset)
            .size(pawnRenderSize)
            .clickable(enabled = pawn.isMovable) {
                onPawnClick(pawn)
            }
            .testTag("pawn_${pawn.color.name.lowercase()}_${pawn.id}"),
        contentAlignment = Alignment.Center
    ) {
        RoyalCrownPawn(
            pawn = pawn,
            size = pawnRenderSize,
            crownEmoji = pawnCrownEmoji,
            pawnSkin = pawnSkinId,
            isTrailEnabled = isTrailEnabled,
            onClick = if (pawn.isMovable) { { onPawnClick(pawn) } } else null
        )
    }
}
