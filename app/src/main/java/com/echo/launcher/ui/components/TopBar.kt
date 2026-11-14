package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.FocusZone
import com.echo.launcher.data.LauncherTab
import com.echo.launcher.data.PowerInfo
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.StatusGreen
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.TextMuted
import com.echo.launcher.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopBar(
    selectedTab: LauncherTab,
    onTabSelected: (LauncherTab) -> Unit,
    currentFocusZone: FocusZone,
    powerInfo: PowerInfo,
    ramUsagePercent: Int = 42,
    onOptimizeClick: (() -> Unit)? = null,
    isDualScreenMode: Boolean = false,
    onToggleDualScreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf(getCurrentTimeString()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = getCurrentTimeString()
            delay(15000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Profile Badge + Geometric ECHO Wordmark + Tab Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UserProfileBadge()

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(SurfaceBorder)
            )

            EchoConsoleMark()

            ConsoleTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                isFocused = currentFocusZone == FocusZone.TOP_TABS
            )
        }

        // Right: 1-Tap Optimizer Button + RG DS Dual Screen Toggle + Controller + Network + Power + Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onOptimizeClick != null) {
                QuickBoostBadge(
                    ramUsagePercent = ramUsagePercent,
                    onClick = onOptimizeClick
                )
            }

            if (onToggleDualScreen != null) {
                DualScreenToggleBadge(
                    isDualScreenMode = isDualScreenMode,
                    onClick = onToggleDualScreen
                )
            }

            ControllerStatusBadge()

            NetworkSignalIcon()

            PowerIndicator(powerInfo = powerInfo)

            Text(
                text = currentTimeString,
                color = PlatinumWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun QuickBoostBadge(
    ramUsagePercent: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StatusGreen.copy(alpha = 0.16f))
            .border(1.dp, StatusGreen.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = "⚡ BOOST • ${ramUsagePercent}% RAM",
            color = StatusGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DualScreenToggleBadge(
    isDualScreenMode: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isDualScreenMode) ElectricCyan.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f)
    val borderColor = if (isDualScreenMode) ElectricCyan else SurfaceBorder
    val textColor = if (isDualScreenMode) ElectricCyan else TextMuted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isDualScreenMode) "⧉ RG DS • 2X" else "⧉ 1X SCREEN",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun UserProfileBadge() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceCard)
            .border(1.5.dp, ElectricCyan.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "P1",
            color = ElectricCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun EchoConsoleMark() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ECHO",
            color = PureWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.5.sp
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ElectricCyan.copy(alpha = 0.15f))
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "OS",
                color = ElectricCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun ConsoleTabSwitcher(
    selectedTab: LauncherTab,
    onTabSelected: (LauncherTab) -> Unit,
    isFocused: Boolean
) {
    val containerBorderColor by animateColorAsState(
        targetValue = if (isFocused) ElectricCyan else SurfaceBorder,
        animationSpec = tween(150),
        label = "TabFocusBorder"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceCard.copy(alpha = 0.88f))
            .border(1.2.dp, containerBorderColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BumperHint(text = "L1")
        Spacer(modifier = Modifier.width(6.dp))

        TabItem(
            title = "Games",
            isSelected = selectedTab == LauncherTab.GAMES,
            onClick = { onTabSelected(LauncherTab.GAMES) }
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .width(1.dp)
                .height(16.dp)
                .background(SurfaceBorder)
        )

        TabItem(
            title = "Media",
            isSelected = selectedTab == LauncherTab.MEDIA,
            onClick = { onTabSelected(LauncherTab.MEDIA) }
        )

        Spacer(modifier = Modifier.width(6.dp))
        BumperHint(text = "R1")
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) ElectricCyan.copy(alpha = 0.22f) else Color.Transparent
    val textColor = if (isSelected) ElectricCyan else TextMuted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BumperHint(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ControllerStatusBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(0.8.dp, SurfaceBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(StatusGreen)
        )
        Text(
            text = "PAD 1",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NetworkSignalIcon() {
    Canvas(modifier = Modifier.size(16.dp)) {
        val strokeW = 1.6.dp.toPx()
        val c = Offset(size.width / 2f, size.height * 0.82f)
        drawCircle(color = PlatinumWhite, radius = 1.6.dp.toPx(), center = c)
        drawArc(
            color = PlatinumWhite,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(size.width * 0.22f, size.height * 0.44f),
            size = Size(size.width * 0.56f, size.height * 0.56f),
            style = Stroke(width = strokeW)
        )
        drawArc(
            color = PlatinumWhite,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(size.width * 0.05f, size.height * 0.18f),
            size = Size(size.width * 0.90f, size.height * 0.90f),
            style = Stroke(width = strokeW)
        )
    }
}

@Composable
private fun PowerIndicator(powerInfo: PowerInfo) {
    if (powerInfo.isHomeConsolePower || powerInfo.isPluggedAC) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Canvas(modifier = Modifier.size(width = 12.dp, height = 14.dp)) {
                val bolt = Path().apply {
                    moveTo(size.width * 0.6f, 0f)
                    lineTo(size.width * 0.15f, size.height * 0.55f)
                    lineTo(size.width * 0.55f, size.height * 0.55f)
                    lineTo(size.width * 0.40f, size.height)
                    lineTo(size.width * 0.85f, size.height * 0.42f)
                    lineTo(size.width * 0.48f, size.height * 0.42f)
                    close()
                }
                drawPath(path = bolt, color = StatusGreen)
            }
            Text(
                text = "AC",
                color = StatusGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Canvas(modifier = Modifier.size(width = 22.dp, height = 12.dp)) {
                val bodyW = size.width - 3.dp.toPx()
                val bodyH = size.height
                drawRoundRect(
                    color = PlatinumWhite.copy(alpha = 0.8f),
                    topLeft = Offset.Zero,
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.3.dp.toPx())
                )
                val fillW = (bodyW - 4.dp.toPx()) * (powerInfo.percentage.coerceIn(0, 100) / 100f)
                if (fillW > 0) {
                    drawRoundRect(
                        color = ElectricCyan,
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(fillW, bodyH - 4.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }
            }
            Text(
                text = "${powerInfo.percentage}%",
                color = PlatinumWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
