package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AiraDarkColorScheme = darkColorScheme(
    primary = AiraCyan,
    onPrimary = DarkBackground,
    primaryContainer = AiraCyanDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = AiraPurple,
    onSecondary = DarkTextPrimary,
    secondaryContainer = AiraPurpleDark,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = AiraGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = AiraRed,
    onError = DarkTextPrimary
)

private val AiraLightColorScheme = lightColorScheme(
    primary = AiraPurple,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = AiraPurpleDark,
    secondary = AiraPink,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = AiraGreen,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = AiraRed,
    onError = LightSurface
)

@Composable
fun AiraTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) AiraDarkColorScheme else AiraLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val bg = if (isDarkTheme) DarkBackground else LightBackground
                window.statusBarColor = bg.toArgb()
                window.navigationBarColor = bg.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDarkTheme
                controller.isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
