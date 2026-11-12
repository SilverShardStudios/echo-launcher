package com.echo.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.AppItem
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.ObsidianBackground
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardBorder
import com.echo.launcher.ui.theme.TextMuted

@Composable
fun AllAppsModal(
    isOpen: Boolean,
    apps: List<AppItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    onLaunch: (AppItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        val gridState = rememberLazyGridState()

        // Auto-scroll when navigating with controller
        LaunchedEffect(selectedIndex) {
            if (apps.isNotEmpty() && selectedIndex in apps.indices) {
                gridState.animateScrollToItem(selectedIndex)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBackground.copy(alpha = 0.94f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 36.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inner click
                    )
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ALL INSTALLED APPS",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${apps.size} Items",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Back / Close Hint [B] Back
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
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
                            Text(
                                text = "B",
                                color = PureWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Close",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // App Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(apps, key = { _, it -> it.id }) { index, item ->
                        val isSelected = index == selectedIndex
                        AllAppsGridItem(
                            app = item,
                            isSelected = isSelected,
                            onClick = {
                                onIndexSelected(index)
                                onLaunch(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllAppsGridItem(
    app: AppItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ElectricCyan else SurfaceCardBorder,
        animationSpec = tween(150),
        label = "GridBorder"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ElectricCyan.copy(alpha = 0.18f) else SurfaceCard.copy(alpha = 0.7f),
        animationSpec = tween(150),
        label = "GridBg"
    )

    Column(
        modifier = Modifier
            .scale(if (isSelected) 1.08f else 1.0f)
            .shadow(
                elevation = if (isSelected) 12.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = ElectricCyan
            )
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(app.accentColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(2).uppercase(),
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.label,
            color = if (isSelected) ElectricCyan else PureWhite,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
