package com.echo.launcher.data

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Immutable
data class SystemTelemetry(
    val usedRamMb: Long,
    val totalRamMb: Long,
    val freeRamMb: Long,
    val ramUsagePercent: Int,
    val freeStorageGb: Float,
    val totalStorageGb: Float,
    val storageUsagePercent: Int,
    val refreshRateHz: Int
)

@Immutable
data class OptimizationResult(
    val freedRamMb: Long,
    val appsClearedCount: Int,
    val ramPercentBefore: Int,
    val ramPercentAfter: Int,
    val telemetryAfter: SystemTelemetry,
    val statusMessage: String
)

class DeviceOptimizer(private val context: Context) {

    private val prefs: SharedPreferences? = try {
        val storageCtx = context.applicationContext.createDeviceProtectedStorageContext()
        storageCtx.getSharedPreferences("echo_optimizer_prefs", Context.MODE_PRIVATE)
    } catch (_: Throwable) {
        try {
            context.getSharedPreferences("echo_optimizer_prefs", Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }

    /** Off by default: stopping background apps before every launch costs time and rarely helps. */
    var isAutoBoostEnabled: Boolean
        get() = try { prefs?.getBoolean("auto_boost_before_launch", false) ?: false } catch (_: Throwable) { false }
        set(value) {
            try { prefs?.edit()?.putBoolean("auto_boost_before_launch", value)?.apply() } catch (_: Throwable) {}
        }

    fun getTelemetry(): SystemTelemetry {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)

            val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
            val freeRamMb = (memInfo.availMem / (1024L * 1024L)).coerceIn(0L, totalRamMb)
            val usedRamMb = (totalRamMb - freeRamMb).coerceAtLeast(0L)
            val ramPercent = ((usedRamMb * 100L) / totalRamMb).toInt().coerceIn(0, 100)

            val stat = StatFs(Environment.getDataDirectory().path)
            val gb = 1024.0 * 1024.0 * 1024.0
            val totalStorageGb = (stat.blockCountLong * stat.blockSizeLong / gb).toFloat().coerceAtLeast(0.1f)
            val freeStorageGb = (stat.availableBlocksLong * stat.blockSizeLong / gb).toFloat().coerceAtLeast(0f)
            val storagePercent = (((totalStorageGb - freeStorageGb) / totalStorageGb) * 100f).toInt().coerceIn(0, 100)

            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            @Suppress("DEPRECATION")
            val hz = wm?.defaultDisplay?.refreshRate?.toInt()?.coerceIn(1, 480) ?: 60

            SystemTelemetry(
                usedRamMb = usedRamMb,
                totalRamMb = totalRamMb,
                freeRamMb = freeRamMb,
                ramUsagePercent = ramPercent,
                freeStorageGb = freeStorageGb,
                totalStorageGb = totalStorageGb,
                storageUsagePercent = storagePercent,
                refreshRateHz = hz
            )
        } catch (_: Throwable) {
            SystemTelemetry(0L, 1L, 0L, 0, 0f, 0.1f, 0, 60)
        }
    }

    /**
     * 1-tap optimization pass. Everything reported is measured, nothing is estimated:
     * 1. Asks Android to stop cached background processes (KILL_BACKGROUND_PROCESSES).
     *    The app about to be launched (excludePackage) is never touched, so it keeps its resume state.
     * 2. Runs GC on ECHO OS itself.
     * 3. Re-reads MemoryInfo and reports the real before/after difference (can be 0 MB if
     *    Android had already reclaimed that memory on its own).
     */
    suspend fun optimizeDevice(
        installedApps: List<AppItem>,
        excludePackage: String? = null
    ): OptimizationResult = withContext(Dispatchers.IO) {
        val before = getTelemetry()
        var requestedCount = 0

        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val myPkg = context.packageName
            if (am != null) {
                val targets = installedApps.asSequence()
                    .map { it.packageName }
                    .filter { it != myPkg && it != excludePackage && it != "android" && !it.startsWith("com.android.systemui") }
                    .toSet()
                for (pkg in targets) {
                    try {
                        am.killBackgroundProcesses(pkg)
                        requestedCount++
                    } catch (_: Throwable) {}
                }
            }
            Runtime.getRuntime().gc()
        } catch (_: Throwable) {}

        delay(400) // let the kernel update its counters before measuring again

        val after = getTelemetry()
        val freedMb = (after.freeRamMb - before.freeRamMb).coerceAtLeast(0L)
        val message = if (freedMb > 0) {
            "Freed $freedMb MB RAM \u2022 ${before.ramUsagePercent}% \u2192 ${after.ramUsagePercent}% used"
        } else {
            "RAM already clean (${after.ramUsagePercent}% used) \u2022 nothing left to reclaim"
        }

        OptimizationResult(
            freedRamMb = freedMb,
            appsClearedCount = requestedCount,
            ramPercentBefore = before.ramUsagePercent,
            ramPercentAfter = after.ramUsagePercent,
            telemetryAfter = after,
            statusMessage = message
        )
    }
}
