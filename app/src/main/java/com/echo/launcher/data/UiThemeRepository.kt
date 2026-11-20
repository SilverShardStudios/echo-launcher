package com.echo.launcher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color

/**
 * Catalog of console UI shells. All shells ship inside the APK; "installing" one simply
 * enables it in the hub so the home screen stays uncluttered until you want it.
 */
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
            version = "v1.5.0",
            sizeText = "Built-in",
            primaryAccent = Color(0xFF00E5FF),
            secondaryAccent = Color(0xFF10B981),
            layoutTag = "CYBER CAROUSEL",
            featureHighlights = listOf(
                "Ambient backlight tinted by each game's icon color",
                "Centered hardware-accelerated card carousel",
                "Floating glass system dock & controller button guide"
            ),
            isBuiltIn = true
        ),
        UiThemePack(
            style = ConsoleUiStyle.NEXTGEN_MEDIA_BAR,
            id = "nextgen_5_horizon",
            title = "Horizon 5 Next-Gen Media UI",
            subtitle = "Top Media Bar, Real Playtime Stats & Control Center",
            description = "Living-room console layout: Games | Media header, top-left icon ribbon with inline title, particle-flare backdrop, Play Game pill, real playtime / last-played cards and a pop-up Control Center.",
            version = "v3.1.0",
            sizeText = "Included",
            primaryAccent = Color(0xFF2563EB),
            secondaryAccent = Color(0xFFF59E0B),
            layoutTag = "FULL-SCREEN MEDIA BAR",
            featureHighlights = listOf(
                "Games | Media header with Search, Settings & profile",
                "Top-left icon ribbon with inline active title",
                "Playtime (30 days) & Last Played from real usage data",
                "Pop-up Control Center dock"
            )
        ),
        UiThemePack(
            style = ConsoleUiStyle.HYBRID_DECK,
            id = "hybrid_portable_deck",
            title = "N-Hybrid Portable Deck UI",
            subtitle = "Square Game Tiles, Light/Dark Theme & Circular Dock",
            description = "Hybrid-handheld home screen: cyan title above large square tiles with a pulsing focus ring, Basic White / Basic Black theme, Recently Played ordering and six circular system buttons.",
            version = "v3.5.0",
            sizeText = "Included",
            primaryAccent = Color(0xFF00C3E3),
            secondaryAccent = Color(0xFFFF4554),
            layoutTag = "SQUARE TILE DECK",
            featureHighlights = listOf(
                "1-tap Basic Black / Basic White theme",
                "Cyan title above the focused square tile",
                "6 circular system buttons + (+) Options / (A) Start bar"
            )
        ),
        UiThemePack(
            style = ConsoleUiStyle.DUAL_SCREEN_3D,
            id = "stereoscopic_dual_3d",
            title = "Dual-Screen 3D Handheld UI",
            subtitle = "Upper 3D Stage + Lower Touch Grid & Bezel Buttons",
            description = "Dual-screen handheld layout: top screen with a rotating 3D box stage, status strip and 3D depth switch; bottom touch screen with applet bar, zoomable icon grid and 3-part bezel buttons.",
            version = "v3.3.0",
            sizeText = "Included",
            primaryAccent = Color(0xFFE11D48),
            secondaryAccent = Color(0xFF0284C7),
            layoutTag = "UPPER 3D + LOWER GRID",
            featureHighlights = listOf(
                "Rotating 3D box stage with 3D depth switch",
                "Status strip with signal, library count & clock",
                "Zoomable touch grid (2-6 columns) + Options | Open | UI Hub bezel"
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
        if (saved != null) result.addAll(saved)
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

    var isHybridLightTheme: Boolean
        get() = try { prefs?.getBoolean("hybrid_light_theme", false) ?: false } catch (_: Throwable) { false }
        set(value) {
            try { prefs?.edit()?.putBoolean("hybrid_light_theme", value)?.apply() } catch (_: Throwable) {}
        }

    /** Library ordering: true = most recently played first (needs Usage access), false = A-Z. */
    var sortByRecentlyPlayed: Boolean
        get() = try { prefs?.getBoolean("sort_recent", true) ?: true } catch (_: Throwable) { true }
        set(value) {
            try { prefs?.edit()?.putBoolean("sort_recent", value)?.apply() } catch (_: Throwable) {}
        }

    var gamepadLayout: GamepadLayout
        get() {
            val raw = try {
                prefs?.getString("gamepad_layout", GamepadLayout.ABXY.name)
            } catch (_: Throwable) {
                GamepadLayout.ABXY.name
            }
            return try {
                GamepadLayout.valueOf(raw ?: GamepadLayout.ABXY.name)
            } catch (_: Throwable) {
                GamepadLayout.ABXY
            }
        }
        set(value) {
            try { prefs?.edit()?.putString("gamepad_layout", value.name)?.apply() } catch (_: Throwable) {}
        }

    var dual3dGridColumns: Int
        get() = try {
            (prefs?.getInt("dual_3d_grid_columns", 4) ?: 4).coerceIn(2, 6)
        } catch (_: Throwable) {
            4
        }
        set(value) {
            try { prefs?.edit()?.putInt("dual_3d_grid_columns", value.coerceIn(2, 6))?.apply() } catch (_: Throwable) {}
        }
}
