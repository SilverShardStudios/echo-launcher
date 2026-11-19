package com.echo.launcher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color

class UiThemeRepository(context: Context) {

    private val prefs: SharedPreferences? = try {
        val storageCtx = context.applicationContext.createDeviceProtectedStorageContext()
        storageCtx.getSharedPreferences("echo_ui_hub_prefs", Context.MODE_PRIVATE)
    } catch (_: Throwable) {
        try {
            context.getSharedPreferences("echo_ui_hub_prefs", Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }

    val availablePacks: List<UiThemePack> = listOf(
        UiThemePack(
            style = ConsoleUiStyle.ECHO_DEFAULT,
            id = "echo_core_os",
            title = "ECHO OS Core",
            subtitle = "Default Cybernetic Console Shell",
            description = "High-contrast obsidian console interface with responsive ambient lighting, centered game carousel, and floating quick dock.",
            version = "v1.3.0",
            sizeText = "Built-in",
            primaryAccent = Color(0xFF00E5FF),
            secondaryAccent = Color(0xFF10B981),
            layoutTag = "CYBER CAROUSEL",
            isBuiltIn = true
        ),
        UiThemePack(
            style = ConsoleUiStyle.NEXTGEN_MEDIA_BAR,
            id = "nextgen_5_horizon",
            title = "Next-Gen 5 Horizon UI",
            subtitle = "Top Media Ribbon & Cinematic Hero Canvas",
            description = "Inspired by next-generation living-room consoles. Features a top-left mini icon ribbon, golden-amber & royal-blue atmospheric particles, and a massive lower-left cinematic hero showcase.",
            version = "v2.4.0",
            sizeText = "4.8 MB Pack",
            primaryAccent = Color(0xFF3B82F6),
            secondaryAccent = Color(0xFFF59E0B),
            layoutTag = "TOP RIBBON + HERO"
        ),
        UiThemePack(
            style = ConsoleUiStyle.HYBRID_DECK,
            id = "hybrid_portable_deck",
            title = "Hybrid Portable Deck UI",
            subtitle = "Clean Square Game Tiles & Circular System Dock",
            description = "Inspired by modern hybrid handheld consoles. Features large square game cartridge tiles with an animated dual-tone cyan/crimson focus frame, floating title header, and circular bottom system buttons.",
            version = "v3.1.0",
            sizeText = "3.6 MB Pack",
            primaryAccent = Color(0xFF00C3E3),
            secondaryAccent = Color(0xFFFF4554),
            layoutTag = "SQUARE TILE ROW"
        ),
        UiThemePack(
            style = ConsoleUiStyle.DUAL_SCREEN_3D,
            id = "stereoscopic_dual_3d",
            title = "Stereoscopic Dual-3D UI",
            subtitle = "Upper 3D Banner Stage & Lower Touch Grid Matrix",
            description = "Inspired by classic dual-screen handhelds & built for Anbernic RG DS. Features a rotating 3D emblem stage on top and a customizable lower touch grid with +/- grid zoom controls.",
            version = "v2.8.0",
            sizeText = "5.2 MB Pack",
            primaryAccent = Color(0xFFE11D48),
            secondaryAccent = Color(0xFF0284C7),
            layoutTag = "3D STAGE + TOUCH GRID"
        )
    )

    fun getDownloadedIds(): Set<String> {
        val saved = try {
            prefs?.getStringSet("downloaded_ui_packs", null)
        } catch (_: Throwable) {
            null
        }
        val result = HashSet<String>()
        result.add("echo_core_os")
        if (saved != null) {
            result.addAll(saved)
        }
        return result
    }

    fun markPackDownloaded(packId: String): Set<String> {
        val current = HashSet(getDownloadedIds())
        current.add(packId)
        try {
            prefs?.edit()?.putStringSet("downloaded_ui_packs", current)?.apply()
        } catch (_: Throwable) {}
        return current
    }

    fun removePackDownload(packId: String): Set<String> {
        if (packId == "echo_core_os") return getDownloadedIds()
        val current = HashSet(getDownloadedIds())
        current.remove(packId)
        try {
            prefs?.edit()?.putStringSet("downloaded_ui_packs", current)?.apply()
        } catch (_: Throwable) {}
        return current
    }

    fun getActiveUiStyle(): ConsoleUiStyle {
        val raw = try {
            prefs?.getString("active_ui_style", ConsoleUiStyle.ECHO_DEFAULT.name)
        } catch (_: Throwable) {
            ConsoleUiStyle.ECHO_DEFAULT.name
        }
        return try {
            ConsoleUiStyle.valueOf(raw ?: ConsoleUiStyle.ECHO_DEFAULT.name)
        } catch (_: Throwable) {
            ConsoleUiStyle.ECHO_DEFAULT
        }
    }

    fun setActiveUiStyle(style: ConsoleUiStyle) {
        try {
            prefs?.edit()?.putString("active_ui_style", style.name)?.apply()
        } catch (_: Throwable) {}
    }
}
