package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.DockAction
import com.echo.launcher.data.FocusZone
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardBorder
import com.echo.launcher.ui.theme.TextMuted

@Composable
fun SystemDock(
    selectedActionIndex: Int,
    onActionSelected: (Int) -> Unit,
    onActionTriggered: (DockAction) -> Unit,
    currentFocusZone: FocusZone,
    modifier: Modifier = Modifier
) {
    val isDockFocused = currentFocusZone == FocusZone.DOCK

    val actions = listOf(
        DockAction.ALL_APPS to "All Apps",
        DockAction.ECHO_UPDATES to "ECHO Updates",
        DockAction.WIFI_BT to "Wi-Fi & BT",
        DockAction.SETTINGS to "Settings"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceCard.copy(alpha = 0.65f))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEachIndexed { index, (action, label) ->
            val isSelected = index == selectedActionIndex
            DockItemButton(
                action = action,
                label = label,
                isSelected = isSelected,
                isDockFocused = isDockFocused,
                onClick = {
                    onActionSelected(index)
                    onActionTriggered(action)
                }
            )
        }
    }
}

@Composable
private fun DockItemButton(
    action: DockAction,
    label: String,
    isSelected: Boolean,
    isDockFocused: Boolean,
    onClick: () -> Unit
) {
    val isHighlighted = isSelected && isDockFocused
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.08f else 1.0f,
        animationSpec = tween(180),
        label = "DockButtonScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> ElectricCyan
            isSelected -> ElectricCyan.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(180),
        label = "DockButtonBorder"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> ElectricCyan.copy(alpha = 0.22f)
            isSelected -> Color.White.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        animationSpec = tween(180),
        label = "DockButtonBg"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .shadow(
                elevation = if (isHighlighted) 10.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ElectricCyan
            )
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Custom Geometric Icon for each action
        DockIconCanvas(
            action = action,
            tint = if (isHighlighted) ElectricCyan else if (isSelected) PureWhite else TextMuted
        )

        Text(
            text = label,
            color = if (isHighlighted) ElectricCyan else if (isSelected) PureWhite else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (isHighlighted || isSelected) FontWeight.Bold else FontWeight.Medium
        )

        if (isHighlighted) {
            // Controller selection hint [A]
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.Black,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun DockIconCanvas(action: DockAction, tint: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        when (action) {
            DockAction.ALL_APPS -> {
                // 4 rounded tiles grid
                val tileSize = size.width * 0.38f
                val spacing = size.width * 0.24f
                val cr = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                drawRoundRect(color = tint, topLeft = Offset(0f, 0f), size = Size(tileSize, tileSize), cornerRadius = cr)
                drawRoundRect(color = tint, topLeft = Offset(tileSize + spacing, 0f), size = Size(tileSize, tileSize), cornerRadius = cr)
                drawRoundRect(color = tint, topLeft = Offset(0f, tileSize + spacing), size = Size(tileSize, tileSize), cornerRadius = cr)
                drawRoundRect(color = tint, topLeft = Offset(tileSize + spacing, tileSize + spacing), size = Size(tileSize, tileSize), cornerRadius = cr)
            }
            DockAction.ECHO_UPDATES -> {
                // Circle with download down-arrow
                drawCircle(color = tint, radius = size.width / 2.2f, style = Stroke(width = 1.4.dp.toPx()))
                val arrowPath = Path().apply {
                    moveTo(size.width / 2f, size.height * 0.30f)
                    lineTo(size.width / 2f, size.height * 0.65f)
                    moveTo(size.width * 0.35f, size.height * 0.50f)
                    lineTo(size.width / 2f, size.height * 0.68f)
                    lineTo(size.width * 0.65f, size.height * 0.50f)
                }
                drawPath(path = arrowPath, color = tint, style = Stroke(width = 1.6.dp.toPx()))
            }
            DockAction.WIFI_BT -> {
                // Wi-Fi signal arcs
                val center = Offset(size.width / 2f, size.height * 0.78f)
                drawCircle(color = tint, radius = 2.dp.toPx(), center = center)
                drawArc(
                    color = tint,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.42f),
                    size = Size(size.width * 0.56f, size.height * 0.56f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawArc(
                    color = tint,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.05f, size.height * 0.18f),
                    size = Size(size.width * 0.90f, size.height * 0.90f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            DockAction.SETTINGS -> {
                // Cogwheel / gear
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = tint, radius = size.width * 0.26f, center = c, style = Stroke(width = 2.2.dp.toPx()))
                // 4 cog spokes
                val spokeLen = size.width * 0.46f
                val strokeW = 2.dp.toPx()
                drawLine(color = tint, start = Offset(c.x, c.y - spokeLen), end = Offset(c.x, c.y + spokeLen), strokeWidth = strokeW)
                drawLine(color = tint, start = Offset(c.x - spokeLen, c.y), end = Offset(c.x + spokeLen, c.y), strokeWidth = strokeW)
                val diag = spokeLen * 0.707f
                drawLine(color = tint, start = Offset(c.x - diag, c.y - diag), end = Offset(c.x + diag, c.y + diag), strokeWidth = strokeW)
                drawLine(color = tint, start = Offset(c.x + diag, c.y - diag), end = Offset(c.x - diag, c.y + diag), strokeWidth = strokeW)
                // Center hole
                drawCircle(color = SurfaceCard, radius = size.width * 0.14f, center = c)
            }
        }
    }
}
