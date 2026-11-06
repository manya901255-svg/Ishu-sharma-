package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R
import com.example.model.BGM_PLAYLIST
import com.example.model.BgmTrack
import kotlin.random.Random

/**
 * RoyalSoundManager: Coordinates ambient background music playlist and sound effects
 * for dice rolling, pawn steps, captures, and royal court celebrations.
 * All songs in the BGM playlist play continuously one after the other.
 */
class RoyalSoundManager(private val context: Context) {

    private val profilePreferences = ProfilePreferences(context)

    // Background music sequential playlist & sound effects
    var isBgmEnabled: Boolean = profilePreferences.isBgmEnabled()
        private set
    var isSfxEnabled: Boolean = profilePreferences.isSfxEnabled()
        private set
    var isHapticsEnabled: Boolean = profilePreferences.isHapticsEnabled()
        private set
    var bgmVolume: Float = profilePreferences.getBgmVolume()
        private set
    var sfxVolume: Float = profilePreferences.getSfxVolume()
        private set
    var sfxPreset: String = profilePreferences.getSfxPreset()
        private set

    // SoundPool for low-latency SFX
    private var soundPool: SoundPool? = null
    private var diceRollSoundId: Int = 0
    private var pawnMoveSoundId: Int = 0
    private var pawnCaptureSoundId: Int = 0
    private var victoryFanfareSoundId: Int = 0
    private var buttonClickSoundId: Int = 0
    private var starSafeSoundId: Int = 0
    private var sixRolledSoundId: Int = 0
    private var isSoundPoolReady = false

    // MediaPlayer for sequential Royal Kingdom background music playlist
    private var bgmPlayer: MediaPlayer? = null
    private var isBgmPrepared = false

    // Current playing track index in BGM_PLAYLIST
    var currentTrackIndex: Int = -1
        private set

    val playlist: List<BgmTrack> get() = BGM_PLAYLIST

    fun getCurrentTrack(): BgmTrack? = BGM_PLAYLIST.getOrNull(currentTrackIndex)

    private var onTrackChangeListener: ((BgmTrack?, Int) -> Unit)? = null

    fun setOnTrackChangeListener(listener: (BgmTrack?, Int) -> Unit) {
        onTrackChangeListener = listener
        listener(getCurrentTrack(), currentTrackIndex)
    }

