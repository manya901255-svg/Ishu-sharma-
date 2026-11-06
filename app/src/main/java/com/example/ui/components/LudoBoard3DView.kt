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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LudoEngine
import com.example.model.LudoColor
import com.example.model.Pawn
import com.example.model.PawnState
import com.example.model.Player
import com.example.model.RegalBoardTheme
import com.example.model.getBoardTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

/**
 * 3D-Styled Ludo Board Component built with Jetpack Compose.
 * Features:
 * - Real 3D Perspective Projection (rotationX = tiltAngle, cameraDistance = 14 * density)
 * - 3D Extruded Wooden / Lacquered Board Plinth with gold rim thickness
 * - Upright Standing 3D Pawns with Counter-Tilt and elongated dynamic cast shadows
 * - 4 colored home quadrants with beveled sunken trays and 3D circular pawn sockets
 * - Central finish area featuring a 4-colored 3D triangle pyramid apex with a golden triumph star
 * - 3D tactile paths for tokens with embossed bevel cells and safe-zone star medallions
 */
val LUDO_TRACK_COORDINATES: List<Pair<Int, Int>> = listOf(
    6 to 0, 6 to 1, 6 to 2, 6 to 3, 6 to 4, 6 to 5,
    5 to 6, 4 to 6, 3 to 6, 2 to 6, 1 to 6, 0 to 6,
    0 to 7,
    0 to 8, 1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8,
    6 to 9, 6 to 10, 6 to 11, 6 to 12, 6 to 13, 6 to 14,
    7 to 14,
    8 to 14, 8 to 13, 8 to 12, 8 to 11, 8 to 10, 8 to 9,
    9 to 8, 10 to 8, 11 to 8, 12 to 8, 13 to 8, 14 to 8,
    14 to 7,
    14 to 6, 13 to 6, 12 to 6, 11 to 6, 10 to 6, 9 to 6,
    8 to 5, 8 to 4, 8 to 3, 8 to 2, 8 to 1, 8 to 0,
    7 to 0
)

fun getHomeStretchCoordinate(color: LudoColor, step: Int): Pair<Int, Int> {
    return StandardLudoBoardLayout.getHomeStretchGridPosition(color, step)
}

val BASE_PAWN_SLOTS: Map<LudoColor, List<Pair<Float, Float>>> = mapOf(
    LudoColor.RED to listOf(2f to 2f, 2f to 4f, 4f to 2f, 4f to 4f),
    LudoColor.GREEN to listOf(11f to 2f, 11f to 4f, 13f to 2f, 13f to 4f),
    LudoColor.YELLOW to listOf(2f to 11f, 2f to 13f, 4f to 11f, 4f to 13f),
    LudoColor.BLUE to listOf(11f to 11f, 11f to 13f, 13f to 11f, 13f to 13f)
)

