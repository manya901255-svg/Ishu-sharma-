package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * SoundEffectManager: High-performance, low-latency sound effects manager in Kotlin
 * designed specifically for Ludo board game actions:
 *  - Rolling the dice (acoustic marble tumble & rumble)
 *  - Moving pawns (tactile wooden/stone stepping clicks with ascending pitch feedback)
 *  - Landing on safe tiles (celestial, shimmering harmonic bell chime for stars and safe zones)
 *  - Capturing opponent pawns (battle strike impact + celebration flourish)
 *  - Rolling a six (magical sparkle flourish)
 *  - Reaching home (royal palace fanfare)
 *  - Match victory (grand orchestral celebration)
 *  - Turn notifications & UI button clicks
 *
 * Employs dual-engine playback:
 * 1. SoundPool for instant hardware-accelerated sound sample playback
 * 2. Real-time Procedural Audio Synthesis via AudioTrack as a zero-dependency,
 *    ultra-reliable acoustic generator that guarantees rich sound effects even if
 *    sample files are unavailable or sound pool streams are occupied.
 */
class SoundEffectManager(private val context: Context) {

    companion object {
        private const val TAG = "SoundEffectManager"
        private const val SAMPLE_RATE = 44100

        const val PRESET_ROYAL = "royal_court"
        const val PRESET_CLASSIC = "classic_ludo"
        const val PRESET_ARCADE = "arcade_punch"
    }

    private val profilePreferences = ProfilePreferences(context)

    // User settings
    var isEnabled: Boolean = profilePreferences.isSfxEnabled()
        private set
    var volume: Float = profilePreferences.getSfxVolume().coerceIn(0f, 1f)
        private set
    var preset: String = profilePreferences.getSfxPreset()
        private set
    var isHapticsEnabled: Boolean = profilePreferences.isHapticsEnabled()
        private set
    var isGameActive: Boolean = true

    // SoundPool resources
    private var soundPool: SoundPool? = null
    private var diceRollSoundId: Int = 0
    private var pawnMoveSoundId: Int = 0
    private var starSafeSoundId: Int = 0
    private var pawnCaptureSoundId: Int = 0
    private var sixRolledSoundId: Int = 0
    private var victoryFanfareSoundId: Int = 0
    private var buttonClickSoundId: Int = 0
    private var isSoundPoolReady = false

    // Single background thread executor for procedural audio synthesis
    private val audioExecutor = Executors.newSingleThreadExecutor()

