package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.TaskStatus
import com.example.model.VerifiedTaskItem
import com.example.ui.theme.*
import com.example.util.LudoPeriodKeys
import kotlinx.coroutines.delay
import java.util.Locale

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

/**
 * RoyalChallengesDialog displays verified daily/weekly gameplay challenges backed by
 * TaskRewardRepository. Progress advances ONLY from actual gameplay / social events;
 * tapping progress bars or action buttons never fabricates completions.
 */
@Composable
fun RoyalChallengesDialog(
    userCoins: Long = 0L,
    userCash: Int = 0,
    verifiedTasks: List<VerifiedTaskItem> = emptyList(),
    onClaimVerifiedTask: (String) -> Unit = {},
    onClaimChallenge: (Long, Int) -> Unit = { _, _ -> },
    onPlayChallenge: () -> Unit,
    onDismiss: () -> Unit
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val dailyResetStr = remember(nowMillis) {
        val secs = (LudoPeriodKeys.msUntilNextDailyReset(nowMillis) / 1000L).coerceAtLeast(0L)
        String.format(Locale.US, "%02dh %02dm %02ds", secs / 3600L, (secs % 3600L) / 60L, secs % 60L)
    }
    val weeklyResetStr = remember(nowMillis) {
        val hrs = (LudoPeriodKeys.msUntilNextWeeklyReset(nowMillis) / 3_600_000L).coerceAtLeast(0L)
        "${hrs / 24L}d ${hrs % 24L}h"
    }

    val completedOrClaimedCount = verifiedTasks.count { it.status != TaskStatus.IN_PROGRESS }
    val totalCount = verifiedTasks.size.coerceAtLeast(1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_challenges_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1F47B8), Color(0xFF10255E), RoyalNavyDark)
                        )
                    )
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Back Button, Live Coins & Live Cash
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(Color(0xFFFFC107))
                            .testTag("daily_challenges_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF212121)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🪙 ${formatCompactCoins(userCoins)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💵 $userCash",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Header Ribbon
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFEF5350), Color(0xFFD32F2F))))
                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Verified Challenges",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Earn rewards by completing real Ludo matches & gameplay events",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .weight(1f)
                        .padding(bottom = 12.dp)
                        .shadow(14.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progress: $completedOrClaimedCount / ${verifiedTasks.size}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Weekly Reset: 🕒 $weeklyResetStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (completedOrClaimedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = EmeraldGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (verifiedTasks.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Play a match to begin tracking verified daily tasks.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(verifiedTasks, key = { it.taskId }) { task ->
                                    VerifiedChallengeCardRow(
                                        task = task,
                                        onPlay = onPlayChallenge,
                                        onClaim = { onClaimVerifiedTask(task.taskId) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF381559)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📋 Daily reset in 🕒 $dailyResetStr (UTC)",
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

@Composable
private fun VerifiedChallengeCardRow(
    task: VerifiedTaskItem,
    onPlay: () -> Unit,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("challenge_card_${task.taskId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = BorderStroke(1.2.dp, if (task.canClaim) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+🪙 ${task.rewardCoins}" + if (task.rewardCash > 0) " +💵 ${task.rewardCash}" else "",
                        color = RoyalGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = task.status.label,
                        color = when (task.status) {
                            TaskStatus.COMPLETED -> EmeraldGreen
                            TaskStatus.CLAIMED -> Color.Gray
                            TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { task.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (task.canClaim || task.status == TaskStatus.CLAIMED) EmeraldGreen else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${task.progress} / ${task.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                when (task.status) {
                    TaskStatus.COMPLETED -> {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("claim_challenge_button_${task.taskId}")
                        ) {
                            Text("Claim", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                    TaskStatus.CLAIMED -> {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Claimed", fontSize = 11.sp)
                        }
                    }
                    TaskStatus.IN_PROGRESS -> {
                        Button(
                            onClick = onPlay,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0091EA)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("play_challenge_button_${task.taskId}")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PLAY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

