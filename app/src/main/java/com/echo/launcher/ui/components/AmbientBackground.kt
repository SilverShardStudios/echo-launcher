package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
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
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "AmbientGlowTransition"
    )

    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val centerGlow = Offset(x = size.width * 0.50f, y = size.height * 0.42f)
                val glowRadius = (size.width * 0.60f).coerceAtLeast(1f)

                val radialBrush = Brush.radialGradient(
                    colors = listOf(
                        animatedAccentColor.copy(alpha = 0.30f),
                        animatedAccentColor.copy(alpha = 0.11f),
                        Color.Transparent
                    ),
                    center = centerGlow,
                    radius = glowRadius
                )

                val topWash = Brush.verticalGradient(
                    colors = listOf(
                        animatedAccentColor.copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.32f
                )

                val bottomVignette = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        ObsidianBackground.copy(alpha = 0.90f)
                    ),
                    startY = size.height * 0.68f,
                    endY = size.height
                )

                onDrawBehind {
                    drawRect(color = ObsidianBackground)
                    drawCircle(
                        brush = radialBrush,
                        radius = glowRadius,
                        center = centerGlow
                    )
                    drawRect(brush = topWash)
                    drawRect(brush = bottomVignette)
                }
            }
    )
}
