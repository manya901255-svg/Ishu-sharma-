package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.viewmodel.AppScreen

/**
 * Top Header Bar matching the MG Ludo reference screenshot:
 * - Circular avatar with crown, online indicator, Lv.12 progress bar
 * - Coin capsule with 25.5K and green +
 * - Diamond capsule with 120 and green +
 * - Notification bell with red badge 3
 * - Golden settings cog
 */
@Composable
fun MgLudoTopHeader(
    playerName: String,
    crownAvatarId: String,
    customPhotoUri: String? = null,
    level: Int,
    coins: Long,
    gems: Int,
    isMusicPlaying: Boolean = false,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMusicClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddCoins: () -> Unit,
    onAddGems: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Player Profile with Crown, Name, Online Status and Level
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onProfileClick() }
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D2554))
                        .border(2.dp, RoyalGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!customPhotoUri.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = customPhotoUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_ishu),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Crown badge on top-left of avatar
                Text(
                    text = "👑",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-4).dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = playerName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Green Online Indicator Dot + "Online"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Online",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Level badge with yellow star & progress bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF081C44))
                            .border(1.dp, RoyalGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(1.dp))
                            Text(
                                text = "Lv.$level",
                                color = RoyalGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // XP Progress bar
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF081C44))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.70f)
                                .background(Brush.horizontalGradient(listOf(RoyalGoldLight, RoyalGold)))
                        )
                    }
                }
            }
        }

        // Currency Wallets & Action Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Coins Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF061536))
                    .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .clickable { onAddCoins() }
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = formatCompactCoins(coins),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Diamonds/Gems Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF061536))
                    .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .clickable { onAddGems() }
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = gems.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Music Player Button (🎵)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isMusicPlaying) Color(0xFF00E676).copy(alpha = 0.35f) else Color(0xFF0D2554).copy(alpha = 0.7f))
                    .border(1.dp, if (isMusicPlaying) Color(0xFF00E676) else Color(0xFF40C4FF).copy(alpha = 0.5f), CircleShape)
                    .clickable { onMusicClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(if (isMusicPlaying) "🎵" else "🔇", fontSize = 16.sp)
            }

            // Notification Bell with Red Badge "3"
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D2554).copy(alpha = 0.7f))
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 16.sp)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("3", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Settings Cog
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D2554).copy(alpha = 0.7f))
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 18.sp)
            }
        }
    }
}

/**
 * Top 4 Action Cards Row matching the reference image:
 * Ranking (🏆), Leaderboard (📅), Tournaments (🎁), Lucky Spin (🚀).
 */
@Composable
fun MgLudoTopPills(
    onRankingClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onTournamentsClick: () -> Unit,
    onLuckySpinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Ranking Card
        MgLudoTopCard(
            icon = "🏆",
            title = "Ranking",
            subtitle = "54.20h",
            modifier = Modifier.weight(1f),
            onClick = onRankingClick
        )

        // 2. Leaderboard Card
        MgLudoTopCard(
            icon = "📅",
            title = "Leaderboard",
            subtitle = "66.7h",
            modifier = Modifier.weight(1f),
            onClick = onLeaderboardClick
        )

        // 3. Tournaments Card
        MgLudoTopCard(
            icon = "🎁",
            title = "Tournaments",
            subtitle = "Get Join",
            modifier = Modifier.weight(1f),
            onClick = onTournamentsClick
        )

        // 4. Lucky Spin Card
        MgLudoTopCard(
            icon = "🚀",
            title = "Lucky Spin",
            subtitle = "Spin Free",
            modifier = Modifier.weight(1f),
            onClick = onLuckySpinClick
        )
    }
}

