package com.echo.launcher.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Data representation of an installed launchable application, retro emulator, or game.
 */
data class AppItem(
    val id: String,
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconBitmap: ImageBitmap?,
    val isGame: Boolean,
    val isMedia: Boolean,
    val isEmulator: Boolean,
    val systemTag: String,
    val accentColor: Color
)

enum class LauncherTab {
    GAMES,
    MEDIA
}

enum class FocusZone {
    TOP_TABS,
    CAROUSEL,
    DOCK,
    ALL_APPS_MODAL
}

enum class DockAction {
    ALL_APPS,
    ECHO_UPDATES,
    WIFI_BT,
    SETTINGS
}
