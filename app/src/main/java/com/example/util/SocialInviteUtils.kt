package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

object SocialInviteUtils {

    const val DEFAULT_INVITE_URL = "https://mgludo.app/play"

    fun getInviteMessage(roomCode: String): String {
        return "🎲 Let's play MG Ludo together! 👑\n" +
                "Join my Royal Room with Code: $roomCode\n" +
                "Download & Play now: $DEFAULT_INVITE_URL?room=$roomCode\n" +
                "Get 5,000 Free Coins on joining!"
    }

    fun shareToWhatsApp(context: Context, roomCode: String) {
        val message = getInviteMessage(roomCode)
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to chooser if WhatsApp app is not installed
            shareGeneral(context, roomCode, "Share to WhatsApp")
        }
    }

    fun shareToInstagram(context: Context, roomCode: String) {
        val message = getInviteMessage(roomCode)
        copyToClipboard(context, message, showToast = false)
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.instagram.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Invite copied! Paste in Instagram DM or Story", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            shareGeneral(context, roomCode, "Share to Instagram")
        }
    }

    fun shareToFacebook(context: Context, roomCode: String) {
        val message = getInviteMessage(roomCode)
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.facebook.katana")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            shareGeneral(context, roomCode, "Share to Facebook")
        }
    }

    fun shareGeneral(context: Context, roomCode: String, title: String = "Invite Friends to MG Ludo") {
        val message = getInviteMessage(roomCode)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Play MG Ludo with me!")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun copyToClipboard(context: Context, text: String, showToast: Boolean = true) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MG Ludo Invite", text)
        clipboard.setPrimaryClip(clip)
        if (showToast) {
            Toast.makeText(context, "Invite code copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
}
