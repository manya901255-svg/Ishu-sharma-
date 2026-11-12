package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.model.DailyMission

@Composable
fun DailyMissionsDialog(
    missions: List<DailyMission>,
    onClaimMission: (DailyMission) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_missions_dialog"),
            color = Color(0xFF071738)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFB300), Color(0xFFF57F17))
                            )
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Back/Close Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF212121))
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { onDismiss() }
                            .testTag("daily_missions_close_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📜 DAILY REGAL MISSIONS",
                            color = Color(0xFF001E3C),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Complete Quests to Win Exclusive Kingdom Cosmetics",
                            color = Color(0xFF3E2723),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Summary banner of active quests
                val completedCount = missions.count { it.isCompleted }
                val claimedCount = missions.count { it.isClaimed }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102852)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quests Status: $claimedCount / ${missions.size} Claimed",
                                color = Color(0xFFFFD54F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Resets daily at 00:00 AM • Rewards include Ancient King Avatars!",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF6F00))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "👑 ROYAL REWARDS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // List of Daily Missions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(missions, key = { it.id }) { mission ->
                        MissionCard(
                            mission = mission,
                            onClaim = { onClaimMission(mission) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: DailyMission,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isClaimed) Color(0xFF16253D) else Color(0xFF0F3266)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (mission.isCompleted && !mission.isClaimed) 2.dp else 1.dp,
            color = if (mission.isCompleted && !mission.isClaimed) Color(0xFF00E676) else Color(0xFF29B6F6).copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mission Icon Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF071A38))
                        .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mission.icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mission.description,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar and Fraction
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { mission.progressFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (mission.isCompleted) Color(0xFF00E676) else Color(0xFFFFB300),
                    trackColor = Color(0xFF081830)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "${mission.currentProgress} / ${mission.targetProgress}",
                    color = if (mission.isCompleted) Color(0xFF00E676) else Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rewards Row & Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reward badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mission.rewardCoins > 0) {
                        val coinsText = if (mission.rewardCoins >= 1000L && mission.rewardCoins % 1000L == 0L) {
                            "🪙 ${mission.rewardCoins / 1000L}K"
                        } else {
                            "🪙 ${mission.rewardCoins}"
                        }
                        RewardPill(coinsText, Color(0xFFFFD54F))
                    }
                    if (mission.rewardGems > 0) {
                        RewardPill("💎 ${mission.rewardGems}", Color(0xFF00E5FF))
                    }
                    if (mission.rewardCosmeticName != null) {
                        RewardPill("👑 ${mission.rewardCosmeticName}", Color(0xFFE040FB))
                    }
                }

                // Claim Button with clear verification status
                Button(
                    onClick = onClaim,
                    enabled = mission.isCompleted && !mission.isClaimed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853),
                        disabledContainerColor = if (mission.isClaimed) Color(0xFF37474F) else Color(0xFF1E3A5F)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = when {
                            mission.isClaimed -> "Claimed ✓"
                            mission.isCompleted -> "CLAIM REWARD 🎁"
                            else -> "In Progress (${mission.currentProgress}/${mission.targetProgress})"
                        },
                        color = if (mission.isClaimed) Color.White.copy(alpha = 0.6f) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardPill(text: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF071A38))
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = accentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
