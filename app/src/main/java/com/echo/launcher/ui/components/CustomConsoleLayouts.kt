package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.AppItem
import com.echo.launcher.data.DockAction
import com.echo.launcher.data.FocusZone
import com.echo.launcher.data.GameStats
import com.echo.launcher.data.GamepadLayout
import com.echo.launcher.data.LauncherTab
import com.echo.launcher.data.PowerInfo
import com.echo.launcher.data.formatDate
import com.echo.launcher.data.formatPlaytime
import com.echo.launcher.data.formatRelativeTime
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.StatusGreen
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardElevated
import com.echo.launcher.ui.theme.TextMuted
import com.echo.launcher.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================================================
 * 1. HORIZON 5 NEXT-GEN LIVING-ROOM CONSOLE UI (FULL-SCREEN REPLACEMENT)
 * Next-generation living-room console architecture:
 * - Top Header: "Games" | "Media" text switcher on left; UI Hub, RAM Boost, 1X/2X toggle,
 *   Search, Settings gear, Profile Avatar (P1), and live HH:mm clock on right.
 * - Top-Left Media Ribbon: Rounded-square app icons (46dp) where the active item expands to
 *   66dp with crisp white outline + amber glow and inline title beside it.
 * - Full-Screen Atmospheric Backdrop: Royal-blue / indigo radial backdrop with drifting amber
 *   particle flare + dominant color bleed from the selected game icon.
 * - Bottom-Left Hero Stage: Monochrome white platform tag pill, large clean hero title,
 *   frosted white "Play Game" button, circular "..." options trigger, and Control Center toggle.
 * - Bottom-Right Real Telemetry Cards:
 *   Card 1: Real Playtime (30 days) & Last Played from Android UsageStatsManager (or prompt to enable).
 *   Card 2: Software Details (Install Date, App Version, and Recent / A-Z Sort toggle).
 * - Bottom Pop-Up Control Center Bar: Controller-focusable dock bar for quick actions.
 * ============================================================================================
 */
