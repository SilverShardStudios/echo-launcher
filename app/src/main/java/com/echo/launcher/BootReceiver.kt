package com.echo.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Automatically boots into ECHO OS immediately upon power-on, direct-boot before unlock,
 * or screen wake so the console never drops the user onto an Android login/home screen.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_REBOOT ||
            action == Intent.ACTION_USER_PRESENT ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            try {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    )
                }
                context.startActivity(launchIntent)
            } catch (_: Exception) {}
        }
    }
}
