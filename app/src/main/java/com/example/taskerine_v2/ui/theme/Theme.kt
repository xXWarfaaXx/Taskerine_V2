package com.example.taskerine_v2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFF5A623)
val OrangeDark = Color(0xFFE09000)
val CreamBackground = Color(0xFFFFF8F2)

private val TaskerineColorScheme = lightColorScheme(
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

@Composable
fun TaskerineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TaskerineColorScheme,
        typography = Typography,
        content = content
    )
}