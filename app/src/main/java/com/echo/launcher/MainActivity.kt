package com.echo.launcher

import android.app.KeyguardManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.echo.launcher.data.rememberPowerState
import com.echo.launcher.ui.EchoLauncherScreen
import com.echo.launcher.ui.theme.EchoTheme

class MainActivity : ComponentActivity() {

    private var keyEventListener: ((KeyEvent) -> Boolean)? = null
    private var motionEventListener: ((MotionEvent) -> Boolean)? = null
    private var isWakeReceiverRegistered = false
    private var resumeTrigger by mutableIntStateOf(0)

    /**
     * Proper startActivityForResult launcher required by Android 10+ RoleManager.ROLE_HOME!
     * (Calling startActivity() with FLAG_ACTIVITY_NEW_TASK causes Android to silently ignore RoleManager).
     */
    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        resumeTrigger++
        if (!isCurrentlyDefaultLauncher()) {
            // If the system Role popup was dismissed or unsupported on this ROM, fall back to Home Settings / Chooser
            launchFallbackHomeSelector()
        } else {
            Toast.makeText(this, "ECHO OS set as Default Console Home!", Toast.LENGTH_SHORT).show()
        }
    }

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

        bypassAndroidLockScreen()
        setupImmersiveMode()
        enableHighRefreshRate()

        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(wakeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(wakeReceiver, filter)
            }
            isWakeReceiverRegistered = true
        } catch (_: Throwable) {}

        setContent {
            EchoTheme {
                val powerInfo = rememberPowerState()
                EchoLauncherScreen(
                    powerInfo = powerInfo.value,
                    resumeTick = resumeTrigger,
                    onRequestDefaultHome = { requestDefaultHomeLauncher() },
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

    /**
     * Multi-stage Default Home Launcher requester that works across Stock Android, Anbernic ROMs,
     * Retroid ROMs, and Android TV boxes:
     * 1) Android 10+ RoleManager via startActivityForResult (NO FLAG_ACTIVITY_NEW_TASK!)
     * 2) FakeHomeTriggerActivity component toggle trick to force Android's native "Choose Home app" popup
     * 3) Direct Settings.ACTION_HOME_SETTINGS / ACTION_MANAGE_DEFAULT_APPS_SETTINGS fallback
     */
    fun requestDefaultHomeLauncher() {
        if (isCurrentlyDefaultLauncher()) {
            Toast.makeText(this, "ECHO OS is already your Default Home!", Toast.LENGTH_SHORT).show()
            resumeTrigger++
            return
        }

        // Stage 1: Try Android 10+ RoleManager properly via ActivityResultLauncher (without FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
                if (roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                ) {
                    val roleIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    homeRoleLauncher.launch(roleIntent)
                    return
                }
            } catch (_: Throwable) {}
        }

        // Stage 2 & 3: Fallback chooser / Home Settings
        launchFallbackHomeSelector()
    }

    private fun launchFallbackHomeSelector() {
        // Try toggling FakeHomeTriggerActivity to force Android's native "Select Home App (Always / Just Once)" dialog
        try {
            val fakeComponent = ComponentName(this, FakeHomeTriggerActivity::class.java)
            packageManager.setComponentEnabledSetting(
                fakeComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            packageManager.setComponentEnabledSetting(
                fakeComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (_: Throwable) {}

        // Also try opening Android's Default Home App Settings screen directly if still not default
        try {
            val homeSettings = Intent(Settings.ACTION_HOME_SETTINGS)
            if (homeSettings.resolveActivity(packageManager) != null) {
                Toast.makeText(this, "Select 'ECHO OS' as your Home App", Toast.LENGTH_LONG).show()
                startActivity(homeSettings)
                return
            }
        } catch (_: Throwable) {}

        try {
            val defaultAppsSettings = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            if (defaultAppsSettings.resolveActivity(packageManager) != null) {
                Toast.makeText(this, "Tap 'Home app' and select 'ECHO OS'", Toast.LENGTH_LONG).show()
                startActivity(defaultAppsSettings)
                return
            }
        } catch (_: Throwable) {}

        try {
            Toast.makeText(this, "Go to Apps -> Default Apps -> Home App -> Select ECHO OS", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (_: Throwable) {}
    }

    private fun isCurrentlyDefaultLauncher(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == packageName
        } catch (_: Throwable) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isWakeReceiverRegistered) {
            try {
                unregisterReceiver(wakeReceiver)
            } catch (_: Throwable) {}
            isWakeReceiverRegistered = false
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTrigger++
        bypassAndroidLockScreen()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

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
        } catch (_: Throwable) {}
    }

    @Suppress("DEPRECATION")
    private fun enableHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val display = windowManager?.defaultDisplay ?: return
                val supportedModes = display.supportedModes ?: return
                val bestMode = supportedModes.maxByOrNull { it.refreshRate }
                if (bestMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = bestMode.modeId
                    window.attributes = params
                }
            }
        } catch (_: Throwable) {}
    }

    private fun setupImmersiveMode() {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            hideSystemBars()
        } catch (_: Throwable) {}
    }

    private fun hideSystemBars() {
        try {
            val decorView = window?.decorView ?: return
            val windowInsetsController = WindowCompat.getInsetsController(window, decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } catch (_: Throwable) {}
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        try {
            if (keyEventListener?.invoke(event) == true) {
                return true
            }
        } catch (_: Throwable) {}
        return super.dispatchKeyEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        try {
            if (motionEventListener?.invoke(event) == true) {
                return true
            }
        } catch (_: Throwable) {}
        return super.onGenericMotionEvent(event)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("super.onBackPressed()"))
    override fun onBackPressed() {
        // Handled by BackHandler in Compose; never exit launcher
    }
}
