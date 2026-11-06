package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyRewardStatus
import com.example.data.LudoRepository
import com.example.ui.theme.*

/**
 * DailyRewardDialog: Displays the 7-day regal reward login streak tracked in Room Database.
 */
@Composable
fun DailyRewardDialog(
    status: DailyRewardStatus?,
    onClaimReward: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentStreak = status?.streakDay ?: 1
    val isClaimedToday = status?.isClaimedToday ?: false

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("daily_reward_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DAILY ROYAL REWARDS",
                                color = RoyalGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Login every day to unlock imperial gold & gems",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("daily_reward_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Streak Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F265C), Color(0xFF1B3D8A), Color(0xFF0F265C))
                            )
                        )
                        .border(1.dp, RoyalGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Streak: Day $currentStreak of 7",
                                color = RoyalGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isClaimedToday) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Claimed", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Claim Available!",
                                color = RoyalGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7 Days Grid
                val schedule = LudoRepository.REWARD_SCHEDULE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First row: Days 1 to 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (day in 1..4) {
                            val isClaimed = if (isClaimedToday) day <= currentStreak else day < currentStreak
                            val isToday = day == currentStreak && !isClaimedToday
                            val (coins, gems) = schedule[day - 1]

                            Box(modifier = Modifier.weight(1f)) {
                                DayRewardCard(
                                    day = day,
                                    coins = coins,
                                    gems = gems,
                                    isClaimed = isClaimed,
                                    isToday = isToday,
                                    pulseScale = if (isToday) pulseScale else 1f
                                )
                            }
                        }
                    }

                    // Second row: Days 5 to 7 (Day 7 is grand prize)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (day in 5..6) {
                            val isClaimed = if (isClaimedToday) day <= currentStreak else day < currentStreak
                            val isToday = day == currentStreak && !isClaimedToday
                            val (coins, gems) = schedule[day - 1]

                            Box(modifier = Modifier.weight(1f)) {
                                DayRewardCard(
                                    day = day,
                                    coins = coins,
                                    gems = gems,
                                    isClaimed = isClaimed,
                                    isToday = isToday,
                                    pulseScale = if (isToday) pulseScale else 1f
                                )
                            }
                        }

                        // Day 7: Grand Maharaja Prize (weight = 2)
                        val isClaimed7 = if (isClaimedToday) 7 <= currentStreak else 7 < currentStreak
                        val isToday7 = 7 == currentStreak && !isClaimedToday
                        val (coins7, gems7) = schedule[6]
                        Box(modifier = Modifier.weight(2f)) {
                            Day7GrandPrizeCard(
                                coins = coins7,
                                gems = gems7,
                                isClaimed = isClaimed7,
                                isToday = isToday7,
                                pulseScale = if (isToday7) pulseScale else 1f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Daily Chest Box Opening Sequence
                var chestState by remember { mutableStateOf("CLOSED") } // "CLOSED", "OPENING", "OPENED"

                if (!isClaimedToday && status != null) {
                    when (chestState) {
                        "CLOSED" -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_chest_closed_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF132A58)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🧰", fontSize = 38.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Day $currentStreak Royal Chest Box",
                                        color = RoyalGoldLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Open the chest to reveal your gold coins & gems!",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            chestState = "OPENING"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("open_daily_chest_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "OPEN CHEST BOX 🔓",
                                            color = RoyalNavyDark,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        "OPENING" -> {
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(800)
                                chestState = "OPENED"
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_chest_opening_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3D7A)),
                                border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGoldLight)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("✨ 📦 ✨", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Unlocking Daily Chest...",
                                        color = RoyalGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        "OPENED" -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_chest_opened_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2E1E)),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E676))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎉 🎁 💎", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Chest Opened! Rewards Revealed:",
                                        color = Color(0xFF00E676),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+🪙 ${status.rewardCoins} Coins",
                                            color = RoyalGoldLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        if (status.rewardGems > 0) {
                                            Text(
                                                text = "+💎 ${status.rewardGems} Gems",
                                                color = SapphireBlueLight,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onClaimReward,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("claim_daily_reward_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "COLLECT REWARD ✓",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF142240))
                            .border(1.dp, Color(0xFF263C70), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today's Royal Gift Claimed! Next reward in 24 hours.",
                                color = Color.White.copy(alpha = 0.8f),
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

@Composable
private fun DayRewardCard(
    day: Int,
    coins: Long,
    gems: Int,
    isClaimed: Boolean,
    isToday: Boolean,
    pulseScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .scale(pulseScale)
            .shadow(if (isToday) 8.dp else 2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isToday -> Brush.verticalGradient(listOf(Color(0xFF264E9A), Color(0xFF122C66)))
                    isClaimed -> Brush.verticalGradient(listOf(Color(0xFF0D1B36), Color(0xFF081224)))
                    else -> Brush.verticalGradient(listOf(Color(0xFF132B5E), Color(0xFF0C1B3E)))
                }
            )
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = when {
                    isToday -> RoyalGold
                    isClaimed -> EmeraldGreen.copy(alpha = 0.5f)
                    else -> Color(0xFF234487)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Day $day",
                color = if (isToday) RoyalGoldLight else Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))

            when {
                isClaimed -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Claimed",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("CLAIMED", color = EmeraldGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                isToday -> {
                    Text("🪙", fontSize = 20.sp)
                    Text(
                        text = "+$coins",
                        color = RoyalGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (gems > 0) {
                        Text("+$gems 💎", color = Color(0xFF81D4FA), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text("+$coins", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun Day7GrandPrizeCard(
    coins: Long,
    gems: Int,
    isClaimed: Boolean,
    isToday: Boolean,
    pulseScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .scale(pulseScale)
            .shadow(if (isToday) 10.dp else 2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isToday) Brush.horizontalGradient(listOf(Color(0xFF4A3403), Color(0xFF876008)))
                else Brush.horizontalGradient(listOf(Color(0xFF1E1E2F), Color(0xFF131326)))
            )
            .border(
                width = if (isToday) 2.5.dp else 1.2.dp,
                brush = Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👑", fontSize = 28.sp)
                Text("DAY 7 GRAND", color = RoyalGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+15,000 COINS 🪙",
                    color = RoyalGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "+25 GEMS 💎",
                    color = Color(0xFF81D4FA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+Maharaja Crown",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            }
        }
    }
}
