package com.example.taskerine_v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFF5A623)
val OrangeDark = Color(0xFFE09000)
val CreamBackground = Color(0xFFFFF8F2)

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    background = CreamBackground,
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF888888),
    outline = OrangePrimary
)

// New dark scheme — keeps the same amber/orange accent so the app still
// feels like Taskerine, just on a dark surface instead of cream/white.
private val DarkColors = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color(0xFF1C1B1F),
    primaryContainer = OrangeDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = OrangePrimary
)

@Composable
fun TaskerineTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}