package com.echo.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

@Composable
fun GameCarousel(
    apps: List<AppItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onOptions: (AppItem) -> Unit,
    currentFocusZone: FocusZone,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardBaseWidth = 168.dp
    val cardBaseHeight = 168.dp
    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedIndex, apps.size) {
        if (apps.isNotEmpty() && selectedIndex in apps.indices) {
            lazyListState.animateScrollToItem(
                index = selectedIndex,
                scrollOffset = -((screenWidth.value / 2) - (cardBaseWidth.value / 2)).toInt()
            )
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
            // Horizontal LazyRow Carousel
            LazyRow(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(218.dp),
                contentPadding = PaddingValues(horizontal = screenWidth / 2 - cardBaseWidth / 2),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(apps, key = { _, item -> item.id }) { index, item ->
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

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Hero Section
            if (activeApp != null) {
                ConsoleGameHero(
                    app = activeApp,
                    isCarouselFocused = isCarouselFocused,
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
    val targetScale = if (isSelected) 1.12f else 0.94f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ConsoleCardScale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && isCarouselFocused -> SurfaceBorderFocused
            isSelected -> SurfaceBorderFocused.copy(alpha = 0.5f)
            else -> SurfaceBorder
        },
        animationSpec = tween(160),
        label = "ConsoleCardBorder"
    )

    val elevation = if (isSelected) 18.dp else 2.dp

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .scale(animatedScale)
            .shadow(elevation, shape = RoundedCornerShape(24.dp), spotColor = ElectricCyan)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCard)
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Specular top rim highlight (authentic console hardware shader look)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Radial accent background fill
            val radialBrush = Brush.radialGradient(
                colors = listOf(
                    app.accentColor.copy(alpha = if (isSelected) 0.28f else 0.08f),
                    CarbonDark
                ),
                center = Offset(size.width / 2f, size.height * 0.40f),
                radius = size.width * 0.8f
            )
            drawRect(brush = radialBrush)

            // Top rim specular highlight line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        SpecularHighlight,
                        Color.Transparent
                    )
                ),
                start = Offset(size.width * 0.15f, 1f),
                end = Offset(size.width * 0.85f, 1f),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // App/Game Icon
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(94.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(app.accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(2).uppercase(),
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active indicator on bottom
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 7.dp)
                    .size(6.dp)
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
    onPlayClicked: () -> Unit,
    onOptionsClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Game Title
        Text(
            text = app.label,
            color = PlatinumWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.6.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        // Metadata Subtitle Line (Status • Category)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (app.isGame) "READY TO PLAY" else "APPLICATION",
                color = ElectricCyan,
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

        Spacer(modifier = Modifier.height(4.dp))

        // Dual Action Buttons: [ ▶ Play ] and [ ⋯ Options ]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Pill Play Button
            PlayPillButton(
                isCarouselFocused = isCarouselFocused,
                onClick = onPlayClicked
            )

            // Secondary Options Button
            OptionsPillButton(
                onClick = onOptionsClicked
            )
        }
    }
}

@Composable
private fun PlayPillButton(
    isCarouselFocused: Boolean,
    onClick: () -> Unit
) {
    val glowColor by animateColorAsState(
        targetValue = if (isCarouselFocused) ElectricCyan else Color.Transparent,
        animationSpec = tween(200),
        label = "PlayGlow"
    )

    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isCarouselFocused) 12.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = ElectricCyan
            )
            .clip(RoundedCornerShape(24.dp))
            .background(PureWhite)
            .border(2.dp, glowColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 7.dp),
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
                fontSize = 13.5.sp,
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
                        text = "A",
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
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "⋯ Options",
                color = TextSecondary,
                fontSize = 12.5.sp,
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
                    text = "Y",
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
            .height(200.dp)
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
            text = "Press (X) or DPAD DOWN to access All Apps",
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}
