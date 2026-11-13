package com.echo.launcher.data

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.echo.launcher.ui.components.DualScreenBottomDeck
import com.echo.launcher.ui.theme.EchoTheme

/**
 * Manages dual-screen detection, live DisplayListener events, and secondary display presentation
 * for dual-screen handhelds and consoles (e.g. Anbernic RG DS, clamshell devices).
 */
class DualScreenManager(private val context: Context) {

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var secondaryPresentation: Presentation? = null

    var hasPhysicalSecondaryDisplay by mutableStateOf(false)
        private set

    /**
     * Automatically detectar Anbernic RG DS and dual-screen handheld hardware models,
     * or load the user's persisted Dual-Screen preference.
     */
    private val prefs = context.getSharedPreferences("echo_os_prefs", Context.MODE_PRIVATE)

    var isDualScreenMode by mutableStateOf(loadInitialDualScreenMode())
        private set

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            checkDisplays()
        }

        override fun onDisplayRemoved(displayId: Int) {
            if (secondaryPresentation?.display?.displayId == displayId) {
                dismissSecondaryPresentation()
            }
            checkDisplays()
        }

        override fun onDisplayChanged(displayId: Int) {
            checkDisplays()
        }
    }

    init {
        checkDisplays()
        displayManager.registerDisplayListener(displayListener, null)
    }

    private fun loadInitialDualScreenMode(): Boolean {
        if (prefs.contains("force_dual_screen")) {
            return prefs.getBoolean("force_dual_screen", false)
        }
        val model = (Build.MODEL + " " + Build.DEVICE + " " + Build.PRODUCT + " " + Build.MANUFACTURER).lowercase()
        return model.contains("rg ds") ||
            model.contains("rgds") ||
            model.contains("rg-ds") ||
            model.contains("dual") ||
            model.contains("duo")
    }

    fun setDualScreenEnabled(enabled: Boolean) {
        isDualScreenMode = enabled
        prefs.edit().putBoolean("force_dual_screen", enabled).apply()
    }

    fun toggleDualScreenMode(): Boolean {
        val next = !isDualScreenMode
        setDualScreenEnabled(next)
        return next
    }

    fun checkDisplays(): Boolean {
        val allDisplays = displayManager.displays
        val presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        hasPhysicalSecondaryDisplay = allDisplays.size > 1 || presentationDisplays.isNotEmpty()
        if (hasPhysicalSecondaryDisplay && !prefs.contains("force_dual_screen")) {
            isDualScreenMode = true
        }
        return hasPhysicalSecondaryDisplay
    }

    fun getSecondaryDisplay(): Display? {
        val presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (presentationDisplays.isNotEmpty()) {
            return presentationDisplays.first()
        }
        val allDisplays = displayManager.displays
        return allDisplays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }
    }

    /**
     * Shows the Command Deck on the secondary physical screen if available (Anbernic RG DS HDMI/MIPI 2nd screen).
     */
    fun showSecondaryPresentation(
        apps: List<AppItem>,
        selectedIndex: Int,
        onSelectIndex: (Int) -> Unit,
        onLaunch: (AppItem) -> Unit,
        onToggleAllApps: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        val secDisplay = getSecondaryDisplay() ?: return
        if (secondaryPresentation?.isShowing == true && secondaryPresentation?.display?.displayId == secDisplay.displayId) {
            return
        }
        dismissSecondaryPresentation()

        try {
            secondaryPresentation = object : Presentation(context, secDisplay) {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    window?.addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                    val composeView = ComposeView(context).apply {
                        setContent {
                            EchoTheme {
                                DualScreenBottomDeck(
                                    apps = apps,
                                    selectedIndex = selectedIndex,
                                    onSelectIndex = onSelectIndex,
                                    onLaunch = onLaunch,
                                    onToggleAllApps = onToggleAllApps,
                                    onOpenSettings = onOpenSettings
                                )
                            }
                        }
                    }
                    setContentView(composeView)
                }
            }
            secondaryPresentation?.show()
        } catch (_: Exception) {}
    }

    fun dismissSecondaryPresentation() {
        try {
            secondaryPresentation?.dismiss()
        } catch (_: Exception) {}
        secondaryPresentation = null
    }

    fun release() {
        try {
            displayManager.unregisterDisplayListener(displayListener)
        } catch (_: Exception) {}
        dismissSecondaryPresentation()
    }
}
