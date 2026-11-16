package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.model.NotificationCategory
import com.example.model.NotificationPreferences

/**
 * Dispatches local Android status-bar notifications for verified game & social events,
 * strictly honoring user category toggles and Android 13+ POST_NOTIFICATIONS runtime permission.
 */
object LudoNotificationHelper {

    private const val CHANNEL_SOCIAL = "mgludo_social"
    private const val CHANNEL_REWARDS = "mgludo_rewards"
    private const val CHANNEL_CLUBS = "mgludo_clubs"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channels = listOf(
                NotificationChannel(CHANNEL_SOCIAL, "Friends & Match Invites", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_REWARDS, "Tasks, Rewards & Achievements", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_CLUBS, "Voice Clubs & Announcements", NotificationManager.IMPORTANCE_DEFAULT)
            )
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isCategoryEnabled(category: NotificationCategory, prefs: NotificationPreferences): Boolean {
        if (!prefs.systemNotificationsEnabled) return false
        return when (category) {
            NotificationCategory.FRIEND_REQUEST -> prefs.friendRequests
            NotificationCategory.CLUB_INVITE -> prefs.clubInvites
            NotificationCategory.CLUB_MESSAGE -> prefs.clubMessages
            NotificationCategory.MATCH_INVITE -> prefs.matchInvites
            NotificationCategory.TASK_COMPLETE -> prefs.taskComplete
            NotificationCategory.REWARD_AVAILABLE -> prefs.rewardsAvailable
            NotificationCategory.ACHIEVEMENT -> prefs.achievements
        }
    }

    fun postNotificationIfPermitted(
        context: Context,
        id: Int,
        category: NotificationCategory,
        title: String,
        body: String,
        prefs: NotificationPreferences
    ) {
        if (!isCategoryEnabled(category, prefs)) return
        if (!hasNotificationPermission(context)) return

        ensureChannels(context)
        val channelId = when (category) {
            NotificationCategory.FRIEND_REQUEST, NotificationCategory.MATCH_INVITE -> CHANNEL_SOCIAL
            NotificationCategory.CLUB_INVITE, NotificationCategory.CLUB_MESSAGE -> CHANNEL_CLUBS
            NotificationCategory.TASK_COMPLETE, NotificationCategory.REWARD_AVAILABLE, NotificationCategory.ACHIEVEMENT -> CHANNEL_REWARDS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        } catch (_: Exception) {
            // Unavailable on headless test environment
        }
    }
}
