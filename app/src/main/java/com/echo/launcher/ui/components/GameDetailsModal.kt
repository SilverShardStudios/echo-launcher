package com.echo.launcher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.echo.launcher.data.GameStats
import com.echo.launcher.data.formatDate
import com.echo.launcher.data.formatPlaytime
import com.echo.launcher.data.formatRelativeTime
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
fun GameDetailsModal(
    isOpen: Boolean,
    app: AppItem?,
    stats: GameStats? = null,
    hasUsageAccess: Boolean = false,
    selectedActionIndex: Int = 0,
    onSelectActionIndex: (Int) -> Unit = {},
    onLaunch: () -> Unit,
    onBoostAndLaunch: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onOpenAppInfo: (() -> Unit)? = null,
    onRequestUsageAccess: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen && app != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        if (app == null) return@AnimatedVisibility
        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBackground.copy(alpha = 0.90f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inner click
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with Icon & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (app.iconBitmap != null) {
                            Image(
                                bitmap = app.iconBitmap,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(app.accentColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.label.take(2).uppercase(),
                                    color = PureWhite,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (app.isFavorite) "★ ${app.label}" else app.label,
                                color = PlatinumWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = app.packageName,
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElectricCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (app.isEmulator) "EMULATOR (${app.systemTag})"
                                        else if (app.isGame) "CONSOLE GAME"
                                        else if (app.isMedia) "MEDIA APP"
                                        else "SYSTEM APP",
                                        color = ElectricCyan,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (app.isFavorite) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEAB308).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "★ PINNED FAVORITE",
                                            color = Color(0xFFEAB308),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4-Column Real Telemetry Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCardElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryStatCell(
                            label = "PLAYTIME (30D)",
                            value = if (hasUsageAccess) formatPlaytime(stats?.playtimeMs30d ?: 0L) else "Enable →",
                            valueColor = if (hasUsageAccess) StatusGreen else ElectricCyan,
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (!hasUsageAccess && onRequestUsageAccess != null) {
                                        Modifier.clickable(onClick = onRequestUsageAccess)
                                    } else Modifier
                                )
                        )
                        TelemetryStatCell(
                            label = "LAST PLAYED",
                            value = if (hasUsageAccess) formatRelativeTime(stats?.lastPlayedMs ?: 0L) else "—",
                            valueColor = PlatinumWhite,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryStatCell(
                            label = "INSTALLED",
                            value = formatDate(stats?.installedMs ?: 0L),
                            valueColor = PlatinumWhite,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryStatCell(
                            label = "VERSION",
                            value = stats?.versionName?.let { "v$it" } ?: "—",
                            valueColor = TextSecondary,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary Row (Actions 0 and 1): Boost & Launch + Standard Launch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (onBoostAndLaunch != null) {
                            val isFocus0 = selectedActionIndex == 0
                            Box(
                                modifier = Modifier
                                    .weight(1.15f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StatusGreen)
                                    .border(
                                        width = if (isFocus0) 2.5.dp else 0.dp,
                                        color = if (isFocus0) ElectricCyan else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(onClick = {
                                        onSelectActionIndex(0)
                                        onDismiss()
                                        onBoostAndLaunch()
                                    })
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "⚡ Boost RAM & Launch",
                                        color = Color.Black,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (isFocus0) KeyBadgeA(darkTint = true)
                                }
                            }
                        }

                        val isFocus1 = selectedActionIndex == 1
                        Box(
                            modifier = Modifier
                                .weight(0.85f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite)
                                .border(
                                    width = if (isFocus1) 2.5.dp else 0.dp,
                                    color = if (isFocus1) ElectricCyan else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(onClick = {
                                    onSelectActionIndex(1)
                                    onDismiss()
                                    onLaunch()
                                })
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "▶ Launch Now",
                                    color = Color.Black,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isFocus1) KeyBadgeA(darkTint = true)
                            }
                        }
                    }

                    // Secondary Row (Actions 2 and 3): Pin Favorite + Android App Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (onToggleFavorite != null) {
                            val isFocus2 = selectedActionIndex == 2
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCardElevated)
                                    .border(
                                        width = if (isFocus2) 2.dp else 1.dp,
                                        color = if (isFocus2) ElectricCyan else if (app.isFavorite) Color(0xFFEAB308) else SurfaceBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectActionIndex(2)
                                        onToggleFavorite()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (app.isFavorite) "★ Unpin" else "☆ Pin Favorite",
                                        color = if (app.isFavorite) Color(0xFFEAB308) else PlatinumWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isFocus2) KeyBadgeA(darkTint = false)
                                }
                            }
                        }

                        val isFocus3 = selectedActionIndex == 3
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCardElevated)
                                .border(
                                    width = if (isFocus3) 2.dp else 1.dp,
                                    color = if (isFocus3) ElectricCyan else SurfaceBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectActionIndex(3)
                                    if (onOpenAppInfo != null) onOpenAppInfo()
                                    else openAndroidAppInfo(context, app.packageName)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Android App Info",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isFocus3) KeyBadgeA(darkTint = false)
                            }
                        }
                    }

                    // Bottom Dismiss Hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "D-Pad Select  •  [A] Confirm  •  [B] Close",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onDismiss)
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Back to Console",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryStatCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun KeyBadgeA(darkTint: Boolean) {
    Box(
        modifier = Modifier
            .size(15.dp)
            .clip(CircleShape)
            .background(if (darkTint) Color.Black.copy(alpha = 0.22f) else ElectricCyan),
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

fun openAndroidAppInfo(context: Context, packageName: String) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
