package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mgLudoDataStore: DataStore<Preferences> by preferencesDataStore(name = "mgludo_user_prefs")

class ThemeAndNotificationPreferences(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIF_FRIEND_REQUEST = booleanPreferencesKey("notif_friend_request")
        val NOTIF_CLUB_INVITE = booleanPreferencesKey("notif_club_invite")
        val NOTIF_CLUB_MESSAGE = booleanPreferencesKey("notif_club_message")
        val NOTIF_MATCH_INVITE = booleanPreferencesKey("notif_match_invite")
        val NOTIF_TASK_COMPLETE = booleanPreferencesKey("notif_task_complete")
        val NOTIF_REWARD_AVAILABLE = booleanPreferencesKey("notif_reward_available")
        val NOTIF_ACHIEVEMENT = booleanPreferencesKey("notif_achievement")
        val NOTIF_ANDROID_PUSH = booleanPreferencesKey("notif_android_push")
    }

    val isDarkThemeFlow: Flow<Boolean> = context.mgLudoDataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: true
    }

    val notificationPrefsFlow: Flow<NotificationPreferences> = context.mgLudoDataStore.data.map { prefs ->
        NotificationPreferences(
            friendRequests = prefs[Keys.NOTIF_FRIEND_REQUEST] ?: true,
            clubInvites = prefs[Keys.NOTIF_CLUB_INVITE] ?: true,
            clubMessages = prefs[Keys.NOTIF_CLUB_MESSAGE] ?: true,
            matchInvites = prefs[Keys.NOTIF_MATCH_INVITE] ?: true,
            taskComplete = prefs[Keys.NOTIF_TASK_COMPLETE] ?: true,
            rewardsAvailable = prefs[Keys.NOTIF_REWARD_AVAILABLE] ?: true,
            achievements = prefs[Keys.NOTIF_ACHIEVEMENT] ?: true,
            systemNotificationsEnabled = prefs[Keys.NOTIF_ANDROID_PUSH] ?: true
        )
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.mgLudoDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = isDark
        }
    }

    suspend fun updateNotificationPrefs(updated: NotificationPreferences) {
        context.mgLudoDataStore.edit { prefs ->
            prefs[Keys.NOTIF_FRIEND_REQUEST] = updated.friendRequests
            prefs[Keys.NOTIF_CLUB_INVITE] = updated.clubInvites
            prefs[Keys.NOTIF_CLUB_MESSAGE] = updated.clubMessages
            prefs[Keys.NOTIF_MATCH_INVITE] = updated.matchInvites
            prefs[Keys.NOTIF_TASK_COMPLETE] = updated.taskComplete
            prefs[Keys.NOTIF_REWARD_AVAILABLE] = updated.rewardsAvailable
            prefs[Keys.NOTIF_ACHIEVEMENT] = updated.achievements
            prefs[Keys.NOTIF_ANDROID_PUSH] = updated.systemNotificationsEnabled
        }
    }
}
