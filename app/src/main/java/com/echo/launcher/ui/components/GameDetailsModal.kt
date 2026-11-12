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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.AppItem
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.ObsidianBackground
import com.echo.launcher.ui.theme.PlatinumWhite
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCard
import com.echo.launcher.ui.theme.SurfaceCardElevated
import com.echo.launcher.ui.theme.TextMuted
import com.echo.launcher.ui.theme.TextSecondary

@Composable
fun GameDetailsModal(
    isOpen: Boolean,
    app: AppItem?,
    onLaunch: () -> Unit,
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
                    .width(480.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume inner click
                    )
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
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

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = app.label,
                                color = PlatinumWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = app.packageName,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            // Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (app.isGame) "CONSOLE GAME" else if (app.isMedia) "MEDIA APPLICATION" else "SYSTEM APP",
                                    color = ElectricCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite)
                                .clickable(onClick = {
                                    onDismiss()
                                    onLaunch()
                                })
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start Application",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // App Info button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCardElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable(onClick = {
                                    openAppInfo(context, app.packageName)
                                })
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Android App Info",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Bottom Dismiss Hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 12.sp,
                            modifier = Modifier.clickable(onClick = onDismiss)
                        )
                    }
                }
            }
        }
    }
}

private fun openAppInfo(context: Context, packageName: String) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
