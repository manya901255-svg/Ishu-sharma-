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
import com.example.ui.theme.RoyalGoldLight
import com.example.util.LudoPeriodKeys
import com.example.viewmodel.LudoUiState
import kotlin.random.Random

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
    onClaimEventWithKey: (Long, Int, String, String) -> Unit = { c, g, n, _ -> onClaimEventReward(c, g, n) },
    onClaimVerifiedTask: (String) -> Unit = {},
    onJoinTournament: (GameMode, Int) -> Unit,
    onOpenLeaderboard: () -> Unit = {},
    onClaimMission: (com.example.model.DailyMission) -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeFeature by remember { mutableStateOf<EventHubFeatureType?>(null) }
    val dayKey = LudoPeriodKeys.currentDayKey()
    val weekKey = LudoPeriodKeys.currentWeekKey()
    val chestKey = "event_chest_$dayKey"
    val albumKey = "event_album_$weekKey"
    val puzzleKey = "event_puzzle_$dayKey"
    val weeklyQualKey = "event_weekly_qual_$weekKey"
    val dailyChestClaimed = chestKey in uiState.claimedBonusIds

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
                        val albumStickers = (uiState.totalGamesPlayed).coerceAtMost(6)
                        EventHubCardItem(
                            timer = "Weekly Album",
                            title = "Album\nHunt",
                            icon = "🎲📖",
                            badge = "$albumStickers/6",
                            badgeColor = if (albumStickers >= 6) Color(0xFF00C853) else Color(0xFF0288D1),
                            bgGradient = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
                            onOpen = { activeFeature = EventHubFeatureType.ALBUM_HUNT }
                        )
                    }

                    // 2. Puzzle Mania
                    item {
                        EventHubCardItem(
                            timer = if (puzzleKey in uiState.claimedBonusIds) "Solved Today ✓" else "Daily Puzzle",
                            title = "Puzzle\nMania",
                            icon = "🧩",
                            subPill = "Roll 6 Challenge",
                            isClaimed = puzzleKey in uiState.claimedBonusIds,
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
                            timer = "Weekly Cup",
                            title = "Weekly\nEvent",
                            icon = "🏆⭐",
                            subPill = "Leaderboard",
                            bgGradient = listOf(Color(0xFFFFFDE7), Color(0xFFFFF59D)),
                            onOpen = { activeFeature = EventHubFeatureType.WEEKLY_EVENT }
                        )
                    }

                    // 5. Collect Quest
                    item {
                        val questsReady = listOf(
                            uiState.todayActivity.diceRolls >= 15,
                            uiState.keysSentCount >= 1,
                            uiState.totalWins >= 2
                        ).count { it }
                        EventHubCardItem(
                            timer = "Daily Goals",
                            title = "Collect\nQuest",
                            icon = "🎒⚽",
                            subPill = "$questsReady/3 Ready",
                            bgGradient = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
                            onOpen = { activeFeature = EventHubFeatureType.COLLECT_QUEST }
                        )
                    }

                    // 6. Mini Event
                    item {
                        EventHubCardItem(
                            timer = "Blitz Arena",
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
                            timer = "Streak: ${uiState.winStreak}",
                            title = "Win\nStreak",
                            icon = "🔥",
                            subPill = "Ranked Boost",
                            bgGradient = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                            onOpen = { activeFeature = EventHubFeatureType.WIN_STREAK }
                        )
                    }

                    // 8. Daily Chest
                    item {
                        val chestEligible = uiState.todayActivity.matchesPlayed >= 1
                        EventHubCardItem(
                            timer = when {
                                dailyChestClaimed -> "Claimed ✓"
                                chestEligible -> "Unlocked!"
                                else -> "Play 1 Match"
                            },
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
                        val completedTasks = uiState.verifiedTasks.count { it.canClaim }
                        EventHubCardItem(
                            timer = "$completedTasks/${uiState.verifiedTasks.size.coerceAtLeast(1)} Ready",
                            title = "Daily\nTasks",
                            icon = "📜👑",
                            subPill = "Verified",
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
                        stickersUnlocked = uiState.totalGamesPlayed.coerceAtMost(6),
                        isAlreadyClaimed = albumKey in uiState.claimedBonusIds,
                        onClaimReward = { coins, gems ->
                            onClaimEventWithKey(coins, gems, "Album Hunt", albumKey)
                            activeFeature = null
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.PUZZLE_MANIA -> {
                    PuzzleManiaDialog(
                        isAlreadyClaimed = puzzleKey in uiState.claimedBonusIds,
                        canAttemptToday = uiState.todayActivity.matchesPlayed >= 1,
                        onSolvePuzzle = { coins, gems ->
                            onClaimEventWithKey(coins, gems, "Puzzle Mania", puzzleKey)
                            activeFeature = null
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.TEAM_PARTY -> {
                    TeamPartyDialog(
                        onPlay2v2 = {
                            activeFeature = null
                            onJoinTournament(GameMode.TEAM_2VS2, 4)
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.WEEKLY_EVENT -> {
                    WeeklyTournamentDialog(
                        uiState = uiState,
                        isBonusClaimed = weeklyQualKey in uiState.claimedBonusIds,
                        onEnterMatch = {
                            activeFeature = null
                            onJoinTournament(GameMode.TOURNAMENT, 4)
                        },
                        onClaimBonus = { coins, gems ->
                            onClaimEventWithKey(coins, gems, "Weekly Tournament Qualifier", weeklyQualKey)
                        },
                        onOpenLeaderboard = {
                            activeFeature = null
                            onOpenLeaderboard()
                        },
                        onDismiss = { activeFeature = null }
                    )
                }
                EventHubFeatureType.COLLECT_QUEST -> {
                    CollectQuestDialog(
                        uiState = uiState,
                        weekKey = weekKey,
                        onClaimQuest = { coins, gems, questName, dedupKey ->
                            onClaimEventWithKey(coins, gems, questName, dedupKey)
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
                        isUnlocked = uiState.todayActivity.matchesPlayed >= 1,
                        onClaimChest = {
                            onClaimEventWithKey(200L, 2, "Daily Mystery Chest", chestKey)
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
                        verifiedTasks = uiState.verifiedTasks,
                        verifiedAchievements = uiState.verifiedAchievements,
                        rewardHistory = uiState.rewardHistory,
                        loginStreak = uiState.loginStreak,
                        onClaimVerifiedTask = onClaimVerifiedTask,
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
    stickersUnlocked: Int,
    isAlreadyClaimed: Boolean,
    onClaimReward: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val canClaim = stickersUnlocked >= 6 && !isAlreadyClaimed
    val cards = listOf(
        Triple("👑", "Maharaja Crown", "Match #1"),
        Triple("🐎", "Royal Steed", "Match #2"),
        Triple("🦚", "Peacock Throne", "Match #3"),
        Triple("🎲", "Golden Dice", "Match #4"),
        Triple("🐘", "Imperial Elephant", "Match #5"),
        Triple("🏰", "Jaipur Palace", "Match #6")
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
                    Text("📖 Album Hunt ($stickersUnlocked/6)", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Play 6 complete Ludo matches to unlock all stickers!", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(cards.size) { i ->
                        val item = cards[i]
                        val unlocked = i < stickersUnlocked
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = if (unlocked) Color(0xFF14346E) else Color(0xFF0B1733)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (unlocked) Color(0xFF00E676) else Color(0xFF37474F))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(if (unlocked) item.first else "🔒", fontSize = 22.sp)
                                Text(item.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(item.third, color = Color(0xFFFFD54F), fontSize = 8.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { onClaimReward(1000L, 10) },
                    enabled = canClaim,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when {
                            isAlreadyClaimed -> "Bounty Claimed ✓"
                            stickersUnlocked < 6 -> "Unlock ${6 - stickersUnlocked} more via Matches"
                            else -> "🎉 Claim Album Bounty (+🪙 1,000 & +💎 10)"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

// 2. Puzzle Mania Sub-Dialog (Fair roll — gated to 1 match played today and 1 claim per day)
@Composable
private fun PuzzleManiaDialog(
    isAlreadyClaimed: Boolean,
    canAttemptToday: Boolean,
    onSolvePuzzle: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var lastRoll by remember { mutableIntStateOf(0) }
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
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (!canAttemptToday) "Complete 1 Ludo match today to unlock Puzzle Mania!" else "Roll a fair 6 to jump directly to the Star Safe Zone!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

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

                if (lastRoll > 0 && !puzzleSolved) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rolled $lastRoll — roll again to hit a 6!", color = RoyalGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    isAlreadyClaimed -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Text("Solved & Claimed Today ✓")
                        }
                    }
                    puzzleSolved -> {
                        Button(
                            onClick = { onSolvePuzzle(250L, 3) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Claim Reward: +🪙 250 & +💎 3", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                    else -> {
                        Button(
                            enabled = canAttemptToday,
                            onClick = {
                                val r = Random.nextInt(1, 7)
                                lastRoll = r
                                if (r == 6) puzzleSolved = true
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAB47BC)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🎲 Roll Fair Puzzle Dice", color = Color.White, fontWeight = FontWeight.Black)
                        }
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
    uiState: LudoUiState,
    isBonusClaimed: Boolean,
    onEnterMatch: () -> Unit,
    onClaimBonus: (Long, Int) -> Unit,
    onOpenLeaderboard: () -> Unit,
    onDismiss: () -> Unit
) {
    val canClaimQualifier = uiState.onlineGamesPlayedCount >= 3 && !isBonusClaimed
    val topStandings = uiState.publicLeaderboard.sortedByDescending { it.matchesWon }.take(4)

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
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Text("Play Online & Tournament matches to climb the live standings", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF111129))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (topStandings.isEmpty()) {
                        Text(
                            text = "No ranked matches recorded yet this week. Play Tournament Round to set the #1 mark!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    } else {
                        topStandings.forEachIndexed { idx, player ->
                            Text(
                                text = "${idx + 1}. ${player.avatarEmoji} ${player.displayName} — ${player.matchesWon} Wins (${player.xp} XP)",
                                color = if (idx == 0) Color(0xFFFFD54F) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEnterMatch,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚔️ Play Tournament Round (Stake 2.5K)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { onClaimBonus(1500L, 5) },
                    enabled = canClaimQualifier,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Text(
                        when {
                            isBonusClaimed -> "Qualifier Claimed ✓"
                            uiState.onlineGamesPlayedCount < 3 -> "Play ${3 - uiState.onlineGamesPlayedCount} more online matches to qualify"
                            else -> "🎁 Claim Qualifier Bonus (+🪙 1,500)"
                        },
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onOpenLeaderboard, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text("View Full Global Leaderboard", color = RoyalGoldLight, fontSize = 11.sp)
                }
            }
        }
    }
}

// 5. Collect Quest Sub-Dialog (Every claim gated by real stats + persisted dedup keys)
@Composable
private fun CollectQuestDialog(
    uiState: LudoUiState,
    weekKey: String,
    onClaimQuest: (Long, Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val q1Key = "event_quest_rolls_$weekKey"
    val q2Key = "event_quest_key_$weekKey"
    val q3Key = "event_quest_wins_$weekKey"

    val q1Done = uiState.todayActivity.diceRolls >= 15
    val q2Done = uiState.keysSentCount >= 1
    val q3Done = uiState.totalWins >= 2

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
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                QuestItemRow(
                    title = "Roll 15 dice today (${uiState.todayActivity.diceRolls}/15)",
                    reward = "+🪙 300",
                    isReady = q1Done,
                    isClaimed = q1Key in uiState.claimedBonusIds
                ) {
                    onClaimQuest(300L, 0, "Quest: Roll 15 Dice", q1Key)
                }
                Spacer(modifier = Modifier.height(6.dp))
                QuestItemRow(
                    title = "Send a vault key to a friend (${uiState.keysSentCount}/1)",
                    reward = "+💎 5 Cash",
                    isReady = q2Done,
                    isClaimed = q2Key in uiState.claimedBonusIds
                ) {
                    onClaimQuest(0L, 5, "Quest: Send Key", q2Key)
                }
                Spacer(modifier = Modifier.height(6.dp))
                QuestItemRow(
                    title = "Win 2 Ludo matches (${uiState.totalWins}/2)",
                    reward = "+🪙 500 & +💎 3",
                    isReady = q3Done,
                    isClaimed = q3Key in uiState.claimedBonusIds
                ) {
                    onClaimQuest(500L, 3, "Quest: 2 Match Wins", q3Key)
                }
            }
        }
    }
}

@Composable
private fun QuestItemRow(
    title: String,
    reward: String,
    isReady: Boolean,
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
            enabled = isReady && !isClaimed,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Text(
                text = when {
                    isClaimed -> "Done ✓"
                    isReady -> "Claim"
                    else -> "Locked"
                },
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
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

// 8. Daily Chest Sub-Dialog (Requires 1 match played today & allows 1 claim/day)
@Composable
private fun DailyChestSubDialog(
    isClaimed: Boolean,
    isUnlocked: Boolean,
    onClaimChest: () -> Unit,
    onDismiss: () -> Unit
) {
    val canClaim = isUnlocked && !isClaimed
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
                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    when {
                        isClaimed -> "You already opened today's chest! Resets at midnight UTC."
                        !isUnlocked -> "Complete at least 1 match today to unlock this Daily Chest!"
                        else -> "Unlocked! Tap the chest below to claim today's verified reward."
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A1838))
                        .border(2.dp, if (canClaim) Color(0xFFFFD54F) else Color.Gray, RoundedCornerShape(16.dp))
                        .clickable(enabled = canClaim) { onClaimChest() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isClaimed) "🎁" else "📦", fontSize = 56.sp)
                    if (canClaim) {
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
                    text = "Chest Contents: +🪙 200 Coins & +💵 2 Cash",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaimChest,
                    enabled = canClaim,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            isClaimed -> "Claimed Today ✓"
                            !isUnlocked -> "Play 1 Match Today to Unlock"
                            else -> "🎉 Tap to Claim Rewards"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
