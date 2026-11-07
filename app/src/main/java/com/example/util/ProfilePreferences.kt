package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File

class ProfilePreferences(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "mg_ludo_profile_prefs"
        private const val KEY_NICKNAME = "pref_player_nickname"
        private const val KEY_CROWN_AVATAR = "pref_crown_avatar"
        private const val KEY_ROYAL_TITLE = "pref_royal_title"
        private const val KEY_PLAYER_LEVEL = "pref_player_level"
        private const val KEY_BGM_ENABLED = "pref_bgm_enabled"
        private const val KEY_SFX_ENABLED = "pref_sfx_enabled"
        private const val KEY_HAPTICS_ENABLED = "pref_haptics_enabled"
        private const val KEY_BGM_VOLUME = "pref_bgm_volume"
        private const val KEY_SFX_VOLUME = "pref_sfx_volume"

        private const val KEY_TOTAL_WINS = "pref_total_wins"
        private const val KEY_GAMES_PLAYED = "pref_games_played"
        private const val KEY_WIN_STREAK = "pref_win_streak"
        private const val KEY_TOTAL_CAPTURES = "pref_total_captures"
        private const val KEY_PAWN_CROWN_AVATAR = "pref_pawn_crown_avatar"
        private const val KEY_BOARD_THEME = "pref_board_theme"
        private const val KEY_SFX_PRESET = "pref_sfx_preset"
        private const val KEY_USER_COINS = "pref_user_coins"
        private const val KEY_USER_GEMS = "pref_user_gems"
        private const val KEY_CUSTOM_PHOTO_URI = "pref_custom_photo_uri"
        private const val KEY_ONLINE_GAMES_PLAYED = "pref_online_games_played"
        private const val KEY_MUSIC_REPEAT_MODE = "pref_music_repeat_mode"
        private const val KEY_MUSIC_SHUFFLE_ENABLED = "pref_music_shuffle_enabled"
        private const val KEY_FAVORITE_SONG_IDS = "pref_favorite_song_ids"

        const val DEFAULT_NICKNAME = "Ishu"
        const val DEFAULT_CROWN_AVATAR = "crown_imperial_gold"
        const val DEFAULT_ROYAL_TITLE = "Maharaja"
        const val DEFAULT_SHUFFLE_ENABLED = false
        const val DEFAULT_PLAYER_LEVEL = 18
        const val DEFAULT_TOTAL_WINS = 28
        const val DEFAULT_GAMES_PLAYED = 42
        const val DEFAULT_WIN_STREAK = 4
        const val DEFAULT_TOTAL_CAPTURES = 89
        const val DEFAULT_PAWN_CROWN_AVATAR = "crown_imperial_gold"
        const val DEFAULT_BOARD_THEME = "imperial_navy"
        const val DEFAULT_SFX_PRESET = "royal_court"
        const val DEFAULT_BGM_ENABLED = true
        const val DEFAULT_SFX_ENABLED = true
        const val DEFAULT_HAPTICS_ENABLED = true
        const val DEFAULT_BGM_VOLUME = 0.80f
        const val DEFAULT_SFX_VOLUME = 0.85f
        const val DEFAULT_REPEAT_MODE = "ALL"
    }

    fun isBgmEnabled(): Boolean = if (com.example.model.BGM_PLAYLIST.isEmpty()) false else prefs.getBoolean(KEY_BGM_ENABLED, DEFAULT_BGM_ENABLED)
    fun setBgmEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BGM_ENABLED, enabled).apply()

    fun isSfxEnabled(): Boolean = prefs.getBoolean(KEY_SFX_ENABLED, DEFAULT_SFX_ENABLED)
    fun setSfxEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SFX_ENABLED, enabled).apply()

    fun isHapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS_ENABLED, DEFAULT_HAPTICS_ENABLED)
    fun setHapticsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()

    fun getBgmVolume(): Float {
        val vol = prefs.getFloat(KEY_BGM_VOLUME, DEFAULT_BGM_VOLUME)
        return if (vol <= 0.01f) DEFAULT_BGM_VOLUME else vol
    }
    fun setBgmVolume(volume: Float) = prefs.edit().putFloat(KEY_BGM_VOLUME, volume).apply()

    fun getSfxVolume(): Float = prefs.getFloat(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME)
    fun setSfxVolume(volume: Float) = prefs.edit().putFloat(KEY_SFX_VOLUME, volume).apply()

    fun getSfxPreset(): String = prefs.getString(KEY_SFX_PRESET, DEFAULT_SFX_PRESET) ?: DEFAULT_SFX_PRESET
    fun setSfxPreset(preset: String) = prefs.edit().putString(KEY_SFX_PRESET, preset).apply()

    fun getMusicRepeatMode(): String = prefs.getString(KEY_MUSIC_REPEAT_MODE, DEFAULT_REPEAT_MODE) ?: DEFAULT_REPEAT_MODE
    fun setMusicRepeatMode(mode: String) = prefs.edit().putString(KEY_MUSIC_REPEAT_MODE, mode).apply()

    fun isMusicShuffleEnabled(): Boolean = prefs.getBoolean(KEY_MUSIC_SHUFFLE_ENABLED, DEFAULT_SHUFFLE_ENABLED)
    fun setMusicShuffleEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MUSIC_SHUFFLE_ENABLED, enabled).apply()

    fun getFavoriteSongIds(): Set<String> = prefs.getStringSet(KEY_FAVORITE_SONG_IDS, emptySet()) ?: emptySet()
    fun setFavoriteSongIds(ids: Set<String>) = prefs.edit().putStringSet(KEY_FAVORITE_SONG_IDS, ids).apply()
    fun toggleSongFavorite(songId: String): Boolean {
        val current = getFavoriteSongIds().toMutableSet()
        val isNowFav = if (current.contains(songId)) {
            current.remove(songId)
            false
        } else {
            current.add(songId)
            true
        }
        setFavoriteSongIds(current)
        return isNowFav
    }

    fun getNickname(): String {
        return prefs.getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME
    }

    fun setNickname(nickname: String) {
        prefs.edit().putString(KEY_NICKNAME, nickname.trim()).apply()
    }

    fun getCrownAvatar(): String {
        return prefs.getString(KEY_CROWN_AVATAR, DEFAULT_CROWN_AVATAR) ?: DEFAULT_CROWN_AVATAR
    }

    fun setCrownAvatar(crownAvatarId: String) {
        prefs.edit().putString(KEY_CROWN_AVATAR, crownAvatarId).apply()
    }

    fun getRoyalTitle(): String {
        return prefs.getString(KEY_ROYAL_TITLE, DEFAULT_ROYAL_TITLE) ?: DEFAULT_ROYAL_TITLE
    }

    fun setRoyalTitle(title: String) {
        prefs.edit().putString(KEY_ROYAL_TITLE, title).apply()
    }

    fun getPlayerLevel(): Int {
        return prefs.getInt(KEY_PLAYER_LEVEL, DEFAULT_PLAYER_LEVEL)
    }

    fun getTotalWins(): Int = prefs.getInt(KEY_TOTAL_WINS, DEFAULT_TOTAL_WINS)
    fun setTotalWins(wins: Int) = prefs.edit().putInt(KEY_TOTAL_WINS, wins).apply()

    fun getGamesPlayed(): Int = prefs.getInt(KEY_GAMES_PLAYED, DEFAULT_GAMES_PLAYED)
    fun setGamesPlayed(games: Int) = prefs.edit().putInt(KEY_GAMES_PLAYED, games).apply()

    fun getWinStreak(): Int = prefs.getInt(KEY_WIN_STREAK, DEFAULT_WIN_STREAK)
    fun setWinStreak(streak: Int) = prefs.edit().putInt(KEY_WIN_STREAK, streak).apply()

    fun getTotalCaptures(): Int = prefs.getInt(KEY_TOTAL_CAPTURES, DEFAULT_TOTAL_CAPTURES)
    fun setTotalCaptures(captures: Int) = prefs.edit().putInt(KEY_TOTAL_CAPTURES, captures).apply()

    fun getPawnCrownAvatar(): String {
        return prefs.getString(KEY_PAWN_CROWN_AVATAR, DEFAULT_PAWN_CROWN_AVATAR) ?: DEFAULT_PAWN_CROWN_AVATAR
    }

    fun setPawnCrownAvatar(avatarId: String) {
        prefs.edit().putString(KEY_PAWN_CROWN_AVATAR, avatarId).apply()
    }

    fun getBoardTheme(): String {
        return prefs.getString(KEY_BOARD_THEME, DEFAULT_BOARD_THEME) ?: DEFAULT_BOARD_THEME
    }

    fun setBoardTheme(themeId: String) {
        prefs.edit().putString(KEY_BOARD_THEME, themeId).apply()
    }

    fun recordGameFinished(isWin: Boolean, captures: Int = 0) {
        val currentGames = getGamesPlayed() + 1
        val currentWins = if (isWin) getTotalWins() + 1 else getTotalWins()
        val currentStreak = if (isWin) getWinStreak() + 1 else 0
        val currentCaptures = getTotalCaptures() + captures

        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, currentGames)
            .putInt(KEY_TOTAL_WINS, currentWins)
            .putInt(KEY_WIN_STREAK, currentStreak)
            .putInt(KEY_TOTAL_CAPTURES, currentCaptures)
            .apply()
    }

    fun getUserCoins(): Long = prefs.getLong(KEY_USER_COINS, 25000L)
    fun setUserCoins(coins: Long) = prefs.edit().putLong(KEY_USER_COINS, coins).apply()

    fun getUserGems(): Int = prefs.getInt(KEY_USER_GEMS, 120)
    fun setUserGems(gems: Int) = prefs.edit().putInt(KEY_USER_GEMS, gems).apply()

    fun getOnlineGamesPlayed(): Int = prefs.getInt(KEY_ONLINE_GAMES_PLAYED, 0)
    fun incrementOnlineGamesPlayed(): Int {
        val next = getOnlineGamesPlayed() + 1
        prefs.edit().putInt(KEY_ONLINE_GAMES_PLAYED, next).apply()
        return next
    }

    fun getCustomPhotoUri(): String? {
        val saved = prefs.getString(KEY_CUSTOM_PHOTO_URI, null)
        val localDpFile = File(context.filesDir, "user_profile_dp.jpg")
        return when {
            localDpFile.exists() -> Uri.fromFile(localDpFile).toString()
            saved != null && !saved.startsWith("content://") -> saved
            else -> null
        }
    }

    fun setCustomPhotoUri(uri: String?) = prefs.edit().putString(KEY_CUSTOM_PHOTO_URI, uri).apply()

    fun saveProfilePhotoLocally(sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val localDpFile = File(context.filesDir, "user_profile_dp.jpg")
            localDpFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            val stableFileUri = Uri.fromFile(localDpFile).toString()
            setCustomPhotoUri(stableFileUri)
            stableFileUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveFullProfile(nickname: String, crownAvatar: String, title: String, pawnCrownAvatar: String = crownAvatar, customPhotoUri: String? = null) {
        var finalPhotoUri = customPhotoUri
        if (finalPhotoUri != null && finalPhotoUri.startsWith("content://")) {
            val localSaved = saveProfilePhotoLocally(Uri.parse(finalPhotoUri))
            if (localSaved != null) finalPhotoUri = localSaved
        } else if (finalPhotoUri == null) {
            val localDpFile = File(context.filesDir, "user_profile_dp.jpg")
            if (localDpFile.exists()) {
                finalPhotoUri = Uri.fromFile(localDpFile).toString()
            }
        }

        prefs.edit()
            .putString(KEY_NICKNAME, nickname.trim())
            .putString(KEY_CROWN_AVATAR, crownAvatar)
            .putString(KEY_ROYAL_TITLE, title)
            .putString(KEY_PAWN_CROWN_AVATAR, pawnCrownAvatar)
            .putString(KEY_CUSTOM_PHOTO_URI, finalPhotoUri)
            .apply()
    }
}
