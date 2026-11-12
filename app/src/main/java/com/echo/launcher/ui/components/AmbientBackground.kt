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
import androidx.compose.ui.unit.dp
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

        val centerGlow = Offset(x = size.width * 0.50f, y = size.height * 0.42f)
        val glowRadius = size.width * 0.60f

        // Dynamic radial ambient glow tracking focused game/emulator
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animatedAccentColor.copy(alpha = 0.34f),
                    animatedAccentColor.copy(alpha = 0.14f),
                    animatedAccentColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = centerGlow,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = centerGlow
        )

        // Handheld Cybernetic Grid Lines (subtle, non-distracting)
        val gridStep = 56.dp.toPx()
        val gridAlpha = 0.035f
        val gridColor = animatedAccentColor.copy(alpha = gridAlpha)

        // Subtle horizontal guide rails
        for (y in 0..(size.height / gridStep).toInt()) {
            val yPos = y * gridStep
            drawLine(
                color = gridColor,
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1f
            )
        }

        // Top specular wash
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    animatedAccentColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = size.height * 0.35f
            )
        )

        // Bottom cinematic vignette
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    ObsidianBackground.copy(alpha = 0.88f)
                ),
                startY = size.height * 0.68f,
                endY = size.height
            )
        )
    }
}