@Composable
private fun MgLudoTopCard(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0288D1), Color(0xFF01579B))
                )
            )
            .border(1.5.dp, Color(0xFF40C4FF).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = Color(0xFFB3E5FC),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * Side Action Button matching the reference image's glossy cyan-blue cards:
 */
@Composable
fun MgLudoSideActionButton(
    icon: String,
    label: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFFF1744),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 46.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0288D1), Color(0xFF01579B))
                )
            )
            .border(1.2.dp, Color(0xFF40C4FF).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                text = label,
                color = Color.White,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 8.5.sp,
                maxLines = 2
            )
        }

        // Notification badge
        if (!badgeText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Left Action Column matching reference image (7 buttons):
 * 1. Get 1k Coins
 * 2. Key Free
 * 3. Treasure (BONUS)
 * 4. Like
 * 5. Avatar
 * 6. Queries
 * 7. Offers (%)
 */
@Composable
fun MgLudoLeftActionColumn(
    keysCount: Int,
    onGetCoinsClick: () -> Unit,
    onKeyFreeClick: () -> Unit,
    onTreasureClick: () -> Unit,
    onLikeClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onQueriesClick: () -> Unit,
    onOffersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Get 1k Coins
        MgLudoSideActionButton(
            icon = "🪙",
            label = "Get 1k",
            badgeText = "AD",
            badgeColor = Color(0xFF00E676),
            onClick = onGetCoinsClick
        )

        // 2. Key Free
        MgLudoSideActionButton(
            icon = "🔑",
            label = "Key Free",
            badgeText = "$keysCount/7",
            badgeColor = Color(0xFFFFD600),
            onClick = onKeyFreeClick
        )

        // 3. Treasure
        MgLudoSideActionButton(
            icon = "🎪",
            label = "Treasure",
            badgeText = "BONUS",
            badgeColor = Color(0xFFFF1744),
            onClick = onTreasureClick
        )

        // 4. Like
        MgLudoSideActionButton(
            icon = "👍",
            label = "Like",
            onClick = onLikeClick
        )

        // 5. Avatar
        MgLudoSideActionButton(
            icon = "👥",
            label = "Avatar",
            badgeText = "VIP",
            badgeColor = Color(0xFFFFD600),
            onClick = onAvatarClick
        )

        // 6. Queries
        MgLudoSideActionButton(
            icon = "📋",
            label = "Queries",
            onClick = onQueriesClick
        )

        // 7. Offers
        MgLudoSideActionButton(
            icon = "🏷️",
            label = "Offers",
            badgeText = "%",
            badgeColor = Color(0xFFFF6D00),
            onClick = onOffersClick
        )
    }
}

/**
 * Right Action Column matching reference image (5 buttons):
 * 1. Daily Reward
 * 2. Missions
 * 3. Tasks
 * 4. Mail (badge 5)
 * 5. Support
 */
@Composable
fun MgLudoRightActionColumn(
    mailCount: Int = 5,
    onDailyRewardClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onMailClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Daily Reward
        MgLudoSideActionButton(
            icon = "🎁",
            label = "Daily",
            badgeText = "FREE",
            badgeColor = Color(0xFF00E676),
            onClick = onDailyRewardClick
        )

        // 2. Missions
        MgLudoSideActionButton(
            icon = "📋",
            label = "Missions",
            badgeText = "NEW",
            badgeColor = Color(0xFFFF1744),
            onClick = onMissionsClick
        )

        // 3. Tasks
        MgLudoSideActionButton(
            icon = "⭐",
            label = "Tasks",
            onClick = onTasksClick
        )

        // 4. Mail
        MgLudoSideActionButton(
            icon = "✉️",
            label = "Mail",
            badgeText = mailCount.toString(),
            badgeColor = Color(0xFFFF1744),
            onClick = onMailClick
        )

        // 5. Support
        MgLudoSideActionButton(
            icon = "🎧",
            label = "Support",
            onClick = onSupportClick
        )
    }
}

/**
 * 4 Carousel Indicator Dots below the promotional card
 */
@Composable
fun MgLudoCarouselDots(
    selectedIndex: Int = 0,
    totalCount: Int = 4,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalCount) {
            val isSelected = i == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.45f)
                    )
            )
        }
    }
}


/**
 * Events Hub Banner matching the reference image:
 * Wide horizontal purple/navy card with golden trophy, "Events Hub",
 * "Play & Win Big Rewards", gift pile, and golden "View Events ▶" button.
 */
