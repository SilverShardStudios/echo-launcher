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

data class BatteryInfo(
    val percentage: Int = 100,
    val isCharging: Boolean = false
)

@Composable
fun rememberBatteryState(): State<BatteryInfo> {
    val context = LocalContext.current
    val batteryState = remember { mutableStateOf(getInitialBatteryInfo(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val pct = if (level >= 0 && scale > 0) {
                    ((level.toFloat() / scale.toFloat()) * 100).toInt()
                } else {
                    100
                }

                batteryState.value = BatteryInfo(percentage = pct, isCharging = isCharging)
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(receiver, filter)
        if (stickyIntent != null) {
            val level = stickyIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = stickyIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = stickyIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val pct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else 100
            batteryState.value = BatteryInfo(percentage = pct, isCharging = isCharging)
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    return batteryState
}

private fun getInitialBatteryInfo(context: Context): BatteryInfo {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val pct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    return BatteryInfo(percentage = pct, isCharging = false)
}
