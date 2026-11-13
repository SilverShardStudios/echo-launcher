package com.echo.launcher.data

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.echo.launcher.ui.components.DualScreenBottomDeck
import com.echo.launcher.ui.theme.EchoTheme

/**
 * Manages dual-screen detection and secondary display presentation
 * for dual-screen handhelds and consoles (e.g. Anbernic RG DS, clamshell devices).
 */
class DualScreenManager(private val context: Context) {

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var secondaryPresentation: Presentation? = null

    var hasPhysicalSecondaryDisplay by mutableStateOf(false)
        private set

    var isDualScreenMode by mutableStateOf(false)

    init {
        checkDisplays()
    }

    fun checkDisplays(): Boolean {
        val displays = displayManager.displays
        hasPhysicalSecondaryDisplay = displays.size > 1
        if (hasPhysicalSecondaryDisplay) {
            isDualScreenMode = true
        }
        return hasPhysicalSecondaryDisplay
    }

    fun getSecondaryDisplay(): Display? {
        val displays = displayManager.displays
        return if (displays.size > 1) displays[1] else null
    }

    /**
     * Shows the Command Deck on the secondary physical screen if available.
     */
    fun showSecondaryPresentation(
        apps: List<AppItem>,
        onLaunch: (AppItem) -> Unit,
        onToggleAllApps: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        val secDisplay = getSecondaryDisplay() ?: return
        if (secondaryPresentation != null) return

        try {
            secondaryPresentation = object : Presentation(context, secDisplay) {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    val composeView = ComposeView(context).apply {
                        setContent {
                            EchoTheme {
                                DualScreenBottomDeck(
                                    apps = apps,
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
            secondaryPresentation = null
        } catch (_: Exception) {}
    }
}
