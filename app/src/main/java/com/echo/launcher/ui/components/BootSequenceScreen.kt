package com.echo.launcher.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.ObsidianBackground
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun BootSequenceScreen(
    onBootFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bootStage by remember { mutableStateOf(0) }
    var bootTelemetry by remember { mutableStateOf("INITIALIZING ECHO KERNEL...") }

    val infiniteTransition = rememberInfiniteTransition(label = "BootPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        // Synthesize futuristic console boot chime
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            delay(150)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            delay(250)
            toneGen.release()
        } catch (_: Exception) {}

        bootTelemetry = "DETECTING HARDWARE // ANBERNIC & CONTROLLER BUS..."
        delay(600)
        bootStage = 1
        bootTelemetry = "SYSTEMS NOMINAL // 60 FPS COMPOSE PIPELINE READY"
        delay(900)
        bootStage = 2
        delay(900)
        onBootFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBootFinished // Tap to skip
            ),
        contentAlignment = Alignment.Center
    ) {
        // Cybernetic background grid & radar line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 48.dp.toPx()
            for (x in 0..(size.width / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0x0A00E5FF),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0x0A00E5FF),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(size.width, y * gridSpacing),
                    strokeWidth = 1f
                )
            }

            // Radial boot glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    radius = size.minDimension * 0.5f,
                    center = center
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Holographic Console Logo Core
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer ring
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.4f),
                        radius = (size.minDimension / 2.2f) * pulseScale,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Middle ring
                    drawCircle(
                        color = ElectricCyan,
                        radius = size.minDimension / 2.8f,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Core point
                    drawCircle(
                        color = PureWhite,
                        radius = size.minDimension / 8f
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Typographic Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ECHO",
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElectricCyan)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OS",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Telemetry line
            Text(
                text = bootTelemetry,
                color = ElectricCyan.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Skip Hint
            Text(
                text = "PRESS ANY BUTTON TO SKIP",
                color = TextMuted.copy(alpha = 0.6f),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }
    }
}
