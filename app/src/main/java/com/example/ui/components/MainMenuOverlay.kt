package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

/**
 * Main Menu Overlay that appears after the welcome screen.
 * Features:
 * - 'Play Online' button
 * - 'Play vs AI' button
 * - 'Pass & Play' / Local Multiplayer button
 * - 'Settings' button
 * - 'Enter Palace Lobby' option
 */
@Composable
fun MainMenuOverlay(
    onPlayOnline: () -> Unit,
    onPlayAi: () -> Unit,
    onPassAndPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onEnterLobby: () -> Unit,
    onOpenBlueprint: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateIn = true
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "menu_alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "menu_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6040A18))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* block taps outside */ }
            .testTag("main_menu_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background wallpaper
        Image(
            painter = painterResource(id = R.drawable.welcome_mg_world),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        )

        // Dark royal blue backdrop tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE030918),
                            Color(0xF5061430),
                            Color(0xFA020610)
                        )
                    )
                )
        )

        // Main Menu Dialog Container
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .scale(scaleAnim)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1E40),
                            Color(0xFF071228),
                            Color(0xFF040A17)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFFECB3),
                            Color(0xFFFFB300),
                            Color(0xFF8D6200),
                            Color(0xFFFFD54F)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MG WORLD",
                            fontFamily = CinzelDecorativeFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RoyalGoldLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ROYAL MAIN MENU",
                            fontFamily = CinzelFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFFFD54F),
                            letterSpacing = 2.sp
                        )
                    }
                }

                IconButton(
                    onClick = onEnterLobby,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .testTag("menu_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close to Lobby",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Menu Options
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. PLAY ONLINE BUTTON
                MainMenuButtonCard(
                    title = "Play Online",
                    subtitle = "Multiplayer Arena • 2P & 4P Fast Match",
                    badge = "HOT",
                    icon = "🌐",
                    primaryGradient = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1), Color(0xFF062258)),
                    borderGradient = listOf(Color(0xFF90CAF9), Color(0xFF1976D2)),
                    testTag = "menu_play_online_button",
                    onClick = onPlayOnline
                )

                // 2. PLAY VS AI BUTTON
                MainMenuButtonCard(
                    title = "Play vs AI",
                    subtitle = "Solo Practice • Maharaja Intelligent Bots",
                    badge = "OFFLINE",
                    icon = "🤖",
                    primaryGradient = listOf(Color(0xFF00897B), Color(0xFF004D40), Color(0xFF00251A)),
                    borderGradient = listOf(Color(0xFF80CBC4), Color(0xFF00897B)),
                    testTag = "menu_play_ai_button",
                    onClick = onPlayAi
                )

                // 3. PASS & PLAY / LOCAL MULTIPLAYER
                MainMenuButtonCard(
                    title = "Pass & Play",
                    subtitle = "2 - 4 Players On Single Device",
                    badge = "FAMILY",
                    icon = "👥",
                    primaryGradient = listOf(Color(0xFFE53935), Color(0xFFB71C1C), Color(0xFF5D0A0A)),
                    borderGradient = listOf(Color(0xFFEF9A9A), Color(0xFFC62828)),
                    testTag = "menu_pass_and_play_button",
                    onClick = onPassAndPlay
                )

                // 4. SETTINGS BUTTON
                MainMenuButtonCard(
                    title = "Settings",
                    subtitle = "Audio, Haptics, Board Themes & 3D View",
                    badge = null,
                    icon = "⚙️",
                    primaryGradient = listOf(Color(0xFF5E35B1), Color(0xFF311B92), Color(0xFF1A0C53)),
                    borderGradient = listOf(Color(0xFFB39DDB), Color(0xFF512DA8)),
                    testTag = "menu_settings_button",
                    onClick = onOpenSettings
                )

                // 5. TECH BLUEPRINT BUTTON
                MainMenuButtonCard(
                    title = "Tech Blueprint",
                    subtitle = "System Architecture & Unity Scripts Bridge",
                    badge = "DOCS",
                    icon = "📐",
                    primaryGradient = listOf(Color(0xFF37474F), Color(0xFF263238), Color(0xFF102027)),
                    borderGradient = listOf(Color(0xFF90A4AE), Color(0xFF546E7A)),
                    testTag = "menu_blueprint_button",
                    onClick = onOpenBlueprint
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. ENTER ROYAL PALACE LOBBY (Bottom Bar)
            Button(
                onClick = onEnterLobby,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("menu_enter_lobby_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("🏰", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENTER ROYAL PALACE LOBBY",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF040A18),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun MainMenuButtonCard(
    title: String,
    subtitle: String,
    badge: String?,
    icon: String,
    primaryGradient: List<Color>,
    borderGradient: List<Color>,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(primaryGradient))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(borderGradient),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RoyalGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF040A18)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xCCFFFFFF),
                    lineHeight = 15.sp
                )
            }

            // Arrow
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = RoyalGoldLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
