package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GameMode
import com.example.viewmodel.LudoUiState

enum class EventHubFeatureType {
    ALBUM_HUNT,
    PUZZLE_MANIA,
    TEAM_PARTY,
    WEEKLY_EVENT,
    COLLECT_QUEST,
    MINI_EVENT,
    WIN_STREAK,
    DAILY_CHEST,
    MATCH_REPLAY,
    DAILY_MISSIONS
}

@Composable
fun EventsHubDialog(
    uiState: LudoUiState,
    onClaimEventReward: (Long, Int, String) -> Unit,
    onJoinTournament: (GameMode, Int) -> Unit,
    onClaimMission: (com.example.model.DailyMission) -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeFeature by remember { mutableStateOf<EventHubFeatureType?>(null) }
    var dailyChestClaimed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("events_hub_dialog_full"),
            color = Color(0xFF00388A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Festive Banner & Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF29B6F6), Color(0xFF0091EA))
                            )
                        )
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC107))
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { onDismiss() }
                            .testTag("events_hub_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Event Hub Title Logo
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EVENT HUB 🎯",
                            color = Color(0xFFFFD54F),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "All 8 Features Active • Play & Claim Rewards",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 8 Event Cards Scrollable Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // 1. Album Hunt
                    item {
                        EventHubCardItem(
                            timer = "Ends in: 28d 20h",
                            title = "Album\nHunt",
                            icon = "🎲📖",
                            badge = "6/6",
                            badgeColor = Color(0xFF00C853),
                            bgGradient = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
                            onOpen = { activeFeature = EventHubFeatureType.ALBUM_HUNT }
                        )
                    }

                    // 2. Puzzle Mania
                    item {
                        EventHubCardItem(
                            timer = "Ends in: 9d 1h",
                            title = "Puzzle\nMania",
                            icon = "🧩",
                            subPill = "Puzzle #4",
                            bgGradient = listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9)),
                            onOpen = { activeFeature = EventHubFeatureType.PUZZLE_MANIA }
                        )
                    }

                    // 3. Team Party
                    item {
                        EventHubCardItem(
                            timer = "Active Now",
                            title = "Team\nParty",
                            icon = "🛡️👥",
                            subPill = "2v2 Mode",
                            bgGradient = listOf(Color(0xFFFFF8E1), Color(0xFFFFE082)),
                            onOpen = { activeFeature = EventHubFeatureType.TEAM_PARTY }
                        )
                    }

                    // 4. Weekly Event
                    item {
                        EventHubCardItem(
                            timer = "Ends in: 6d 18h",
                            title = "Weekly\nEvent",
                            icon = "🏆⭐",
                            subPill = "1M Pot",
                            bgGradient = listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D)),
                            onOpen = { activeFeature = EventHubFeatureType.WEEKLY_EVENT }
                        )
                    }

                    // 5. Collect Quest
                    item {
                        EventHubCardItem(
                            timer = "Ends in: 12h 52m",
                            title = "Collect\nQuest",
                            icon = "🎒⚽",
                            subPill = "3/5 Ready",
                            bgGradient = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
                            onOpen = { activeFeature = EventHubFeatureType.COLLECT_QUEST }
                        )
                    }

                    // 6. Mini Event
                    item {
                        EventHubCardItem(
                            timer = "Ends in: 18h 2m",
                            title = "Mini\nEvent",
                            icon = "⏱️💨",
                            subPill = "Blitz 2P",
                            bgGradient = listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
                            onOpen = { activeFeature = EventHubFeatureType.MINI_EVENT }
                        )
                    }

                    // 7. Win Streak
                    item {
                        EventHubCardItem(
                            timer = "Active Streak",
                            title = "Win\nStreak",
                            icon = "🔥",
                            subPill = "2.5X Boost",
                            bgGradient = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                            onOpen = { activeFeature = EventHubFeatureType.WIN_STREAK }
                        )
                    }

                    // 8. Daily Chest
                    item {
                        EventHubCardItem(
                            timer = if (dailyChestClaimed) "Next: 2h 32m" else "Ready!",
                            title = "Daily\nChest",
                            icon = if (dailyChestClaimed) "🎁" else "📦",
                            isClaimed = dailyChestClaimed,
                            bgGradient = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC)),
                            onOpen = { activeFeature = EventHubFeatureType.DAILY_CHEST }
                        )
                    }

                    // 9. Match Replay
                    item {
                        EventHubCardItem(
                            timer = "Replays: ${uiState.savedReplays.size}",
                            title = "Match\nReplay",
                            icon = "🎬📽️",
                            subPill = "Theater",
                            bgGradient = listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9)),
                            onOpen = { activeFeature = EventHubFeatureType.MATCH_REPLAY }
                        )
                    }

                    // 10. Daily Missions
                    item {
                        EventHubCardItem(
                            timer = "${uiState.dailyMissions.count { it.isCompleted }}/${uiState.dailyMissions.size} Ready",
                            title = "Daily\nMissions",
                            icon = "📜👑",
                            subPill = "Regal Quests",
                            bgGradient = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)),
                            onOpen = { activeFeature = EventHubFeatureType.DAILY_MISSIONS }
                        )
                    }
                }
            }

            // Sub-feature interactive dialogs
            when (activeFeature) {
                EventHubFeatureType.ALBUM_HUNT -> {
                    AlbumHuntDialog(
                        onClaimReward = { coins, gems ->
                            onClaimEventReward(coins, gems, "Album Hunt")
                            activeFeature = null
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.PUZZLE_MANIA -> {
                    PuzzleManiaDialog(
                        onSolvePuzzle = { coins, gems ->
                            onClaimEventReward(coins, gems, "Puzzle Mania")
                            activeFeature = null
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.TEAM_PARTY -> {
                    TeamPartyDialog(
                        onPlay2v2 = {
                            activeFeature = null
                            onJoinTournament(GameMode.PASS_AND_PLAY, 4)
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.WEEKLY_EVENT -> {
                    WeeklyTournamentDialog(
                        onEnterMatch = {
                            activeFeature = null
                            onJoinTournament(GameMode.TOURNAMENT, 4)
                        },
                        onClaimBonus = { coins, gems ->
                            onClaimEventReward(coins, gems, "Weekly Tournament Qualifier")
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.COLLECT_QUEST -> {
                    CollectQuestDialog(
                        onClaimQuest = { coins, gems, questName ->
                            onClaimEventReward(coins, gems, questName)
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.MINI_EVENT -> {
                    MiniEventDialog(
                        onPlayBlitz = {
                            activeFeature = null
                            onJoinTournament(GameMode.QUICK_RUSH, 2)
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.WIN_STREAK -> {
                    WinStreakDialog(
                        winStreak = uiState.winStreak,
                        onPlayRanked = {
                            activeFeature = null
                            onJoinTournament(GameMode.ONLINE_MULTIPLAYER, 2)
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.DAILY_CHEST -> {
                    DailyChestSubDialog(
                        isClaimed = dailyChestClaimed,
                        onClaimChest = {
                            dailyChestClaimed = true
                            onClaimEventReward(20000L, 20, "Daily Mystery Chest")
                            activeFeature = null
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.MATCH_REPLAY -> {
                    MatchReplayViewerDialog(
                        replays = uiState.savedReplays,
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.DAILY_MISSIONS -> {
                    DailyMissionsDialog(
                        missions = uiState.dailyMissions,
                        onClaimMission = { mission ->
                            onClaimMission(mission)
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                null -> {}
            }
        }
    }
}

@Composable
private fun EventHubCardItem(
    timer: String,
    title: String,
    icon: String,
    badge: String? = null,
    badgeColor: Color = Color(0xFFD50000),
    subPill: String? = null,
    isClaimed: Boolean = false,
    bgGradient: List<Color>,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFB0BEC5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgGradient))
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer header pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF37474F).copy(alpha = 0.8f))
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(timer, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            // Title
            Text(
                text = title,
                color = Color(0xFF1A237E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )

            // Icon with optional badge
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 28.sp)
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(badge, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (subPill != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF263238))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(subPill, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // [Open] Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isClaimed) Color(0xFF9E9E9E) else Color(0xFF00B0FF))
                    .clickable(enabled = !isClaimed) { onOpen() }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isClaimed) "Claimed" else "Play / Open",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// 1. Album Hunt Sub-Dialog
@Composable
private fun AlbumHuntDialog(
    onClaimReward: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var claimed by remember { mutableStateOf(false) }
    val cards = listOf(
        Triple("👑", "Maharaja Crown", "⭐ 5-Star"),
        Triple("🐎", "Royal Steed", "⭐ 3-Star"),
        Triple("🦚", "Peacock Throne", "⭐ 4-Star"),
        Triple("🎲", "Golden Dice", "⭐ 5-Star"),
        Triple("🐘", "Imperial Elephant", "⭐ 4-Star"),
        Triple("🏰", "Jaipur Palace", "⭐ 5-Star")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A224E)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📖 Album Hunt: Royal Heritage", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Collect all 6 heritage stickers to claim the Royal Bounty!", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(cards.size) { i ->
                        val item = cards[i]
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14346E)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(item.first, fontSize = 22.sp)
                                Text(item.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(item.third, color = Color(0xFFFFD54F), fontSize = 8.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        claimed = true
                        onClaimReward(100000L, 250)
                    },
                    enabled = !claimed,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (claimed) "Bounty Claimed ✓" else "🎉 Claim Album Bounty (+🪙 100K & +💎 250)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

// 2. Puzzle Mania Sub-Dialog
@Composable
private fun PuzzleManiaDialog(
    onSolvePuzzle: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var diceRolled by remember { mutableStateOf(false) }
    var puzzleSolved by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A3A)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFAB47BC))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🧩 Puzzle Mania: Escape Trap", color = Color(0xFFE1BEE7), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Your Yellow pawn is being chased! Roll a 6 to jump directly to the Star Safe Zone!", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))

                // Puzzle board track representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2454))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (puzzleSolved) "💨" else "🟡", fontSize = 24.sp)
                        Text("You", color = Color(0xFFFFD54F), fontSize = 10.sp)
                    }
                    Text("➡️", color = Color.White)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔴", fontSize = 24.sp)
                        Text("Enemy", color = Color(0xFFFF5252), fontSize = 10.sp)
                    }
                    Text("➡️", color = Color.White)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (puzzleSolved) "🟡⭐" else "⭐", fontSize = 24.sp)
                        Text("Safe Star", color = Color(0xFF00E676), fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (puzzleSolved) {
                    Text("🎉 Safe Star Reached! Enemy evaded!", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSolvePuzzle(25000L, 50) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Claim Reward: +🪙 25,000 & +💎 50", color = Color.White, fontWeight = FontWeight.Black)
                    }
                } else {
                    Button(
                        onClick = {
                            diceRolled = true
                            puzzleSolved = true
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAB47BC)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🎲 Roll Puzzle Dice (Guaranteed 6)", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// 3. Team Party Sub-Dialog
@Composable
private fun TeamPartyDialog(
    onPlay2v2: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162544)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF29B6F6))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️ Team Party: 2v2 Royale", color = Color(0xFF4FC3F7), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Co-op with a teammate! Teammates cannot capture each other and can pass pawns safely to win!", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(
                        modifier = Modifier.weight(1f).padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3666))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👑 YOUR TEAM", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🟡 You (Yellow)\n🔵 Ally (Blue)", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f).padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF521C26))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚔️ RIVAL TEAM", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🔴 Rival (Red)\n🟢 Rival (Green)", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPlay2v2,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚔️ Start 2v2 Team Party Match", color = Color(0xFF061833), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// 4. Weekly Tournament Sub-Dialog
@Composable
private fun WeeklyTournamentDialog(
    onEnterMatch: () -> Unit,
    onClaimBonus: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var bonusClaimed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E40)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆 Weekly Maharaja Cup", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Text("Prize Pool: 🪙 1,000,000 Coins • Ends in 6d 18h", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF111129))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("1. 👑 Maharaja_Vikram - 4,820 pts (Prize: 🪙 500K)", color = Color(0xFFFFD54F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("2. 🥈 Queen_Sanjana - 3,940 pts (Prize: 🪙 250K)", color = Color.White, fontSize = 10.sp)
                    Text("3. 🥉 Raja_Dev - 3,110 pts (Prize: 🪙 100K)", color = Color.White, fontSize = 10.sp)
                    Text("7. 🎖️ You (Ishu) - 1,840 pts (Prize: 🪙 25K)", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEnterMatch,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚔️ Play Tournament Round (Stake 2.5K)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        bonusClaimed = true
                        onClaimBonus(15000L, 20)
                    },
                    enabled = !bonusClaimed,
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Text(if (bonusClaimed) "Claimed ✓" else "🎁 Claim Qualifier Bonus (+🪙 15K)", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. Collect Quest Sub-Dialog
@Composable
private fun CollectQuestDialog(
    onClaimQuest: (Long, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var quest1Claimed by remember { mutableStateOf(false) }
    var quest2Claimed by remember { mutableStateOf(false) }
    var quest3Claimed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2554)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4FC3F7))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎒 Collect Quests Milestone", color = Color(0xFF4FC3F7), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                QuestItemRow("Roll three 6s in one match", "+🪙 20,000", quest1Claimed) {
                    quest1Claimed = true
                    onClaimQuest(20000L, 0, "Quest: Three 6s")
                }
                Spacer(modifier = Modifier.height(6.dp))
                QuestItemRow("Send key to a royal friend", "+💎 50 Gems", quest2Claimed) {
                    quest2Claimed = true
                    onClaimQuest(0L, 50, "Quest: Send Key")
                }
                Spacer(modifier = Modifier.height(6.dp))
                QuestItemRow("Reach home with 2 pawns", "+🪙 35,000", quest3Claimed) {
                    quest3Claimed = true
                    onClaimQuest(35000L, 10, "Quest: 2 Pawns Home")
                }
            }
        }
    }
}

@Composable
private fun QuestItemRow(
    title: String,
    reward: String,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF143673))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(reward, color = Color(0xFFFFD54F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onClaim,
            enabled = !isClaimed,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(if (isClaimed) "Done ✓" else "Claim", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 6. Mini Event Sub-Dialog
@Composable
private fun MiniEventDialog(
    onPlayBlitz: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF381220)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏱️ Mini Event: Quick Rush Blitz", color = Color(0xFFFF8A80), fontSize = 15.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("High-speed 2-Player match where 2 pawns start already active on board! Fast 10-second turn timer for quick wins!", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onPlayBlitz,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚡ Play Quick Rush Blitz Now", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// 7. Win Streak Sub-Dialog
@Composable
private fun WinStreakDialog(
    winStreak: Int,
    onPlayRanked: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C0A)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF9800))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥 Win Streak Challenge", color = Color(0xFFFFB74D), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current Streak: $winStreak Wins in a Row! • 2.5X Winnings Boost Active!", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))
                Text("Next Milestone: 5 Wins = 3.0X Pot Multiplier + 📦 Royal Mystery Chest!", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onPlayRanked,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚔️ Play Ranked Match to Extend Streak", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// 8. Daily Chest Sub-Dialog (Opens with finger tap!)
@Composable
private fun DailyChestSubDialog(
    isClaimed: Boolean,
    onClaimChest: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "daily_finger_bounce")
    val fingerOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "daily_finger_offset"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14244D)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📦 Daily Mystery Chest", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (isClaimed) "You already opened today's chest! Next chest in 2h 32m."
                    else "Tap the chest box below with your finger to open today's rewards!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Chest Box
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A1838))
                        .border(2.dp, if (isClaimed) Color.Gray else Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isClaimed) { onClaimChest() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isClaimed) "🎁" else "📦", fontSize = 56.sp)
                    if (!isClaimed) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = fingerOffset.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("👆", fontSize = 28.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF9800))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("TAP TO OPEN!", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Chest Contents: +🪙 20,000 Coins & +💎 20 Gems",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaimChest,
                    enabled = !isClaimed,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isClaimed) "Claimed Today ✓" else "🎉 Tap to Claim Rewards", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