@Composable
fun NextGenHorizonLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
    selectedTab: LauncherTab,
    onTabSelected: (LauncherTab) -> Unit,
    gameStats: Map<String, GameStats>,
    hasUsageAccess: Boolean,
    onRequestUsageAccess: () -> Unit,
    sortByRecent: Boolean,
    onToggleSort: () -> Unit,
    showControlCenter: Boolean,
    onToggleControlCenter: () -> Unit,
    controlCenterIndex: Int,
    onControlCenterAction: (Int) -> Unit,
    currentFocusZone: FocusZone,
    gamepadLayout: GamepadLayout,
    powerInfo: PowerInfo,
    ramUsagePercent: Int,
    isDualScreenMode: Boolean,
    onToggleDualScreen: () -> Unit,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    onOpenUiHub: () -> Unit,
    onOptimize: () -> Unit,
    onAllApps: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeApp = apps.getOrNull(selectedIndex)
    val activeStats = activeApp?.let { gameStats[it.packageName] }
    val confirmBtn = if (gamepadLayout == GamepadLayout.ABXY) "A" else "B"

    val accent = activeApp?.accentColor ?: Color(0xFF2563EB)
    val animatedAccent by animateColorAsState(
        targetValue = accent,
        animationSpec = tween(450),
        label = "Horizon5Accent"
    )

    var currentTime by remember { mutableStateOf(formatTime("HH:mm")) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatTime("HH:mm")
            delay(15000)
        }
    }

    val particleTransition = rememberInfiniteTransition(label = "HorizonParticles")
    val particleDrift by particleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ParticleDrift"
    )

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
        }
    }

    val isControlCenterVisible = showControlCenter || currentFocusZone == FocusZone.DOCK

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val mainRadial = Brush.radialGradient(
                    colors = listOf(
                        animatedAccent.copy(alpha = 0.48f),
                        Color(0xFF0A1532).copy(alpha = 0.92f),
                        Color(0xFF03060E)
                    ),
                    center = Offset(size.width * 0.68f, size.height * 0.42f),
                    radius = (size.width * 0.85f).coerceAtLeast(1f)
                )
                val goldenArcGlow = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.22f + particleDrift * 0.08f),
                        Color(0xFFD97706).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * (0.18f + particleDrift * 0.08f), size.height * 0.14f),
                    radius = (size.width * 0.48f).coerceAtLeast(1f)
                )
                val bottomVignette = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF020409).copy(alpha = 0.88f)
                    ),
                    startY = size.height * 0.50f,
                    endY = size.height
                )
                onDrawBehind {
                    drawRect(color = Color(0xFF03060E))
                    drawRect(brush = mainRadial)
                    drawRect(brush = goldenArcGlow)
                    drawRect(brush = bottomVignette)
                }
            }
            .padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER + MEDIA RIBBON
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Games | Media text tabs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Text(
                            text = "Games",
                            color = if (selectedTab == LauncherTab.GAMES) PureWhite else PureWhite.copy(alpha = 0.45f),
                            fontSize = 19.sp,
                            fontWeight = if (selectedTab == LauncherTab.GAMES) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.clickable { onTabSelected(LauncherTab.GAMES) }
                        )
                        Text(
                            text = "Media",
                            color = if (selectedTab == LauncherTab.MEDIA) PureWhite else PureWhite.copy(alpha = 0.45f),
                            fontSize = 19.sp,
                            fontWeight = if (selectedTab == LauncherTab.MEDIA) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.clickable { onTabSelected(LauncherTab.MEDIA) }
                        )
                    }

                    // Right: UI Hub, Boost RAM, Dual Screen, Search, Settings, Profile, Clock
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                                .clickable(onClick = onOpenUiHub)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "❖ UI Hub",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .clickable(onClick = onOptimize)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ ${ramUsagePercent}% RAM",
                                color = StatusGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDualScreenMode) ElectricCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                                .border(1.dp, if (isDualScreenMode) ElectricCyan else Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                                .clickable(onClick = onToggleDualScreen)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDualScreenMode) "2X DS" else "1X",
                                color = if (isDualScreenMode) ElectricCyan else PureWhite.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "⌕",
                            color = PureWhite.copy(alpha = 0.85f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onAllApps)
                        )

                        Text(
                            text = "⚙",
                            color = PureWhite.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            modifier = Modifier.clickable(onClick = onOpenSettings)
                        )

                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))))
                                    .border(1.2.dp, PureWhite.copy(alpha = 0.8f), CircleShape)
                                    .clickable(onClick = onToggleControlCenter),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "P1", color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen)
                                    .border(1.dp, Color.Black, CircleShape)
                            )
                        }

                        Text(
                            text = currentTime,
                            color = PureWhite.copy(alpha = 0.92f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // TOP-LEFT MINI SQUIRCLE MEDIA BAR RIBBON
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item(key = "horizon_hub_tile") {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenUiHub),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❖", color = Color(0xFFF59E0B), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    itemsIndexed(apps, key = { _, app -> "h5_${app.id}" }) { index, app ->
                        val isSelected = index == selectedIndex
                        val boxSize = if (isSelected) 66.dp else 46.dp

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(boxSize)
                                    .shadow(
                                        elevation = if (isSelected) 14.dp else 0.dp,
                                        shape = RoundedCornerShape(if (isSelected) 16.dp else 12.dp),
                                        spotColor = Color(0xFFF59E0B)
                                    )
                                    .clip(RoundedCornerShape(if (isSelected) 16.dp else 12.dp))
                                    .background(Color(0xFF111827))
                                    .border(
                                        width = if (isSelected) 2.2.dp else 1.dp,
                                        color = if (isSelected) PureWhite else PureWhite.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(if (isSelected) 16.dp else 12.dp)
                                    )
                                    .clickable {
                                        if (isSelected) onLaunch(app) else onIndexSelected(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (app.iconBitmap != null) {
                                    Image(
                                        bitmap = app.iconBitmap,
                                        contentDescription = app.label,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(if (isSelected) 4.dp else 3.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = app.label.take(2).uppercase(),
                                        color = PureWhite,
                                        fontSize = if (isSelected) 16.sp else 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isSelected) {
                                Text(
                                    text = if (app.isFavorite) "★ ${app.label}" else app.label,
                                    color = PureWhite,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }
                    }

                    item(key = "horizon_library_tile") {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onAllApps),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▦", color = PureWhite, fontSize = 17.sp)
                        }
                    }
                }
            }

            // 2. LOWER CINEMATIC HERO SECTION + REAL TELEMETRY CARDS
            if (activeApp != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Bottom-Left Hero Stage
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(PureWhite)
                                        .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = activeApp.systemTag,
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                                Text(
                                    text = if (activeApp.isEmulator) "Full Emulator Suite"
                                    else if (activeApp.isMedia) "Media Application"
                                    else "Full Game",
                                    color = PureWhite.copy(alpha = 0.78f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (activeApp.isFavorite) {
                                    Text(
                                        text = "• ★ Favorite",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = activeApp.label,
                                color = PureWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                val isHeroPlayFocused = currentFocusZone != FocusZone.DOCK
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(PureWhite.copy(alpha = 0.94f))
                                        .border(
                                            width = if (isHeroPlayFocused) 2.2.dp else 1.dp,
                                            color = if (isHeroPlayFocused) PureWhite else Color.Transparent,
                                            shape = RoundedCornerShape(28.dp)
                                        )
                                        .clickable { onLaunch(activeApp) }
                                        .padding(horizontal = 32.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Play Game",
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = confirmBtn,
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.14f))
                                        .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                                        .clickable { onOptions(activeApp) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⋯",
                                        color = PureWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isControlCenterVisible) Color(0xFFF59E0B).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f))
                                        .border(1.dp, if (isControlCenterVisible) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.24f), CircleShape)
                                        .clickable(onClick = onToggleControlCenter),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⊞",
                                        color = if (isControlCenterVisible) Color(0xFFF59E0B) else PureWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Bottom-Right Real Data Cards (Playtime & Software Details)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Card 1: Real Playtime & Last Played (or Usage Access prompt)
                            Box(
                                modifier = Modifier
                                    .width(185.dp)
                                    .height(106.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xCC111827))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (hasUsageAccess) onLaunch(activeApp) else onRequestUsageAccess()
                                    }
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (activeApp.iconBitmap != null) {
                                            Image(
                                                bitmap = activeApp.iconBitmap,
                                                contentDescription = activeApp.label,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = if (hasUsageAccess) "ACTIVITY & PLAYTIME" else "PLAYTIME STATS",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.4.sp
                                            )
                                            Text(
                                                text = if (hasUsageAccess) "30-Day Tracker" else "Permission Needed",
                                                color = PureWhite,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (hasUsageAccess) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "PLAYED ${formatPlaytime(activeStats?.playtimeMs30d ?: 0L)}",
                                                color = StatusGreen,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "Last played: ${formatRelativeTime(activeStats?.lastPlayedMs ?: 0L)}",
                                                color = TextSecondary,
                                                fontSize = 9.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "Tap to enable Usage Access",
                                                color = ElectricCyan,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Unlocks real playtime & recent sort",
                                                color = TextMuted,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // Card 2: Software Details & Library Sort Switcher
                            Box(
                                modifier = Modifier
                                    .width(195.dp)
                                    .height(106.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xCC111827))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                    .clickable(onClick = onToggleSort)
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SOFTWARE INFO",
                                            color = ElectricCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ElectricCyan.copy(alpha = 0.16f))
                                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = if (sortByRecent) "🕒 Recent" else "A–Z",
                                                color = ElectricCyan,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = "Version: ${activeStats?.versionName?.let { "v$it" } ?: "—"}",
                                            color = PureWhite,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Installed: ${formatDate(activeStats?.installedMs ?: 0L)}",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = activeApp.packageName,
                                            color = TextMuted,
                                            fontSize = 8.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Control Center Bar (Controller-Focusable)
                    if (isControlCenterVisible) {
                        val items = listOf(
                            "🏠" to "Home",
                            "▦" to "All Apps",
                            "⚡" to "Boost RAM",
                            "❖" to "UI Hub",
                            "⧉" to if (isDualScreenMode) "2X DS" else "1X",
                            "⚙" to "System"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xE60F172A))
                                .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEachIndexed { idx, (icon, label) ->
                                val isFocusedItem = currentFocusZone == FocusZone.DOCK && controlCenterIndex == idx
                                ControlCenterItem(
                                    icon = icon,
                                    label = label,
                                    isFocused = isFocusedItem,
                                    onClick = { onControlCenterAction(idx) }
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
private fun ControlCenterItem(
    icon: String,
    label: String,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.08f))
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = if (isFocused) PureWhite else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 13.sp, color = if (isFocused) Color.Black else PureWhite)
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isFocused) Color.Black else PureWhite,
            fontWeight = if (isFocused) FontWeight.Black else FontWeight.SemiBold
        )
    }
}

