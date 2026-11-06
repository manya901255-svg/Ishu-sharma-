package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.theme.*

data class DailyChallengeItem(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val starReward: Int,
    val coinReward: Long,
    val cashReward: Int,
    val currentProgress: Int,
    val targetProgress: Int,
    val hasAdBoost: Boolean = false,
    val actionText: String = "PLAY"
)

val HUGE_CHALLENGES_POOL = listOf(
    DailyChallengeItem("ch_online_classic", "Play 2 Online Classic Game(s)", "🅲", 25, 50000L, 50, 0, 2, true, "PLAY"),
    DailyChallengeItem("ch_use_gifts", "Use 2 Gifts for Friends", "🎁", 20, 25000L, 25, 0, 2, true, "PLAY"),
    DailyChallengeItem("ch_speed_up_chest", "Speed Up Chest Opening by 3 Hours", "📦", 30, 40000L, 50, 0, 3, true, "Go"),
    DailyChallengeItem("ch_capture_pawns", "Capture 5 Opponent Pawns", "🎯", 35, 75000L, 75, 0, 5, true, "PLAY"),
    DailyChallengeItem("ch_roll_three_sixes", "Roll three 6s in any match", "🎲", 20, 30000L, 30, 0, 3, false, "PLAY"),
    DailyChallengeItem("ch_win_streak", "Achieve a 2-Win Streak Online", "🔥", 50, 100000L, 100, 0, 2, false, "PLAY"),
    DailyChallengeItem("ch_send_keys", "Send Keys to 3 Royal Friends", "🔑", 25, 50000L, 40, 0, 3, false, "Go"),
    DailyChallengeItem("ch_rush_champion", "Complete 2 Quick Rush Matches", "⚡", 40, 80000L, 80, 0, 2, true, "PLAY")
)

/**
 * RoyalChallengesDialog renders the authentic "Daily Challenges" interface shown in Screenshot 3:
 * - Top currency bar with Coins & Cash
 * - Header ribbon: "Daily Challenges" with (i) info button
 * - Subtitle: "Complete challenges to collect rewards"
 * - Tier 1 Rewards: Card pack illustration
 * - Progress bar: Star 10/75 with "Resets in 3d 7h"
 * - Interactive refresh 🔄 generates brand new challenges continuously!
 * - Completing challenges grants huge coin/cash rewards and brings in fresh challenges.
 */