    // Vibrator for tactile haptic feedback
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    init {
        initSoundPool()
        if (isBgmEnabled && BGM_PLAYLIST.isNotEmpty()) {
            try {
                initBgmPlayerForTrack(0)
            } catch (e: Exception) {
                Log.e("RoyalSoundManager", "Error initializing first BGM track", e)
            }
        }
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) {
                            isSoundPoolReady = true
                        }
                    }
                }

            soundPool?.let { pool ->
                diceRollSoundId = pool.load(context, R.raw.dice_roll, 1)
                pawnMoveSoundId = pool.load(context, R.raw.pawn_move, 1)
                pawnCaptureSoundId = pool.load(context, R.raw.pawn_capture, 1)
                victoryFanfareSoundId = pool.load(context, R.raw.victory_fanfare, 1)
                buttonClickSoundId = pool.load(context, R.raw.button_click, 1)
                starSafeSoundId = pool.load(context, R.raw.star_safe, 1)
                sixRolledSoundId = pool.load(context, R.raw.six_rolled, 1)
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error initializing SoundPool", e)
        }
    }

    private var consecutivePlaybackErrors: Int = 0

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
    private var hasAudioFocus = false
    private var wasPlayingBeforeFocusLoss = false

    private val audioFocusChangeListener = android.media.AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            android.media.AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                wasPlayingBeforeFocusLoss = false
                pauseAmbientBgm()
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                wasPlayingBeforeFocusLoss = bgmPlayer?.isPlaying == true
                hasAudioFocus = false
                pauseAmbientBgm()
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                try {
                    val ducked = (bgmVolume * 0.25f).coerceIn(0f, 1f)
                    bgmPlayer?.setVolume(ducked, ducked)
                } catch (_: Exception) {}
            }
            android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                try {
                    val vol = if (isBgmEnabled) bgmVolume else 0f
                    bgmPlayer?.setVolume(vol, vol)
                    if (wasPlayingBeforeFocusLoss && isBgmEnabled) {
                        bgmPlayer?.start()
                        wasPlayingBeforeFocusLoss = false
                        onPlaybackStateListener?.invoke(true)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        @Suppress("DEPRECATION")
        val result = audioManager?.requestAudioFocus(
            audioFocusChangeListener,
            android.media.AudioManager.STREAM_MUSIC,
            android.media.AudioManager.AUDIOFOCUS_GAIN
        ) ?: android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED
        hasAudioFocus = result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (hasAudioFocus) {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(audioFocusChangeListener)
            hasAudioFocus = false
        }
    }

    var repeatMode: com.example.model.MusicRepeatMode = try {
        com.example.model.MusicRepeatMode.valueOf(profilePreferences.getMusicRepeatMode())
    } catch (_: Exception) {
        com.example.model.MusicRepeatMode.ALL
    }
        private set

    fun cycleRepeatMode(): com.example.model.MusicRepeatMode {
        val next = when (repeatMode) {
            com.example.model.MusicRepeatMode.ALL -> com.example.model.MusicRepeatMode.ONE
            com.example.model.MusicRepeatMode.ONE -> com.example.model.MusicRepeatMode.OFF
            com.example.model.MusicRepeatMode.OFF -> com.example.model.MusicRepeatMode.ALL
        }
        repeatMode = next
        profilePreferences.setMusicRepeatMode(next.name)
        return next
    }

    fun isSongFavorite(id: String): Boolean = profilePreferences.getFavoriteSongIds().contains(id)

    fun toggleSongFavorite(id: String): Boolean = profilePreferences.toggleSongFavorite(id)

    private var onPlaybackStateListener: ((Boolean) -> Unit)? = null

    fun setOnPlaybackStateListener(listener: (Boolean) -> Unit) {
        onPlaybackStateListener = listener
        listener(isPlaying())
    }

    fun isPlaying(): Boolean = bgmPlayer?.isPlaying == true

    /**
     * Initializes and prepares the MediaPlayer for a specific BGM track from assets.
     */
    private fun initBgmPlayerForTrack(index: Int) {
        if (BGM_PLAYLIST.isEmpty()) {
            currentTrackIndex = -1
            onPlaybackStateListener?.invoke(false)
            onTrackChangeListener?.invoke(null, -1)
            return
        }
        val safeIndex = index.coerceIn(0, BGM_PLAYLIST.lastIndex)
        val track = BGM_PLAYLIST[safeIndex]
        currentTrackIndex = safeIndex

        try {
            bgmPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
            }
            if (bgmPlayer == null) {
                bgmPlayer = MediaPlayer().apply {
                    setOnCompletionListener {
                        handleTrackCompletion()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("RoyalSoundManager", "MediaPlayer error: what=$what, extra=$extra")
                        isBgmPrepared = false
                        onPlaybackStateListener?.invoke(false)
                        true
                    }
                }
            }

            var prepared = false
            if (track.assetFileName.isNotBlank()) {
                // Strategy 1: Direct AssetFileDescriptor
                try {
                    val afd = context.assets.openFd("bgm/${track.assetFileName}")
                    bgmPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    bgmPlayer?.prepare()
                    prepared = true
                } catch (e: Exception) {
                    Log.w("RoyalSoundManager", "Direct assetFd failed for ${track.title}, falling back to cache extraction: ${e.message}")
                }

                // Strategy 2: If direct fd failed, stream asset into cache file and play
                if (!prepared) {
                    try {
                        bgmPlayer?.reset()
                        val cacheFile = java.io.File(context.cacheDir, "bgm_${track.assetFileName}")
                        if (!cacheFile.exists() || cacheFile.length() < 1024) {
                            context.assets.open("bgm/${track.assetFileName}").use { input ->
                                cacheFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        if (cacheFile.exists() && cacheFile.length() > 0) {
                            bgmPlayer?.setDataSource(cacheFile.absolutePath)
                            bgmPlayer?.prepare()
                            prepared = true
                        }
                    } catch (e2: Exception) {
                        Log.e("RoyalSoundManager", "Cached file playback failed for ${track.title}", e2)
                    }
                }
            }

            if (!prepared && track.rawResId != 0) {
                try {
                    bgmPlayer?.reset()
                    val afd = context.resources.openRawResourceFd(track.rawResId)
                    bgmPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    bgmPlayer?.prepare()
                    prepared = true
                } catch (e: Exception) {
                    Log.e("RoyalSoundManager", "Failed to load raw res for track ${track.title}", e)
                }
            }

            if (prepared) {
                isBgmPrepared = true
                val effectiveVol = if (bgmVolume > 0.05f) bgmVolume else 0.80f
                bgmVolume = effectiveVol
                bgmPlayer?.setVolume(effectiveVol, effectiveVol)

                if (isBgmEnabled) {
                    requestAudioFocus()
                    bgmPlayer?.start()
                    onPlaybackStateListener?.invoke(true)
                } else {
                    onPlaybackStateListener?.invoke(false)
                }
            } else {
                isBgmPrepared = false
                onPlaybackStateListener?.invoke(false)
            }
            onTrackChangeListener?.invoke(track, safeIndex)
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error initializing track $safeIndex: ${track.title}", e)
            isBgmPrepared = false
            onPlaybackStateListener?.invoke(false)
            onTrackChangeListener?.invoke(track, safeIndex)
        }
    }

    private fun handleTrackCompletion() {
        if (BGM_PLAYLIST.isEmpty()) return
        when (repeatMode) {
            com.example.model.MusicRepeatMode.ONE -> {
                try {
                    bgmPlayer?.seekTo(0)
                    bgmPlayer?.start()
                    onPlaybackStateListener?.invoke(true)
                } catch (e: Exception) {
                    initBgmPlayerForTrack(currentTrackIndex)
                }
            }
            com.example.model.MusicRepeatMode.ALL -> {
                playNextTrack()
            }
            com.example.model.MusicRepeatMode.OFF -> {
                onPlaybackStateListener?.invoke(false)
            }
        }
    }

    // --- Sequential Ambient Background Music Control ---

    fun playNextTrack() {
        if (BGM_PLAYLIST.isEmpty()) return
        val nextIdx = (currentTrackIndex + 1) % BGM_PLAYLIST.size
        initBgmPlayerForTrack(nextIdx)
    }

    fun playPreviousTrack() {
        if (BGM_PLAYLIST.isEmpty()) return
        val prevIdx = if (currentTrackIndex - 1 < 0) BGM_PLAYLIST.lastIndex else currentTrackIndex - 1
        initBgmPlayerForTrack(prevIdx)
    }

    fun selectTrack(index: Int) {
        if (index in BGM_PLAYLIST.indices) {
            isBgmEnabled = true
            profilePreferences.setBgmEnabled(true)
            if (bgmVolume <= 0.05f) {
                setBgmVolume(0.80f)
            }
            initBgmPlayerForTrack(index)
        }
    }

    fun togglePlayPause(): Boolean {
        if (BGM_PLAYLIST.isEmpty()) {
            stopAmbientBgm()
            return false
        }
        val playing = bgmPlayer?.isPlaying == true
        if (playing) {
            pauseAmbientBgm()
            return false
        } else {
            if (!isBgmEnabled) {
                setBgmEnabled(true)
            } else {
                startAmbientBgm()
            }
            return true
        }
    }

    fun startAmbientBgm() {
        if (BGM_PLAYLIST.isEmpty()) {
            stopAmbientBgm()
            return
        }
        try {
            if (currentTrackIndex < 0 || !isBgmPrepared || bgmPlayer == null) {
                initBgmPlayerForTrack(if (currentTrackIndex >= 0) currentTrackIndex else 0)
            } else {
                requestAudioFocus()
                bgmPlayer?.setVolume(bgmVolume, bgmVolume)
                if (bgmPlayer?.isPlaying == false) {
                    bgmPlayer?.start()
                }
                onPlaybackStateListener?.invoke(true)
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error starting BGM", e)
            initBgmPlayerForTrack(0)
        }
    }

    fun pauseAmbientBgm() {
        try {
            if (bgmPlayer?.isPlaying == true) {
                bgmPlayer?.pause()
                onPlaybackStateListener?.invoke(false)
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error pausing BGM", e)
        }
    }

    fun resumeAmbientBgm() {
        if (BGM_PLAYLIST.isEmpty()) {
            stopAmbientBgm()
            return
        }
        try {
            if (isBgmEnabled && bgmPlayer?.isPlaying == false && isBgmPrepared) {
                requestAudioFocus()
                bgmPlayer?.start()
                onPlaybackStateListener?.invoke(true)
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error resuming BGM", e)
        }
    }

    fun stopAmbientBgm() {
        try {
            abandonAudioFocus()
            if (bgmPlayer?.isPlaying == true) {
                bgmPlayer?.stop()
                isBgmPrepared = false
                onPlaybackStateListener?.invoke(false)
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error stopping BGM", e)
        }
    }

    fun setBgmEnabled(enabled: Boolean) {
        isBgmEnabled = enabled
        profilePreferences.setBgmEnabled(enabled)
        try {
            if (enabled) {
                requestAudioFocus()
                bgmPlayer?.setVolume(bgmVolume, bgmVolume)
                if (bgmPlayer?.isPlaying == false) {
                    bgmPlayer?.start()
                    onPlaybackStateListener?.invoke(true)
                }
            } else {
                pauseAmbientBgm()
                abandonAudioFocus()
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error toggling BGM", e)
        }
    }

    fun setBgmVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        bgmVolume = clamped
        profilePreferences.setBgmVolume(clamped)
        try {
            if (isBgmEnabled) {
                bgmPlayer?.setVolume(clamped, clamped)
            }
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error setting BGM volume", e)
        }
    }

    // --- Sound Effects Control ---

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
        profilePreferences.setSfxEnabled(enabled)
    }

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        sfxVolume = clamped
        profilePreferences.setSfxVolume(clamped)
    }

    fun setSfxPreset(preset: String) {
        sfxPreset = preset
        profilePreferences.setSfxPreset(preset)
    }

    private fun getPresetPitchMultiplier(): Float {
        return when (sfxPreset) {
            "classic_ludo" -> 1.15f
            "arcade_punch" -> 1.30f
            else -> 1.0f // royal_court
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        isHapticsEnabled = enabled
        profilePreferences.setHapticsEnabled(enabled)
    }

    var isGameActive: Boolean = true
        private set

    fun setGameActive(active: Boolean) {
        isGameActive = active
    }

    /**
     * Theme-Appropriate Sound Engine:
     * Plays the authentic acoustic sound of marble dice rolling on velvet
     */
    fun playDiceRoll() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibrateDiceRoll()
        }
        if (!isSfxEnabled) return
        try {
            val baseRate = getPresetPitchMultiplier()
            // Velvet dampening with multi-stage marble tumbling acoustic rate
            val rate = (Random.nextFloat() * 0.10f + 0.95f) * baseRate
            soundPool?.play(diceRollSoundId, sfxVolume, sfxVolume, 1, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing marble dice roll sound", e)
        }
    }

    /**
     * Plays the crisp wooden/stone pawn movement step
     */
    fun playPawnMove() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibrateClick()
        }
        if (!isSfxEnabled) return
        try {
            val baseRate = getPresetPitchMultiplier()
            val rate = (Random.nextFloat() * 0.06f + 0.97f) * baseRate
            soundPool?.play(pawnMoveSoundId, sfxVolume, sfxVolume, 1, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing pawn move sound", e)
        }
    }

    /**
     * Theme-Appropriate Sound Engine:
     * Plays dramatic pawn strike impact combined with a triumphant royal fanfare when capturing an opponent
     */
    fun playPawnCapture() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibratePawnCapture()
        }
        if (!isSfxEnabled) return
        try {
            val rate = getPresetPitchMultiplier()
            // Battle strike impact
            soundPool?.play(pawnCaptureSoundId, sfxVolume, sfxVolume, 2, 0, rate.coerceIn(0.5f, 2.0f))
            // Triumphant fanfare flourish celebration for capture
            soundPool?.play(victoryFanfareSoundId, sfxVolume * 0.9f, sfxVolume * 0.9f, 2, 0, 1.2f)
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing triumphant pawn capture fanfare", e)
        }
    }

    /**
     * Plays the triumphant royal fanfare celebration sound
     */
    fun playVictoryFanfare() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibrateVictory()
        }
        if (!isSfxEnabled) return
        try {
            soundPool?.play(victoryFanfareSoundId, sfxVolume, sfxVolume, 2, 0, 1.0f)
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing victory fanfare", e)
        }
    }

    /**
     * Turn alert chime for when it becomes the player's turn
     */
    fun playTurnAlert() {
        if (!isGameActive || !isSfxEnabled) return
        try {
            soundPool?.play(buttonClickSoundId, sfxVolume * 0.7f, sfxVolume * 0.7f, 1, 0, 1.6f)
        } catch (_: Exception) {}
    }

    /**
     * Plays the regal jewel/coin button click sound
     */
    fun playButtonClick() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibrateClick()
        }
        if (!isSfxEnabled) return
        try {
            val rate = getPresetPitchMultiplier()
            soundPool?.play(buttonClickSoundId, sfxVolume * 0.8f, sfxVolume * 0.8f, 1, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing button click", e)
        }
    }

    /**
     * Plays celestial star spot landing chime
     */
    fun playStarSafe() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibrateStarSafe()
        }
        if (!isSfxEnabled) return
        try {
            val rate = getPresetPitchMultiplier()
            soundPool?.play(starSafeSoundId, sfxVolume * 0.9f, sfxVolume * 0.9f, 2, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing star safe sound", e)
        }
    }

    /**
     * Plays triumphant magical sparkle flourish when player rolls a 6
     */
    fun playSixRolled() {
        if (!isGameActive) return
        if (isHapticsEnabled) {
            vibrateSixRolled()
        }
        if (!isSfxEnabled) return
        try {
            val rate = getPresetPitchMultiplier()
            soundPool?.play(sixRolledSoundId, sfxVolume, sfxVolume, 2, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error playing six rolled sound", e)
        }
    }

    // --- Haptic Vibrations ---

    fun vibrateDiceRoll() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 30, 40, 40, 50, 60)
                val amplitudes = intArrayOf(0, 100, 0, 150, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    fun vibratePawnCapture() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 50, 120)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (_: Exception) {}
    }

    fun vibrateVictory() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 40, 60, 40, 120, 80, 200)
                val amplitudes = intArrayOf(0, 180, 0, 180, 0, 220, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }

    fun vibrateClick() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun vibrateStarSafe() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 30, 60)
                val amplitudes = intArrayOf(0, 100, 0, 160)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    fun vibrateSixRolled() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 40, 50, 40, 100)
                val amplitudes = intArrayOf(0, 140, 0, 180, 0, 240)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(180)
            }
        } catch (_: Exception) {}
    }

    /**
     * Clean release of audio resources
     */
    fun release() {
        try {
            bgmPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            bgmPlayer = null
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            Log.e("RoyalSoundManager", "Error releasing audio resources", e)
        }
    }
}
