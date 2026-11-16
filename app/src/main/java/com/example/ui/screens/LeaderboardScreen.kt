package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendshipItem
import com.example.model.PublicProfileItem
import com.example.model.VoiceClubItem
import com.example.ui.components.formatCompactCoins
import com.example.ui.theme.RoyalGold
import com.example.viewmodel.LudoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    uiState: LudoUiState = LudoUiState(),
    onSendFriendRequest: (PublicProfileItem) -> Unit = {},
    onToggleFollow: (String, String) -> Unit = { _, _ -> },
    onOpenClub: (VoiceClubItem) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Global League", "Clubs", "Friends")
    val myUid = uiState.authUserId ?: uiState.inventoryUiState.playerId
    val friendIds = remember(uiState.friends) { uiState.friends.map { it.friendUid }.toSet() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Leaderboard",
                            tint = RoyalGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Royal Leaderboard",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("leaderboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .testTag("leaderboard_screen")
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.testTag("leaderboard_tab_$index"),
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    val profiles = uiState.publicLeaderboard
                    if (profiles.isEmpty()) {
                        EmptyLeaderboardState(
                            emoji = "🏆",
                            title = "Global Rankings Syncing",
                            subtitle = "Complete online matches to climb the live global league leaderboard!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(profiles, key = { _, p -> p.userId }) { index, profile ->
                                val rank = index + 1
                                val isCurrentUser = profile.userId == myUid
                                val isFollowing = profile.userId in uiState.followedPlayerIds
                                val isFriend = profile.userId in friendIds
                                LiveLeaderboardRow(
                                    rank = rank,
                                    profile = profile,
                                    isCurrentUser = isCurrentUser,
                                    isFollowing = isFollowing,
                                    isFriend = isFriend,
                                    onToggleFollow = { onToggleFollow(profile.userId, profile.displayName) },
                                    onSendFriendRequest = { onSendFriendRequest(profile) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    val clubs = uiState.publicClubs
                    if (clubs.isEmpty()) {
                        EmptyLeaderboardState(
                            emoji = "🎙️",
                            title = "No Voice Clubs Ranked Yet",
                            subtitle = "Create or join a Voice Club in the Club tab to rank among top communities!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(clubs, key = { _, c -> c.clubId }) { index, club ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            RankBadge(rank = index + 1)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "${club.avatarEmoji} ${club.name}",
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${club.memberCount} Members • ${if (!club.isPrivate) "Public" else "Private"}",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onOpenClub(club) },
                                            modifier = Modifier.minimumInteractiveComponentSize(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    val friends = uiState.friends
                    if (friends.isEmpty()) {
                        EmptyLeaderboardState(
                            emoji = "👥",
                            title = "No Kingdom Friends Yet",
                            subtitle = "Add players from Global Rankings or share your referral code to compete with friends!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(friends, key = { it.friendUid }) { friend ->
                                FriendLeaderboardRow(friend = friend)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLeaderboardState(
    emoji: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 44.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val isTop3 = rank in 1..3
    val badgeColor = when (rank) {
        1 -> RoyalGold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(badgeColor)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            color = if (isTop3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LiveLeaderboardRow(
    rank: Int,
    profile: PublicProfileItem,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    isFriend: Boolean,
    onToggleFollow: () -> Unit,
    onSendFriendRequest: () -> Unit
) {
    val winPct = if (profile.matchesPlayed > 0) (profile.matchesWon * 100) / profile.matchesPlayed else 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isCurrentUser) 8.dp else 2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isCurrentUser) 2.dp else 1.dp,
            color = if (isCurrentUser) RoyalGold else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    RankBadge(rank = rank)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, RoyalGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.avatarEmoji.ifBlank { "👑" }, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCurrentUser) "${profile.displayName} (You)" else profile.displayName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (rank in 1..3) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Star, contentDescription = "Top Ranked", tint = RoyalGold, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "Lv.${profile.level} • ${profile.matchesWon}/${profile.matchesPlayed} Wins ($winPct%)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🏆 ${profile.matchesWon} Wins",
                        color = RoyalGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🪙 ${formatCompactCoins(profile.totalEarnings.toLong())}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            if (!isCurrentUser) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onToggleFollow,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("follow_player_${profile.userId}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Following ✓" else "Follow",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isFriend) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSendFriendRequest,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("add_friend_${profile.userId}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendLeaderboardRow(friend: FriendshipItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(friend.avatarEmoji.ifBlank { "👑" }, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = friend.friendName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (friend.isOnline) "🟢 Online Now" else "Offline",
                        color = if (friend.isOnline) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = "Friend",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

