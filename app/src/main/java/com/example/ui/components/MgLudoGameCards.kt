package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

/**
 * Screen 1: Play Online Featured Promotional Card
 * Matches the MG Ludo reference design with globe, cartoon characters,
 * lush golden-leaf border, and glowing green "GO LIVE" pill button.
 */
@Composable
fun MgLudoPlayOnlineCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(205.dp)
            .height(275.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .testTag("mode_card_Play_Online"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            3.dp,
            Brush.linearGradient(
                listOf(RoyalGoldLight, Color(0xFF00E676), RoyalGold, RoyalGoldLight)
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main high-resolution artwork
            Image(
                painter = painterResource(id = R.drawable.card_play_online),
                contentDescription = "Play Online",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlaid glowing green "GO LIVE" pill button at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E676), Color(0xFF00C853))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "GO LIVE ▶",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "JOIN GAME",
                        color = Color(0xFFE8F5E9),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Screen 2: Quick Rush Card
 * Vibrant orange gradient card with fire and dice artwork,
 * "BLITZ" badge, "Quick Rush" title, "1 Pawn Home to Win" subtitle, and Play button.
 */
@Composable
fun MgLudoQuickRushCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(205.dp)
            .height(275.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .testTag("mode_card_Quick_Rush"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(3.dp, RoyalGoldLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFF6D00), // Vivid orange
                            Color(0xFFFF8F00), // Amber
                            Color(0xFFDD2C00)  // Deep fiery red-orange
                        )
                    )
                )
                .padding(12.dp)
        ) {
            // Ambient Radial Glow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x40FFFFFF), Color.Transparent)
                        )
                    )
            )

            // Top Header: Flash icon + BLITZ badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 18.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD50000))
                        .border(1.2.dp, RoyalGoldLight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BLITZ 🔥",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Center: Fire and Dice 2D Illustration with Depth
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔥", fontSize = 34.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🎲", fontSize = 48.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🔥", fontSize = 34.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("✨ ⚡ ✨", fontSize = 14.sp)
            }

            // Bottom Content: Titles and Interactive Play Button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quick Rush",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "1 Pawn Home to Win!",
                    color = RoyalGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Play Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF3D00), Color(0xFFFF9100))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY BLITZ ▶",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Screen 2: vs AI Match Card
 * Royal indigo/purple gradient card with smart robot and golden crown illustration,
 * "OFFLINE" badge, "vs AI Match" title, "Offline Royal Match" subtitle, and Play button.
 */
@Composable
fun MgLudoVsAiCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(205.dp)
            .height(275.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .testTag("mode_card_vs_AI_Mode"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(3.dp, RoyalGoldLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF5E35B1), // Royal purple
                            Color(0xFF3949AB), // Indigo
                            Color(0xFF1A237E)  // Deep navy indigo
                        )
                    )
                )
                .padding(12.dp)
        ) {
            // Ambient Radial Glow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x3500E5FF), Color.Transparent)
                        )
                    )
            )

            // Top Header: Bot icon + OFFLINE badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 18.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00B0FF))
                        .border(1.2.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OFFLINE 🎮",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Center: Robot with Golden Crown Illustration
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👑", fontSize = 32.sp)
                Text("🤖", fontSize = 48.sp)
                Text("✨ ⚙️ ✨", fontSize = 12.sp)
            }

            // Bottom Content: Titles and Interactive Play Button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "vs AI Match",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Offline Royal Match",
                    color = Color(0xFFE1BEE7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Play Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF7C4DFF), Color(0xFF2979FF))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY OFFLINE ▶",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Screen 3: Private Room Card
 * Vibrant pink/magenta gradient card with golden key, diamond and castle illustration,
 * "CODE" badge, "Private Room" title, "Play with Friends" subtitle,
 * and fast action buttons for Create Room and Join Room.
 */
@Composable
fun MgLudoPrivateRoomCard(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(205.dp)
            .height(275.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable { onCreateClick() }
            .testTag("mode_card_Private_Room"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(3.dp, RoyalGoldLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFE91E63), // Vibrant pink
                            Color(0xFFD81B60), // Deep magenta
                            Color(0xFF880E4F)  // Rich wine burgundy
                        )
                    )
                )
                .padding(12.dp)
        ) {
            // Ambient Radial Glow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x35FFD54F), Color.Transparent)
                        )
                    )
            )

            // Top Header: Key icon + CODE badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 18.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFA000))
                        .border(1.2.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CODE 🔒",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Center: Golden Key, Castle & Diamond Illustration
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🔑", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🏰", fontSize = 48.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("💎", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("✨ 🌟 ✨", fontSize = 12.sp)
            }

            // Bottom Content: Titles and TWO Fast Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Private Room",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Play with Friends",
                    color = Color(0xFFFCE4EC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dual Action Buttons: Create Room & Join Room
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(RoyalGoldLight, RoyalGold)
                                )
                            )
                            .border(1.2.dp, Color.White, RoundedCornerShape(14.dp))
                            .clickable { onCreateClick() }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CREATE",
                            color = Color(0xFF3E2723),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFC2185B), Color(0xFF880E4F))
                                )
                            )
                            .border(1.2.dp, Color.White, RoundedCornerShape(14.dp))
                            .clickable { onJoinClick() }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JOIN",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bonus Carousel Card: 2 vs 2 Team Up
 */
@Composable
fun MgLudoTwoVsTwoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(205.dp)
            .height(275.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .testTag("mode_card_2_vs_2"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(3.dp, RoyalGoldLight)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.card_two_vs_two),
                contentDescription = "2 vs 2",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF0288D1))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TEAM UP ▶",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Exact Featured MG Ludo Hero Card matching the reference image:
 * Features the 3D classic Ludo board with gotis, central dice, gold coins,
 * "MG Ludo" logo, crowned top border, and green "Play Now ▶" capsule button.
 */
@Composable
fun MgLudoExactFeaturedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .height(290.dp)
            .clickable { onClick() }
            .testTag("mode_card_MG_Ludo"),
        contentAlignment = Alignment.TopCenter
    ) {
        // Main Card Body (with offset for crown)
        Card(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize()
                .shadow(16.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(2.5.dp, Color(0xFF80D8FF).copy(alpha = 0.9f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.mg_ludo_hero_card),
                    contentDescription = "MG Ludo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlaid glossy green "Play Now ▶" button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF009624))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(22.dp))
                        .padding(horizontal = 22.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Play Now",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3D Golden Crown on top of card
        Text(
            text = "👑",
            fontSize = 32.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
        )
    }
}

/**
 * Adjacent Carrom Card matching the reference image carousel:
 */
@Composable
fun MgLudoCarromCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(top = 16.dp)
            .width(200.dp)
            .height(274.dp)
            .shadow(12.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .testTag("mode_card_Carrom"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(2.5.dp, Color(0xFF80D8FF).copy(alpha = 0.7f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.mg_carrom_card),
                contentDescription = "Carrom",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

