package com.echo.launcher.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Real per-app statistics. Playtime and last-played come from Android's UsageStatsManager,
 * which needs the user to grant "Usage access" in system settings. Install date always works.
 */
@Immutable
data class GameStats(
    val playtimeMs30d: Long,
    val lastPlayedMs: Long,
    val installedMs: Long,
    val versionName: String?
)

class GameStatsRepository(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Throwable) {
            false
        }
    }

    fun openUsageAccessSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Throwable) {}
        }
    }

    /** Returns stats keyed by package name for the given apps. */
    suspend fun loadStats(apps: List<AppItem>): Map<String, GameStats> = withContext(Dispatchers.IO) {
        val usage = HashMap<String, Pair<Long, Long>>() // pkg -> (foregroundMs, lastUsed)
        if (hasUsageAccess()) {
            try {
                val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val end = System.currentTimeMillis()
                val start = end - TimeUnit.DAYS.toMillis(30)
                val aggregated = usm.queryAndAggregateUsageStats(start, end)
                for ((pkg, s) in aggregated) {
                    usage[pkg] = s.totalTimeInForeground to s.lastTimeUsed
                }
            } catch (_: Throwable) {}
        }

        val pm = context.packageManager
        val result = HashMap<String, GameStats>(apps.size)
        for (app in apps) {
            if (result.containsKey(app.packageName)) continue
            val info = try {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(app.packageName, 0)
            } catch (_: Throwable) {
                null
            }
            val u = usage[app.packageName]
            result[app.packageName] = GameStats(
                playtimeMs30d = u?.first ?: 0L,
                lastPlayedMs = u?.second ?: 0L,
                installedMs = info?.firstInstallTime ?: 0L,
                versionName = info?.versionName
            )
        }
        result
    }
}

fun formatPlaytime(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalMin = TimeUnit.MILLISECONDS.toMinutes(ms)
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

fun formatRelativeTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return "Never"
    val diff = System.currentTimeMillis() - timestampMs
    if (diff < 0) return "Just now"
    val min = TimeUnit.MILLISECONDS.toMinutes(diff)
    return when {
        min < 1 -> "Just now"
        min < 60 -> "${min}m ago"
        min < 60 * 24 -> "${min / 60}h ago"
        min < 60 * 24 * 30 -> "${min / (60 * 24)}d ago"
        else -> "${min / (60 * 24 * 30)}mo ago"
    }
}

fun formatDate(timestampMs: Long): String {
    if (timestampMs <= 0L) return "Unknown"
    return try {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestampMs))
    } catch (_: Throwable) {
        "Unknown"
    }
}
