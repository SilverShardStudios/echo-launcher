package com.echo.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
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
        var isRegistered = false
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null) return
                try {
                    powerState.value = parseBatteryIntent(intent)
                } catch (_: Throwable) {}
            }
        }

        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
            isRegistered = true
            if (stickyIntent != null) {
                powerState.value = parseBatteryIntent(stickyIntent)
            }
        } catch (_: Throwable) {}

        onDispose {
            if (isRegistered) {
                try {
                    context.unregisterReceiver(receiver)
                } catch (_: Throwable) {}
            }
        }
    }

    return powerState
}

private fun parseBatteryIntent(intent: Intent): PowerInfo {
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
        ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else {
        100
    }

    val isHomeConsole = !present || (isPluggedAC && pct == 100) || plugged == BatteryManager.BATTERY_PLUGGED_AC

    return PowerInfo(
        percentage = pct,
        isCharging = isCharging,
        isPluggedAC = isPluggedAC,
        isHomeConsolePower = isHomeConsole
    )
}

private fun getInitialPowerInfo(context: Context): PowerInfo {
    return try {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val pct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        PowerInfo(
            percentage = if (pct in 0..100) pct else 100,
            isCharging = false,
            isPluggedAC = false,
            isHomeConsolePower = pct !in 1..99
        )
    } catch (_: Throwable) {
        PowerInfo()
    }
}
