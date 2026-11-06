package com.example.ui.components

import java.util.Locale
import kotlin.math.abs

/**
 * Formats coin amounts into compact, readable strings for game UI badges and labels.
 * E.g., 500 -> "500", 2500 -> "2.5K", 10000 -> "10K", 1500000 -> "1.5M"
 */
fun formatCompactCoins(amount: Long): String {
    val isNegative = amount < 0
    val absVal = abs(amount)
    val formatted = when {
        absVal >= 1_000_000_000L -> {
            val value = absVal / 1_000_000_000.0
            if (value >= 100.0) {
                String.format(Locale.US, "%.0fB", value)
            } else {
                String.format(Locale.US, "%.1fB", value).replace(".0B", "B")
            }
        }
        absVal >= 1_000_000L -> {
            val value = absVal / 1_000_000.0
            if (value >= 100.0) {
                String.format(Locale.US, "%.0fM", value)
            } else {
                String.format(Locale.US, "%.1fM", value).replace(".0M", "M")
            }
        }
        absVal >= 10_000L -> {
            val value = absVal / 1_000.0
            if (value >= 100.0) {
                String.format(Locale.US, "%.0fK", value)
            } else {
                String.format(Locale.US, "%.1fK", value).replace(".0K", "K")
            }
        }
        absVal >= 1_000L -> {
            val value = absVal / 1_000.0
            String.format(Locale.US, "%.1fK", value).replace(".0K", "K")
        }
        else -> String.format(Locale.US, "%d", absVal)
    }
    return if (isNegative) "-$formatted" else formatted
}

fun formatCompactCoins(amount: Int): String = formatCompactCoins(amount.toLong())

fun formatCompactCoins(amount: Number): String = formatCompactCoins(amount.toLong())
