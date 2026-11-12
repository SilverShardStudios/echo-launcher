package com.echo.launcher

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.echo.launcher.data.rememberBatteryState
import com.echo.launcher.ui.EchoLauncherScreen
import com.echo.launcher.ui.theme.EchoTheme

class MainActivity : ComponentActivity() {

    private var keyEventListener: ((KeyEvent) -> Boolean)? = null
    private var motionEventListener: ((MotionEvent) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock to immersive full-screen console mode
        setupImmersiveMode()

        setContent {
            EchoTheme {
                val batteryInfo = rememberBatteryState()
                EchoLauncherScreen(
                    batteryInfo = batteryInfo.value,
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

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
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

    /**
     * Pressing back on the console launcher should never exit the launcher home screen.
     */
    @Deprecated("Deprecated in Java", ReplaceWith("super.onBackPressed()"))
    override fun onBackPressed() {
        // Handled by BackHandler in Compose; never exit launcher
    }
}
