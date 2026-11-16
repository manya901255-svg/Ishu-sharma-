package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppNotificationItem
import com.example.model.NotificationPreferences
import com.example.ui.theme.RoyalGoldBorder
import com.example.util.LudoNotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsCenterDialog(
    notifications: List<AppNotificationItem>,
    preferences: NotificationPreferences,
    onMarkRead: (String) -> Unit,
    onUpdatePreferences: (NotificationPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) }
    var hasSysPermission by remember { mutableStateOf(LudoNotificationHelper.hasNotificationPermission(context)) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSysPermission = granted
        if (granted) {
            onUpdatePreferences(preferences.copy(systemNotificationsEnabled = true))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.84f)
                .widthIn(max = 600.dp)
                .testTag("notifications_center_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, RoyalGoldBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notifications & Preferences",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("close_notifications_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                TabRow(selectedTabIndex = activeTab) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Inbox (${notifications.count { !it.isRead }})") })
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Preferences") })
                }

                Spacer(Modifier.height(10.dp))

                if (activeTab == 0) {
                    if (notifications.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(38.dp))
                                Text("You're all caught up!", fontWeight = FontWeight.Bold)
                                Text("Friend requests, club invites, match invites & task rewards appear here.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        val fmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(notifications, key = { it.notifId }) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onMarkRead(item.notifId) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (item.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.title, fontWeight = FontWeight.Bold)
                                            Text(fmt.format(Date(item.createdAtMillis)), style = MaterialTheme.typography.labelSmall)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(item.body, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasSysPermission) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Android Push Permission", fontWeight = FontWeight.Bold)
                                            Text("Allow status-bar alerts for match invites & completed tasks.", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(
                                            onClick = {
                                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Text("Enable")
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            NotifPrefRow(
                                label = "System Status-Bar Notifications",
                                checked = preferences.systemNotificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasSysPermission) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onUpdatePreferences(preferences.copy(systemNotificationsEnabled = enabled))
                                    }
                                }
                            )
                        }
                        item {
                            NotifPrefRow("Friend Requests", preferences.friendRequests) {
                                onUpdatePreferences(preferences.copy(friendRequests = it))
                            }
                        }
                        item {
                            NotifPrefRow("Club Invitations", preferences.clubInvites) {
                                onUpdatePreferences(preferences.copy(clubInvites = it))
                            }
                        }
                        item {
                            NotifPrefRow("Club Messages & Announcements", preferences.clubMessages) {
                                onUpdatePreferences(preferences.copy(clubMessages = it))
                            }
                        }
                        item {
                            NotifPrefRow("Match Invites", preferences.matchInvites) {
                                onUpdatePreferences(preferences.copy(matchInvites = it))
                            }
                        }
                        item {
                            NotifPrefRow("Task Completed Alerts", preferences.taskComplete) {
                                onUpdatePreferences(preferences.copy(taskComplete = it))
                            }
                        }
                        item {
                            NotifPrefRow("Rewards Available", preferences.rewardsAvailable) {
                                onUpdatePreferences(preferences.copy(rewardsAvailable = it))
                            }
                        }
                        item {
                            NotifPrefRow("Achievements Unlocked", preferences.achievements) {
                                onUpdatePreferences(preferences.copy(achievements = it))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifPrefRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (checked) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
