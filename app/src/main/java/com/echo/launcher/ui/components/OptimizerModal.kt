package com.echo.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.GamepadLayout
import com.echo.launcher.data.OptimizationResult
import com.echo.launcher.data.SystemTelemetry
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.ObsidianBackground
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.StatusGreen
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardElevated
import com.echo.launcher.ui.theme.TextMuted
import com.echo.launcher.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun OptimizerModal(
    isOpen: Boolean,
    telemetry: SystemTelemetry,
    lastResult: OptimizationResult?,
    isOptimizing: Boolean,
    autoBoostEnabled: Boolean,
    onToggleAutoBoost: (Boolean) -> Unit,
    gamepadLayout: GamepadLayout,
    onToggleGamepadLayout: () -> Unit,
    sortByRecent: Boolean,
    onToggleSortOrder: () -> Unit,
    hasUsageAccess: Boolean,
    onRequestUsageAccess: () -> Unit,
    onRunOptimization: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(180)),
        modifier = modifier.fillMaxSize()
    ) {
        val animatedRamProgress by animateFloatAsState(
            targetValue = (telemetry.ramUsagePercent / 100f).coerceIn(0.05f, 1f),
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            label = "RamProgress"
        )

        val animatedStorageProgress by animateFloatAsState(
            targetValue = (telemetry.storageUsagePercent / 100f).coerceIn(0.05f, 1f),
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            label = "StorageProgress"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBackground.copy(alpha = 0.92f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inside clicks
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricCyan.copy(alpha = 0.18f))
                                    .border(1.dp, ElectricCyan, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚡", fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    text = "ECHO SYSTEM & TURBO OPTIMIZER",
                                    color = PureWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "REAL-TIME MEMORY TELEMETRY, CACHE TRIM & CONSOLE CONTROLS",
                                    color = ElectricCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusGreen.copy(alpha = 0.16f))
                                .border(1.dp, StatusGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${telemetry.refreshRateHz}Hz DISPLAY",
                                color = StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Telemetry Gauges: RAM & Storage
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCardElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // RAM Usage Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SYSTEM RAM USAGE",
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${telemetry.usedRamMb} MB / ${telemetry.totalRamMb} MB (${telemetry.ramUsagePercent}%) • ${telemetry.freeRamMb} MB FREE",
                                    color = ElectricCyan,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedRamProgress)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Brush.horizontalGradient(colors = listOf(ElectricCyan, StatusGreen)))
                                )
                            }
                        }

                        // Internal Storage Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "INTERNAL FLASH STORAGE",
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.1f GB FREE / %.1f GB TOTAL (%d%%)",
                                        telemetry.freeStorageGb,
                                        telemetry.totalStorageGb,
                                        telemetry.storageUsagePercent
                                    ),
                                    color = PlatinumWhite,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedStorageProgress)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF7C4DFF))
                                )
                            }
                        }
                    }

                    // Optimization Status Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (lastResult != null) StatusGreen.copy(alpha = 0.12f)
                                else ElectricCyan.copy(alpha = 0.08f)
                            )
                            .border(
                                1.dp,
                                if (lastResult != null) StatusGreen.copy(alpha = 0.6f)
                                else ElectricCyan.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        if (isOptimizing) {
                            Text(
                                text = "⚡ TRIMMING CACHED APPS & MEASURING MEMORY...",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (lastResult != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "✓ MEMORY OPTIMIZATION COMPLETE",
                                    color = StatusGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = lastResult.statusMessage,
                                    color = PlatinumWhite,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                text = "Closes cached background apps and compacts ECHO OS heap. Reports real Android memory readings.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Console Quick Controls (2x2 Grid)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickSettingTile(
                                title = "Auto-Boost on Launch",
                                subtitle = "Stops other cached apps; keeps target intact",
                                badgeText = if (autoBoostEnabled) "ON" else "OFF",
                                badgeActive = autoBoostEnabled,
                                onClick = { onToggleAutoBoost(!autoBoostEnabled) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickSettingTile(
                                title = "Gamepad Confirm [Y]",
                                subtitle = "Swap A / B button layout",
                                badgeText = if (gamepadLayout == GamepadLayout.ABXY) "ABXY (A=OK)" else "BAYX (B=OK)",
                                badgeActive = true,
                                onClick = onToggleGamepadLayout,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickSettingTile(
                                title = "Library Order [X]",
                                subtitle = "Sort carousel & app drawer",
                                badgeText = if (sortByRecent) "Recent" else "A–Z",
                                badgeActive = true,
                                onClick = onToggleSortOrder,
                                modifier = Modifier.weight(1f)
                            )
                            QuickSettingTile(
                                title = "Playtime Tracking",
                                subtitle = "Real usage stats permission",
                                badgeText = if (hasUsageAccess) "✓ Active" else "Enable →",
                                badgeActive = hasUsageAccess,
                                onClick = onRequestUsageAccess,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElectricCyan)
                                .clickable(enabled = !isOptimizing, onClick = onRunOptimization)
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isOptimizing) "OPTIMIZING..." else "⚡ BOOST & OPTIMIZE NOW",
                                    color = Color.Black,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "A",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceCardElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                                .clickable(onClick = onDismiss)
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF334B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "B",
                                        color = PureWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text(
                                    text = "Done",
                                    color = PlatinumWhite,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSettingTile(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(if (badgeActive) ElectricCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                .border(
                    0.8.dp,
                    if (badgeActive) ElectricCyan else SurfaceBorder,
                    RoundedCornerShape(7.dp)
                )
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = badgeText,
                color = if (badgeActive) ElectricCyan else TextMuted,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
    }
}
