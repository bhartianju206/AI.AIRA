package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AiraDarkColorScheme = darkColorScheme(
    primary = AiraCyan,
    onPrimary = DarkBackground,
    primaryContainer = AiraCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = AiraPurple,
    onSecondary = TextPrimary,
    secondaryContainer = AiraPurpleDark,
    onSecondaryContainer = TextPrimary,
    tertiary = AiraGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = AiraRed,
    onError = TextPrimary
)

@Composable
fun AiraTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AiraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
