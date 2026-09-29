package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LuxDarkColorScheme = darkColorScheme(
    primary = LuxPureWhite,
    onPrimary = LuxObsidian,
    primaryContainer = LuxElevatedSurface,
    onPrimaryContainer = LuxPureWhite,
    secondary = LuxSilver,
    onSecondary = LuxObsidian,
    secondaryContainer = LuxDarkSurface,
    onSecondaryContainer = LuxSilver,
    tertiary = LuxPlatinum,
    onTertiary = LuxObsidian,
    background = LuxObsidian,
    onBackground = LuxPureWhite,
    surface = LuxDarkSurface,
    onSurface = LuxPureWhite,
    surfaceVariant = LuxElevatedSurface,
    onSurfaceVariant = LuxTextSecondary,
    outline = LuxBorderSubtle,
    outlineVariant = LuxBorderGlow,
    error = LuxError,
    onError = LuxPureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // LUX is an ultra-premium black-and-white theme by default
    dynamicColor: Boolean = false, // Keep pure bespoke black-and-white palette
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = LuxObsidian.toArgb()
                it.navigationBarColor = LuxObsidian.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = LuxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
