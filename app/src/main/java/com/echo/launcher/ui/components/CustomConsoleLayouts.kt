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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.StatusGreen
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardElevated
import com.echo.launcher.ui.theme.TextMuted
import com.echo.launcher.ui.theme.TextSecondary

/**
 * 1. NEXT-GEN 5 HORIZON UI SHELL
 * Top-left mini media bar ribbon + massive atmospheric backdrop + bottom-left cinematic hero showcase.
 */
@Composable
fun NextGenHorizonLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    onOpenUiHub: () -> Unit,
    onOptimize: () -> Unit,
    onAllApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeApp = apps.getOrNull(selectedIndex)
    val accent = activeApp?.accentColor ?: Color(0xFF3B82F6)
    val animatedAccent by animateColorAsState(
        targetValue = accent,
        animationSpec = tween(420),
        label = "NextGenAccent"
    )

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val royalGlow = Brush.radialGradient(
                    colors = listOf(
                        animatedAccent.copy(alpha = 0.42f),
                        Color(0xFF0B162C).copy(alpha = 0.85f),
                        Color(0xFF04070F)
                    ),
                    center = Offset(size.width * 0.72f, size.height * 0.38f),
                    radius = (size.width * 0.78f).coerceAtLeast(1f)
                )
                val goldenParticleFlare = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.24f, size.height * 0.18f),
                    radius = (size.width * 0.42f).coerceAtLeast(1f)
                )
                onDrawBehind {
                    drawRect(color = Color(0xFF04070F))
                    drawRect(brush = royalGlow)
                    drawRect(brush = goldenParticleFlare)
                }
            }
            .padding(horizontal = 32.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP RIBBON: Small rounded-square game icons + active title pill beside the selected icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(apps, key = { _, app -> "ng_${app.id}" }) { index, app ->
                        val isSelected = index == selectedIndex
                        val tileScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 0.94f,
                            animationSpec = tween(160),
                            label = "NgTileScale"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 56.dp else 44.dp)
                                    .scale(tileScale)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(SurfaceCardElevated)
                                    .border(
                                        width = if (isSelected) 2.2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(13.dp)
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
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = app.label.take(2).uppercase(),
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isSelected) {
                                Text(
                                    text = app.label,
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Quick UI Hub & Library Mini Icons on Top-Right of Ribbon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenUiHub)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "❖ UI Hub",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onAllApps)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "▦ Library",
                            color = PlatinumWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // LOWER CINEMATIC HERO AREA: Left Metadata + Play Pill, Right Floating Artwork Emblem
            if (activeApp != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left Hero Column
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeApp.systemTag,
                                    color = PureWhite,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            if (activeApp.isFavorite) {
                                Text(
                                    text = "★ PINNED",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Text(
                            text = activeApp.label,
                            color = PureWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "${activeApp.packageName} • ULTRA-LOW LATENCY MODE READY",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // High-contrast White Pill Play Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(PureWhite)
                                    .clickable { onLaunch(activeApp) }
                                    .padding(horizontal = 32.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Play Game",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Circular Options Button (...)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                                    .clickable { onOptions(activeApp) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⋯",
                                    color = PureWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // 1-Tap Turbo Boost Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(StatusGreen.copy(alpha = 0.16f))
                                    .border(1.dp, StatusGreen.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                                    .clickable(onClick = onOptimize)
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = "⚡ Boost RAM",
                                    color = StatusGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Right Hero Card Showcase
                    Box(
                        modifier = Modifier
                            .width(210.dp)
                            .height(126.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        animatedAccent.copy(alpha = 0.45f),
                                        Color(0xFF101829)
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .clickable { onLaunch(activeApp) }
                            .padding(14.dp)
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
                                    text = "PROGRESS ACTIVITY",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "100%",
                                    color = PureWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (activeApp.iconBitmap != null) {
                                    Image(
                                        bitmap = activeApp.iconBitmap,
                                        contentDescription = activeApp.label,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Resume Instant",
                                        color = PureWhite,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap or press [A]",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. HYBRID PORTABLE DECK UI SHELL
 * Clean horizontal row of large square game tiles with animated cyan border, floating title above,
 * and circular system buttons centered in a bottom strip.
 */
@Composable
fun HybridPortableDeckLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
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
    val pulseTransition = rememberInfiniteTransition(label = "HybridBorderPulse")
    val borderAlpha by pulseTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF191C21))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Active Game Title Banner Above the Square Tile Row
        if (activeApp != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = if (activeApp.isFavorite) "★ ${activeApp.label}" else activeApp.label,
                    color = Color(0xFF00C3E3),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Large Square Game Tile Row
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp),
            contentPadding = PaddingValues(horizontal = 38.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(apps, key = { _, app -> "hyb_${app.id}" }) { index, app ->
                val isSelected = index == selectedIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 0.96f,
                    animationSpec = tween(150),
                    label = "HybridTileScale"
                )

                Box(
                    modifier = Modifier
                        .size(138.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF242930))
                        .border(
                            width = if (isSelected) 3.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00C3E3).copy(alpha = borderAlpha)
                            else Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
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
                                .size(92.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = app.label.take(2).uppercase(),
                            color = PureWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (app.isFavorite) {
                        Text(
                            text = "★",
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        )
                    }
                }
            }
        }

        // Circular System Dock Row + Bottom Divider Rule
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val dockCircles = listOf(
                Triple(DockAction.OPTIMIZE, "⚡", Color(0xFF10B981)),
                Triple(DockAction.UI_HUB, "❖", Color(0xFFFF4554)),
                Triple(DockAction.ALL_APPS, "▦", Color(0xFF00C3E3)),
                Triple(DockAction.ECHO_UPDATES, "⬇", Color(0xFFF59E0B)),
                Triple(DockAction.WIFI_BT, "📶", Color(0xFF38BDF8)),
                Triple(DockAction.SETTINGS, "⚙", Color(0xFF94A3B8))
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockCircles.forEachIndexed { idx, (action, icon, tint) ->
                    val isSelected = currentFocusZone == FocusZone.DOCK && selectedDockIndex == idx
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF282D35))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00C3E3) else Color.White.copy(alpha = 0.14f),
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
                            color = tint,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Thin horizontal rule + controller legend
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C3E3))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF4554))
                    )
                    Text(
                        text = "HYBRID PORTABLE DECK MODE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "[Y] Options",
                        color = PlatinumWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { activeApp?.let(onOptions) }
                    )
                    Text(
                        text = "[A] Start",
                        color = Color(0xFF00C3E3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.clickable { activeApp?.let(onLaunch) }
                    )
                }
            }
        }
    }
}

