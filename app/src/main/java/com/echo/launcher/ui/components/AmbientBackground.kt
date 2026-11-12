package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.echo.launcher.ui.theme.ObsidianBackground

@Composable
fun AmbientBackground(
    targetAccentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedAccentColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "AmbientGlowTransition"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Base dark obsidian canvas
        drawRect(color = ObsidianBackground)

        val centerGlow = Offset(x = size.width * 0.50f, y = size.height * 0.40f)
        val glowRadius = size.width * 0.55f

        // PS5-inspired radial ambient glow that smoothly tracks focused item
        val ambientBrush = Brush.radialGradient(
            colors = listOf(
                animatedAccentColor.copy(alpha = 0.32f),
                animatedAccentColor.copy(alpha = 0.16f),
                animatedAccentColor.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = centerGlow,
            radius = glowRadius
        )

        drawCircle(
            brush = ambientBrush,
            radius = glowRadius,
            center = centerGlow
        )

        // Soft secondary top-light wash
        val topWashBrush = Brush.verticalGradient(
            colors = listOf(
                animatedAccentColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            startY = 0f,
            endY = size.height * 0.35f
        )
        drawRect(brush = topWashBrush)

        // Bottom cinematic shadow to frame the system dock
        val bottomVignette = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                ObsidianBackground.copy(alpha = 0.85f)
            ),
            startY = size.height * 0.70f,
            endY = size.height
        )
        drawRect(brush = bottomVignette)
    }
}
