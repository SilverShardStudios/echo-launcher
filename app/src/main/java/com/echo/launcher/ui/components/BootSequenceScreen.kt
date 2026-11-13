package com.echo.launcher.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun BootSequenceScreen(
    onBootFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bootTelemetry by remember { mutableStateOf("INITIALIZING ECHO CONSOLE BIOS...") }

    val infiniteTransition = rememberInfiniteTransition(label = "BootPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        // Synthesize home console startup chime
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            delay(160)
            toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 220)
            delay(260)
            toneGen.release()
        } catch (_: Exception) {}

        bootTelemetry = "HDMI DISPLAY OUTPUT DETECTED // 60Hz SYNC..."
        delay(600)
        bootTelemetry = "GAMEPAD CONTROLLER BUS INITIALIZED // PAD 1 ACTIVE"
        delay(800)
        bootTelemetry = "ALL SYSTEMS NOMINAL // WELCOME TO ECHO OS"
        delay(800)
        onBootFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBootFinished // Press any button or tap to skip
            ),
        contentAlignment = Alignment.Center
    ) {
        // Cybernetic background grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 56.dp.toPx()
            for (x in 0..(size.width / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0x0C00E5FF),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0x0C00E5FF),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(size.width, y * gridSpacing),
                    strokeWidth = 1f
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    radius = size.minDimension * 0.55f,
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
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.45f),
                        radius = (size.minDimension / 2.2f) * pulseScale,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = ElectricCyan,
                        radius = size.minDimension / 2.8f,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                    drawCircle(
                        color = PureWhite,
                        radius = size.minDimension / 8f
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Typographic Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ECHO",
                    color = PureWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElectricCyan)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CONSOLE",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Console Telemetry
            Text(
                text = bootTelemetry,
                color = ElectricCyan.copy(alpha = 0.90f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Skip Hint
            Text(
                text = "PRESS (A) OR ANY BUTTON TO START",
                color = TextMuted.copy(alpha = 0.65f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }
    }
}