/**
 * ============================================================================================
 * 2. N-HYBRID PORTABLE DECK UI (FULL-SCREEN REPLACEMENT)
 * Authentic hybrid-handheld home screen:
 * - Instant "Basic Black" (#2B2D31) / "Basic White" (#EBEBEB) theme toggle + Sort pill.
 * - Top Status Bar: Player avatar (P1), Theme toggle, Recent/A-Z sort pill on left;
 *   UI Hub, 1X/2X DS toggle, live clock, signal & battery % on right.
 * - Center Stage: Floating cyan cartridge title + subtle playtime/tag subtitle above
 *   large 148dp square game tiles with pulsing cyan outer focus ring + end-of-row All Software circle.
 * - Lower Dock: 6 circular system action bubbles with floating active label + full-width bottom rule.
 * - Footer: Dual-Tone Controller grip icon + RAM badge on left; (+) Options / Confirm Start on right.
 * ============================================================================================
 */
@Composable
fun HybridPortableDeckLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
    gameStats: Map<String, GameStats>,
    hasUsageAccess: Boolean,
    sortByRecent: Boolean,
    onToggleSort: () -> Unit,
    gamepadLayout: GamepadLayout,
    powerInfo: PowerInfo,
    ramUsagePercent: Int,
    isLightTheme: Boolean,
    onToggleLightTheme: () -> Unit,
    isDualScreenMode: Boolean,
    onToggleDualScreen: () -> Unit,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    selectedDockIndex: Int,
    currentFocusZone: FocusZone,
    onDockActionSelected: (Int) -> Unit,
    onDockActionTriggered: (DockAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeApp = apps.getOrNull(selectedIndex)
    val activeStats = activeApp?.let { gameStats[it.packageName] }
    val confirmBtn = if (gamepadLayout == GamepadLayout.ABXY) "A" else "B"

    var currentTime by remember { mutableStateOf(formatTime("HH:mm")) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatTime("HH:mm")
            delay(15000)
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "HybridFocusRing")
    val ringAlpha by pulseTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RingAlpha"
    )

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
        }
    }

    val bgCol = if (isLightTheme) Color(0xFFEBEBEB) else Color(0xFF2B2D31)
    val primaryTextCol = if (isLightTheme) Color(0xFF222222) else PureWhite
    val secondaryTextCol = if (isLightTheme) Color(0xFF52525B) else Color(0xFF94A3B8)
    val titleCyanCol = if (isLightTheme) Color(0xFF0097A7) else Color(0xFF00C3E3)
    val circleBgCol = if (isLightTheme) PureWhite else Color(0xFF3A3D42)
    val circleBorderCol = if (isLightTheme) Color.Black.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.16f)
    val dividerCol = if (isLightTheme) Color.Black.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.22f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgCol)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP STATUS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4554))
                        .border(2.dp, PureWhite, CircleShape)
                        .clickable { onDockActionTriggered(DockAction.UI_HUB) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P1",
                        color = PureWhite,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(circleBgCol)
                        .border(1.dp, circleBorderCol, RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleLightTheme)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLightTheme) "☀ Basic White" else "☾ Basic Black",
                        color = primaryTextCol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(circleBgCol)
                        .border(1.dp, titleCyanCol.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleSort)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (sortByRecent) "🕒 Recent" else "A–Z",
                        color = titleCyanCol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFF4554).copy(alpha = 0.16f))
                        .border(1.dp, Color(0xFFFF4554), RoundedCornerShape(14.dp))
                        .clickable { onDockActionTriggered(DockAction.UI_HUB) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "❖ UI Hub",
                        color = Color(0xFFFF4554),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(circleBgCol)
                        .border(1.dp, circleBorderCol, RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleDualScreen)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDualScreenMode) "⧉ 2X DS" else "⧉ 1X",
                        color = titleCyanCol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = currentTime,
                    color = primaryTextCol,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "📶",
                    fontSize = 12.5.sp,
                    color = primaryTextCol
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${powerInfo.percentage} %",
                        color = primaryTextCol,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(12.dp)
                            .border(1.4.dp, primaryTextCol, RoundedCornerShape(2.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((powerInfo.percentage / 100f).coerceIn(0.15f, 1f))
                                .fillMaxHeight()
                                .background(if (powerInfo.isPluggedAC) StatusGreen else primaryTextCol)
                        )
                    }
                }
            }
        }

        // 2. CENTER CARTRIDGE TITLE + SQUARE GAME TILES ROW
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (activeApp != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 17.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(titleCyanCol),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 8.dp, height = 7.dp)
                                .background(bgCol)
                        )
                    }
                    Text(
                        text = if (activeApp.isFavorite) "★ ${activeApp.label}" else activeApp.label,
                        color = titleCyanCol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val playtimeMs = activeStats?.playtimeMs30d ?: 0L
                val metaSubtitle = if (hasUsageAccess && playtimeMs > 0L) {
                    "${activeApp.systemTag}  •  Played ${formatPlaytime(playtimeMs)}"
                } else {
                    activeApp.systemTag
                }
                Text(
                    text = metaSubtitle,
                    color = secondaryTextCol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(162.dp),
                contentPadding = PaddingValues(horizontal = 42.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(apps, key = { _, app -> "nhyb_${app.id}" }) { index, app ->
                    val isSelected = index == selectedIndex && currentFocusZone != FocusZone.DOCK
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.07f else 0.97f,
                        animationSpec = tween(140),
                        label = "NHybridScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(144.dp)
                            .scale(scale)
                            .shadow(
                                elevation = if (isSelected) 10.dp else 3.dp,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF00C3E3).copy(alpha = ringAlpha) else circleBgCol)
                            .padding(if (isSelected) 4.5.dp else 0.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isLightTheme) PureWhite else Color(0xFF1F2226))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) bgCol else circleBorderCol,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                if (index == selectedIndex) onLaunch(app) else onIndexSelected(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            app.accentColor.copy(alpha = 0.34f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (app.iconBitmap != null) {
                                Image(
                                    bitmap = app.iconBitmap,
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .size(98.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = app.label.take(2).uppercase(),
                                    color = primaryTextCol,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        if (app.isFavorite) {
                            Text(
                                text = "★",
                                color = Color(0xFFF59E0B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                            )
                        }
                    }
                }

                item(key = "hybrid_all_software_circle") {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(circleBgCol)
                            .border(1.5.dp, circleBorderCol, CircleShape)
                            .clickable { onDockActionTriggered(DockAction.ALL_APPS) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "▦", color = titleCyanCol, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text(
                                text = "All Software",
                                color = primaryTextCol,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. LOWER CIRCULAR SYSTEM DOCK + FULL-WIDTH FOOTER RULE
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val dockItems = listOf(
                Triple(DockAction.OPTIMIZE, "⚡", "Turbo Boost (${ramUsagePercent}% RAM)" to Color(0xFF10B981)),
                Triple(DockAction.UI_HUB, "❖", "ECHO UI Hub & Themes" to Color(0xFFFF4554)),
                Triple(DockAction.ALL_APPS, "▦", "All Software & Emulators" to Color(0xFF00C3E3)),
                Triple(DockAction.ECHO_UPDATES, "💬", "ECHO News & Releases" to Color(0xFFF59E0B)),
                Triple(DockAction.WIFI_BT, "📶", "Wireless & Controllers" to Color(0xFF3B82F6)),
                Triple(DockAction.SETTINGS, "⚙", "System Settings" to Color(0xFF64748B))
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockItems.forEachIndexed { idx, (action, icon, meta) ->
                    val (_, iconColor) = meta
                    val isSelected = currentFocusZone == FocusZone.DOCK && selectedDockIndex == idx
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(circleBgCol)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) titleCyanCol else circleBorderCol,
                                shape = CircleShape
                            )
                            .clickable {
                                onDockActionSelected(idx)
                                onDockActionTriggered(action)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon,
                            color = iconColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val activeDockLabel = dockItems.getOrNull(selectedDockIndex)?.third?.first ?: "System Dock"
            if (currentFocusZone == FocusZone.DOCK) {
                Text(
                    text = activeDockLabel,
                    color = titleCyanCol,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(1.dp)
                    .background(dividerCol)
            )

            // Bottom Footer Legend Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bottom-Left: Dual-Tone Controller Grip Icon + Live RAM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 7.dp, height = 15.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                .background(Color(0xFF00C3E3))
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 7.dp, height = 15.dp)
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(Color(0xFFFF4554))
                        )
                    }
                    Text(
                        text = "RAM ${ramUsagePercent}%",
                        color = primaryTextCol.copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onDockActionTriggered(DockAction.OPTIMIZE) }
                    )
                }

                // Bottom-Right: (+) Options   (A/B) Start
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.clickable { activeApp?.let(onOptions) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(primaryTextCol),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = bgCol, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Text(
                            text = "Options",
                            color = primaryTextCol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.clickable { activeApp?.let(onLaunch) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(primaryTextCol),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = confirmBtn, color = bgCol, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Text(
                            text = "Start",
                            color = primaryTextCol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * ============================================================================================
 * 3. DUAL-SCREEN 3D HANDHELD UI (SPLIT INTO UPPER STAGE + LOWER TOUCH DECK)
 * Used stacked together on single-screen devices, OR mapped 1-per-display on dual-screen clamshells!
 * ============================================================================================
 */
@Composable
fun StereoscopicDual3DLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
    gameStats: Map<String, GameStats>,
    hasUsageAccess: Boolean,
    gridColumns: Int,
    onGridColumnsChange: (Int) -> Unit,
    depth3dLevel: Float,
    onToggleDepth3d: () -> Unit,
    gamepadLayout: GamepadLayout,
    powerInfo: PowerInfo,
    ramUsagePercent: Int,
    isDualScreenMode: Boolean,
    onToggleDualScreen: () -> Unit,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    onOpenUiHub: () -> Unit,
    onOptimize: () -> Unit,
    onAllApps: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeApp = apps.getOrNull(selectedIndex)
    val activeStats = activeApp?.let { gameStats[it.packageName] }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E131D))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Dual3DUpperStage(
            activeApp = activeApp,
            totalAppCount = apps.size,
            stats = activeStats,
            hasUsageAccess = hasUsageAccess,
            powerInfo = powerInfo,
            ramUsagePercent = ramUsagePercent,
            depth3dLevel = depth3dLevel,
            onToggleDepth3d = onToggleDepth3d,
            onOptimize = onOptimize,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
        )

        Dual3DLowerTouchDeck(
            apps = apps,
            selectedIndex = selectedIndex,
            gridColumns = gridColumns,
            onGridColumnsChange = onGridColumnsChange,
            gamepadLayout = gamepadLayout,
            isDualScreenMode = isDualScreenMode,
            onToggleDualScreen = onToggleDualScreen,
            onIndexSelected = onIndexSelected,
            onLaunch = onLaunch,
            onOptions = onOptions,
            onOpenUiHub = onOpenUiHub,
            onOptimize = onOptimize,
            onAllApps = onAllApps,
            onOpenSettings = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
        )
    }
}

@Composable
fun Dual3DUpperStage(
    activeApp: AppItem?,
    totalAppCount: Int,
    stats: GameStats?,
    hasUsageAccess: Boolean,
    powerInfo: PowerInfo,
    ramUsagePercent: Int,
    depth3dLevel: Float,
    onToggleDepth3d: () -> Unit,
    onOptimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dateTimeStr by remember { mutableStateOf(formatTime("MM/dd (EEE) HH:mm")) }
    LaunchedEffect(Unit) {
        while (true) {
            dateTimeStr = formatTime("MM/dd (EEE) HH:mm")
            delay(15000)
        }
    }

    val spinTransition = rememberInfiniteTransition(label = "Dual3DRotation")
    val spinAngle by spinTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpinAngle"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .drawWithCache {
                val skyGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D2B44),
                        Color(0xFF2E4369),
                        Color(0xFFDCE6F2)
                    )
                )
                onDrawBehind {
                    drawRect(brush = skyGradient)
                    val horizonY = size.height * 0.72f
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(0f, horizonY),
                        end = Offset(size.width, horizonY),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
            }
            .border(2.dp, Color(0xFF475569), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: [Online ▂▄▆█] | [▦ X Titles  ⚡ RAM Y%] | [Date Time 🔋 Z%]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.48f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Online",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "▂▄▆█",
                        color = Color(0xFF10B981),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "▦ $totalAppCount Titles",
                        color = PlatinumWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "⚡ RAM ${ramUsagePercent}%",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable(onClick = onOptimize)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateTimeStr,
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "🔋 ${powerInfo.percentage}%",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Center 3D Rotating Cartridge Stage + Glossy Speech-Bubble Title Banner + 3D Depth Slider
            if (activeApp != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(44.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.22f),
                            radius = size.width * 0.45f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.38f),
                            radius = size.width * 0.25f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (depth3dLevel > 0.1f) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .graphicsLayer {
                                            translationX = 6f * depth3dLevel
                                            rotationY = spinAngle * depth3dLevel
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(activeApp.accentColor.copy(alpha = 0.35f))
                                        .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .graphicsLayer {
                                        rotationY = spinAngle
                                        translationY = (spinAngle * 0.2f)
                                    }
                                    .shadow(10.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PureWhite)
                                    .border(2.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (activeApp.iconBitmap != null) {
                                    Image(
                                        bitmap = activeApp.iconBitmap,
                                        contentDescription = activeApp.label,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = activeApp.label.take(2).uppercase(),
                                        color = Color(0xFF1E293B),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Speech-Bubble Title Banner
                        Column(
                            modifier = Modifier
                                .width(245.dp)
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(PureWhite)
                                .border(2.dp, activeApp.accentColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (activeApp.isFavorite) "★ ${activeApp.label}" else activeApp.label,
                                color = Color(0xFF0F172A),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(1.dp)
                                    .background(Color(0xFFCBD5E1))
                            )
                            val playedText = if (hasUsageAccess && (stats?.playtimeMs30d ?: 0L) > 0L) {
                                "${activeApp.systemTag} • PLAYED ${formatPlaytime(stats?.playtimeMs30d ?: 0L)}"
                            } else {
                                "${activeApp.systemTag} SOFTWARE"
                            }
                            Text(
                                text = playedText,
                                color = Color(0xFF475569),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right Edge: Interactive 3D Depth Slider Switch ("3D ON / OFF")
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onToggleDepth3d)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "3D",
                            color = if (depth3dLevel > 0f) Color(0xFF10B981) else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = if (depth3dLevel > 0f) Alignment.TopCenter else Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (depth3dLevel > 0f) Color(0xFF10B981) else Color.Gray)
                            )
                        }
                        Text(
                            text = if (depth3dLevel > 0f) "ON" else "OFF",
                            color = PureWhite,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Dual3DLowerTouchDeck(
    apps: List<AppItem>,
    selectedIndex: Int,
    gridColumns: Int,
    onGridColumnsChange: (Int) -> Unit,
    gamepadLayout: GamepadLayout,
    isDualScreenMode: Boolean,
    onToggleDualScreen: () -> Unit,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    onOpenUiHub: () -> Unit,
    onOptimize: () -> Unit,
    onAllApps: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val activeApp = apps.getOrNull(selectedIndex)
    val confirmBtn = if (gamepadLayout == GamepadLayout.ABXY) "A" else "B"

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            gridState.animateScrollToItem(selectedIndex)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD8E1EC))
            .border(2.dp, Color(0xFF94A3B8), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Applet Strip + [⊟] / [⊞] Grid Zoom Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HandheldAppletPill(icon = "🎨", label = "Themes", tint = Color(0xFFE11D48), onClick = onOpenUiHub)
                HandheldAppletPill(icon = "▦", label = "Library", tint = Color(0xFFF97316), onClick = onAllApps)
                HandheldAppletPill(icon = "⚡", label = "Boost", tint = Color(0xFF10B981), onClick = onOptimize)
                HandheldAppletPill(icon = "⚙", label = "Settings", tint = Color(0xFF0284C7), onClick = onOpenSettings)
                HandheldAppletPill(
                    icon = "⧉",
                    label = if (isDualScreenMode) "2X DS" else "1X",
                    tint = Color(0xFF6366F1),
                    onClick = onToggleDualScreen
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .shadow(2.dp, RoundedCornerShape(7.dp))
                        .clip(RoundedCornerShape(7.dp))
                        .background(PureWhite)
                        .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(7.dp))
                        .clickable { onGridColumnsChange((gridColumns + 1).coerceAtMost(6)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⊟", color = Color(0xFF1E293B), fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .shadow(2.dp, RoundedCornerShape(7.dp))
                        .clip(RoundedCornerShape(7.dp))
                        .background(PureWhite)
                        .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(7.dp))
                        .clickable { onGridColumnsChange((gridColumns - 1).coerceAtLeast(2)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⊞", color = Color(0xFF1E293B), fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // 2. Center Tactile Cartridge Slot Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            state = gridState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(apps, key = { _, app -> "ds3d_${app.id}" }) { index, app ->
                val isSelected = index == selectedIndex
                val slotHeight = when (gridColumns) {
                    2 -> 76.dp
                    3 -> 68.dp
                    else -> 58.dp
                }

                Box(
                    modifier = Modifier
                        .height(slotHeight)
                        .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhite)
                        .border(
                            width = if (isSelected) 3.dp else 1.2.dp,
                            color = if (isSelected) Color(0xFFF97316) else Color(0xFF94A3B8),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (isSelected) onLaunch(app) else onIndexSelected(index)
                        }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (app.iconBitmap != null) {
                            Image(
                                bitmap = app.iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(if (gridColumns <= 3) 46.dp else 38.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(app.accentColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.label.take(2).uppercase(),
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        if (gridColumns <= 3) {
                            Text(
                                text = app.label,
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (app.isFavorite) {
                        Text(
                            text = "★",
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }

        // 3. Bottom Bezel Buttons: [ Options (Y) ] | [ ▶ Open (A/B) ] | [ ❖ UI Hub ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .shadow(3.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhite)
                    .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(12.dp))
                    .clickable { activeApp?.let(onOptions) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Options (Y)",
                    color = Color(0xFF1E293B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1.25f)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFEDD5), PureWhite)))
                    .border(2.dp, Color(0xFFF97316), RoundedCornerShape(12.dp))
                    .clickable { activeApp?.let(onLaunch) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶ Open ($confirmBtn)",
                    color = Color(0xFFC2410C),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .shadow(3.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhite)
                    .border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenUiHub)
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❖ UI Hub",
                    color = Color(0xFFE11D48),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun HandheldAppletPill(
    icon: String,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(PureWhite)
            .border(1.dp, tint.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = label,
            color = Color(0xFF1E293B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(pattern: String): String {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    } catch (_: Throwable) {
        "12:00"
    }
}
