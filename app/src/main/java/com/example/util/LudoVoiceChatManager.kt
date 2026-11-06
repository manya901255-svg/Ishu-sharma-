package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Manages real-time in-game voice chat:
 * - Records local mic audio and calculates real-time voice amplitude level
 * - Handles mic mute / unmute & speaker mute / unmute
 * - Coordinates room voice chatter and speaking indicators for all players
 */
class LudoVoiceChatManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var recordJob: Job? = null
    private var roomChatterJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val _isVoiceConnected = MutableStateFlow(true)
    val isVoiceConnected: StateFlow<Boolean> = _isVoiceConnected.asStateFlow()

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isSpeakerMuted = MutableStateFlow(false)
    val isSpeakerMuted: StateFlow<Boolean> = _isSpeakerMuted.asStateFlow()

    private val _localVoiceLevel = MutableStateFlow(0f)
    val localVoiceLevel: StateFlow<Float> = _localVoiceLevel.asStateFlow()

    private val _isLocalSpeaking = MutableStateFlow(false)
    val isLocalSpeaking: StateFlow<Boolean> = _isLocalSpeaking.asStateFlow()

    private val _activeSpeakerId = MutableStateFlow<String?>(null)
    val activeSpeakerId: StateFlow<String?> = _activeSpeakerId.asStateFlow()

    private val _voiceChatNotification = MutableStateFlow<String?>(null)
    val voiceChatNotification: StateFlow<String?> = _voiceChatNotification.asStateFlow()

    private val voiceLines = listOf(
        "Wah kya chaal chali!",
        "Chalo bhai jaldi dice phek!",
        "Mera 6 aane wala hai abhi!",
        "Arey yaar Bach gaya!",
        "Nice roll brother!",
        "Dekh ke chalna mere pichhe!",
        "Royal King entry!",
        "Ab to direct Home jayenge!"
    )

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startVoiceChat() {
        _isVoiceConnected.value = true
        startAudioCaptureIfPermitted()
        startRoomVoiceChatter()
    }

    fun toggleMic() {
        _isMicMuted.value = !_isMicMuted.value
        if (_isMicMuted.value) {
            _localVoiceLevel.value = 0f
            _isLocalSpeaking.value = false
        }
    }

    fun toggleSpeaker() {
        _isSpeakerMuted.value = !_isSpeakerMuted.value
    }

    fun startAudioCaptureIfPermitted() {
        if (!hasRecordPermission()) return
        if (recordJob?.isActive == true) return

        recordJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = if (minBufSize > 0) minBufSize else 2048

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.startRecording()
                    val audioBuffer = ShortArray(bufferSize / 2)

                    while (isActive && _isVoiceConnected.value) {
                        if (_isMicMuted.value) {
                            _localVoiceLevel.value = 0f
                            _isLocalSpeaking.value = false
                            delay(100)
                            continue
                        }

                        val readCount = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                        if (readCount > 0) {
                            var sumSquares = 0.0
                            for (i in 0 until readCount) {
                                val sample = audioBuffer[i].toDouble()
                                sumSquares += sample * sample
                            }
                            val rms = sqrt(sumSquares / readCount)
                            val normalizedLevel = ((rms / 3000.0).coerceIn(0.0, 1.0)).toFloat()
                            _localVoiceLevel.value = normalizedLevel
                            _isLocalSpeaking.value = normalizedLevel > 0.12f
                        }
                        delay(60)
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted or revoked
            } catch (e: Exception) {
                // Audio hardware in use or unavailable
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                audioRecord = null
            }
        }
    }

    private fun startRoomVoiceChatter() {
        if (roomChatterJob?.isActive == true) return
        roomChatterJob = scope.launch {
            while (isActive && _isVoiceConnected.value) {
                delay(7000)
                if (!_isSpeakerMuted.value) {
                    val line = voiceLines.random()
                    _voiceChatNotification.value = line
                    delay(3000)
                    _voiceChatNotification.value = null
                }
            }
        }
    }

    fun onPlayerTurnStart(playerId: String, isAi: Boolean) {
        if (isAi && !_isSpeakerMuted.value) {
            scope.launch {
                if ((0..100).random() < 35) {
                    _activeSpeakerId.value = playerId
                    delay(1800)
                    _activeSpeakerId.value = null
                }
            }
        }
    }

    fun stopVoiceChat() {
        _isVoiceConnected.value = false
        recordJob?.cancel()
        recordJob = null
        roomChatterJob?.cancel()
        roomChatterJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _localVoiceLevel.value = 0f
        _isLocalSpeaking.value = false
        _activeSpeakerId.value = null
    }
}
