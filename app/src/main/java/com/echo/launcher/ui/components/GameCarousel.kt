package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.AppItem
import com.echo.launcher.data.FocusZone
import com.echo.launcher.data.GamepadLayout
import com.echo.launcher.ui.theme.BadgeBlue
import com.echo.launcher.ui.theme.CarbonDark
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.SpecularHighlight
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceBorderFocused
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardElevated
import com.echo.launcher.ui.theme.TextMuted
import com.echo.launcher.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun GameCarousel(
    apps: List<AppItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    currentFocusZone: FocusZone,
    gamepadLayout: GamepadLayout = GamepadLayout.ABXY,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Responsive sizing for Anbernic RG DS, 4:3, 1:1 (RG Cube) and 16:9 displays
    val isCompactScreen = screenHeight < 500.dp
    val cardBaseWidth = if (isCompactScreen) 136.dp else 164.dp
    val cardBaseHeight = if (isCompactScreen) 136.dp else 164.dp
    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            val targetOffset = -((screenWidth.value / 2f) - (cardBaseWidth.value / 2f)).toInt()
            val firstVisible = lazyListState.firstVisibleItemIndex
            // Instant snap if jumping far, smooth scroll for single-step D-pad movement
            if (abs(selectedIndex - firstVisible) > 4) {
                lazyListState.scrollToItem(selectedIndex, targetOffset)
            } else {
                lazyListState.animateScrollToItem(selectedIndex, targetOffset)
            }
        }
    }

    val isCarouselFocused = currentFocusZone == FocusZone.CAROUSEL
    val activeApp = apps.getOrNull(selectedIndex)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (apps.isEmpty()) {
            EmptyCarouselState()
        } else {
            LazyRow(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompactScreen) 174.dp else 214.dp),
                contentPadding = PaddingValues(horizontal = screenWidth / 2 - cardBaseWidth / 2),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    items = apps,
                    key = { _, item -> item.id },
                    contentType = { _, _ -> "game_card" }
                ) { index, item ->
                    val isSelected = index == selectedIndex
                    ConsoleGameCard(
                        app = item,
                        isSelected = isSelected,
                        isCarouselFocused = isCarouselFocused,
                        cardWidth = cardBaseWidth,
                        cardHeight = cardBaseHeight,
                        onClick = {
                            if (isSelected) {
                                onLaunch(item)
                            } else {
                                onIndexSelected(index)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeApp != null) {
                ConsoleGameHero(
                    app = activeApp,
                    isCarouselFocused = isCarouselFocused,
                    gamepadLayout = gamepadLayout,
                    onPlayClicked = { onLaunch(activeApp) },
                    onOptionsClicked = { onOptions(activeApp) }
                )
            }
        }
    }
}

@Composable
private fun ConsoleGameCard(
    app: AppItem,
    isSelected: Boolean,
    isCarouselFocused: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    onClick: () -> Unit
) {
    val targetScale = if (isSelected) 1.10f else 0.94f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "ConsoleCardScale"
    )

    val borderColor = when {
        isSelected && isCarouselFocused -> SurfaceBorderFocused
        isSelected -> SurfaceBorderFocused.copy(alpha = 0.55f)
        else -> SurfaceBorder
    }

    val cardShape = remember { RoundedCornerShape(22.dp) }

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(cardShape)
            .background(SurfaceCard)
            .drawWithCache {
                val radialBrush = Brush.radialGradient(
                    colors = listOf(
                        app.accentColor.copy(alpha = if (isSelected) 0.32f else 0.08f),
                        CarbonDark
                    ),
                    center = Offset(size.width / 2f, size.height * 0.40f),
                    radius = (size.width * 0.8f).coerceAtLeast(1f)
                )
                val specLine = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        SpecularHighlight,
                        Color.Transparent
                    )
                )
                onDrawBehind {
                    drawRect(brush = radialBrush)
                    drawLine(
                        brush = specLine,
                        start = Offset(size.width * 0.15f, 1f),
                        end = Offset(size.width * 0.85f, 1f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(app.accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(2).uppercase(),
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black.copy(alpha = 0.60f))
                .border(0.6.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = app.systemTag,
                color = if (app.isEmulator) BadgeBlue else TextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        if (app.isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFEAB308).copy(alpha = 0.22f))
                    .border(0.8.dp, Color(0xFFEAB308), RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "★",
                    color = Color(0xFFEAB308),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 7.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isCarouselFocused) ElectricCyan else TextMuted)
            )
        }
    }
}

@Composable
private fun ConsoleGameHero(
    app: AppItem,
    isCarouselFocused: Boolean,
    gamepadLayout: GamepadLayout,
    onPlayClicked: () -> Unit,
    onOptionsClicked: () -> Unit
) {
    val confirmBtn = if (gamepadLayout == GamepadLayout.ABXY) "A" else "B"
    val optionsBtn = "Y"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = app.label,
            color = PlatinumWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.6.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (app.isEmulator) "RETRO EMULATOR" else if (app.isGame) "READY TO PLAY" else "APPLICATION",
                color = if (app.isEmulator) BadgeBlue else ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "•",
                color = TextMuted,
                fontSize = 10.sp
            )
            Text(
                text = app.packageName.takeLast(28),
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlayPillButton(
                isCarouselFocused = isCarouselFocused,
                confirmButtonLabel = confirmBtn,
                onClick = onPlayClicked
            )

            OptionsPillButton(
                optionsButtonLabel = optionsBtn,
                onClick = onOptionsClicked
            )
        }
    }
}

@Composable
private fun PlayPillButton(
    isCarouselFocused: Boolean,
    confirmButtonLabel: String,
    onClick: () -> Unit
) {
    val glowColor by animateColorAsState(
        targetValue = if (isCarouselFocused) ElectricCyan else Color.Transparent,
        animationSpec = tween(150),
        label = "PlayGlow"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(PureWhite)
            .border(2.dp, glowColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = path, color = Color.Black)
            }

            Text(
                text = "Play",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            if (isCarouselFocused) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmButtonLabel,
                        color = Color.Black,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionsPillButton(
    optionsButtonLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCardElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "⋯ Options",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAB308).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionsButtonLabel,
                    color = Color(0xFFEAB308),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun EmptyCarouselState() {
    Column(
        modifier = Modifier
            .height(170.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No items in this category",
            color = TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Press (X) or DPAD DOWN to access All Apps & Emulators",
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}
