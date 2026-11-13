package com.echo.launcher

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.echo.launcher.data.rememberPowerState
import com.echo.launcher.ui.EchoLauncherScreen
import com.echo.launcher.ui.theme.EchoTheme

class MainActivity : ComponentActivity() {

    private var keyEventListener: ((KeyEvent) -> Boolean)? = null
    private var motionEventListener: ((MotionEvent) -> Boolean)? = null

    /**
     * Listens for screen power-on / wake events while ECHO OS is running so that
     * turning the console off and back on immediately dismisses the Android lock screen
     * and shows ECHO OS without any Android login prompt.
     */
    private val wakeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> {
                    bypassAndroidLockScreen()
                    hideSystemBars()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bypass Android lock/login screen immediately on cold boot or power button wake
        bypassAndroidLockScreen()
        setupImmersiveMode()
        enableHighRefreshRate()

        // Register screen wake receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(wakeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(wakeReceiver, filter)
        }

        setContent {
            EchoTheme {
                val powerInfo = rememberPowerState()
                EchoLauncherScreen(
                    powerInfo = powerInfo.value,
                    onHandleKeyEvent = { listener ->
                        keyEventListener = listener
                    },
                    onHandleMotionEvent = { listener ->
                        motionEventListener = listener
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(wakeReceiver)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        bypassAndroidLockScreen()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    /**
     * Shows ECHO OS directly over the Android Keyguard / Lock Screen and dismisses it
     * so powering on the console goes straight into ECHO OS.
     */
    @Suppress("DEPRECATION")
    private fun bypassAndroidLockScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                keyguardManager?.requestDismissKeyguard(this, null)
            } else {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (_: Exception) {}
    }

    /**
     * Requests the highest refresh rate supported by the display (60Hz / 90Hz / 120Hz)
     * for zero-lag input and scrolling.
     */
    private fun enableHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    this.display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val supportedModes = display?.supportedModes ?: return
                val bestMode = supportedModes.maxByOrNull { it.refreshRate }
                if (bestMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = bestMode.modeId
                    window.attributes = params
                }
            }
        } catch (_: Exception) {}
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        hideSystemBars()
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Intercept hardware controller & keyboard navigation events for zero-touch navigation.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (keyEventListener?.invoke(event) == true) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Intercept Anbernic, Retroid, and analog stick / HAT axis motion events.
     */
    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (motionEventListener?.invoke(event) == true) {
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("super.onBackPressed()"))
    override fun onBackPressed() {
        // Handled by BackHandler in Compose; never exit launcher
    }
}
