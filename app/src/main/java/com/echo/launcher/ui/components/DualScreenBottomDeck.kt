package com.echo.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.AppItem
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.ObsidianBackground
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.StatusGreen
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardElevated
import com.echo.launcher.ui.theme.TextMuted

@Composable
fun DualScreenBottomDeck(
    apps: List<AppItem>,
    selectedIndex: Int = 0,
    onSelectIndex: ((Int) -> Unit)? = null,
    onLaunch: (AppItem) -> Unit,
    onToggleAllApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onOptimizeDevice: (() -> Unit)? = null,
    ramUsagePercent: Int = 42,
    onToggleDualScreenMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var perfMode by remember { mutableStateOf("TURBO") }
    val deckListState = rememberLazyListState()
    val focusedApp = apps.getOrNull(selectedIndex)

    LaunchedEffect(selectedIndex) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            deckListState.animateScrollToItem(selectedIndex.coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Deck Header: Telemetry & Performance Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(StatusGreen)
                    )
                    Text(
                        text = "RG DS COMMAND DECK // RAM ${ramUsagePercent}%",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Performance Mode Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("ECO", "BALANCED", "TURBO").forEach { mode ->
                        val isSelected = perfMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan else Color.Transparent)
                                .clickable {
                                    perfMode = mode
                                    if (mode == "TURBO") {
                                        onOptimizeDevice?.invoke()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = mode,
                                color = if (isSelected) Color.Black else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Active Game Quick-Launch Banner on Bottom Touch Screen
            if (focusedApp != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardElevated)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { onLaunch(focusedApp) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (focusedApp.iconBitmap != null) {
                            Image(
                                bitmap = focusedApp.iconBitmap,
                                contentDescription = focusedApp.label,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column {
                            Text(
                                text = if (focusedApp.isFavorite) "★ ${focusedApp.label}" else focusedApp.label,
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${focusedApp.systemTag} • TAP TO LAUNCH ON UPPER SCREEN",
                                color = ElectricCyan,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "▶ LAUNCH",
                            color = Color.Black,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Touch App Matrix
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TOUCH LAUNCH MATRIX",
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                LazyRow(
                    state = deckListState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = apps,
                        key = { _, app -> "deck_${app.id}" },
                        contentType = { _, _ -> "deck_card" }
                    ) { idx, app ->
                        val isCurrent = idx == selectedIndex
                        QuickDeckAppCard(
                            app = app,
                            isSelected = isCurrent,
                            onClick = {
                                if (isCurrent || onSelectIndex == null) {
                                    onLaunch(app)
                                } else {
                                    onSelectIndex(idx)
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Quick Controls & Hardware Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onOptimizeDevice != null) {
                        DeckActionPill(
                            label = "Boost RAM",
                            icon = "⚡",
                            accentColor = StatusGreen,
                            onClick = onOptimizeDevice
                        )
                    }
                    DeckActionPill(
                        label = "All Apps",
                        icon = "▦",
                        onClick = onToggleAllApps
                    )
                    DeckActionPill(
                        label = "Settings",
                        icon = "⚙",
                        onClick = onOpenSettings
                    )
                    if (onToggleDualScreenMode != null) {
                        DeckActionPill(
                            label = "1x / 2x",
                            icon = "⧉",
                            accentColor = ElectricCyan,
                            onClick = onToggleDualScreenMode
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeckIconAction(
                        icon = "🔊",
                        onClick = {
                            val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                            audio?.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
                        }
                    )
                    DeckIconAction(
                        icon = "📶",
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (_: Exception) {}
                        }
                    )
                    DeckIconAction(
                        icon = "⚡",
                        onClick = {
                            if (onOptimizeDevice != null) {
                                onOptimizeDevice()
                            } else {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDeckAppCard(
    app: AppItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderCol = if (isSelected) ElectricCyan else SurfaceBorder
    val bgCol = if (isSelected) SurfaceCardElevated else SurfaceCard

    Column(
        modifier = Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgCol)
            .border(if (isSelected) 1.8.dp else 1.dp, borderCol, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(app.accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(2).uppercase(),
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (app.isFavorite) "★ ${app.label}" else app.label,
            color = if (isSelected) ElectricCyan else PlatinumWhite,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DeckActionPill(
    label: String,
    icon: String,
    accentColor: Color = PureWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = icon, fontSize = 11.sp, color = accentColor)
        Text(
            text = label,
            color = PlatinumWhite,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DeckIconAction(
    icon: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceCardElevated)
            .border(1.dp, SurfaceBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 12.sp)
    }
}
