package com.example.util

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Central provider for Firebase instances that strictly binds the named Firestore database ID
 * from firebase_applet_config.xml and gracefully returns null if FirebaseApp isn't initialized
 * (e.g. during isolated offline unit tests).
 */
object FirebaseProviders {

    fun isFirebaseReady(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun getDatabaseId(context: Context): String {
        return try {
            context.getString(R.string.firestore_database_id)
        } catch (_: Exception) {
            "ai-studio-android-mgludo-e0dac952-b692-4936-989c-4243033408cd"
        }
    }

    fun getFirestoreOrNull(context: Context): FirebaseFirestore? {
        return try {
            if (!isFirebaseReady(context)) null
            else FirebaseFirestore.getInstance(getDatabaseId(context))
        } catch (e: Exception) {
            Log.w("FirebaseProviders", "Firestore unavailable: ${e.message}")
            null
        }
    }

    fun getAuthOrNull(context: Context): FirebaseAuth? {
        return try {
            if (!isFirebaseReady(context)) null
            else FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseProviders", "Auth unavailable: ${e.message}")
            null
        }
    }
}

enum class FirestoreOperation { READ, WRITE }

fun handleFirestoreError(
    e: Exception,
    operation: FirestoreOperation,
    path: String,
    auth: FirebaseAuth? = null
): String {
    val currentUid = auth?.currentUser?.uid
    val code = (e as? FirebaseFirestoreException)?.code
    Log.e("FirestoreError", "[$operation] path=$path uid=$currentUid code=$code msg=${e.message}", e)
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Permission denied on $path. Please verify sign-in and task/room authorization."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Network temporarily unavailable. Changes will sync when reconnected."
        FirebaseFirestoreException.Code.NOT_FOUND ->
            "Requested record was not found."
        FirebaseFirestoreException.Code.ALREADY_EXISTS ->
            "This reward or action has already been recorded."
        else -> e.message ?: "Unexpected server error occurred."
    }
}

object LudoPeriodKeys {
    private val utcTz: TimeZone = TimeZone.getTimeZone("UTC")

    fun currentDayKey(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utcTz }
        return sdf.format(Date(nowMillis))
    }

    fun currentWeekKey(nowMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(utcTz, Locale.US).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
            timeInMillis = nowMillis
        }
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    fun previousDayKey(nowMillis: Long = System.currentTimeMillis()): String {
        return currentDayKey(nowMillis - 86_400_000L)
    }

    fun msUntilNextDailyReset(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(utcTz, Locale.US).apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (cal.timeInMillis - nowMillis).coerceAtLeast(0L)
    }

    fun msUntilNextWeeklyReset(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(utcTz, Locale.US).apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            do {
                add(Calendar.DAY_OF_YEAR, 1)
            } while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
        }
        return (cal.timeInMillis - nowMillis).coerceAtLeast(0L)
    }
}
