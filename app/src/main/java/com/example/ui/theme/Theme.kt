package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RoyalDarkColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = RoyalNavyDark,
    primaryContainer = RoyalBlueSurface,
    onPrimaryContainer = RoyalGoldLight,
    secondary = RoyalGoldWarm,
    onSecondary = RoyalNavyDark,
    secondaryContainer = RoyalBlueCard,
    onSecondaryContainer = RoyalGoldLight,
    tertiary = LudoClubCyanPill,
    onTertiary = RoyalNavyDark,
    background = RoyalNavyDark,
    onBackground = TextLight,
    surface = RoyalNavy,
    onSurface = TextLight,
    surfaceVariant = RoyalBlueCard,
    onSurfaceVariant = TextMuted,
    outline = RoyalGoldDark,
    outlineVariant = RoyalMarbleBorder,
    error = LudoClubRedBadge,
    onError = TextLight
)

private val RoyalLightColorScheme = lightColorScheme(
    primary = RoyalLightPrimary,
    onPrimary = TextLight,
    primaryContainer = RoyalLightPrimaryContainer,
    onPrimaryContainer = RoyalNavyDark,
    secondary = RoyalLightSecondary,
    onSecondary = TextLight,
    secondaryContainer = RoyalLightSurfaceVariant,
    onSecondaryContainer = RoyalLightTextPrimary,
    tertiary = SapphireBlue,
    onTertiary = TextLight,
    background = RoyalLightBackground,
    onBackground = RoyalLightTextPrimary,
    surface = RoyalLightSurface,
    onSurface = RoyalLightTextPrimary,
    surfaceVariant = RoyalLightSurfaceVariant,
    onSurfaceVariant = RoyalLightTextMuted,
    outline = RoyalLightBorder,
    outlineVariant = RoyalMarbleShadow,
    error = RubyRed,
    onError = TextLight
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
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val colorScheme = if (darkTheme) RoyalDarkColorScheme else RoyalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