@Composable
fun LudoBoard3DView(
    players: List<Player>,
    onPawnClick: (Pawn) -> Unit,
    pawnCrownEmoji: String? = null,
    boardTheme: RegalBoardTheme = getBoardTheme("imperial_navy"),
    is3DMode: Boolean = true,
    tiltAngle: Float = 28f,
    onCycleAngle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    // Smooth transition between 2D (0f) and 3D (tiltAngle)
    val animatedTilt by animateFloatAsState(
        targetValue = if (is3DMode) tiltAngle else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessLow
        ),
        label = "boardTiltAnim"
    )

    // Pulse animation for active clickable pawns
    val infiniteTransition = rememberInfiniteTransition(label = "pawn_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawn_float"
    )

    val isTilted = animatedTilt > 2f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating 3D Camera Mode & Perspective Badge
        Row(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (is3DMode) Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
                    else Brush.horizontalGradient(listOf(Color(0xFF263238), Color(0xFF37474F)))
                )
                .border(
                    width = 1.dp,
                    color = if (is3DMode) Color.White else Color(0xFF546E7A),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onCycleAngle?.invoke() }
                .padding(horizontal = 10.dp, vertical = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (is3DMode) "🎮 3D PERSPECTIVE (${animatedTilt.toInt()}°)" else "📐 2D TOP-DOWN",
                color = if (is3DMode) Color(0xFF002910) else Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
            if (onCycleAngle != null && is3DMode) {
                Text("⟳", color = Color(0xFF002910), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 3D Table Stage containing Shadow, Plinth Extrusion, and Tilted Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Table Ambient Contact Shadow (cast on table beneath the board)
            if (isTilted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .height(38.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (animatedTilt * 0.48f).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x99000000), Color(0x33000000), Color.Transparent)
                            )
                        )
                )
            }

            // 2. Physical 3D Extruded Board Plinth (Wooden/Lacquered Thickness Skirt)
            if (isTilted) {
                val skirtHeight = (12f + animatedTilt * 0.32f).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.985f)
                        .height(skirtHeight)
                        .align(Alignment.BottomCenter)
                        .offset(y = (skirtHeight * 0.38f))
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2C394E),
                                    Color(0xFF141D2D),
                                    Color(0xFF080D18)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0x33FFFFFF),
                                    RoyalGold,
                                    Color(0xFFFFA000),
                                    RoyalGold,
                                    Color(0x33FFFFFF)
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                ) {
                    // Gold Inlay Line on Plinth Skirt
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .height(1.dp)
                            .align(Alignment.Center)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, RoyalGoldLight, Color.Transparent)
                                )
                            )
                    )
                }
            }

            // 3. The 3D Projected Board Itself (Tilted in 3D Perspective)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationX = animatedTilt
                        cameraDistance = 14f * density
                        transformOrigin = TransformOrigin(0.5f, 0.72f)
                        shadowElevation = (animatedTilt * 0.65f).dp.toPx()
                    }
                    .shadow(
                        elevation = if (isTilted) 20.dp else 10.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(boardTheme.outerFrameColor)
                    .border(3.2.dp, boardTheme.borderColor, RoundedCornerShape(20.dp))
                    .padding(4.dp)
                    .testTag("ludo_board_3d_view")
            ) {
        val boardSize = maxWidth
        val cellSize = boardSize / 15f

        val bluePlayer = players.find { it.color == LudoColor.BLUE }
        val redPlayer = players.find { it.color == LudoColor.RED }
        val yellowPlayer = players.find { it.color == LudoColor.YELLOW }
        val greenPlayer = players.find { it.color == LudoColor.GREEN }

        // 1. Canvas Layer: 3D Board Surface, Quadrants, Paths, and Central Finish
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = size.width / 15f
            val width = size.width
            val height = size.height

            // Base Background Tray
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        boardTheme.trackBgColor,
                        boardTheme.outerFrameColor
                    ),
                    center = Offset(width / 2f, height / 2f),
                    radius = width * 0.7f
                )
            )

            // Inner Shadow around perimeter of board tray
            val trayPadding = 2.dp.toPx()
            drawRoundRect(
                color = Color(0x33000000),
                topLeft = Offset(trayPadding, trayPadding),
                size = Size(width - trayPadding * 2, height - trayPadding * 2),
                cornerRadius = CornerRadius(14.dp.toPx())
            )

            // 1A. Four 3D Colored Home Quadrants (6x6 cells each)
            draw3DHomeQuadrant(
                color = LudoColor.RED,
                topLeft = Offset(0f, 0f),
                size = Size(6f * cellPx, 6f * cellPx),
                cellPx = cellPx,
                playerName = redPlayer?.name ?: "Red"
            )

            draw3DHomeQuadrant(
                color = LudoColor.GREEN,
                topLeft = Offset(9f * cellPx, 0f),
                size = Size(6f * cellPx, 6f * cellPx),
                cellPx = cellPx,
                playerName = greenPlayer?.name ?: "Green"
            )

            draw3DHomeQuadrant(
                color = LudoColor.YELLOW,
                topLeft = Offset(0f, 9f * cellPx),
                size = Size(6f * cellPx, 6f * cellPx),
                cellPx = cellPx,
                playerName = yellowPlayer?.name ?: "Yellow"
            )

            draw3DHomeQuadrant(
                color = LudoColor.BLUE,
                topLeft = Offset(9f * cellPx, 9f * cellPx),
                size = Size(6f * cellPx, 6f * cellPx),
                cellPx = cellPx,
                playerName = bluePlayer?.name ?: "Blue"
            )

            // 1B. 52 3D Outer Track Cells with Beveled Relief
            LUDO_TRACK_COORDINATES.forEachIndexed { index, (r, c) ->
                val x = c * cellPx
                val y = r * cellPx

                // Cell Base with subtle 3D bevel
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.94f),
                    topLeft = Offset(x + 1f, y + 1f),
                    size = Size(cellPx - 2f, cellPx - 2f),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // 3D Top-Left Light Highlight
                drawLine(
                    color = Color(0x66FFFFFF),
                    start = Offset(x + 1f, y + 1f),
                    end = Offset(x + cellPx - 1f, y + 1f),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = Color(0x66FFFFFF),
                    start = Offset(x + 1f, y + 1f),
                    end = Offset(x + 1f, y + cellPx - 1f),
                    strokeWidth = 1.5.dp.toPx()
                )

                // 3D Bottom-Right Bevel Shadow
                drawLine(
                    color = Color(0x33000000),
                    start = Offset(x + 1f, y + cellPx - 1f),
                    end = Offset(x + cellPx - 1f, y + cellPx - 1f),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = Color(0x33000000),
                    start = Offset(x + cellPx - 1f, y + 1f),
                    end = Offset(x + cellPx - 1f, y + cellPx - 1f),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Start Cells colored indicators
                when (index) {
                    1 -> draw3DStartCell(x, y, cellPx, LudoColor.RED)
                    14 -> draw3DStartCell(x, y, cellPx, LudoColor.GREEN)
                    27 -> draw3DStartCell(x, y, cellPx, LudoColor.BLUE)
                    40 -> draw3DStartCell(x, y, cellPx, LudoColor.YELLOW)
                }

                // Safe Zones: Star badges (cells 1, 9, 14, 22, 27, 35, 40, 48)
                if (index in listOf(1, 9, 14, 22, 27, 35, 40, 48)) {
                    draw3DSafeStarMedallion(x + cellPx / 2f, y + cellPx / 2f, cellPx * 0.36f)
                }

                // Track Goldfish Bonus Markers
                if (index in listOf(10, 12, 36, 38)) {
                    drawTrackGoldfish(x + cellPx / 2f, y + cellPx / 2f, cellPx * 0.32f)
                }
            }

            // 1C. Four 3D Home Stretches (Colored Runways leading to Center)
            for (step in 1..5) {
                // Blue (Left to Center)
                val (bc, br) = getHomeStretchCoordinate(LudoColor.BLUE, step)
                draw3DHomeStretchTile(bc * cellPx, br * cellPx, cellPx, Color(0xFF039BE5), Color(0xFF81D4FA))

                // Red (Top to Center)
                val (rc, rr) = getHomeStretchCoordinate(LudoColor.RED, step)
                draw3DHomeStretchTile(rc * cellPx, rr * cellPx, cellPx, Color(0xFFE53935), Color(0xFFFF8A80))

                // Green (Right to Center)
                val (gc, gr) = getHomeStretchCoordinate(LudoColor.GREEN, step)
                draw3DHomeStretchTile(gc * cellPx, gr * cellPx, cellPx, Color(0xFF43A047), Color(0xFFA5D6A7))

                // Yellow (Bottom to Center)
                val (yc, yr) = getHomeStretchCoordinate(LudoColor.YELLOW, step)
                draw3DHomeStretchTile(yc * cellPx, yr * cellPx, cellPx, Color(0xFFFDD835), Color(0xFFFFF59D))
            }

            // 1D. Central Finish Area: 3D Royal Finish Pyramid with 4 Colored Triangles
            draw3DCentralFinishArea(
                centerX = width / 2f,
                centerY = height / 2f,
                cellPx = cellPx
            )
        }

        // 2. Interactive Pawns Layer: Placed with 3D depth, shadows, and bounce
        players.forEach { player ->
            player.pawns.forEach { pawn ->
                PawnPlacementItem(
                    pawn = pawn,
                    player = player,
                    cellSize = cellSize,
                    bounceOffset = if (pawn.isMovable) bounceOffset else 0f,
                    tiltAngle = animatedTilt,
                    pawnCrownEmoji = pawnCrownEmoji,
                    onPawnClick = onPawnClick
                )
            }
        }
    }
}
}
}

