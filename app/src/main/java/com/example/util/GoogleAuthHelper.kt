package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed class GoogleSignInOutcome {
    data class Success(val user: FirebaseUser) : GoogleSignInOutcome()
    data class Error(val message: String, val cancelled: Boolean = false) : GoogleSignInOutcome()
}

/**
 * Jetpack Credential Manager helper for Google Sign-In + FirebaseAuth.
 * Strictly follows Play Services Credential Manager guidelines (`GetSignInWithGoogleOption` + `GetGoogleIdOption`).
 */
object GoogleAuthHelper {

    private fun resolveWebClientId(context: Context): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId) else ""
    }

    private fun generateHashedNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun signInInteractive(activity: Activity): GoogleSignInOutcome {
        val auth = FirebaseProviders.getAuthOrNull(activity)
            ?: return GoogleSignInOutcome.Error("Firebase Auth is not initialized.")
        val webClientId = resolveWebClientId(activity)
        if (webClientId.isBlank()) {
            return GoogleSignInOutcome.Error("Missing Google Web Client ID configuration.")
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
            .setNonce(generateHashedNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleIdCredential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCred).await()
                val user = authResult.user
                if (user != null) {
                    GoogleSignInOutcome.Success(user)
                } else {
                    GoogleSignInOutcome.Error("Google sign-in returned no Firebase user.")
                }
            } else {
                GoogleSignInOutcome.Error("Unsupported credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInOutcome.Error("Sign-in cancelled.", cancelled = true)
        } catch (e: NoCredentialException) {
            GoogleSignInOutcome.Error("No Google account found on this device. Please add a Google account in device settings.")
        } catch (e: Exception) {
            Log.e("GoogleAuthHelper", "Interactive Google sign-in failed", e)
            GoogleSignInOutcome.Error(e.message ?: "Google Sign-In failed.")
        }
    }

    suspend fun trySilentSignIn(activity: Activity): FirebaseUser? {
        val auth = FirebaseProviders.getAuthOrNull(activity) ?: return null
        auth.currentUser?.let { return it }
        val webClientId = resolveWebClientId(activity)
        if (webClientId.isBlank()) return null

        return try {
            val credentialManager = CredentialManager.create(activity)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .setNonce(generateHashedNonce())
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleIdCredential.idToken, null)
                auth.signInWithCredential(firebaseCred).await().user
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signOut(context: Context) {
        try {
            FirebaseProviders.getAuthOrNull(context)?.signOut()
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("GoogleAuthHelper", "Sign out cleanup warning: ${e.message}")
        }
    }
}
