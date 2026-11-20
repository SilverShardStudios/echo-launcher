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
            title = "ECHO OS Cyber Core",
            subtitle = "Default Cybernetic Console OS",
            description = "High-contrast obsidian console interface with responsive ambient lighting, centered game carousel, and floating quick dock.",
            version = "v1.4.0",
            sizeText = "Built-in",
            ratingText = "★ 4.9",
            downloadsText = "Core OS",
            primaryAccent = Color(0xFF00E5FF),
            secondaryAccent = Color(0xFF10B981),
            layoutTag = "CYBER CAROUSEL",
            featureHighlights = listOf(
                "Dynamic dominant-color ambient backlight",
                "Centered 120Hz hardware-accelerated card carousel",
                "Floating glass system dock & controller button guide"
            ),
            isBuiltIn = true
        ),
        UiThemePack(
            style = ConsoleUiStyle.NEXTGEN_MEDIA_BAR,
            id = "nextgen_5_horizon",
            title = "Horizon 5 Next-Gen Media UI",
            subtitle = "Full-Screen Top Media Bar, Trophy Hub & Control Center",
            description = "Full-screen living-room console architecture with top Games | Media header, top-left mini squircle media ribbon, golden particle flare backdrop, Trophy & Playtime tracker cards, and a pop-up bottom Control Center bar.",
            version = "v3.0.0",
            sizeText = "6.4 MB Pack",
            ratingText = "★ 5.0",
            downloadsText = "48.2K",
            primaryAccent = Color(0xFF2563EB),
            secondaryAccent = Color(0xFFF59E0B),
            layoutTag = "FULL-SCREEN MEDIA BAR",
            featureHighlights = listOf(
                "Top Games | Media text switcher + Search, Gear & Profile bar",
                "Top-left mini squircle icon strip with inline active title",
                "Bottom-left Play Game pill + Quick Control Center dock bar",
                "Bottom-right Trophy Progress & Playtime Activity cards"
            )
        ),
        UiThemePack(
            style = ConsoleUiStyle.HYBRID_DECK,
            id = "hybrid_portable_deck",
            title = "N-Hybrid Portable Deck UI",
            subtitle = "Full-Screen Square Game Cartridges, Light/Dark Theme & Circular Dock",
            description = "Authentic hybrid handheld home screen with floating cyan title above large 1:1 square game tiles, dual-layer pulsing cyan focus ring, Basic White / Basic Black theme toggle, and 6 colorful circular bottom system buttons.",
            version = "v3.4.0",
            sizeText = "5.1 MB Pack",
            ratingText = "★ 4.9",
            downloadsText = "62.7K",
            primaryAccent = Color(0xFF00C3E3),
            secondaryAccent = Color(0xFFFF4554),
            layoutTag = "SQUARE CARTRIDGE DECK",
            featureHighlights = listOf(
                "1-Tap Basic Black / Basic White console theme toggle",
                "Floating cyan title & cartridge indicator above focused square tile",
                "6 circular system bubbles (News, UI Shop, Album, Boost, Settings, Sleep)",
                "Full-width bottom divider rule with dual-controller status & (+) Options"
            )
        ),
        UiThemePack(
            style = ConsoleUiStyle.DUAL_SCREEN_3D,
            id = "stereoscopic_dual_3d",
            title = "Dual-Screen 3D Handheld UI",
            subtitle = "Upper 3D Rotating Cartridge Stage + Lower Stylus Touch Grid & Bezel",
            description = "Authentic dual-screen handheld interface! Top screen features a 3D rotating cartridge stage with 5-bar signal, play-coin counter, date/time & 3D depth slider. Bottom touch screen features a 3D beveled icon grid with zoom controls and 3-part bottom bezel.",
            version = "v3.2.0",
            sizeText = "5.8 MB Pack",
            ratingText = "★ 5.0",
            downloadsText = "54.9K",
            primaryAccent = Color(0xFFE11D48),
            secondaryAccent = Color(0xFF0284C7),
            layoutTag = "UPPER 3D + LOWER GRID",
            featureHighlights = listOf(
                "Top 3D rotating box-art stage with interactive 3D Depth slider",
                "Top status bar with 5-bar wireless badge, Play Coins & live date/clock",
                "Lower touch screen with [Miiverse / Friends / Notifications / Browser] applet bar",
                "Dynamic [⊟]/[⊞] grid size zoom (1-Row up to 6-Column grid) + [Options | Open | UI Hub] bottom bezel"
            )
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

    fun markAllPacksDownloaded(): Set<String> {
        val allIds = availablePacks.map { it.id }.toSet()
        try {
            prefs?.edit()?.putStringSet("downloaded_ui_packs", allIds)?.apply()
        } catch (_: Throwable) {}
        return allIds
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

    var isHybridLightTheme: Boolean
        get() = try { prefs?.getBoolean("hybrid_light_theme", false) ?: false } catch (_: Throwable) { false }
        set(value) {
            try { prefs?.edit()?.putBoolean("hybrid_light_theme", value)?.apply() } catch (_: Throwable) {}
        }
}
