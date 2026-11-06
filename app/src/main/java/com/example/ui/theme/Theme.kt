package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RoyalColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = RoyalNavyDark,
    primaryContainer = RoyalBlueSurface,
    onPrimaryContainer = RoyalGoldLight,
    secondary = RoyalGoldWarm,
    onSecondary = RoyalNavyDark,
    secondaryContainer = RoyalBlueCard,
    onSecondaryContainer = RoyalGoldLight,
    tertiary = RubyRed,
    onTertiary = TextLight,
    background = RoyalNavyDark,
    onBackground = TextLight,
    surface = RoyalNavy,
    onSurface = TextLight,
    surfaceVariant = RoyalBlueCard,
    onSurfaceVariant = TextMuted,
    outline = RoyalGoldDark,
    outlineVariant = RoyalMarbleBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = RoyalNavyDark.toArgb()
                window.navigationBarColor = RoyalNavyDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = RoyalColorScheme,
        typography = Typography,
        content = content
    )
}
