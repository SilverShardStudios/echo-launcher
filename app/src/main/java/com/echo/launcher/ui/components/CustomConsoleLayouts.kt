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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.echo.launcher.data.LauncherTab
import com.echo.launcher.data.PowerInfo
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
import kotlin.math.abs

/**
 * ============================================================================================
 * 1. HORIZON 5 NEXT-GEN LIVING-ROOM CONSOLE UI (FULL-SCREEN REPLACEMENT)
 * Authentic next-gen living-room console home screen:
 * - Top Header: "Games" (bright white bold) | "Media" (dimmed gray) on the left;
 *   Search icon, Settings gear, UI Hub switcher, Profile Avatar, and live HH:mm clock on the right.
 * - Top-Left Media Ribbon: Horizontal strip of small rounded-square icons (48dp, selected expands to 68dp
 *   with crisp white border + golden aura and the selected game's title immediately to the right).
 * - Full-Screen Atmospheric Backdrop: Deep royal-blue / indigo gradient with animated golden particle flares
 *   and dominant game color bleed.
 * - Bottom-Left Hero Area: Game logo/title, platform badge ("[PS5 / NEXT-GEN]" style monochrome white badge),
 *   glowing translucent white pill "Play Game" button, circular "..." options button, and quick Control Center toggle.
 * - Bottom-Right Trophy & Activity Cards: Glassmorphic Trophy Progress card (Gold/Silver/Bronze breakdown + %)
 *   and Official News / Playtime Activity card.
 * - Optional Bottom Control Center Bar: Translucent dock bar with Controller battery, Sound, Mic, Boost RAM, UI Hub, Power.
 * ============================================================================================
 */
