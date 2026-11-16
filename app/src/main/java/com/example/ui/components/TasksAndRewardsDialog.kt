package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RewardHistoryEntry
import com.example.model.TaskCategory
import com.example.model.TaskStatus
import com.example.model.VerifiedTaskItem
import com.example.ui.theme.*
import com.example.util.LudoPeriodKeys
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasksAndRewardsDialog(
    tasks: List<VerifiedTaskItem>,
    achievements: List<VerifiedTaskItem>,
    rewardHistory: List<RewardHistoryEntry>,
    loginStreak: Int,
    onClaimTask: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Tasks & Missions, 1 = Achievements, 2 = Reward History
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val dailyResetLabel = formatDurationMs(LudoPeriodKeys.msUntilNextDailyReset(nowMillis))
    val weeklyResetLabel = formatDurationMs(LudoPeriodKeys.msUntilNextWeeklyReset(nowMillis))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 600.dp)
                .testTag("tasks_rewards_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, RoyalGoldBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Verified Tasks & Rewards",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Daily Reset: $dailyResetLabel • Weekly Reset: $weeklyResetLabel • Streak: ${loginStreak}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("close_tasks_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(10.dp))

                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Tasks (${tasks.size})") },
                        modifier = Modifier.testTag("tasks_tab_missions")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Achievements") },
                        modifier = Modifier.testTag("tasks_tab_achievements")
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("History (${rewardHistory.size})") },
                        modifier = Modifier.testTag("tasks_tab_history")
                    )
                }

                Spacer(Modifier.height(10.dp))

                when (activeTab) {
                    0 -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All") }
                            )
                            listOf(
                                TaskCategory.DAILY,
                                TaskCategory.WEEKLY,
                                TaskCategory.LOGIN_STREAK,
                                TaskCategory.WIN_STREAK,
                                TaskCategory.CLUB,
                                TaskCategory.TOURNAMENT,
                                TaskCategory.LEVEL,
                                TaskCategory.EVENT
                            ).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        val filtered = if (selectedCategory == null) tasks else tasks.filter { it.category == selectedCategory }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.taskId }) { task ->
                                VerifiedTaskRow(task = task, onClaim = { onClaimTask(task.taskId) })
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(achievements, key = { it.taskId }) { ach ->
                                VerifiedTaskRow(task = ach, onClaim = { onClaimTask(ach.taskId) })
                            }
                        }
                    }

                    else -> {
                        if (rewardHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("No verified rewards claimed yet", fontWeight = FontWeight.SemiBold)
                                    Text("Complete tasks or achievements in real gameplay to earn and claim rewards.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(rewardHistory, key = { it.txId }) { entry ->
                                    RewardHistoryRow(entry)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerifiedTaskRow(
    task: VerifiedTaskItem,
    onClaim: () -> Unit
) {
    val statusColor = when (task.status) {
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        TaskStatus.COMPLETED -> EmeraldGreen
        TaskStatus.CLAIMED -> Color(0xFF78909C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("verified_task_card_${task.taskId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        border = BorderStroke(
            1.dp,
            if (task.canClaim) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = task.status.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("task_status_${task.taskId}")
                            )
                        }
                    }
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+🪙 ${task.rewardCoins}" + if (task.rewardCash > 0) "  +💵 ${task.rewardCash}" else "",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = RoyalGold
                    )
                    if (task.rewardXp > 0) {
                        Text(
                            text = "+${task.rewardXp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { task.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (task.canClaim || task.status == TaskStatus.CLAIMED) EmeraldGreen else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Progress: ${task.progress} / ${task.target}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onClaim,
                    enabled = task.canClaim,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("claim_task_button_${task.taskId}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.canClaim) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (task.canClaim) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    when (task.status) {
                        TaskStatus.CLAIMED -> {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Claimed", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Claimed", fontWeight = FontWeight.Bold)
                        }
                        TaskStatus.COMPLETED -> {
                            Text("Claim Reward", fontWeight = FontWeight.ExtraBold)
                        }
                        TaskStatus.IN_PROGRESS -> {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("In Progress", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardHistoryRow(entry: RewardHistoryEntry) {
    val formatter = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reward_history_row_${entry.txId}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(
                    text = "${entry.sourceType} • ${formatter.format(Date(entry.createdAtMillis))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+🪙 ${entry.coinsDelta}" + if (entry.cashDelta > 0) " +💵 ${entry.cashDelta}" else "",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
                if (entry.xpDelta > 0) {
                    Text("+${entry.xpDelta} XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hrs >= 24L) {
        val days = hrs / 24L
        "${days}d ${hrs % 24L}h"
    } else {
        String.format(Locale.US, "%02dh %02dm %02ds", hrs, mins, secs)
    }
}
