package com.echo.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = PureWhite,
    secondary = ElectricCyanDim,
    onSecondary = Color.Black,
    background = ObsidianBackground,
    onBackground = PureWhite,
    surface = SurfaceDark,
    onSurface = PureWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary
)

@Composable
fun EchoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