/**
 * Draws a 3D-styled Home Quadrant with beveled container, recessed white tray,
 * and four 3D circular pawn sockets with metallic trim.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DHomeQuadrant(
    color: LudoColor,
    topLeft: Offset,
    size: Size,
    cellPx: Float,
    playerName: String
) {
    val (primaryColor, darkShade, lightShade) = when (color) {
        LudoColor.YELLOW -> Triple(Color(0xFFFFB300), Color(0xFFB78103), Color(0xFFFFE082))
        LudoColor.RED -> Triple(Color(0xFFE53935), Color(0xFF8E0000), Color(0xFFFFCDD2))
        LudoColor.BLUE -> Triple(Color(0xFF1E88E5), Color(0xFF0D47A1), Color(0xFFBBDEFB))
        LudoColor.GREEN -> Triple(Color(0xFF43A047), Color(0xFF1B5E20), Color(0xFFC8E6C9))
    }

    // Outer 3D Beveled Quadrant Box
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(lightShade, primaryColor, darkShade),
            start = topLeft,
            end = Offset(topLeft.x + size.width, topLeft.y + size.height)
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(6.dp.toPx())
    )

    // Sunken Inner Tray (White/Cream with inner ambient shadow)
    val trayInset = cellPx * 0.75f
    val trayTopLeft = Offset(topLeft.x + trayInset, topLeft.y + trayInset)
    val traySize = Size(size.width - trayInset * 2, size.height - trayInset * 2)

    // Inner tray shadow
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(trayTopLeft.x - 2f, trayTopLeft.y - 2f),
        size = Size(traySize.width + 4f, traySize.height + 4f),
        cornerRadius = CornerRadius(14.dp.toPx())
    )

    // Inner Tray Surface
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF0F4F8)),
            startY = trayTopLeft.y,
            endY = trayTopLeft.y + traySize.height
        ),
        topLeft = trayTopLeft,
        size = traySize,
        cornerRadius = CornerRadius(12.dp.toPx())
    )

    // Four 3D Recessed Pawn Sockets
    val socketRadius = cellPx * 0.78f
    val socketOffsets = listOf(
        Offset(trayTopLeft.x + traySize.width * 0.28f, trayTopLeft.y + traySize.height * 0.28f),
        Offset(trayTopLeft.x + traySize.width * 0.72f, trayTopLeft.y + traySize.height * 0.28f),
        Offset(trayTopLeft.x + traySize.width * 0.28f, trayTopLeft.y + traySize.height * 0.72f),
        Offset(trayTopLeft.x + traySize.width * 0.72f, trayTopLeft.y + traySize.height * 0.72f)
    )

    socketOffsets.forEach { center ->
        // Outer Socket Ring Shadow (sunken depth)
        drawCircle(
            color = Color(0x33000000),
            radius = socketRadius + 2f,
            center = Offset(center.x + 1f, center.y + 2f)
        )

        // Socket Metallic Rim
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(primaryColor, lightShade, darkShade, primaryColor),
                center = center
            ),
            radius = socketRadius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Socket Inner Cavity
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    lightShade.copy(alpha = 0.45f),
                    primaryColor.copy(alpha = 0.25f),
                    Color(0xFFE2E8F0)
                ),
                center = center,
                radius = socketRadius
            ),
            radius = socketRadius - 2f,
            center = center
        )
    }
}

/**
 * Draws a 3D Start Cell with matching quadrant color and beveled arrow/shield
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DStartCell(
    x: Float,
    y: Float,
    cellPx: Float,
    color: LudoColor
) {
    val fillColor = when (color) {
        LudoColor.YELLOW -> Color(0xFFFFB300)
        LudoColor.RED -> Color(0xFFE53935)
        LudoColor.BLUE -> Color(0xFF1E88E5)
        LudoColor.GREEN -> Color(0xFF43A047)
    }

    drawRoundRect(
        color = fillColor,
        topLeft = Offset(x + 1f, y + 1f),
        size = Size(cellPx - 2f, cellPx - 2f),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    // Inner bright 3D ring
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = cellPx * 0.25f,
        center = Offset(x + cellPx / 2f, y + cellPx / 2f),
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * Draws a 3D Embossed Golden Safe Star Medallion
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DSafeStarMedallion(
    cx: Float,
    cy: Float,
    radius: Float
) {
    // Outer Drop Shadow
    drawCircle(
        color = Color(0x44000000),
        radius = radius + 2f,
        center = Offset(cx, cy + 1.5f)
    )

    // Golden Shield Base
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF3B0), Color(0xFFFFB300), Color(0xFF8D6200)),
            center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
            radius = radius * 1.2f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // 5-Pointed Star
    val path = Path()
    val points = 5
    val outerRadius = radius * 0.75f
    val innerRadius = outerRadius * 0.45f
    val angleStep = Math.PI / points

    for (i in 0 until (2 * points)) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - Math.PI / 2
        val px = (cx + r * Math.cos(angle)).toFloat()
        val py = (cy + r * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()

    drawPath(path, color = Color.White)
}

/**
 * Draws a 3D Home Stretch tile with beveled glossy enamel surface
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DHomeStretchTile(
    x: Float,
    y: Float,
    cellPx: Float,
    baseColor: Color,
    highlightColor: Color
) {
    // Beveled Tile
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(highlightColor, baseColor),
            start = Offset(x, y),
            end = Offset(x + cellPx, y + cellPx)
        ),
        topLeft = Offset(x + 1f, y + 1f),
        size = Size(cellPx - 2f, cellPx - 2f),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    // Top Highlight
    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(x + 2f, y + 2f),
        end = Offset(x + cellPx - 2f, y + 2f),
        strokeWidth = 1.dp.toPx()
    )
}

/**
 * Draws the 3D Central Finish Area:
 * 4 colored beveled triangles meeting at an elevated 3D golden pyramid apex with a star triumph emblem.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DCentralFinishArea(
    centerX: Float,
    centerY: Float,
    cellPx: Float
) {
    val halfArea = 1.5f * cellPx
    val left = centerX - halfArea
    val top = centerY - halfArea
    val right = centerX + halfArea
    val bottom = centerY + halfArea

    // 1. Blue Triangle (Left)
    val bluePath = Path().apply {
        moveTo(left, top)
        lineTo(centerX, centerY)
        lineTo(left, bottom)
        close()
    }
    // 1. Red Triangle (Left)
    drawPath(
        path = bluePath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE53935), Color(0xFFC62828)),
            start = Offset(left, centerY),
            end = Offset(centerX, centerY)
        )
    )

    // 2. Green Triangle (Top)
    val redPath = Path().apply {
        moveTo(left, top)
        lineTo(right, top)
        lineTo(centerX, centerY)
        close()
    }
    drawPath(
        path = redPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32)),
            start = Offset(centerX, top),
            end = Offset(centerX, centerY)
        )
    )

    // 3. Blue Triangle (Right)
    val greenPath = Path().apply {
        moveTo(right, top)
        lineTo(right, bottom)
        lineTo(centerX, centerY)
        close()
    }
    drawPath(
        path = greenPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF039BE5), Color(0xFF0277BD)),
            start = Offset(right, centerY),
            end = Offset(centerX, centerY)
        )
    )

    // 4. Yellow Triangle (Bottom)
    val yellowPath = Path().apply {
        moveTo(left, bottom)
        lineTo(right, bottom)
        lineTo(centerX, centerY)
        close()
    }
    drawPath(
        path = yellowPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFDD835), Color(0xFFF57F17)),
            start = Offset(centerX, bottom),
            end = Offset(centerX, centerY)
        )
    )

    // Central 3D Elevated Golden Apex Pyramid
    val apexRadius = cellPx * 0.65f

    // Shadow
    drawCircle(
        color = Color(0x66000000),
        radius = apexRadius + 3f,
        center = Offset(centerX, centerY + 2f)
    )

    // Gold Bevel Medallion
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFF8D6200)),
            center = Offset(centerX - apexRadius * 0.3f, centerY - apexRadius * 0.3f),
            radius = apexRadius * 1.1f
        ),
        radius = apexRadius,
        center = Offset(centerX, centerY)
    )

    // Inner Center Triumph Crown/Star
    drawCircle(
        color = Color(0xFF0D47A1),
        radius = apexRadius * 0.42f,
        center = Offset(centerX, centerY)
    )

    draw3DSafeStarMedallion(centerX, centerY, apexRadius * 0.38f)
}

/**
 * Handles individual pawn positioning with 3D elevation, dynamic cast shadow, and counter-tilt
 */
