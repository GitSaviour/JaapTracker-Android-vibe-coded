// In ui/theme/Theme.kt
package com.example.jaaptracker.ui.theme
import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// This uses the new colors you defined in Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    background = DarkBlue,
    surface = MidBlue,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    secondary = LightBlue,
    onSecondary = TextSecondary
)

@Composable
fun JaaptrackerTheme(
    content: @Composable () -> Unit
) {
    // We are forcing dark theme by default now
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}