@Composable
fun RoyalChallengesDialog(
    userCoins: Long = 1250000L,
    userCash: Int = 72,
    onClaimChallenge: (Long, Int) -> Unit,
    onPlayChallenge: () -> Unit,
    onDismiss: () -> Unit
) {
    var challenges by remember {
        mutableStateOf(
            listOf(
                HUGE_CHALLENGES_POOL[0],
                HUGE_CHALLENGES_POOL[1],
                HUGE_CHALLENGES_POOL[2]
            )
        )
    }

    var starProgress by remember { mutableIntStateOf(10) }
    val maxStars = 75

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_challenges_screen"),
            color = Color(0xFF2E63DE) // Royal Blue top background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Back Button, Coins, Cash
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC107))
                            .clickable { onDismiss() }
                            .testTag("daily_challenges_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Currency Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coins Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("1.25 M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cash Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💵", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("72", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Header Ribbon: "Daily Challenges" + (i) Button
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFEF5350), Color(0xFFD32F2F))
                                )
                            )
                            .border(2.5.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Daily Challenges",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0288D1))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("i", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Complete challenges to collect rewards",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Main Purple Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .weight(1f)
                        .padding(bottom = 12.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC08EC7)) // Soft Lavender / Violet
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Section: Tier 1 Rewards & Card Pack
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Tier 1 Rewards",
                                color = Color(0xFF4A148C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Card Pack Graphic
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF00C853))
                                    .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🃏", fontSize = 36.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Resets in 3d 7h pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF176))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Resets in 🕒 3d 7h",
                                        color = Color(0xFF3E2723),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Star Progress Bar (10/75)
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⭐", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color(0xFF261042))
                                        .border(1.dp, Color(0xFFE1BEE7), RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(starProgress.toFloat() / maxStars)
                                            .align(Alignment.CenterStart)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF00E676), Color(0xFF00B0FF))
                                                )
                                            )
                                    )
                                    Text(
                                        text = "$starProgress/$maxStars",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Challenge Cards List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            challenges.forEach { ch ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF4E8)),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD7CCC8))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        val isCompleted = ch.currentProgress >= ch.targetProgress

                                        // Title & Refresh
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ch.title,
                                                color = Color(0xFF2E1C44),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val available = HUGE_CHALLENGES_POOL.filter { poolItem ->
                                                        challenges.none { it.id == poolItem.id }
                                                    }
                                                    val replacement = available.randomOrNull() ?: HUGE_CHALLENGES_POOL.random()
                                                    challenges = challenges.map { if (it.id == ch.id) replacement else it }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Refresh Challenge",
                                                    tint = Color(0xFF5D4037),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Reward pills + Action button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Icon badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE1BEE7)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(ch.iconEmoji, fontSize = 16.sp)
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                // Star Reward pill
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color.White)
                                                        .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("⭐", fontSize = 12.sp)
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("${ch.starReward}", color = Color(0xFF263238), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))

                                                // Coin Reward pill
                                                if (ch.coinReward > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.White)
                                                            .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("🪙", fontSize = 12.sp)
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                if (ch.coinReward >= 1000) "${ch.coinReward / 1000} K" else "${ch.coinReward}",
                                                                color = Color(0xFFE65100),
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(4.dp))

                                                // Cash / Gems Reward pill
                                                if (ch.cashReward > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.White)
                                                            .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("💵", fontSize = 12.sp)
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("${ch.cashReward}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }

                                            // Action Button [CLAIM] or [PLAY] / [Go]
                                            if (isCompleted) {
                                                Button(
                                                    onClick = {
                                                        onClaimChallenge(ch.coinReward, ch.cashReward)
                                                        starProgress = minOf(maxStars, starProgress + ch.starReward)
                                                        // Immediately rotate in a fresh brand new challenge
                                                        val available = HUGE_CHALLENGES_POOL.filter { poolItem ->
                                                            challenges.none { it.id == poolItem.id }
                                                        }
                                                        val replacement = available.randomOrNull() ?: HUGE_CHALLENGES_POOL.random()
                                                        challenges = challenges.map { if (it.id == ch.id) replacement else it }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF00C853)
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text(
                                                        text = "CLAIM 🎉",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        if (ch.currentProgress + 1 >= ch.targetProgress) {
                                                            challenges = challenges.map {
                                                                if (it.id == ch.id) it.copy(currentProgress = it.targetProgress) else it
                                                            }
                                                        } else {
                                                            onPlayChallenge()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF0091EA)
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text(
                                                        text = ch.actionText,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Progress Bar (0/2 or 0/3)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(14.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(Color(0xFF455A64))
                                                    .clickable {
                                                        // Tapping advances progress for testing & instant play
                                                        challenges = challenges.map {
                                                            if (it.id == ch.id) it.copy(currentProgress = minOf(it.targetProgress, it.currentProgress + 1)) else it
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val progressRatio = (ch.currentProgress.toFloat() / ch.targetProgress).coerceIn(0f, 1f)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(progressRatio)
                                                        .align(Alignment.CenterStart)
                                                        .background(if (isCompleted) Color(0xFF00E676) else Color(0xFF29B6F6))
                                                )
                                                Text(
                                                    text = "${ch.currentProgress}/${ch.targetProgress}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (ch.hasAdBoost) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFE0E0E0))
                                                        .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            challenges = challenges.map {
                                                                if (it.id == ch.id) it.copy(currentProgress = minOf(it.targetProgress, it.currentProgress + 1)) else it
                                                            }
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("🎬", fontSize = 10.sp)
                                                        Text("+1", color = Color(0xFF37474F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom timer banner: "Challenges refresh in 7h 31m"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF381559))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📋", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Challenges refresh in 🕒 7h 31m",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