@Composable
fun MgLudoEventsHubBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF38006B), // Deep purple
                        Color(0xFF4A148C), // Violet
                        Color(0xFF1A237E)  // Navy blue
                    )
                )
            )
            .border(2.dp, RoyalGold, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Golden Trophy
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Events Hub",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Play & Win Big Rewards",
                        color = RoyalGoldLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Gift and Golden Action Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎁", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(RoyalGoldLight, RoyalGold)
                            )
                        )
                        .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                        .clickable { onClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "View Events ▶",
                        color = Color(0xFF3E2723),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * "⚡ Quick Play" Section matching the reference image:
 * - Header row with "⚡ Quick Play" and "See All ▶"
 * - 4 mode cards: 2 Player Ludo, 4 Player Ludo, Online Ludo, Private Room
 */
@Composable
fun MgLudoQuickPlaySection(
    onTwoPlayerClick: () -> Unit,
    onFourPlayerClick: () -> Unit,
    onOnlineClick: () -> Unit,
    onPrivateRoomClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡",
                    color = RoyalGold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Quick Play",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = "See All ▶",
                color = Color(0xFF80D8FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4 Colorful Mode Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card 1: 2 Player Ludo (Purple)
            QuickPlayModeCard(
                icon = "👥",
                title = "2 Player\nLudo",
                bgGradient = listOf(Color(0xFF8E24AA), Color(0xFF4A148C)),
                borderColor = Color(0xFFBA68C8),
                modifier = Modifier.weight(1f),
                onClick = onTwoPlayerClick
            )

            // Card 2: 4 Player Ludo (Green)
            QuickPlayModeCard(
                icon = "👥",
                title = "4 Player\nLudo",
                bgGradient = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                borderColor = Color(0xFF81C784),
                modifier = Modifier.weight(1f),
                onClick = onFourPlayerClick
            )

            // Card 3: Online Ludo (Blue)
            QuickPlayModeCard(
                icon = "🌐",
                title = "Online\nLudo",
                bgGradient = listOf(Color(0xFF0288D1), Color(0xFF01579B)),
                borderColor = Color(0xFF4FC3F7),
                modifier = Modifier.weight(1f),
                onClick = onOnlineClick
            )

            // Card 4: Private Room (Orange)
            QuickPlayModeCard(
                icon = "👥",
                title = "Private\nRoom",
                bgGradient = listOf(Color(0xFFE65100), Color(0xFFBF360C)),
                borderColor = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f),
                onClick = onPrivateRoomClick
            )
        }
    }
}

@Composable
private fun QuickPlayModeCard(
    icon: String,
    title: String,
    bgGradient: List<Color>,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(68.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(bgGradient))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}

/**
 * 5-Tab Bottom Navigation Dock matching the reference image:
 * Store, Inventory (badge 2), Home (raised capsule), Friends (badge 8), Club.
 */
@Composable
fun MgLudoBottomDock(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    onInventoryClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onClubClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(16.dp),
        color = Color(0xFF05153A),
        border = BorderStroke(1.2.dp, Color(0xFF00B0FF).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Store
            DockTabItem(
                icon = "🏪",
                label = "Store",
                isSelected = currentScreen == AppScreen.SHOP,
                badge = null
            ) {
                onScreenSelected(AppScreen.SHOP)
            }

            // Tab 2: Inventory
            DockTabItem(
                icon = "🎒",
                label = "Inventory",
                isSelected = false,
                badge = "2"
            ) {
                onInventoryClick()
            }

            // Tab 3: Home (Elevated Central Blue Capsule with White House)
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .size(width = 62.dp, height = 62.dp)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF0288D1), Color(0xFF01579B))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                    .clickable { onScreenSelected(AppScreen.HOME) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏠", fontSize = 22.sp)
                    Text(
                        text = "Home",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Tab 4: Friends
            DockTabItem(
                icon = "👥",
                label = "Friends",
                isSelected = false,
                badge = "8"
            ) {
                onFriendsClick()
            }

            // Tab 5: Club
            DockTabItem(
                icon = "🏆",
                label = "Club",
                isSelected = false,
                badge = null
            ) {
                onClubClick()
            }
        }
    }
}

@Composable
private fun DockTabItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF90CAF9),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }

        if (!badge.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF1744)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