@Composable
private fun BoxWithConstraintsScope.PawnPlacementItem(
    pawn: Pawn,
    player: Player,
    cellSize: Dp,
    bounceOffset: Float,
    tiltAngle: Float,
    pawnCrownEmoji: String?,
    onPawnClick: (Pawn) -> Unit
) {
    if (pawn.state == PawnState.FINISHED) return // reached goal

    val (r, c) = when (pawn.state) {
        PawnState.IN_BASE -> {
            val slots = BASE_PAWN_SLOTS[player.color] ?: listOf(2f to 2f, 2f to 4f, 4f to 2f, 4f to 4f)
            slots.getOrNull(pawn.id) ?: (2f to 2f)
        }
        PawnState.ON_BOARD -> {
            val globalPos = ((pawn.trackPosition % 52) + 52) % 52
            val coord = LUDO_TRACK_COORDINATES.getOrElse(globalPos) { 6 to 0 }
            coord.first.toFloat() to coord.second.toFloat()
        }
        PawnState.IN_HOME_STRETCH -> {
            val coord = getHomeStretchCoordinate(player.color, pawn.homeStep.coerceIn(1, 5))
            coord.first.toFloat() to coord.second.toFloat()
        }
        PawnState.FINISHED -> 7f to 7f
    }

    val pawnSize = cellSize * 0.96f
    val xOffset = cellSize * c + (cellSize - pawnSize) / 2f
    val yOffset = cellSize * r + (cellSize - pawnSize) / 2f

    val isTilted = tiltAngle > 2f

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(pawnSize),
        contentAlignment = Alignment.Center
    ) {
        // 1. Dynamic 3D Cast Shadow on the board surface behind the pawn
        val shadowScale = if (pawn.isMovable) (1f - (bounceOffset / -10f)).coerceIn(0.65f, 1f) else 1f
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(
                    x = if (isTilted) (tiltAngle * 0.10f).dp else 0.dp,
                    y = if (isTilted) (3.5f + tiltAngle * 0.16f).dp else 2.dp
                )
                .size(
                    width = pawnSize * 0.78f * shadowScale,
                    height = pawnSize * (if (isTilted) 0.32f + tiltAngle * 0.005f else 0.28f) * shadowScale
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x88000000), Color(0x33000000), Color.Transparent)
                    )
                )
        )

        // 2. Standing 3D Token Pawn (Counter-Tilted so it stands upright perpendicular to the 3D board plane!)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    if (isTilted) {
                        rotationX = -tiltAngle * 0.82f
                        translationY = bounceOffset.dp.toPx()
                    } else {
                        translationY = bounceOffset.dp.toPx()
                    }
                }
        ) {
            RoyalCrownPawn(
                pawn = pawn,
                size = pawnSize,
                crownEmoji = pawnCrownEmoji,
                onClick = { onPawnClick(pawn) }
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrackGoldfish(
    cx: Float,
    cy: Float,
    size: Float
) {
    val fishColor = Color(0xFFFF7043)
    val finColor = Color(0xFFFFAB91)

    // Body (oval)
    drawOval(
        color = fishColor,
        topLeft = Offset(cx - size * 0.7f, cy - size * 0.4f),
        size = Size(size * 1.1f, size * 0.8f)
    )

    // Tail fin
    val tailPath = Path().apply {
        moveTo(cx + size * 0.3f, cy)
        lineTo(cx + size * 0.8f, cy - size * 0.45f)
        lineTo(cx + size * 0.65f, cy)
        lineTo(cx + size * 0.8f, cy + size * 0.45f)
        close()
    }
    drawPath(tailPath, finColor)

    // Eye
    drawCircle(
        color = Color.White,
        radius = size * 0.12f,
        center = Offset(cx - size * 0.35f, cy - size * 0.12f)
    )
    drawCircle(
        color = Color(0xFF1A237E),
        radius = size * 0.06f,
        center = Offset(cx - size * 0.37f, cy - size * 0.12f)
    )
}

