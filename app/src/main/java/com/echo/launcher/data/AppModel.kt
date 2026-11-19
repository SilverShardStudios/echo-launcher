package com.echo.launcher.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Data representation of an installed launchable application, retro emulator, or game.
 * Marked @Immutable so Jetpack Compose skips unnecessary card recompositions during scrolling.
 */
@Immutable
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
    val accentColor: Color,
    val isFavorite: Boolean = false
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
    OPTIMIZE,
    UI_HUB,
    ALL_APPS,
    ECHO_UPDATES,
    WIFI_BT,
    SETTINGS
}

enum class ConsoleUiStyle {
    ECHO_DEFAULT,
    NEXTGEN_MEDIA_BAR,
    HYBRID_DECK,
    DUAL_SCREEN_3D
}

@Immutable
data class UiThemePack(
    val style: ConsoleUiStyle,
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val version: String,
    val sizeText: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val layoutTag: String,
    val isBuiltIn: Boolean = false
)
