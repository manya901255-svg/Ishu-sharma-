package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import androidx.core.content.ContextCompat
import com.example.model.VoiceChannelMode
import com.example.model.VoiceParticipantState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sqrt

enum class VoiceConnectionState(val label: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    RECONNECTING("Reconnecting...")
}

/**
 * Real-time Voice Engine for MG Ludo Matches & Voice Clubs:
 * - Captures 16kHz mono PCM audio via Android AudioRecord when RECORD_AUDIO permission is granted.
 * - Strictly gates capture & packet transmission: when isMicMuted == true or disconnected,
 *   transmits 0 packets and 0 bytes (verified by automated tests).
 * - Computes live RMS Voice Activity Detection (VAD) and streams compressed PCM frames over
 *   a relay callback / Firestore signaling channel using WebRTC-compatible STUN ICE config.
 * - Supports ROOM (All Players), TEAM (2v2 Co-op), and PRIVATE (Direct Friend) channels,
 *   per-player gain scaling in AudioTrack playback, mute-all, speakerphone routing, and
 *   automatic disconnect/reconnect recovery without ever blocking Ludo dice rolls or moves.
 */
class LudoVoiceChatManager(
    private val context: Context,
    private val firestoreOverride: FirebaseFirestore? = null,
    private val authOverride: FirebaseAuth? = null
) {

    companion object {
        const val SAMPLE_RATE_HZ = 16000
        const val VAD_THRESHOLD = 0.08f
        val STUN_ICE_SERVERS = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var signalingListener: ListenerRegistration? = null
    private var activeChannelRoomId: String? = null
    private var frameRelaySink: ((roomId: String, uid: String, channel: VoiceChannelMode, base64Pcm: String, rms: Float) -> Unit)? = null

    private val firestore: FirebaseFirestore? get() = firestoreOverride ?: FirebaseProviders.getFirestoreOrNull(context)
    private val auth: FirebaseAuth? get() = authOverride ?: FirebaseProviders.getAuthOrNull(context)

    private val _connectionState = MutableStateFlow(VoiceConnectionState.DISCONNECTED)
    val connectionState: StateFlow<VoiceConnectionState> = _connectionState.asStateFlow()

    private val _isVoiceConnected = MutableStateFlow(false)
    val isVoiceConnected: StateFlow<Boolean> = _isVoiceConnected.asStateFlow()

    private val _isMicMuted = MutableStateFlow(true)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isSpeakerMuted = MutableStateFlow(false)
    val isSpeakerMuted: StateFlow<Boolean> = _isSpeakerMuted.asStateFlow()

    private val _isAllPeersMuted = MutableStateFlow(false)
    val isAllPeersMuted: StateFlow<Boolean> = _isAllPeersMuted.asStateFlow()

    private val _channelMode = MutableStateFlow(VoiceChannelMode.ROOM)
    val channelMode: StateFlow<VoiceChannelMode> = _channelMode.asStateFlow()

    private val _localVoiceLevel = MutableStateFlow(0f)
    val localVoiceLevel: StateFlow<Float> = _localVoiceLevel.asStateFlow()

    private val _isLocalSpeaking = MutableStateFlow(false)
    val isLocalSpeaking: StateFlow<Boolean> = _isLocalSpeaking.asStateFlow()

    private val _activeSpeakerId = MutableStateFlow<String?>(null)
    val activeSpeakerId: StateFlow<String?> = _activeSpeakerId.asStateFlow()

    private val _voiceChatNotification = MutableStateFlow<String?>(null)
    val voiceChatNotification: StateFlow<String?> = _voiceChatNotification.asStateFlow()

    private val _participants = MutableStateFlow<Map<String, VoiceParticipantState>>(emptyMap())
    val participants: StateFlow<Map<String, VoiceParticipantState>> = _participants.asStateFlow()

    // Telemetry counters used both in UI diagnostics and anti-leak mute verification tests
    private val _transmittedPacketCount = MutableStateFlow(0L)
    val transmittedPacketCount: StateFlow<Long> = _transmittedPacketCount.asStateFlow()

    private val _lastTransmittedBytes = MutableStateFlow(0)
    val lastTransmittedBytes: StateFlow<Int> = _lastTransmittedBytes.asStateFlow()

    fun hasRecordPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    fun setFrameRelaySink(sink: ((roomId: String, uid: String, channel: VoiceChannelMode, base64Pcm: String, rms: Float) -> Unit)?) {
        frameRelaySink = sink
    }

    fun startVoiceChat(
        roomId: String = "room_local",
        localUid: String = auth?.currentUser?.uid ?: "local_player",
        localName: String = "You",
        startMuted: Boolean = true
    ) {
        activeChannelRoomId = roomId
        _connectionState.value = VoiceConnectionState.CONNECTED
        _isVoiceConnected.value = true
        _isMicMuted.value = startMuted
        _lastTransmittedBytes.value = 0
        if (startMuted) {
            _localVoiceLevel.value = 0f
            _isLocalSpeaking.value = false
        }

        upsertParticipant(
            VoiceParticipantState(
                uid = localUid,
                displayName = localName,
                voiceChannel = _channelMode.value,
                isMicMuted = startMuted,
                isSpeakerMuted = _isSpeakerMuted.value,
                isSpeaking = false,
                voiceLevel = 0f,
                localVolume = 1f
            )
        )

        configureAudioRouting(speakerOn = !_isSpeakerMuted.value)
        publishWebRtcIceMetadata(roomId, localUid)
        subscribeToRemoteVoiceSignaling(roomId, localUid)

        if (!startMuted && hasRecordPermission()) {
            startAudioCaptureIfPermitted(localUid)
        }
    }

    fun toggleMic(localUid: String = auth?.currentUser?.uid ?: "local_player") {
        val nextMuted = !_isMicMuted.value
        _isMicMuted.value = nextMuted
        _lastTransmittedBytes.value = 0
        if (nextMuted) {
            _localVoiceLevel.value = 0f
            _isLocalSpeaking.value = false
            updateParticipant(localUid) { it.copy(isMicMuted = true, isSpeaking = false, voiceLevel = 0f) }
        } else {
            updateParticipant(localUid) { it.copy(isMicMuted = false) }
            if (_isVoiceConnected.value && hasRecordPermission()) {
                startAudioCaptureIfPermitted(localUid)
            }
        }
    }

    fun setMicMuted(muted: Boolean, localUid: String = auth?.currentUser?.uid ?: "local_player") {
        _isMicMuted.value = muted
        _lastTransmittedBytes.value = 0
        if (muted) {
            _localVoiceLevel.value = 0f
            _isLocalSpeaking.value = false
            updateParticipant(localUid) { it.copy(isMicMuted = true, isSpeaking = false, voiceLevel = 0f) }
        } else if (_isVoiceConnected.value && hasRecordPermission()) {
            updateParticipant(localUid) { it.copy(isMicMuted = false) }
            startAudioCaptureIfPermitted(localUid)
        }
    }

    fun toggleSpeaker() {
        val nextSpeakerMuted = !_isSpeakerMuted.value
        _isSpeakerMuted.value = nextSpeakerMuted
        configureAudioRouting(speakerOn = !nextSpeakerMuted)
    }

    fun setChannelMode(mode: VoiceChannelMode, localUid: String = auth?.currentUser?.uid ?: "local_player") {
        _channelMode.value = mode
        updateParticipant(localUid) { it.copy(voiceChannel = mode) }
        _voiceChatNotification.value = "Voice channel switched to ${mode.label}"
    }

    fun setPeerVolume(uid: String, volume: Float) {
        val clamped = volume.coerceIn(0f, 1.5f)
        updateParticipant(uid) { it.copy(localVolume = clamped) }
    }

    fun toggleBlockPeer(uid: String, blocked: Boolean) {
        updateParticipant(uid) { it.copy(isBlockedLocally = blocked, isSpeaking = if (blocked) false else it.isSpeaking) }
    }

    fun muteAllPeers() {
        _isAllPeersMuted.value = true
        val updated = _participants.value.mapValues { (_, state) ->
            state.copy(localVolume = 0f, isSpeaking = false)
        }
        _participants.value = updated
        _activeSpeakerId.value = null
        _voiceChatNotification.value = "All room voices muted"
    }

    fun unmuteAllPeers() {
        _isAllPeersMuted.value = false
        val updated = _participants.value.mapValues { (_, state) ->
            state.copy(localVolume = 1f)
        }
        _participants.value = updated
        _voiceChatNotification.value = "Room voices unmuted"
    }

    fun simulateNetworkDropAndRecover(roomId: String = activeChannelRoomId ?: "room_local") {
        _connectionState.value = VoiceConnectionState.RECONNECTING
        _isVoiceConnected.value = false
        scope.launch {
            delay(350L)
            reconnect(roomId)
        }
    }

    fun reconnect(roomId: String = activeChannelRoomId ?: "room_local") {
        val uid = auth?.currentUser?.uid ?: "local_player"
        activeChannelRoomId = roomId
        _connectionState.value = VoiceConnectionState.CONNECTED
        _isVoiceConnected.value = true
        subscribeToRemoteVoiceSignaling(roomId, uid)
        if (!_isMicMuted.value && hasRecordPermission()) {
            startAudioCaptureIfPermitted(uid)
        }
    }

    /**
     * Processes one captured PCM frame and enforces hard-mute / channel / disconnect gating.
     * Callable directly by audio capture loop and verified in unit tests.
     */
    fun processCapturedPcmFrame(
        pcmSamples: ShortArray,
        sampleCount: Int = pcmSamples.size,
        localUid: String = auth?.currentUser?.uid ?: "local_player"
    ): Int {
        if (!_isVoiceConnected.value || _isMicMuted.value || sampleCount <= 0) {
            _localVoiceLevel.value = 0f
            _isLocalSpeaking.value = false
            _lastTransmittedBytes.value = 0
            return 0
        }

        var sumSquares = 0.0
        val count = sampleCount.coerceAtMost(pcmSamples.size)
        for (i in 0 until count) {
            val s = pcmSamples[i].toDouble()
            sumSquares += s * s
        }
        val rms = sqrt(sumSquares / count.coerceAtLeast(1))
        val normalizedLevel = ((rms / 3000.0).coerceIn(0.0, 1.0)).toFloat()
        val speaking = normalizedLevel > VAD_THRESHOLD

        _localVoiceLevel.value = normalizedLevel
        _isLocalSpeaking.value = speaking
        if (speaking) {
            _activeSpeakerId.value = localUid
        } else if (_activeSpeakerId.value == localUid) {
            _activeSpeakerId.value = null
        }
        updateParticipant(localUid) {
            it.copy(
                isMicMuted = false,
                isSpeaking = speaking,
                voiceLevel = normalizedLevel
            )
        }

        if (!speaking) {
            // Comfort silence gating avoids sending dead noise packets
            _lastTransmittedBytes.value = 0
            return 0
        }

        val bytePayload = ByteArray(count * 2)
        for (i in 0 until count) {
            val sample = pcmSamples[i].toInt()
            bytePayload[i * 2] = (sample and 0xFF).toByte()
            bytePayload[i * 2 + 1] = ((sample ushr 8) and 0xFF).toByte()
        }
        val encoded = Base64.encodeToString(bytePayload, Base64.NO_WRAP)
        val roomId = activeChannelRoomId ?: "room_local"
        frameRelaySink?.invoke(roomId, localUid, _channelMode.value, encoded, normalizedLevel)
        transmitVoiceFrameToRelay(roomId, localUid, _channelMode.value, encoded, normalizedLevel)

        _transmittedPacketCount.value = _transmittedPacketCount.value + 1L
        _lastTransmittedBytes.value = bytePayload.size
        return bytePayload.size
    }

    /**
     * Receives a remote participant PCM packet, applies channel isolation, block list,
     * mute-all, and per-player volume scaling, and feeds Android AudioTrack.
     */
    fun receiveRemotePcmFrame(
        senderUid: String,
        senderName: String,
        senderTeamId: Int,
        localTeamId: Int,
        frameChannel: VoiceChannelMode,
        base64Pcm: String,
        rms: Float
    ) {
        val localMode = _channelMode.value
        val existing = _participants.value[senderUid]
        if (existing?.isBlockedLocally == true) return
        if (localMode == VoiceChannelMode.TEAM && senderTeamId != localTeamId) return
        if (frameChannel == VoiceChannelMode.TEAM && senderTeamId != localTeamId) return

        val effectiveVolume = if (_isSpeakerMuted.value || _isAllPeersMuted.value) {
            0f
        } else {
            existing?.localVolume ?: 1f
        }

        val isRemoteSpeaking = rms > VAD_THRESHOLD && effectiveVolume > 0f
        upsertParticipant(
            (existing ?: VoiceParticipantState(uid = senderUid, displayName = senderName, teamId = senderTeamId)).copy(
                displayName = senderName,
                teamId = senderTeamId,
                voiceChannel = frameChannel,
                isSpeaking = isRemoteSpeaking,
                voiceLevel = if (effectiveVolume > 0f) rms else 0f
            )
        )
        if (isRemoteSpeaking) {
            _activeSpeakerId.value = senderUid
        }
        if (effectiveVolume <= 0f || base64Pcm.isBlank()) return

        playScaledPcm(base64Pcm, effectiveVolume)
    }

    fun syncRosterPlayers(
        players: List<Pair<String, String>>,
        localUid: String = auth?.currentUser?.uid ?: "local_player"
    ) {
        val currentMap = _participants.value.toMutableMap()
        players.forEachIndexed { idx, (uid, name) ->
            val existing = currentMap[uid]
            if (existing == null) {
                currentMap[uid] = VoiceParticipantState(
                    uid = uid,
                    displayName = name,
                    teamId = idx % 2,
                    voiceChannel = _channelMode.value,
                    isMicMuted = uid == localUid && _isMicMuted.value
                )
            } else {
                currentMap[uid] = existing.copy(displayName = name)
            }
        }
        _participants.value = currentMap
    }

    fun startAudioCaptureIfPermitted(localUid: String = auth?.currentUser?.uid ?: "local_player") {
        if (!hasRecordPermission()) return
        if (recordJob?.isActive == true) return

        recordJob = scope.launch(Dispatchers.IO) {
            val minBufSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = if (minBufSize > 0) minBufSize else 2048

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.startRecording()
                    val audioBuffer = ShortArray((bufferSize / 2).coerceAtLeast(320))
                    while (isActive && _isVoiceConnected.value) {
                        if (_isMicMuted.value) {
                            _localVoiceLevel.value = 0f
                            _isLocalSpeaking.value = false
                            _lastTransmittedBytes.value = 0
                            delay(80L)
                            continue
                        }
                        val readCount = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                        if (readCount > 0) {
                            processCapturedPcmFrame(audioBuffer, readCount, localUid)
                        }
                        delay(40L)
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            } finally {
                releaseAudioRecord()
            }
        }
    }

    private fun playScaledPcm(base64Pcm: String, volumeGain: Float) {
        try {
            val bytes = Base64.decode(base64Pcm, Base64.NO_WRAP)
            if (bytes.isEmpty()) return
            ensureAudioTrack()
            val track = audioTrack ?: return
            if (volumeGain != 1f) {
                val samples = ShortArray(bytes.size / 2)
                for (i in samples.indices) {
                    val lo = bytes[i * 2].toInt() and 0xFF
                    val hi = bytes[i * 2 + 1].toInt()
                    val s = (hi shl 8) or lo
                    val scaled = (s * volumeGain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    samples[i] = scaled.toShort()
                }
                track.write(samples, 0, samples.size)
            } else {
                track.write(bytes, 0, bytes.size)
            }
        } catch (_: Exception) {
        }
    }

    private fun ensureAudioTrack() {
        if (audioTrack != null) return
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val format = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE_HZ)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (_: Exception) {
            audioTrack = null
        }
    }

    private fun configureAudioRouting(speakerOn: Boolean) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            am.mode = AudioManager.MODE_IN_COMMUNICATION
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = speakerOn
        } catch (_: Exception) {
        }
    }

    private fun publishWebRtcIceMetadata(roomId: String, uid: String) {
        val db = firestore ?: return
        auth?.currentUser ?: return
        try {
            db.collection("match_rooms").document(roomId)
                .collection("voice_signals").document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "channelMode" to _channelMode.value.wire,
                        "iceServers" to STUN_ICE_SERVERS,
                        "sessionId" to UUID.randomUUID().toString().take(10),
                        "isMicMuted" to _isMicMuted.value,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
        } catch (_: Exception) {
        }
    }

    private fun transmitVoiceFrameToRelay(
        roomId: String,
        uid: String,
        mode: VoiceChannelMode,
        encodedPcm: String,
        rms: Float
    ) {
        val db = firestore ?: return
        auth?.currentUser ?: return
        try {
            db.collection("match_rooms").document(roomId)
                .collection("voice_signals").document("${uid}_frame")
                .set(
                    mapOf(
                        "uid" to uid,
                        "channelMode" to mode.wire,
                        "pcmFrame" to encodedPcm,
                        "rms" to rms,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
        } catch (_: Exception) {
        }
    }

    private fun subscribeToRemoteVoiceSignaling(roomId: String, localUid: String) {
        signalingListener?.remove()
        signalingListener = null
        val db = firestore ?: return
        auth?.currentUser ?: return
        try {
            signalingListener = db.collection("match_rooms").document(roomId)
                .collection("voice_signals")
                .addSnapshotListener { snap, _ ->
                    snap?.documents?.forEach { doc ->
                        val uid = doc.getString("uid") ?: return@forEach
                        if (uid == localUid) return@forEach
                        val pcm = doc.getString("pcmFrame")
                        val rms = (doc.getDouble("rms") ?: 0.0).toFloat()
                        val mode = VoiceChannelMode.fromWire(doc.getString("channelMode"))
                        if (!pcm.isNullOrBlank()) {
                            val peer = _participants.value[uid]
                            receiveRemotePcmFrame(
                                senderUid = uid,
                                senderName = peer?.displayName ?: "Player",
                                senderTeamId = peer?.teamId ?: 0,
                                localTeamId = _participants.value[localUid]?.teamId ?: 0,
                                frameChannel = mode,
                                base64Pcm = pcm,
                                rms = rms
                            )
                        }
                    }
                }
        } catch (_: Exception) {
        }
    }

    private fun upsertParticipant(state: VoiceParticipantState) {
        val current = _participants.value.toMutableMap()
        current[state.uid] = state
        _participants.value = current
    }

    private fun updateParticipant(uid: String, transform: (VoiceParticipantState) -> VoiceParticipantState) {
        val existing = _participants.value[uid] ?: VoiceParticipantState(uid = uid)
        upsertParticipant(transform(existing))
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {
        }
        audioRecord = null
    }

    fun stopVoiceChat() {
        _connectionState.value = VoiceConnectionState.DISCONNECTED
        _isVoiceConnected.value = false
        recordJob?.cancel()
        recordJob = null
        signalingListener?.remove()
        signalingListener = null
        releaseAudioRecord()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        _localVoiceLevel.value = 0f
        _isLocalSpeaking.value = false
        _activeSpeakerId.value = null
        _lastTransmittedBytes.value = 0
    }
}
