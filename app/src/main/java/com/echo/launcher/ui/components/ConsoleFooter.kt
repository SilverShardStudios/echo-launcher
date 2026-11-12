package com.echo.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.DockAction
import com.echo.launcher.data.FocusZone
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.TextMuted

@Composable
fun ConsoleFooter(
    selectedActionIndex: Int,
    onActionSelected: (Int) -> Unit,
    onActionTriggered: (DockAction) -> Unit,
    currentFocusZone: FocusZone,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Quick-Action System Dock
        SystemDock(
            selectedActionIndex = selectedActionIndex,
            onActionSelected = onActionSelected,
            onActionTriggered = onActionTriggered,
            currentFocusZone = currentFocusZone
        )

        // Right: Controller Button Legend Guide
        ConsoleButtonGuide()
    }
}

@Composable
fun ConsoleButtonGuide() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ControllerButtonHint(badge = "A", color = Color(0xFF00E5FF), label = "Play")
        ControllerButtonHint(badge = "X", color = Color(0xFF3B82F6), label = "All Apps")
        ControllerButtonHint(badge = "Y", color = Color(0xFFEAB308), label = "Options")
        ControllerButtonHint(badge = "L1/R1", color = Color.White.copy(alpha = 0.85f), isPill = true, label = "Switch Tab")
    }
}

@Composable
private fun ControllerButtonHint(
    badge: String,
    color: Color,
    label: String,
    isPill: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isPill) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 1.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = color,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f))
                    .border(1.dp, color.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = color,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
