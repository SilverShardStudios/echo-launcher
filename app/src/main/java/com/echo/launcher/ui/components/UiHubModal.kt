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
    onSelectIndex: (Int) -> Unit,
    onDownloadOrApply: (UiThemePack) -> Unit,
    onUninstallPack: (UiThemePack) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        val listState = rememberLazyListState()

        LaunchedEffect(selectedIndex) {
            if (packs.isNotEmpty() && selectedIndex in packs.indices) {
                listState.animateScrollToItem(selectedIndex)
            }
        }

        val focusedPack = packs.getOrNull(selectedIndex) ?: packs.firstOrNull()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBackground.copy(alpha = 0.95f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 28.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inner clicks
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
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
                                .background(ElectricCyan.copy(alpha = 0.18f))
                                .border(1.2.dp, ElectricCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❖", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text(
                                text = "ECHO UI HUB & CONSOLE SHELL STORE",
                                color = PureWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "DOWNLOAD & HOT-SWAP COMPLETE CONSOLE INTERFACE ARCHITECTURES",
                                color = ElectricCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusGreen.copy(alpha = 0.15f))
                                .border(1.dp, StatusGreen.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${downloadedPackIds.size}/${packs.size} INSTALLED",
                                color = StatusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
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
                                Text(text = "B", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                            Text(
                                text = "Close Hub",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // UI Cards Carousel
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(packs, key = { _, pack -> pack.id }) { index, pack ->
                        val isSelected = index == selectedIndex
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

                // Selected UI Pack Details & Action Bar
                if (focusedPack != null) {
                    val isDownloaded = downloadedPackIds.contains(focusedPack.id)
                    val isActive = activeStyle == focusedPack.style
                    val isDownloading = downloadingPackId == focusedPack.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SurfaceCardElevated)
                            .border(1.dp, focusedPack.primaryAccent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = focusedPack.title,
                                    color = PureWhite,
                                    fontSize = 16.sp,
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
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Text(
                                text = focusedPack.description,
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isDownloaded && !focusedPack.isBuiltIn && !isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                        .clickable { onUninstallPack(focusedPack) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Uninstall",
                                        color = TextMuted,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val btnBg = when {
                                isActive -> StatusGreen
                                isDownloaded -> focusedPack.primaryAccent
                                else -> PureWhite
                            }

                            val btnLabel = when {
                                isDownloading -> "DOWNLOADING ${(downloadProgress * 100).toInt()}%..."
                                isActive -> "✓ ACTIVE UI SHELL"
                                isDownloaded -> "⚡ APPLY UI SHELL"
                                else -> "⬇ DOWNLOAD & INSTALL"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(btnBg)
                                    .clickable(enabled = !isDownloading) {
                                        onDownloadOrApply(focusedPack)
                                    }
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = btnLabel,
                                        color = Color.Black,
                                        fontSize = 12.5.sp,
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
        targetValue = if (isSelected) 1.05f else 0.96f,
        animationSpec = tween(160, easing = FastOutSlowInEasing),
        label = "UiPackScale"
    )

    val borderCol by animateColorAsState(
        targetValue = when {
            isSelected -> pack.primaryAccent
            isActive -> StatusGreen.copy(alpha = 0.7f)
            else -> SurfaceBorder
        },
        animationSpec = tween(160),
        label = "UiPackBorder"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress.coerceIn(0f, 1f),
        animationSpec = tween(120),
        label = "UiDownloadProgress"
    )

    Column(
        modifier = Modifier
            .width(235.dp)
            .height(210.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(if (isSelected) 2.2.dp else 1.dp, borderCol, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Live Vector Schematic Preview of the UI Layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            pack.primaryAccent.copy(alpha = 0.25f),
                            pack.secondaryAccent.copy(alpha = 0.14f),
                            Color(0xFF0A0E17)
                        )
                    )
                )
                .border(1.dp, pack.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            UiSchematicCanvas(pack = pack)

            // Top-right badge
            val badgeText = when {
                isActive -> "ACTIVE"
                isDownloaded -> "INSTALLED"
                else -> "CLOUD PACK"
            }
            val badgeColor = when {
                isActive -> StatusGreen
                isDownloaded -> pack.primaryAccent
                else -> Color.White.copy(alpha = 0.75f)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .border(0.8.dp, badgeColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = pack.layoutTag,
                color = pack.primaryAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = pack.title,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pack.subtitle,
                color = TextMuted,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Download progress bar or status pill
        if (isDownloading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
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
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isActive) "● Running" else if (isDownloaded) "▶ Tap to Apply" else "⬇ Tap to Download",
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
        val cr = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        when (pack.style) {
            ConsoleUiStyle.ECHO_DEFAULT -> {
                // Center carousel cards + bottom floating dock
                val cardW = size.width * 0.24f
                val cardH = size.height * 0.45f
                val cy = size.height * 0.22f
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
                // Bottom dock pill
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.28f),
                    topLeft = Offset(size.width * 0.22f, size.height * 0.80f),
                    size = Size(size.width * 0.56f, size.height * 0.14f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
            ConsoleUiStyle.NEXTGEN_MEDIA_BAR -> {
                // Top-left small icon ribbon + bottom-left hero artwork & CTA
                for (i in 0 until 5) {
                    val isFirst = i == 1
                    val s = if (isFirst) 16.dp.toPx() else 12.dp.toPx()
                    drawRoundRect(
                        color = if (isFirst) pack.secondaryAccent else Color.White.copy(alpha = 0.30f),
                        topLeft = Offset(8.dp.toPx() + i * 20.dp.toPx(), 6.dp.toPx()),
                        size = Size(s, s),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
                // Hero title & play pill bottom-left
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(8.dp.toPx(), size.height * 0.52f),
                    size = Size(size.width * 0.48f, 9.dp.toPx()),
                    cornerRadius = cr
                )
                drawRoundRect(
                    color = pack.primaryAccent,
                    topLeft = Offset(8.dp.toPx(), size.height * 0.72f),
                    size = Size(size.width * 0.28f, 12.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
            ConsoleUiStyle.HYBRID_DECK -> {
                // Large square tiles row + bottom circular icons
                val sq = size.height * 0.46f
                for (i in 0 until 4) {
                    val active = i == 1
                    drawRoundRect(
                        color = if (active) pack.primaryAccent else Color.White.copy(alpha = 0.22f),
                        topLeft = Offset(8.dp.toPx() + i * (sq + 8.dp.toPx()), size.height * 0.16f),
                        size = Size(sq, sq),
                        cornerRadius = cr
                    )
                }
                // Circular system buttons on bottom
                for (i in 0 until 5) {
                    drawCircle(
                        color = if (i % 2 == 0) pack.primaryAccent.copy(alpha = 0.7f) else pack.secondaryAccent.copy(alpha = 0.7f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(size.width * 0.26f + i * 16.dp.toPx(), size.height * 0.84f)
                    )
                }
            }
            ConsoleUiStyle.DUAL_SCREEN_3D -> {
                // Top 3D banner box + bottom grid of squares
                drawRoundRect(
                    color = pack.primaryAccent.copy(alpha = 0.5f),
                    topLeft = Offset(size.width * 0.18f, 2.dp.toPx()),
                    size = Size(size.width * 0.64f, size.height * 0.36f),
                    cornerRadius = cr
                )
                // Divider hinge
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(0f, size.height * 0.44f),
                    end = Offset(size.width, size.height * 0.44f),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Lower touch grid (2 rows x 4 cols)
                val cell = 11.dp.toPx()
                val startX = size.width * 0.22f
                for (r in 0 until 2) {
                    for (c in 0 until 4) {
                        drawRoundRect(
                            color = if (r == 0 && c == 1) pack.secondaryAccent else Color.White.copy(alpha = 0.28f),
                            topLeft = Offset(startX + c * (cell + 5.dp.toPx()), size.height * 0.52f + r * (cell + 4.dp.toPx())),
                            size = Size(cell, cell),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
