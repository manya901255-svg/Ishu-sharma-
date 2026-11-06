package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeaderboardUser
import com.example.ui.components.formatCompactCoins
import com.example.ui.theme.*

val SAMPLE_LEADERBOARD = listOf(
    LeaderboardUser(1, "Maharaja Jai Singh", "Grand Emperor", "avatar_maharaja", 3420, "78%", 1450000),
    LeaderboardUser(2, "Rani Padmini", "Supreme Empress", "avatar_maharani", 3180, "74%", 1280000),
    LeaderboardUser(3, "Senapati Amar", "Grand General", "avatar_warrior", 2950, "71%", 980000),
    LeaderboardUser(4, "Yuvraj Devraj", "Crown Prince", "avatar_prince", 2740, "68%", 850000),
    LeaderboardUser(5, "Raja Pratap", "Desert King", "avatar_maharaja", 2590, "66%", 720000),
    LeaderboardUser(6, "Dewan Hemu", "Royal Minister", "avatar_warrior", 2410, "63%", 610000),
    LeaderboardUser(7, "Nawab Faiz", "Imperial Courtier", "avatar_prince", 2280, "61%", 540000),
    LeaderboardUser(8, "Maharani Gayatri", "Royal Sovereign", "avatar_maharani", 2150, "59%", 490000),
    LeaderboardUser(12, "You (Maharaja)", "Court Champion", "avatar_maharaja", 1850, "58%", 320000)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Global League", "Weekly Royal", "Kingdom Friends")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = RoyalGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Royal Leaderboard",
                            color = RoyalGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalNavyDark)
            )
        },
        containerColor = RoyalNavyDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("leaderboard_screen")
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RoyalNavy,
                contentColor = RoyalGold
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) RoyalGold else TextMuted,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SAMPLE_LEADERBOARD) { user ->
                    LeaderboardRow(user = user)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(user: LeaderboardUser) {
    val isCurrentUser = user.name.contains("You")
    val isTop3 = user.rank in 1..3

    val rankBadgeColor = when (user.rank) {
        1 -> RoyalGold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> RoyalNavyAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isCurrentUser) 8.dp else 2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) RoyalBlueSurface else RoyalBlueCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrentUser) 2.dp else 1.dp,
            color = if (isCurrentUser) RoyalGold else RoyalNavyAccent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isTop3) rankBadgeColor else RoyalNavyDark)
                        .border(1.dp, rankBadgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${user.rank}",
                        color = if (isTop3) RoyalNavyDark else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RoyalBlueCard)
                        .border(1.5.dp, rankBadgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (user.avatar) {
                            "avatar_maharaja" -> "🤴"
                            "avatar_maharani" -> "👸"
                            "avatar_warrior" -> "👳"
                            else -> "🥷"
                        },
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold
                        )
                        if (isTop3) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Star, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = "${user.title} • Win: ${user.winRate}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${user.trophies}",
                        color = RoyalGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "🪙 ${formatCompactCoins(user.totalEarnings)}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