/**
 * 3. STEREOSCOPIC DUAL-3D UI SHELL
 * Upper 3D rotating emblem stage + lower touch grid matrix with [+] / [-] grid column zoom buttons
 * and large bottom [Open] / [UI Hub] / [Boost] buttons.
 */
@Composable
fun StereoscopicDual3DLayout(
    apps: List<AppItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    onOpenUiHub: () -> Unit,
    onOptimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gridColumns by remember { mutableIntStateOf(5) }
    val gridState = rememberLazyGridState()
    val activeApp = apps.getOrNull(selectedIndex)

    val spinTransition = rememberInfiniteTransition(label = "3DEmblemFloat")
    val floatOffset by spinTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            gridState.animateScrollToItem(selectedIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // UPPER 3D STAGE BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            (activeApp?.accentColor ?: Color(0xFFE11D48)).copy(alpha = 0.32f),
                            Color(0xFF101624)
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            if (activeApp != null) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Floating 3D Game Cartridge Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .graphicsLayer {
                                    translationY = floatOffset
                                    rotationY = floatOffset * 1.8f
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(2.dp, PureWhite.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeApp.iconBitmap != null) {
                                Image(
                                    bitmap = activeApp.iconBitmap,
                                    contentDescription = activeApp.label,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = activeApp.label.take(2).uppercase(),
                                    color = PureWhite,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE11D48))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "3D STEREOSCOPIC STAGE • ${activeApp.systemTag}",
                                    color = PureWhite,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = if (activeApp.isFavorite) "★ ${activeApp.label}" else activeApp.label,
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activeApp.packageName,
                                color = TextSecondary,
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Top-Right Grid Zoom Controls [-][+] & Quick Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCardElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { gridColumns = (gridColumns + 1).coerceAtMost(7) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "⊟", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCardElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { gridColumns = (gridColumns - 1).coerceAtLeast(3) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "⊞", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // LOWER TOUCH GRID MATRIX + BOTTOM BEZEL BUTTONS ([Options] [Open] [UI Hub])
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF131823))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                state = gridState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(apps, key = { _, app -> "3ds_${app.id}" }) { index, app ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .height(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else SurfaceCard)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFE11D48) else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                if (isSelected) onLaunch(app) else onIndexSelected(index)
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (app.iconBitmap != null) {
                                Image(
                                    bitmap = app.iconBitmap,
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(9.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            if (gridColumns <= 4) {
                                Text(
                                    text = app.label,
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Dual-Screen Handheld Touch Bar: [Options] | [▶ OPEN] | [❖ UI Hub] | [⚡ Boost]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .clickable { activeApp?.let(onOptions) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⋯ Options (Y)",
                        color = PlatinumWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE11D48))
                        .clickable { activeApp?.let(onLaunch) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ OPEN SOFTWARE (A)",
                        color = PureWhite,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardElevated)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenUiHub)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "❖ UI Hub",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusGreen.copy(alpha = 0.16f))
                        .border(1.dp, StatusGreen.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onOptimize)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ Boost",
                        color = StatusGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
