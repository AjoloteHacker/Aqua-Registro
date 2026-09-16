package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AquaLightColorScheme = lightColorScheme(
    primary = AquaPrimary,
    onPrimary = AquaSurface,
    secondary = AquaSecondary,
    onSecondary = AquaSurface,
    tertiary = AquaTertiary,
    onTertiary = AquaTextPrimary,
    background = AquaBackground,
    onBackground = AquaTextPrimary,
    surface = AquaSurface,
    onSurface = AquaTextPrimary,
    surfaceVariant = AquaBackground,
    onSurfaceVariant = AquaTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = AquaLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION")
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AquaPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}