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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.BatteryInfo
import com.echo.launcher.data.FocusZone
import com.echo.launcher.data.LauncherTab
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
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf(getCurrentTimeString()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = getCurrentTimeString()
            delay(10000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Profile Badge + Geometric ECHO Wordmark + Tab Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            UserProfileBadge()

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(SurfaceBorder)
            )

            EchoConsoleMark()

            // Console Tab Switcher [L1] Games | Media [R1]
            ConsoleTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                isFocused = currentFocusZone == FocusZone.TOP_TABS
            )
        }

        // Right: Controller Status + Wi-Fi + Battery + Digital Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connected Controller Badge
            ControllerStatusBadge()

            // Wi-Fi Signal Icon
            WifiSignalIcon()

            // Live Battery percentage
            BatteryIndicator(batteryInfo = batteryInfo)

            // Digital Clock
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
private fun UserProfileBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(1.2.dp, ElectricCyan.copy(alpha = 0.6f), CircleShape),
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.5.sp
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ElectricCyan.copy(alpha = 0.15f))
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "OS",
                color = ElectricCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
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
        animationSpec = tween(200),
        label = "TabFocusBorder"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(1.dp, containerBorderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BumperHint(text = "L1")
        Spacer(modifier = Modifier.width(4.dp))

        TabItem(
            title = "Games",
            isSelected = selectedTab == LauncherTab.GAMES,
            onClick = { onTabSelected(LauncherTab.GAMES) }
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .width(1.dp)
                .height(14.dp)
                .background(SurfaceBorder)
        )

        TabItem(
            title = "Media",
            isSelected = selectedTab == LauncherTab.MEDIA,
            onClick = { onTabSelected(LauncherTab.MEDIA) }
        )

        Spacer(modifier = Modifier.width(4.dp))
        BumperHint(text = "R1")
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ElectricCyan.copy(alpha = 0.20f) else Color.Transparent,
        animationSpec = tween(200),
        label = "TabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) ElectricCyan else TextMuted,
        animationSpec = tween(200),
        label = "TabText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
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
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun BumperHint(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.8.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
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
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        // Dot indicator
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
private fun WifiSignalIcon() {
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
private fun BatteryIndicator(batteryInfo: BatteryInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 24.dp, height = 13.dp)) {
            val bodyWidth = size.width - 3.dp.toPx()
            val bodyHeight = size.height
            val capWidth = 2.2.dp.toPx()
            val capHeight = bodyHeight * 0.45f

            val strokeColor = if (batteryInfo.isCharging) StatusGreen else PlatinumWhite.copy(alpha = 0.8f)

            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(0f, 0f),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = 1.3.dp.toPx())
            )

            drawRoundRect(
                color = strokeColor,
                topLeft = Offset(bodyWidth + 0.5.dp.toPx(), (bodyHeight - capHeight) / 2f),
                size = Size(capWidth, capHeight),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )

            val fillPadding = 2.dp.toPx()
            val maxFillWidth = bodyWidth - (fillPadding * 2)
            val fillWidth = (maxFillWidth * (batteryInfo.percentage.coerceIn(0, 100) / 100f))
            val fillColor = when {
                batteryInfo.isCharging -> StatusGreen
                batteryInfo.percentage <= 15 -> Color(0xFFFF334B)
                batteryInfo.percentage <= 30 -> Color(0xFFFFB300)
                else -> ElectricCyan
            }

            if (fillWidth > 0) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(fillPadding, fillPadding),
                    size = Size(fillWidth, bodyHeight - (fillPadding * 2)),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }

        Text(
            text = "${batteryInfo.percentage}%",
            color = if (batteryInfo.isCharging) StatusGreen else PlatinumWhite.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