@Composable
fun NextGenHorizonLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
    selectedTab: LauncherTab,
    onTabSelected: (LauncherTab) -> Unit,
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
    val accent = activeApp?.accentColor ?: Color(0xFF2563EB)
    val animatedAccent by animateColorAsState(
        targetValue = accent,
        animationSpec = tween(450),
        label = "Horizon5Accent"
    )

    var showControlCenter by remember { mutableStateOf(false) }
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
            listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
        }
    }

    //Deterministic Trophy & Playtime stats per game package so each title feels alive
    val appHash = remember(activeApp?.packageName) { abs(activeApp?.packageName?.hashCode() ?: 77) }
    val trophyPercent = remember(appHash) { (35 + (appHash % 65)).coerceIn(15, 100) }
    val goldCount = remember(appHash) { 1 + (appHash % 4) }
    val silverCount = remember(appHash) { 3 + (appHash % 9) }
    val bronzeCount = remember(appHash) { 8 + (appHash % 24) }
    val hoursPlayed = remember(appHash) { 12 + (appHash % 140) }

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
            .padding(horizontal = 34.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER + MEDIA RIBBON SECTION
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top Bar: Games | Media on Left; Search, Control Center, UI Hub, Settings, Clock on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Games | Media text tabs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
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

                    // Right: Search, UI Hub, Boost RAM, Dual Screen toggle, Settings, Profile, Clock
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // UI Hub quick pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                                .clickable(onClick = onOpenUiHub)
                                .padding(horizontal = 11.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "❖ UI Hub",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Boost RAM quick pill
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

                        // 1x/2x Screen Switcher
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

                        // Search / All Apps icon
                        Text(
                            text = "⌕",
                            color = PureWhite.copy(alpha = 0.85f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onAllApps)
                        )

                        // Settings Gear icon
                        Text(
                            text = "⚙",
                            color = PureWhite.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            modifier = Modifier.clickable(onClick = onOpenSettings)
                        )

                        // Online Profile Avatar with green status dot
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))))
                                    .border(1.2.dp, PureWhite.copy(alpha = 0.8f), CircleShape)
                                    .clickable { showControlCenter = !showControlCenter },
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
                    // Store / UI Hub Quick Tile at index 0 style
                    item(key = "horizon_hub_tile") {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                                    )
                                )
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

                            // Inline title right next to the active icon in the top media ribbon
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

                    // Library Grid tile at end of ribbon
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

            // 2. LOWER CINEMATIC HERO SECTION + TROPHY / ACTIVITY CARDS
            if (activeApp != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Bottom-Left: Game Hero Logo/Title, Platform Tag, Play Game Pill, (...) Options, Control Center
                        Column(
                            modifier = Modifier.weight(1.15f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Monochrome White Platform Tag Badge + Full Game tag
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
                                    text = if (activeApp.isEmulator) "Full Emulator Suite" else "Full Game",
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

                            // Large Hero Game Title
                            Text(
                                text = activeApp.label,
                                color = PureWhite,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Light,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Play Game Pill + (...) Circle + Control Center Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Translucent frosted white Play Game pill with bright focus border
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(PureWhite.copy(alpha = 0.92f))
                                        .border(2.dp, PureWhite, RoundedCornerShape(28.dp))
                                        .clickable { onLaunch(activeApp) }
                                        .padding(horizontal = 36.dp, vertical = 11.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Play Game",
                                        color = Color.Black,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Circular (...) Options button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
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

                                // Quick Control Center Bar Toggle
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (showControlCenter) Color(0xFFF59E0B).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f))
                                        .border(1.dp, if (showControlCenter) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.24f), CircleShape)
                                        .clickable { showControlCenter = !showControlCenter },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⊞",
                                        color = if (showControlCenter) Color(0xFFF59E0B) else PureWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Bottom-Right: Trophy Progress & Activity Cards
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Card 1: Official Activity / Resume Card
                            Box(
                                modifier = Modifier
                                    .width(175.dp)
                                    .height(112.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xCC111827))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                    .clickable { onLaunch(activeApp) }
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
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "IN PROGRESS",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "Quick Resume",
                                                color = PureWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "PLAYED ${hoursPlayed}h",
                                            color = PureWhite.copy(alpha = 0.85f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Press [A] to jump in",
                                            color = TextMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }

                            // Card 2: Trophy Progress Breakdown Card
                            Box(
                                modifier = Modifier
                                    .width(195.dp)
                                    .height(112.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xCC111827))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                    .clickable { onOptions(activeApp) }
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
                                            text = "🏆 TROPHIES",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "$trophyPercent%",
                                            color = PureWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    // Progress Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(trophyPercent / 100f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFFF59E0B))
                                        )
                                    }

                                    // Gold / Silver / Bronze counts
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TrophyMiniCounter(label = "G", count = goldCount, color = Color(0xFFFBBF24))
                                        TrophyMiniCounter(label = "S", count = silverCount, color = Color(0xFFCBD5E1))
                                        TrophyMiniCounter(label = "B", count = bronzeCount, color = Color(0xFFD97706))
                                    }
                                }
                            }
                        }
                    }

                    // Optional Pop-up Bottom Control Center Dock Bar
                    if (showControlCenter) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xE60F172A))
                                .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ControlCenterItem(icon = "🏠", label = "Home") { showControlCenter = false }
                            ControlCenterItem(icon = "▦", label = "Switcher", onClick = onAllApps)
                            ControlCenterItem(icon = "⚡", label = "Boost RAM", onClick = onOptimize)
                            ControlCenterItem(icon = "❖", label = "UI Hub", onClick = onOpenUiHub)
                            ControlCenterItem(icon = "🎮", label = "Pad ${powerInfo.percentage}%") { showControlCenter = false }
                            ControlCenterItem(icon = "⚙", label = "System", onClick = onOpenSettings)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyMiniCounter(label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(13.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, color = color, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
        }
        Text(
            text = count.toString(),
            color = PureWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ControlCenterItem(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 13.sp, color = PureWhite)
        Text(text = label, fontSize = 11.sp, color = PureWhite, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * ============================================================================================
 * 2. N-HYBRID PORTABLE DECK UI (FULL-SCREEN REPLACEMENT)
 * Authentic hybrid console home screen:
 * - Supports instant "Basic Black" (#2D2D2D) and "Basic White" (#EBEBEB) theme toggle!
 * - Top Left: Circular Player Avatar + small Online indicator.
 * - Top Right: Live Time ("15:20"), Wi-Fi Signal bars, Battery % + horizontal battery icon,
 *   Basic White/Black Theme Toggle pill, and 1x/2x Dual-Screen toggle.
 * - Center Stage:
 *   - Floating Title directly above the active square tile in crisp turquoise-cyan (#00B8D4)
 *     with a game-card cartridge icon beside it.
 *   - Edge-to-edge horizontal row of large 1:1 square game tiles (150dp x 150dp) with an
 *     outer pulsing cyan focus ring + inner white/dark gap border.
 *   - At the end of the row: circular "All Software" (4-squares grid) button.
 * - Lower Dock: Centered row of 6 circular system buttons (50dp diameter) with drop-shadow borders
 *   and a floating label below the active circle:
 *   1. News / Updates (Red/Orange)
 *   2. UI Shop / Hub (Amber/Orange bag)
 *   3. Album / Media (Blue)
 *   4. Turbo RAM Boost (Emerald Green)
 *   5. System Settings (Gear)
 *   6. All Software Drawer (Cyan)
 * - Bottom Footer Bar: Full-width 1.dp horizontal divider rule;
 *   Left: Dual-controller icon + live RAM badge;
 *   Right: "(+) Options" and "(A) Start" button prompts.
 * ============================================================================================
 */
@Composable
fun HybridPortableDeckLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
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
        // 1. TOP STATUS BAR: Left Player Avatar Circles; Right Theme Switch, Dual Screen, Time, Wi-Fi, Battery
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top-Left: Player Profile Circle + Page/Favorites badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4554))
                        .border(2.dp, PureWhite, CircleShape)
                        .clickable { onDockActionTriggered(DockAction.UI_HUB) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P1",
                        color = PureWhite,
                        fontSize = 12.sp,
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
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top-Right: UI Hub button, Dual-Screen toggle, Clock, Wi-Fi, Battery %
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                        fontSize = 10.5.sp,
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "📶",
                    fontSize = 13.sp,
                    color = primaryTextCol
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${powerInfo.percentage} %",
                        color = primaryTextCol,
                        fontSize = 13.sp,
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

        // 2. CENTER CARTRIDGE TITLE + LARGE SQUARE GAME TILES ROW
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Active Game Title directly above the tiles with game-card cartridge icon
            if (activeApp != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Little cartridge icon
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
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.dp),
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

                    // Outer pulsing cyan ring + inner neutral frame
                    Box(
                        modifier = Modifier
                            .size(148.dp)
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
                        // Subtle colored wash behind icon so every tile looks like full cover art
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
                                        .size(102.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = app.label.take(2).uppercase(),
                                    color = primaryTextCol,
                                    fontSize = 28.sp,
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

                // Circular "All Software" Grid Button at end of row
                item(key = "hybrid_all_software_circle") {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            .size(48.dp)
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
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Full-width horizontal divider line
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
                // Bottom-Left: Dual-Joy Grip Icon + Active Mode
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
                        color = primaryTextCol.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { onDockActionTriggered(DockAction.OPTIMIZE) }
                    )
                }

                // Bottom-Right: (+) Options   (A) Start
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
                            Text(text = "A", color = bgCol, fontSize = 10.sp, fontWeight = FontWeight.Black)
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
 * 3. DUAL-SCREEN 3D HANDHELD UI (AUTHENTIC TOP 3D STAGE + BOTTOM TOUCH SCREEN)
 * Looks like an actual dual-screen handheld home menu:
 * - TOP SCREEN (Upper 3D Stereoscopic Display):
 *   - Top Status Strip: Green "Internet" pill + 5 signal bars on the left,
 *     Footsteps / Play Coins ("🪙 300") in the center, and Date + Time + Battery bars on the right.
 *   - 3D Stereoscopic Stage: Soft sky-white/silver grid floor & floating clouds with a rotating
 *     3D Cartridge / Software Box in the center, sound-wave rings on both sides, and floating speech-bubble title plate.
 *   - Interactive 3D Depth Slider on the right edge ("3D ●━━ OFF").
 * - LOWER SCREEN (Touch Stylus Grid Matrix):
 *   - Top Applet Bar: Quick circular/rounded icons for:
 *     [📝 Game Notes] [😊 Friend List] [🔔 Notifications] [🌐 Web Browser] [❖ UI Hub] [⚡ Boost RAM]
 *     plus the iconic [⊟] (shrink tiles / more columns) and [⊞] (enlarge tiles / fewer columns) zoom buttons!
 *   - Center Touch Grid: Beveled 3D tactile white/silver squares with inner glossy frames and animated
 *     orange/cyan corner selection brackets around the focused cartridge.
 *   - Bottom 3-Button Handheld Bezel Bar:
 *     Large tactile rounded-top bezel buttons along the bottom edge:
 *     [ 🔧 Options (Y) ]  |  [ ▶ Open (A) ]  |  [ ❖ UI Hub / Manual ]
 * ============================================================================================
 */
@Composable
fun StereoscopicDual3DLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
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
    var gridColumns by remember { mutableIntStateOf(4) }
    var depth3dLevel by remember { mutableFloatStateOf(1.0f) }
    val gridState = rememberLazyGridState()
    val activeApp = apps.getOrNull(selectedIndex)
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

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            gridState.animateScrollToItem(selectedIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E131D))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ==================== TOP 3D STEREOSCOPIC SCREEN ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
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
                        // Subtle 3D perspective horizon lines at the bottom of the top screen
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
                // Top Handheld Status Bar: [Internet ▂▄▆█] | [🪙 300  👣 4,820] | [Date Time 🔋]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.48f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Green Internet badge + signal bars
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
                                text = "Internet",
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

                    // Center: Play Coins & Steps Counter + RAM %
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🪙 300",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "⚡ RAM ${ramUsagePercent}%",
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable(onClick = onOptimize)
                        )
                    }

                    // Right: Date/Time + Dual Screen Switch + Battery
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

                // Center 3D Rotating Cartridge Box Stage + Speech Bubble Banner + 3D Depth Slider
                if (activeApp != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Acoustic Wave Rings
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

                        // Center 3D Cartridge & Floating White Speech Bubble Title Plate
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // 3D Parallax Dual-Layer Cartridge Box
                            Box(contentAlignment = Alignment.Center) {
                                // Stereoscopic 3D ghost layer when 3D slider > 0
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

                            // Classic White Glossy Speech-Bubble Title Banner
                            Column(
                                modifier = Modifier
                                    .width(240.dp)
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
                                    fontSize = 16.sp,
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
                                Text(
                                    text = "${activeApp.systemTag} SOFTWARE",
                                    color = Color(0xFF475569),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Right Edge: Interactive 3D Depth Slider Switch ("3D / OFF")
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .clickable {
                                    depth3dLevel = if (depth3dLevel > 0.5f) 0f else 1f
                                }
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

        // ==================== LOWER TOUCH SCREEN (APPLET BAR + 3D GRID + 3-BUTTON BEZEL) ====================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
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
                // Left: Classic Top Touch Applets (Notes, Friends, Notifications, Browser, UI Hub, Boost)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HandheldAppletPill(icon = "🎨", label = "Themes", tint = Color(0xFFE11D48), onClick = onOpenUiHub)
                    HandheldAppletPill(icon = "😊", label = "Friends", tint = Color(0xFFF97316), onClick = onAllApps)
                    HandheldAppletPill(icon = "🔔", label = "Alerts", tint = Color(0xFF10B981), onClick = onOptimize)
                    HandheldAppletPill(icon = "🌐", label = "Settings", tint = Color(0xFF0284C7), onClick = onOpenSettings)
                    HandheldAppletPill(
                        icon = "⧉",
                        label = if (isDualScreenMode) "2X DS" else "1X",
                        tint = Color(0xFF6366F1),
                        onClick = onToggleDualScreen
                    )
                }

                // Right: Grid Size Zoom Buttons [⊟] (More Columns) and [⊞] (Larger Icons)
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
                            .clickable { gridColumns = (gridColumns + 1).coerceAtMost(6) },
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
                            .clickable { gridColumns = (gridColumns - 1).coerceAtLeast(2) },
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

            // 3. Classic 3-Part Bottom Touch Bezel Buttons: [ Options ] | [ ▶ Open ] | [ UI Hub ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Bezel Button: Options / Manual
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

                // Center Primary Bezel Button: Open
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFEDD5), PureWhite)
                            )
                        )
                        .border(2.dp, Color(0xFFF97316), RoundedCornerShape(12.dp))
                        .clickable { activeApp?.let(onLaunch) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ Open (A)",
                        color = Color(0xFFC2410C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Right Bezel Button: UI Hub
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
