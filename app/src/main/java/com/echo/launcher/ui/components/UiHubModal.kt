package com.echo.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.ConsoleUiStyle
import com.echo.launcher.data.UiThemePack
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

@Composable
fun UiHubModal(
    isOpen: Boolean,
    packs: List<UiThemePack>,
    downloadedPackIds: Set<String>,
    activeStyle: ConsoleUiStyle,
    downloadingPackId: String?,
    downloadProgress: Float,
    selectedIndex: Int,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onSelectIndex: (Int) -> Unit,
    onDownloadOrApply: (UiThemePack) -> Unit,
    onInstantPreview: (UiThemePack) -> Unit,
    onDownloadAllPacks: () -> Unit,
    onUninstallPack: (UiThemePack) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(180)),
        modifier = modifier.fillMaxSize()
    ) {
        val visiblePacks = remember(packs, selectedFilter, downloadedPackIds) {
            filterUiPacks(packs, selectedFilter, downloadedPackIds)
        }

        val listState = rememberLazyListState()
        val safeSelectedIndex = selectedIndex.coerceIn(0, (visiblePacks.size - 1).coerceAtLeast(0))
        val focusedPack = visiblePacks.getOrNull(safeSelectedIndex)

        LaunchedEffect(safeSelectedIndex, visiblePacks.size) {
            if (visiblePacks.isNotEmpty() && safeSelectedIndex in visiblePacks.indices) {
                listState.animateScrollToItem(safeSelectedIndex)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBackground.copy(alpha = 0.96f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 26.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inner click
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TOP STORE HEADER + CATEGORY TABS + INSTALL ALL BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                .border(1.2.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❖", color = Color(0xFFF59E0B), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }

                        Column {
                            Text(
                                text = "ECHO UI HUB // CONSOLE ARCHITECTURE STORE",
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "INSTALL, TEST-DRIVE & HOT-SWAP CONSOLE INTERFACES  •  L1/R1 CATEGORY  •  Y TEST DRIVE",
                                color = ElectricCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Filter Tabs: ALL | INSTALLED | LIVING ROOM | HANDHELD
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            UI_HUB_FILTERS.forEach { tab ->
                                val isTabActive = selectedFilter == tab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isTabActive) ElectricCyan else SurfaceCard)
                                        .border(
                                            1.dp,
                                            if (isTabActive) ElectricCyan else SurfaceBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onFilterChange(tab) }
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tab,
                                        color = if (isTabActive) Color.Black else TextMuted,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Right Header Actions: Install All + Close
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (downloadedPackIds.size < packs.size) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(StatusGreen.copy(alpha = 0.16f))
                                    .border(1.dp, StatusGreen, RoundedCornerShape(14.dp))
                                    .clickable(onClick = onDownloadAllPacks)
                                    .padding(horizontal = 11.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "⬇ Install All (${packs.size})",
                                    color = StatusGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(StatusGreen.copy(alpha = 0.14f))
                                    .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "✓ ALL ${packs.size} READY",
                                    color = StatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF334B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "B", color = PureWhite, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                            }
                            Text(
                                text = "Close",
                                color = PureWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 2. INTERACTIVE UI PACK CARDS CAROUSEL (or Empty State)
                if (visiblePacks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(198.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No UI shells in this category",
                            color = TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(visiblePacks, key = { _, pack -> pack.id }) { index, pack ->
                            val isSelected = index == safeSelectedIndex
                            val isDownloaded = downloadedPackIds.contains(pack.id)
                            val isActive = activeStyle == pack.style
                            val isDownloadingThis = downloadingPackId == pack.id

                            UiPackCard(
                                pack = pack,
                                isSelected = isSelected,
                                isDownloaded = isDownloaded,
                                isActive = isActive,
                                isDownloading = isDownloadingThis,
                                downloadProgress = if (isDownloadingThis) downloadProgress else 0f,
                                onClick = {
                                    if (isSelected) {
                                        onDownloadOrApply(pack)
                                    } else {
                                        onSelectIndex(index)
                                    }
                                }
                            )
                        }
                    }
                }

                // 3. DETAILED INSPECTOR & ACTION DECK FOR FOCUSED UI PACK
                if (focusedPack != null) {
                    val isDownloaded = downloadedPackIds.contains(focusedPack.id)
                    val isActive = activeStyle == focusedPack.style
                    val isDownloading = downloadingPackId == focusedPack.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SurfaceCardElevated)
                            .border(1.2.dp, focusedPack.primaryAccent.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Inspector Details + Feature Highlights
                        Column(
                            modifier = Modifier.weight(1.3f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = focusedPack.title,
                                    color = PureWhite,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(focusedPack.primaryAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${focusedPack.version} • ${focusedPack.sizeText}",
                                        color = focusedPack.primaryAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = focusedPack.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Feature Highlight Chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                focusedPack.featureHighlights.take(3).forEach { feat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(0.6.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "• $feat",
                                            color = PlatinumWhite,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right Action Buttons: Uninstall | Instant Test Drive | Install & Apply
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isDownloaded && !focusedPack.isBuiltIn && !isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                        .clickable { onUninstallPack(focusedPack) }
                                        .padding(horizontal = 11.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = "Uninstall",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (!isActive) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, focusedPack.primaryAccent.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                        .clickable { onInstantPreview(focusedPack) }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "👁 Test Drive",
                                        color = focusedPack.primaryAccent,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEAB308).copy(alpha = 0.22f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Y",
                                            color = Color(0xFFEAB308),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            val btnBg = when {
                                isActive -> StatusGreen
                                isDownloaded -> focusedPack.primaryAccent
                                else -> PureWhite
                            }

                            val btnLabel = when {
                                isDownloading -> "INSTALLING..."
                                isActive -> "✓ ACTIVE CONSOLE UI"
                                isDownloaded -> "⚡ APPLY UI SHELL"
                                else -> "⬇ INSTALL & APPLY"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(btnBg)
                                    .clickable(enabled = !isDownloading) {
                                        onDownloadOrApply(focusedPack)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Text(
                                        text = btnLabel,
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.16f)),
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UiPackCard(
    pack: UiThemePack,
    isSelected: Boolean,
    isDownloaded: Boolean,
    isActive: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 0.96f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "UiPackScale"
    )

    val borderCol by animateColorAsState(
        targetValue = when {
            isSelected -> pack.primaryAccent
            isActive -> StatusGreen.copy(alpha = 0.7f)
            else -> SurfaceBorder
        },
        animationSpec = tween(150),
        label = "UiPackBorder"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress.coerceIn(0f, 1f),
        animationSpec = tween(110),
        label = "UiDownloadProgress"
    )

    Column(
        modifier = Modifier
            .width(235.dp)
            .height(198.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(if (isSelected) 2.2.dp else 1.dp, borderCol, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(11.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // High-detail schematic wireframe preview of the console UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(106.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            pack.primaryAccent.copy(alpha = 0.28f),
                            pack.secondaryAccent.copy(alpha = 0.14f),
                            Color(0xFF070B12)
                        )
                    )
                )
                .border(1.dp, pack.primaryAccent.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
        ) {
            UiSchematicCanvas(pack = pack)

            // Version pill top-left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.70f))
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = pack.version,
                    color = ElectricCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Status pill top-right
            val badgeText = when {
                isActive -> "● ACTIVE"
                isDownloaded -> "✓ INSTALLED"
                else -> "AVAILABLE"
            }
            val badgeColor = when {
                isActive -> StatusGreen
                isDownloaded -> pack.primaryAccent
                else -> PureWhite.copy(alpha = 0.85f)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .border(0.7.dp, badgeColor.copy(alpha = 0.8f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = pack.layoutTag,
                color = pack.primaryAccent,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = pack.title,
                color = PureWhite,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pack.subtitle,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isDownloading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(pack.primaryAccent)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pack.sizeText,
                    color = PlatinumWhite,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isActive) "Running" else if (isDownloaded) "▶ Apply" else "⬇ Install",
                    color = if (isActive) StatusGreen else if (isDownloaded) pack.primaryAccent else PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UiSchematicCanvas(pack: UiThemePack) {
    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        val cr = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        when (pack.style) {
            ConsoleUiStyle.ECHO_DEFAULT -> {
                val cardW = size.width * 0.24f
                val cardH = size.height * 0.45f
                val cy = size.height * 0.24f
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.2f),
                    topLeft = Offset(size.width * 0.08f, cy + 4.dp.toPx()),
                    size = Size(cardW * 0.85f, cardH * 0.85f),
                    cornerRadius = cr
                )
                drawRoundRect(
                    color = pack.primaryAccent.copy(alpha = 0.85f),
                    topLeft = Offset(size.width * 0.38f, cy),
                    size = Size(cardW, cardH),
                    cornerRadius = cr
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.2f),
                    topLeft = Offset(size.width * 0.70f, cy + 4.dp.toPx()),
                    size = Size(cardW * 0.85f, cardH * 0.85f),
                    cornerRadius = cr
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.28f),
                    topLeft = Offset(size.width * 0.22f, size.height * 0.80f),
                    size = Size(size.width * 0.56f, size.height * 0.13f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
            ConsoleUiStyle.NEXTGEN_MEDIA_BAR -> {
                for (i in 0 until 6) {
                    val isFirst = i == 1
                    val s = if (isFirst) 15.dp.toPx() else 11.dp.toPx()
                    drawRoundRect(
                        color = if (isFirst) PureWhite else Color.White.copy(alpha = 0.30f),
                        topLeft = Offset(6.dp.toPx() + i * 18.dp.toPx(), 16.dp.toPx()),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(6.dp.toPx(), size.height * 0.56f),
                    size = Size(size.width * 0.42f, 8.dp.toPx()),
                    cornerRadius = cr
                )
                drawRoundRect(
                    color = PureWhite,
                    topLeft = Offset(6.dp.toPx(), size.height * 0.74f),
                    size = Size(size.width * 0.26f, 11.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                drawRoundRect(
                    color = pack.secondaryAccent.copy(alpha = 0.55f),
                    topLeft = Offset(size.width * 0.62f, size.height * 0.56f),
                    size = Size(size.width * 0.34f, size.height * 0.34f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
            ConsoleUiStyle.HYBRID_DECK -> {
                val sq = size.height * 0.42f
                for (i in 0 until 4) {
                    val active = i == 1
                    drawRoundRect(
                        color = if (active) pack.primaryAccent else Color.White.copy(alpha = 0.24f),
                        topLeft = Offset(8.dp.toPx() + i * (sq + 7.dp.toPx()), size.height * 0.22f),
                        size = Size(sq, sq),
                        cornerRadius = cr
                    )
                }
                for (i in 0 until 6) {
                    drawCircle(
                        color = if (i == 1) pack.secondaryAccent else Color.White.copy(alpha = 0.65f),
                        radius = 4.dp.toPx(),
                        center = Offset(size.width * 0.23f + i * 14.dp.toPx(), size.height * 0.82f)
                    )
                }
            }
            ConsoleUiStyle.DUAL_SCREEN_3D -> {
                drawRoundRect(
                    color = Color(0xFF2E4369),
                    topLeft = Offset(size.width * 0.08f, 14.dp.toPx()),
                    size = Size(size.width * 0.84f, size.height * 0.34f),
                    cornerRadius = cr
                )
                drawRoundRect(
                    color = Color(0xFFD8E1EC).copy(alpha = 0.85f),
                    topLeft = Offset(size.width * 0.08f, size.height * 0.54f),
                    size = Size(size.width * 0.84f, size.height * 0.42f),
                    cornerRadius = cr
                )
            }
        }
    }
}

val UI_HUB_FILTERS = listOf("ALL", "INSTALLED", "LIVING ROOM", "HANDHELD")

fun filterUiPacks(packs: List<UiThemePack>, filter: String, installed: Set<String>): List<UiThemePack> {
    return when (filter) {
        "INSTALLED" -> packs.filter { installed.contains(it.id) }
        "LIVING ROOM" -> packs.filter {
            it.style == ConsoleUiStyle.NEXTGEN_MEDIA_BAR || it.style == ConsoleUiStyle.ECHO_DEFAULT
        }
        "HANDHELD" -> packs.filter {
            it.style == ConsoleUiStyle.HYBRID_DECK || it.style == ConsoleUiStyle.DUAL_SCREEN_3D
        }
        else -> packs
    }
}
