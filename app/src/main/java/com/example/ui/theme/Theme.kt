package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BlackOutline,
    onPrimary = Color.White,
    secondary = PinkDrawerFill,
    onSecondary = DarkText,
    tertiary = LeagueHeadingBrightGreen,
    background = BackgroundCanvas,
    surface = CardBackground,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
