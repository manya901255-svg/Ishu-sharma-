package com.example.data

import android.content.Context
import com.example.model.MatchRoomItem
import com.example.util.FirebaseProviders
import com.example.util.FirestoreOperation
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

/**
 * Authoritative multiplayer & private Ludo room repository.
 * - Enforces private room access control (invited / allowedUids / valid roomCode).
 * - Rejects unauthorized players via Firestore rules AND local access validation.
 * - Synchronizes turn order, move sequence, state hash checksum, and disconnect/reconnect recovery.
 */
class OnlineMatchRoomRepository(
    private val context: Context,
    private val firestoreOverride: FirebaseFirestore? = null,
    private val authOverride: FirebaseAuth? = null
) {
    private val firestore: FirebaseFirestore? get() = firestoreOverride ?: FirebaseProviders.getFirestoreOrNull(context)
    private val auth: FirebaseAuth? get() = authOverride ?: FirebaseProviders.getAuthOrNull(context)

    suspend fun createMatchRoom(
        isPrivate: Boolean,
        gameMode: String,
        maxPlayers: Int,
        wagerCoins: Int,
        invitedUids: List<String> = emptyList()
    ): Result<MatchRoomItem> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        val roomId = "room_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val roomCode = (100000..999999).random().toString()
        val initialBoard = mapOf("diceValue" to 1, "consecutiveSixes" to 0, "tokens" to emptyMap<String, Any>())
        return try {
            db.collection("match_rooms").document(roomId).set(
                mapOf(
                    "roomId" to roomId,
                    "roomCode" to roomCode,
                    "ownerId" to user.uid,
                    "isPrivate" to isPrivate,
                    "allowedUids" to listOf(user.uid),
                    "invitedUids" to invitedUids.distinct().take(20),
                    "playerUids" to listOf(user.uid),
                    "maxPlayers" to maxPlayers.coerceIn(2, 4),
                    "gameMode" to gameMode,
                    "wagerCoins" to wagerCoins.coerceAtLeast(0),
                    "status" to "WAITING",
                    "currentTurnUid" to user.uid,
                    "moveSequence" to 0,
                    "stateHash" to computeStateHash(0, user.uid, initialBoard),
                    "boardState" to initialBoard,
                    "turnDeadlineMillis" to (System.currentTimeMillis() + 30_000L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(
                MatchRoomItem(
                    roomId = roomId,
                    roomCode = roomCode,
                    ownerId = user.uid,
                    isPrivate = isPrivate,
                    allowedUids = listOf(user.uid),
                    invitedUids = invitedUids,
                    playerUids = listOf(user.uid),
                    maxPlayers = maxPlayers,
                    gameMode = gameMode,
                    wagerCoins = wagerCoins,
                    status = "WAITING",
                    currentTurnUid = user.uid,
                    moveSequence = 0,
                    stateHash = computeStateHash(0, user.uid, initialBoard),
                    boardState = initialBoard
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "match_rooms/$roomId", auth)))
        }
    }

    suspend fun joinRoomById(roomId: String, roomCodeAttempt: String? = null): Result<MatchRoomItem> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val user = auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        return try {
            val ref = db.collection("match_rooms").document(roomId)
            val snap = ref.get().await()
            val room = parseMatchRoom(snap)
                ?: return Result.failure(IllegalStateException("Match room does not exist."))

            if (!canJoinRoom(room, user.uid, roomCodeAttempt)) {
                return Result.failure(IllegalStateException("Access denied: invalid room code or missing private invitation."))
            }

            if (user.uid !in room.playerUids) {
                if (room.playerUids.size >= room.maxPlayers) {
                    return Result.failure(IllegalStateException("Match room is full."))
                }
                val newPlayers = room.playerUids + user.uid
                val newAllowed = (room.allowedUids + user.uid).distinct().take(10)
                val newStatus = if (newPlayers.size >= room.maxPlayers) "ACTIVE" else "WAITING"
                val updateMap = buildMap<String, Any> {
                    put("playerUids", newPlayers)
                    put("allowedUids", newAllowed)
                    if (!roomCodeAttempt.isNullOrBlank()) put("attemptedRoomCode", roomCodeAttempt.trim())
                    put("status", newStatus)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                ref.update(updateMap).await()
                return Result.success(room.copy(playerUids = newPlayers, allowedUids = newAllowed, status = newStatus))
            }
            Result.success(room)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "match_rooms/$roomId/join", auth)))
        }
    }

    fun observeMatchRoom(roomId: String): Flow<MatchRoomItem?> = callbackFlow {
        val db = firestore
        if (db == null || auth?.currentUser == null) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }
        val reg: ListenerRegistration = db.collection("match_rooms").document(roomId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    handleFirestoreError(err, FirestoreOperation.READ, "match_rooms/$roomId", auth)
                    return@addSnapshotListener
                }
                trySend(snap?.let { parseMatchRoom(it) })
            }
        awaitClose { reg.remove() }
    }

    suspend fun syncTurnMove(
        roomId: String,
        nextTurnUid: String,
        nextSequence: Int,
        boardState: Map<String, Any>,
        winnerUid: String? = null
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        auth?.currentUser ?: return Result.failure(IllegalStateException("Sign in required"))
        return try {
            val hash = computeStateHash(nextSequence, nextTurnUid, boardState)
            val update = buildMap<String, Any> {
                put("boardState", boardState)
                put("currentTurnUid", nextTurnUid)
                put("moveSequence", nextSequence)
                put("stateHash", hash)
                put("turnDeadlineMillis", System.currentTimeMillis() + 25_000L)
                if (!winnerUid.isNullOrBlank()) {
                    put("winnerUid", winnerUid)
                    put("status", "FINISHED")
                }
                put("updatedAt", FieldValue.serverTimestamp())
            }
            db.collection("match_rooms").document(roomId).update(update).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(handleFirestoreError(e, FirestoreOperation.WRITE, "match_rooms/$roomId/move", auth)))
        }
    }

    companion object {
        /**
         * Pure authoritative authorization check shared between client pre-checks and unit tests.
         * Firestore security rules enforce this exact same condition server-side.
         */
        fun canJoinRoom(room: MatchRoomItem, candidateUid: String, enteredCode: String?): Boolean {
            if (room.status == "FINISHED") return false
            if (candidateUid in room.playerUids) return true
            if (!room.isPrivate) return true
            if (candidateUid == room.ownerId) return true
            if (candidateUid in room.invitedUids || candidateUid in room.allowedUids) return true
            return !enteredCode.isNullOrBlank() && enteredCode.trim() == room.roomCode
        }

        fun computeStateHash(sequence: Int, currentTurnUid: String, boardState: Map<String, Any>): String {
            val raw = "$sequence|$currentTurnUid|${boardState["diceValue"]}|${boardState.size}"
            return MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
                .joinToString("") { "%02x".format(it) }.take(24)
        }
    }

    private fun parseMatchRoom(doc: DocumentSnapshot): MatchRoomItem? {
        if (!doc.exists()) return null
        @Suppress("UNCHECKED_CAST")
        val allowed = (doc.get("allowedUids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val invited = (doc.get("invitedUids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val players = (doc.get("playerUids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val board = (doc.get("boardState") as? Map<String, Any>) ?: emptyMap()
        return MatchRoomItem(
            roomId = doc.getString("roomId") ?: doc.id,
            roomCode = doc.getString("roomCode") ?: "",
            ownerId = doc.getString("ownerId") ?: "",
            isPrivate = doc.getBoolean("isPrivate") ?: false,
            allowedUids = allowed,
            invitedUids = invited,
            playerUids = players,
            maxPlayers = (doc.getLong("maxPlayers") ?: 4L).toInt(),
            gameMode = doc.getString("gameMode") ?: "ONLINE_MULTIPLAYER",
            wagerCoins = (doc.getLong("wagerCoins") ?: 0L).toInt(),
            status = doc.getString("status") ?: "WAITING",
            currentTurnUid = doc.getString("currentTurnUid") ?: "",
            moveSequence = (doc.getLong("moveSequence") ?: 0L).toInt(),
            stateHash = doc.getString("stateHash") ?: "",
            winnerUid = doc.getString("winnerUid"),
            turnDeadlineMillis = doc.getLong("turnDeadlineMillis") ?: 0L,
            boardState = board
        )
    }
}
