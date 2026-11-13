package com.echo.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

data class PowerInfo(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val isPluggedAC: Boolean = true,
    val isHomeConsolePower: Boolean = true
)

@Composable
fun rememberPowerState(): State<PowerInfo> {
    val context = LocalContext.current
    val powerState = remember { mutableStateOf(getInitialPowerInfo(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                val isPluggedAC = plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                        plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                        plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

                val pct = if (level >= 0 && scale > 0) {
                    ((level.toFloat() / scale.toFloat()) * 100).toInt()
                } else {
                    100
                }

                // If not present or permanently plugged on AC, treat as home console wall power
                val isHomeConsole = !present || (isPluggedAC && pct == 100) || plugged == BatteryManager.BATTERY_PLUGGED_AC

                powerState.value = PowerInfo(
                    percentage = pct,
                    isCharging = isCharging,
                    isPluggedAC = isPluggedAC,
                    isHomeConsolePower = isHomeConsole
                )
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(receiver, filter)
        if (stickyIntent != null) {
            val level = stickyIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = stickyIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = stickyIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = stickyIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val present = stickyIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val isPluggedAC = plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                    plugged == BatteryManager.BATTERY_PLUGGED_USB

            val pct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else 100

            val isHomeConsole = !present || (isPluggedAC && pct == 100) || plugged == BatteryManager.BATTERY_PLUGGED_AC

            powerState.value = PowerInfo(
                percentage = pct,
                isCharging = isCharging,
                isPluggedAC = isPluggedAC,
                isHomeConsolePower = isHomeConsole
            )
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    return powerState
}

private fun getInitialPowerInfo(context: Context): PowerInfo {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val pct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    return PowerInfo(
        percentage = pct,
        isCharging = false,
        isPluggedAC = true,
        isHomeConsolePower = true
    )
}
