package com.example.network

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

/**
 * JioSaavnDecryptor: Decrypts encrypted media URLs from JioSaavn CDN responses using DES/ECB/PKCS5Padding.
 */
object JioSaavnDecryptor {
    private const val TAG = "JioSaavnDecryptor"
    private const val ALGORITHM = "DES"
    private const val TRANSFORMATION = "DES/ECB/PKCS5Padding"
    private const val SECRET_KEY = "38346591"

    fun decryptUrl(encryptedMediaUrl: String?): String? {
        if (encryptedMediaUrl.isNullOrEmpty()) return null
        return try {
            val keyBytes = SECRET_KEY.toByteArray(StandardCharsets.UTF_8)
            val secretKeySpec = SecretKeySpec(keyBytes, ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

            val decodedBytes = Base64.decode(encryptedMediaUrl.trim(), Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            var decryptedUrl = String(decryptedBytes, StandardCharsets.UTF_8).trim()

            // Ensure https protocol
            if (decryptedUrl.startsWith("http://")) {
                decryptedUrl = decryptedUrl.replaceFirst("http://", "https://")
            }
            decryptedUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt media url: ${e.message}")
            null
        }
    }
}