    // Tactile vibrator integration
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrator service unavailable: ${e.message}")
            null
        }
    }

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) {
                            isSoundPoolReady = true
                        }
                    }
                }

            soundPool?.let { pool ->
                diceRollSoundId = loadRawResourceSafely(pool, R.raw.dice_roll)
                pawnMoveSoundId = loadRawResourceSafely(pool, R.raw.pawn_move)
                starSafeSoundId = loadRawResourceSafely(pool, R.raw.star_safe)
                pawnCaptureSoundId = loadRawResourceSafely(pool, R.raw.pawn_capture)
                sixRolledSoundId = loadRawResourceSafely(pool, R.raw.six_rolled)
                victoryFanfareSoundId = loadRawResourceSafely(pool, R.raw.victory_fanfare)
                buttonClickSoundId = loadRawResourceSafely(pool, R.raw.button_click)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool, fallback to procedural synthesis", e)
        }
    }

    private fun loadRawResourceSafely(pool: SoundPool, resId: Int): Int {
        return try {
            pool.load(context, resId, 1)
        } catch (e: Exception) {
            Log.w(TAG, "Could not load raw audio resource: ${e.message}")
            0
        }
    }

    // ==========================================
    // Core Game Actions
    // ==========================================

    /**
     * 1. ROLLING THE DICE
     * Simulates the physical rolling, tumbling, and rattling of marble/wood dice
     * on the velvet-lined Ludo board.
     * Features subtle randomized pitch variation (0.94x - 1.08x) for authentic acoustics.
     */
    fun playDiceRoll() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateDiceRollPattern()
        }

        if (!isEnabled || volume <= 0.01f) return

        val basePitch = getPresetPitchMultiplier()
        val randomPitch = (Random.nextFloat() * 0.12f + 0.94f) * basePitch
        val clampedPitch = randomPitch.coerceIn(0.6f, 1.8f)

        var playedInPool = false
        if (isSoundPoolReady && diceRollSoundId != 0) {
            try {
                val streamId = soundPool?.play(
                    diceRollSoundId,
                    volume,
                    volume,
                    1,
                    0,
                    clampedPitch
                ) ?: 0
                playedInPool = streamId > 0
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool dice roll failed: ${e.message}")
            }
        }

        // Procedural synthesis fallback if sample wasn't played
        if (!playedInPool) {
            playProceduralSound { ProceduralAudioSynthesizer.generateDiceRollPcm(volume) }
        }
    }

    /**
     * 2. MOVING PAWNS
     * Plays a crisp, satisfying wooden/stone pawn tap on the board square.
     * Supports pitch scaling based on movement progress (e.g. stepIndex 1 to 6)
     * so players get clear ascending acoustic feedback as the pawn hops toward home.
     *
     * @param stepIndex Current step in the sequence (1-indexed)
     * @param totalSteps Total number of steps in this turn (e.g., dice roll value)
     * @param isHomeStretch True if the pawn is moving in its safe colored home path
     */
    fun playPawnMove(stepIndex: Int = 1, totalSteps: Int = 1, isHomeStretch: Boolean = false) {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibratePawnStep()
        }

        if (!isEnabled || volume <= 0.01f) return

        // Ascending pitch progression per step: gives a lively, interactive bounce feel
        val stepMultiplier = if (totalSteps > 1) {
            1.0f + ((stepIndex.coerceIn(1, 6) - 1) * 0.045f)
        } else {
            1.0f
        }
        val stretchBonus = if (isHomeStretch) 1.12f else 1.0f
        val pitch = (getPresetPitchMultiplier() * stepMultiplier * stretchBonus).coerceIn(0.7f, 1.9f)

        var playedInPool = false
        if (isSoundPoolReady && pawnMoveSoundId != 0) {
            try {
                val streamId = soundPool?.play(
                    pawnMoveSoundId,
                    volume,
                    volume,
                    1,
                    0,
                    pitch
                ) ?: 0
                playedInPool = streamId > 0
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool pawn move failed: ${e.message}")
            }
        }

        if (!playedInPool) {
            val baseFreq = if (isHomeStretch) 660f else (520f + (stepIndex * 35f))
            playProceduralSound {
                ProceduralAudioSynthesizer.generatePawnTapPcm(
                    frequencyHz = baseFreq,
                    volume = volume
                )
            }
        }
    }

    /**
     * Helper to play a sequence of pawn steps with timed delays (e.g., 110ms between hops).
     */
    fun playPawnStepsSequence(
        steps: Int,
        scope: CoroutineScope,
        isHomeStretch: Boolean = false,
        onStep: ((step: Int) -> Unit)? = null
    ) {
        if (steps <= 0) return
        scope.launch(Dispatchers.Main) {
            for (step in 1..steps) {
                playPawnMove(stepIndex = step, totalSteps = steps, isHomeStretch = isHomeStretch)
                onStep?.invoke(step)
                if (step < steps) {
                    delay(115)
                }
            }
        }
    }

    /**
     * 3. LANDING ON SAFE TILES (Stars & Protected Squares)
     * Plays a shimmering, celestial harmonic chime (major triad bell resonance).
     * In Ludo, star tiles and colored starting safe spots grant complete immunity
     * from enemy capture, so this sound conveys safety, serenity, and blessing!
     */
    fun playSafeTile() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateSafeTile()
        }

        if (!isEnabled || volume <= 0.01f) return

        val pitch = getPresetPitchMultiplier().coerceIn(0.7f, 1.6f)
        var playedInPool = false

        if (isSoundPoolReady && starSafeSoundId != 0) {
            try {
                val streamId = soundPool?.play(
                    starSafeSoundId,
                    volume,
                    volume,
                    2,
                    0,
                    pitch
                ) ?: 0
                playedInPool = streamId > 0
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool safe tile failed: ${e.message}")
            }
        }

        if (!playedInPool) {
            playProceduralSound { ProceduralAudioSynthesizer.generateSafeTileChimePcm(volume) }
        }
    }

    /**
     * Alias for playSafeTile (backward compatibility with star spot naming)
     */
    fun playStarSafe() = playSafeTile()

    /**
     * 4. CAPTURING AN OPPONENT PAWN
     * Plays a dramatic strike impact paired with a triumphant royal flourish.
     */
    fun playPawnCapture() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateCapture()
        }

        if (!isEnabled || volume <= 0.01f) return

        var playedInPool = false
        if (isSoundPoolReady && pawnCaptureSoundId != 0) {
            try {
                soundPool?.play(pawnCaptureSoundId, volume, volume, 2, 0, getPresetPitchMultiplier())
                if (victoryFanfareSoundId != 0) {
                    soundPool?.play(victoryFanfareSoundId, volume * 0.85f, volume * 0.85f, 2, 0, 1.25f)
                }
                playedInPool = true
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool capture failed: ${e.message}")
            }
        }

        if (!playedInPool) {
            playProceduralSound { ProceduralAudioSynthesizer.generateCapturePcm(volume) }
        }
    }

    /**
     * 5. ROLLING A SIX
     * Plays a magical sparkle chime celebrating the bonus roll opportunity.
     */
    fun playSixRolled() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateSix()
        }

        if (!isEnabled || volume <= 0.01f) return

        var playedInPool = false
        if (isSoundPoolReady && sixRolledSoundId != 0) {
            try {
                val streamId = soundPool?.play(sixRolledSoundId, volume, volume, 2, 0, getPresetPitchMultiplier())
                playedInPool = (streamId ?: 0) > 0
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool six rolled failed: ${e.message}")
            }
        }

        if (!playedInPool) {
            playProceduralSound { ProceduralAudioSynthesizer.generateSixSparklePcm(volume) }
        }
    }

    /**
     * 6. PAWN REACHES HOME TRIANGLE
     * Plays royal coronation celebration fanfare.
     */
    fun playPawnHome() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateVictoryFanfare()
        }

        if (!isEnabled || volume <= 0.01f) return

        var playedInPool = false
        if (isSoundPoolReady && victoryFanfareSoundId != 0) {
            try {
                val streamId = soundPool?.play(victoryFanfareSoundId, volume, volume, 2, 0, 1.15f)
                playedInPool = (streamId ?: 0) > 0
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool home fanfare failed: ${e.message}")
            }
        }

        if (!playedInPool) {
            playProceduralSound { ProceduralAudioSynthesizer.generateVictoryFanfarePcm(volume) }
        }
    }

    /**
     * 7. VICTORY FANFARE (Match won)
     */
    fun playVictory() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateVictoryFanfare()
        }

        if (!isEnabled || volume <= 0.01f) return

        var playedInPool = false
        if (isSoundPoolReady && victoryFanfareSoundId != 0) {
            try {
                val streamId = soundPool?.play(victoryFanfareSoundId, volume, volume, 2, 0, 1.0f)
                playedInPool = (streamId ?: 0) > 0
            } catch (e: Exception) {
                Log.w(TAG, "SoundPool victory failed: ${e.message}")
            }
        }

        if (!playedInPool) {
            playProceduralSound { ProceduralAudioSynthesizer.generateVictoryFanfarePcm(volume) }
        }
    }

    fun playVictoryFanfare() = playVictory()

    /**
     * 8. TURN NOTIFICATION ALERT
     * A pleasant high chime notifying the human player it is their turn to roll.
     */
    fun playTurnNotification() {
        if (!isGameActive || !isEnabled || volume <= 0.01f) return
        try {
            if (isSoundPoolReady && buttonClickSoundId != 0) {
                soundPool?.play(buttonClickSoundId, volume * 0.7f, volume * 0.7f, 1, 0, 1.6f)
            } else {
                playProceduralSound {
                    ProceduralAudioSynthesizer.generatePawnTapPcm(frequencyHz = 880f, volume = volume * 0.7f)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * 9. BUTTON & UI CLICK
     */
    fun playButtonClick() {
        if (!isGameActive) return

        if (isHapticsEnabled) {
            vibrateClick()
        }

        if (!isEnabled || volume <= 0.01f) return

        try {
            if (isSoundPoolReady && buttonClickSoundId != 0) {
                soundPool?.play(buttonClickSoundId, volume * 0.8f, volume * 0.8f, 1, 0, getPresetPitchMultiplier())
            } else {
                playProceduralSound {
                    ProceduralAudioSynthesizer.generatePawnTapPcm(frequencyHz = 750f, volume = volume * 0.8f)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "SoundPool button click failed: ${e.message}")
        }
    }

    /**
     * 10. COIN REWARD CHINK
     */
    fun playCoinReward() {
        if (!isGameActive || !isEnabled || volume <= 0.01f) return
        playProceduralSound { ProceduralAudioSynthesizer.generateCoinChimePcm(volume) }
    }

    /**
     * 11. INVALID MOVE / BLOCKED
     */
    fun playInvalidMove() {
        if (!isGameActive) return
        vibrateClick()
        if (!isEnabled || volume <= 0.01f) return
        playProceduralSound {
            ProceduralAudioSynthesizer.generatePawnTapPcm(frequencyHz = 220f, volume = volume * 0.6f)
        }
    }

    // ==========================================
    // Settings & Controls
    // ==========================================

    fun setSfxEnabled(enabled: Boolean) {
        isEnabled = enabled
        profilePreferences.setSfxEnabled(enabled)
    }

    fun setSfxVolume(newVolume: Float) {
        val clamped = newVolume.coerceIn(0f, 1f)
        volume = clamped
        profilePreferences.setSfxVolume(clamped)
    }

    fun setSfxPreset(newPreset: String) {
        preset = newPreset
        profilePreferences.setSfxPreset(newPreset)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        isHapticsEnabled = enabled
        profilePreferences.setHapticsEnabled(enabled)
    }

    private fun getPresetPitchMultiplier(): Float {
        return when (preset) {
            PRESET_CLASSIC -> 1.15f
            PRESET_ARCADE -> 1.30f
            else -> 1.0f // PRESET_ROYAL
        }
    }

    // ==========================================
    // Procedural Audio Playback
    // ==========================================

    private fun playProceduralSound(generator: () -> ShortArray) {
        if (audioExecutor.isShutdown) return
        try {
            audioExecutor.execute {
                var audioTrack: AudioTrack? = null
                try {
                    val pcmData = generator()
                    val minBufferSize = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    val bufferSize = maxOf(minBufferSize, pcmData.size * 2)

                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    audioTrack.write(pcmData, 0, pcmData.size)
                    audioTrack.play()

                    val durationMs = (pcmData.size.toFloat() / SAMPLE_RATE * 1000).toLong() + 25L
                    Thread.sleep(durationMs.coerceIn(25L, 600L))
                } catch (e: Exception) {
                    Log.w(TAG, "Procedural audio playback issue: ${e.message}")
                } finally {
                    try {
                        audioTrack?.stop()
                    } catch (_: Exception) {}
                    try {
                        audioTrack?.release()
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio executor task rejected: ${e.message}")
        }
    }

    // ==========================================
    // Tactile Haptic Vibration Patterns
    // ==========================================

    private fun vibrateDiceRollPattern() {
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

    private fun vibratePawnStep() {
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

    private fun vibrateSafeTile() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 30, 70)
                val amplitudes = intArrayOf(0, 120, 0, 180)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateCapture() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 50, 140)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(160)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateSix() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 40, 50, 40, 110)
                val amplitudes = intArrayOf(0, 140, 0, 180, 0, 240)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(180)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateVictoryFanfare() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 40, 60, 40, 120, 80, 220)
                val amplitudes = intArrayOf(0, 180, 0, 180, 0, 220, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    /**
     * Clean release of sound system resources
     */
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            audioExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing sound effect resources", e)
        }
    }

    // ==========================================
    // Procedural Sound Synthesizer (PCM Engine)
    // ==========================================
    private object ProceduralAudioSynthesizer {

        /**
         * Generates physical dice roll acoustics (multi-impact rattle on board with decay)
         */
        fun generateDiceRollPcm(volume: Float): ShortArray {
            val durationSec = 0.45f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            // Multiple impact times for dice bouncing
            val impactTimes = listOf(0.02f, 0.09f, 0.17f, 0.26f, 0.35f, 0.41f)
            val impactPitches = listOf(380f, 440f, 320f, 480f, 360f, 410f)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                var signal = 0.0

                for (idx in impactTimes.indices) {
                    val hitTime = impactTimes[idx]
                    if (t >= hitTime) {
                        val dt = t - hitTime
                        val decay = exp(-dt * 45.0)
                        val freq = impactPitches[idx]
                        // Wooden resonant thump + subtle randomized friction
                        val tone = sin(2.0 * PI * freq * dt)
                        val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.25
                        signal += (tone + noise) * decay
                    }
                }

                val masterEnv = (1.0 - (t / durationSec)).coerceIn(0.0, 1.0)
                val finalVal = (signal * masterEnv * volume * 18000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }

        /**
         * Generates crisp wooden pawn step clack
         */
        fun generatePawnTapPcm(frequencyHz: Float, volume: Float): ShortArray {
            val durationSec = 0.08f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                // Rapid percussive envelope: steep exponential decay
                val decay = exp(-t * 80.0)
                val primaryTone = sin(2.0 * PI * frequencyHz * t)
                val overtone = 0.35 * sin(2.0 * PI * (frequencyHz * 2.2) * t)
                val initialClick = if (t < 0.005f) (Random.nextDouble() * 2.0 - 1.0) * 0.4 else 0.0

                val signal = (primaryTone + overtone + initialClick) * decay
                val finalVal = (signal * volume * 22000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }

        /**
         * Generates celestial safe tile landing chime:
         * Harmonious major triad (E5, G#5, B5, E6) with shimmering bell decay.
         */
        fun generateSafeTileChimePcm(volume: Float): ShortArray {
            val durationSec = 0.65f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            // Celestial E Major Chord frequencies
            val notes = listOf(659.25, 830.61, 987.77, 1318.51) // E5, G#5, B5, E6
            val delays = listOf(0.00, 0.03, 0.06, 0.09) // Quick arpeggio

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                var signal = 0.0

                for (n in notes.indices) {
                    val noteDelay = delays[n]
                    if (t >= noteDelay) {
                        val dt = t - noteDelay
                        val bellDecay = exp(-dt * 6.5)
                        // Pure sine + crystal overtone shimmer
                        val fundamental = sin(2.0 * PI * notes[n] * dt)
                        val shimmer = 0.25 * sin(2.0 * PI * (notes[n] * 2.0) * dt)
                        signal += (fundamental + shimmer) * bellDecay * 0.28
                    }
                }

                val finalVal = (signal * volume * 25000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }

        /**
         * Generates capture strike impact
         */
        fun generateCapturePcm(volume: Float): ShortArray {
            val durationSec = 0.35f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val decay = exp(-t * 22.0)
                // Pitch sweeps down quickly for heavy impact thud
                val freq = (420.0 - (t * 600.0)).coerceAtLeast(80.0)
                val impact = sin(2.0 * PI * freq * t)
                val noise = if (t < 0.02f) (Random.nextDouble() * 2.0 - 1.0) * 0.6 else 0.0

                val signal = (impact + noise) * decay
                val finalVal = (signal * volume * 24000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }

        /**
         * Generates rolling a six sparkle flourish
         */
        fun generateSixSparklePcm(volume: Float): ShortArray {
            val durationSec = 0.45f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val delays = listOf(0.0, 0.05, 0.10, 0.15)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                var signal = 0.0
                for (n in notes.indices) {
                    val noteDelay = delays[n]
                    if (t >= noteDelay) {
                        val dt = t - noteDelay
                        val decay = exp(-dt * 9.0)
                        signal += sin(2.0 * PI * notes[n] * dt) * decay * 0.3
                    }
                }
                val finalVal = (signal * volume * 24000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }

        /**
         * Generates victory celebration fanfare
         */
        fun generateVictoryFanfarePcm(volume: Float): ShortArray {
            val durationSec = 0.85f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            val chordNotes = listOf(523.25, 659.25, 783.99, 1046.50) // C major grand chord
            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0 - (t / durationSec)).coerceIn(0.0, 1.0)
                var signal = 0.0
                for (freq in chordNotes) {
                    signal += sin(2.0 * PI * freq * t) * 0.25
                }
                val finalVal = (signal * env * volume * 23000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }

        /**
         * Generates bright coin chime
         */
        fun generateCoinChimePcm(volume: Float): ShortArray {
            val durationSec = 0.28f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val samples = ShortArray(numSamples)

            val f1 = 1480.0
            val f2 = 1975.0

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val decay1 = exp(-t * 18.0)
                val decay2 = if (t >= 0.04) exp(-(t - 0.04) * 20.0) else 0.0

                val s1 = sin(2.0 * PI * f1 * t) * decay1 * 0.5
                val s2 = sin(2.0 * PI * f2 * (t - 0.04)) * decay2 * 0.6

                val signal = s1 + s2
                val finalVal = (signal * volume * 22000.0).toInt().coerceIn(-32767, 32767)
                samples[i] = finalVal.toShort()
            }
            return samples
        }
    }
}
