package com.echo.launcher.data

import android.app.Activity
import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages dual-screen detection, live DisplayListener events, and secondary display presentation
 * for dual-screen handhelds and consoles (e.g. Anbernic RG DS, clamshell devices).
 *
 * Uses safe DeviceProtectedStorage so it never crashes during Android Direct Boot (before unlock),
 * and uses lightweight hardware-accelerated native Views inside Secondary Display Presentation
 * so Presentation never crashes with missing Compose ViewTreeLifecycleOwner exceptions.
 */
class DualScreenManager(context: Context) {

    private val appContext = context.applicationContext ?: context
    private val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    private var secondaryPresentation: Presentation? = null

    var hasPhysicalSecondaryDisplay by mutableStateOf(false)
        private set

    private val prefs by lazy {
        try {
            val storageContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appContext.createDeviceProtectedStorageContext()
            } else {
                appContext
            }
            storageContext.getSharedPreferences("echo_os_prefs", Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }

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
        try {
            checkDisplays()
            displayManager?.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
        } catch (_: Throwable) {}
    }

    private fun loadInitialDualScreenMode(): Boolean {
        try {
            val p = prefs
            if (p != null && p.contains("force_dual_screen")) {
                return p.getBoolean("force_dual_screen", false)
            }
        } catch (_: Throwable) {}

        val model = "${Build.MODEL} ${Build.DEVICE} ${Build.PRODUCT} ${Build.MANUFACTURER}".lowercase()
        return model.contains("rg ds") ||
            model.contains("rgds") ||
            model.contains("rg-ds") ||
            model.contains("dual") ||
            model.contains("duo")
    }

    fun setDualScreenEnabled(enabled: Boolean) {
        isDualScreenMode = enabled
        try {
            prefs?.edit()?.putBoolean("force_dual_screen", enabled)?.apply()
        } catch (_: Throwable) {}
    }

    fun toggleDualScreenMode(): Boolean {
        val next = !isDualScreenMode
        setDualScreenEnabled(next)
        return next
    }

    fun checkDisplays(): Boolean {
        return try {
            val dm = displayManager ?: return false
            val allDisplays = dm.displays ?: emptyArray()
            val presentationDisplays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION) ?: emptyArray()
            hasPhysicalSecondaryDisplay = allDisplays.size > 1 || presentationDisplays.isNotEmpty()
            if (hasPhysicalSecondaryDisplay && prefs?.contains("force_dual_screen") != true) {
                isDualScreenMode = true
            }
            hasPhysicalSecondaryDisplay
        } catch (_: Throwable) {
            false
        }
    }

    fun getSecondaryDisplay(): Display? {
        return try {
            val dm = displayManager ?: return null
            val presentationDisplays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            if (!presentationDisplays.isNullOrEmpty()) {
                return presentationDisplays.first()
            }
            val allDisplays = dm.displays
            allDisplays?.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Shows the Secondary Screen Telemetry & Touch Command Deck on external/secondary physical display
     * using crash-proof native Android views (avoids Presentation ComposeView LifecycleOwner crashes).
     */
    fun showSecondaryPresentation(
        activityContext: Context,
        apps: List<AppItem>,
        selectedIndex: Int,
        onSelectIndex: (Int) -> Unit,
        onLaunch: (AppItem) -> Unit,
        onToggleAllApps: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        val secDisplay = getSecondaryDisplay() ?: return
        if (activityContext is Activity && (activityContext.isFinishing || activityContext.isDestroyed)) {
            return
        }

        try {
            val activeApp = apps.getOrNull(selectedIndex)
            if (secondaryPresentation?.isShowing == true && secondaryPresentation?.display?.displayId == secDisplay.displayId) {
                updatePresentationContent(secondaryPresentation!!, activeApp, apps, onLaunch, onToggleAllApps, onOpenSettings)
                return
            }
            dismissSecondaryPresentation()

            secondaryPresentation = object : Presentation(activityContext, secDisplay) {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    window?.addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                    updatePresentationContent(this, activeApp, apps, onLaunch, onToggleAllApps, onOpenSettings)
                }
            }
            secondaryPresentation?.show()
        } catch (_: Throwable) {
            secondaryPresentation = null
        }
    }

    private fun updatePresentationContent(
        presentation: Presentation,
        activeApp: AppItem?,
        apps: List<AppItem>,
        onLaunch: (AppItem) -> Unit,
        onToggleAllApps: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        try {
            val ctx = presentation.context
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(AndroidColor.parseColor("#080A0F"))
                setPadding(40, 28, 40, 28)
                gravity = Gravity.CENTER_VERTICAL
            }

            val header = TextView(ctx).apply {
                text = "ECHO OS // RG DS SECONDARY DISPLAY"
                setTextColor(AndroidColor.parseColor("#00E5FF"))
                textSize = 13f
                setPadding(0, 0, 0, 18)
            }
            root.addView(header)

            val titleView = TextView(ctx).apply {
                text = activeApp?.label ?: "ECHO OS READY"
                setTextColor(AndroidColor.WHITE)
                textSize = 24f
                setPadding(0, 0, 0, 8)
            }
            root.addView(titleView)

            val subView = TextView(ctx).apply {
                text = if (activeApp != null) "${activeApp.systemTag} • TAP TO LAUNCH" else "${apps.size} INSTALLED TITLES"
                setTextColor(AndroidColor.parseColor("#8E9BAE"))
                textSize = 12f
                setPadding(0, 0, 0, 28)
            }
            root.addView(subView)

            val actionsRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            fun makePill(label: String, bgHex: String, fgColor: Int, onClick: () -> Unit): TextView {
                return TextView(ctx).apply {
                    text = label
                    setTextColor(fgColor)
                    textSize = 13f
                    setPadding(36, 18, 36, 18)
                    background = GradientDrawable().apply {
                        cornerRadius = 32f
                        setColor(AndroidColor.parseColor(bgHex))
                    }
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { rightMargin = 20 }
                    layoutParams = lp
                    setOnClickListener { onClick() }
                }
            }

            if (activeApp != null) {
                actionsRow.addView(
                    makePill("▶ LAUNCH NOW", "#00E5FF", AndroidColor.BLACK) { onLaunch(activeApp) }
                )
            }
            actionsRow.addView(
                makePill("▦ ALL APPS", "#141A26", AndroidColor.WHITE) { onToggleAllApps() }
            )
            actionsRow.addView(
                makePill("⚙ SETTINGS", "#141A26", AndroidColor.WHITE) { onOpenSettings() }
            )

            root.addView(actionsRow)
            presentation.setContentView(FrameLayout(ctx).apply { addView(root) })
        } catch (_: Throwable) {}
    }

    fun dismissSecondaryPresentation() {
        try {
            secondaryPresentation?.dismiss()
        } catch (_: Throwable) {}
        secondaryPresentation = null
    }

    fun release() {
        try {
            displayManager?.unregisterDisplayListener(displayListener)
        } catch (_: Throwable) {}
        dismissSecondaryPresentation()
    }
}
