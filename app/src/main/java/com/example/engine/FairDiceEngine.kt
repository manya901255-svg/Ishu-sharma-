package com.example.engine

import com.example.model.FairDiceProof
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object FairDiceEngine {
    private val secureRandom = SecureRandom()
    private var currentServerSeed: String = generateRandomSeed()
    private var currentClientSeed: String = "client_maharaja_seed_" + System.currentTimeMillis()
    private var nonce: Long = 0

    init {
        rollNewSeeds()
    }

    fun rollNewSeeds() {
        currentServerSeed = generateRandomSeed()
        nonce = 0
    }

    private fun generateRandomSeed(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Provably Fair Dice Roll:
     * roll = (HMAC_SHA256(serverSeed, clientSeed:nonce) % 6) + 1
     */
    fun rollDice(): Pair<Int, FairDiceProof> {
        nonce++
        val message = "$currentClientSeed:$nonce"
        val hmacBytes = computeHmacSha256(currentServerSeed, message)
        
        // Take first 4 bytes as unsigned integer
        val intVal = (hmacBytes[0].toInt() and 0xFF shl 24) or
                (hmacBytes[1].toInt() and 0xFF shl 16) or
                (hmacBytes[2].toInt() and 0xFF shl 8) or
                (hmacBytes[3].toInt() and 0xFF)
        val positiveInt = intVal.toLong() and 0xFFFFFFFFL
        val diceResult = ((positiveInt % 6) + 1).toInt()

        val proof = FairDiceProof(
            roll = diceResult,
            timestamp = System.currentTimeMillis(),
            serverSeedHash = sha256(currentServerSeed),
            clientSeed = currentClientSeed,
            nonce = nonce
        )

        return Pair(diceResult, proof)
    }

    private fun computeHmacSha256(key: String, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data.toByteArray())
    }

    fun verifyRoll(serverSeed: String, clientSeed: String, nonce: Long, expectedRoll: Int): Boolean {
        val message = "$clientSeed:$nonce"
        val hmacBytes = computeHmacSha256(serverSeed, message)
        val intVal = (hmacBytes[0].toInt() and 0xFF shl 24) or
                (hmacBytes[1].toInt() and 0xFF shl 16) or
                (hmacBytes[2].toInt() and 0xFF shl 8) or
                (hmacBytes[3].toInt() and 0xFF)
        val positiveInt = intVal.toLong() and 0xFFFFFFFFL
        val calculatedRoll = ((positiveInt % 6) + 1).toInt()
        return calculatedRoll == expectedRoll
    }
}
