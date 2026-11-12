package com.echo.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val defaultPalette = listOf(
        Color(0xFF00E5FF), // Electric Cyan
        Color(0xFF7C4DFF), // Deep Violet
        Color(0xFFFF0055), // Cyber Pink
        Color(0xFF00E676), // Neon Emerald
        Color(0xFFFF9100), // Plasma Amber
        Color(0xFF2979FF)  // Azure Blue
    )

    suspend fun loadInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val myPackage = context.packageName
        val apps = mutableListOf<AppItem>()

        for ((index, resolveInfo) in resolveInfos.withIndex()) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName == myPackage) continue

            val activityName = resolveInfo.activityInfo.name
            val label = resolveInfo.loadLabel(pm)?.toString() ?: packageName
            val appInfo = resolveInfo.activityInfo.applicationInfo

            val emulatorClassification = detectEmulator(packageName, label)
            val isEmulator = emulatorClassification != null
            val isGame = isEmulator || isGameApp(appInfo, packageName, label)
            val isMedia = isMediaApp(appInfo, packageName, label)
            val systemTag = emulatorClassification ?: if (isGame) "CONSOLE" else if (isMedia) "MEDIA" else "SYSTEM"

            var imageBitmap: androidx.compose.ui.graphics.ImageBitmap? = null
            var accentColor = defaultPalette[index % defaultPalette.size]

            try {
                val iconDrawable = resolveInfo.loadIcon(pm)
                val bitmap = drawableToBitmap(iconDrawable)
                if (bitmap != null) {
                    imageBitmap = bitmap.asImageBitmap()
                    accentColor = extractDominantColor(bitmap, accentColor)
                }
            } catch (e: Exception) {
                Log.w("AppRepository", "Failed to extract icon for $packageName", e)
            }

            apps.add(
                AppItem(
                    id = "$packageName/$activityName",
                    packageName = packageName,
                    activityName = activityName,
                    label = label,
                    iconBitmap = imageBitmap,
                    isGame = isGame,
                    isMedia = isMedia,
                    isEmulator = isEmulator,
                    systemTag = systemTag,
                    accentColor = accentColor
                )
            )
        }

        apps.sortBy { it.label.lowercase() }
        apps
    }

    /**
     * Enhanced detection for Anbernic, Retroid, and handheld retro emulators.
     */
    private fun detectEmulator(packageName: String, label: String): String? {
        val p = packageName.lowercase()
        val l = label.lowercase()
        return when {
            p.contains("retroarch") || l.contains("retroarch") -> "RETROARCH"
            p.contains("drastic") || p.contains("melonds") || l.contains("drastic") || l.contains("melonds") -> "NINTENDO DS"
            p.contains("citra") || p.contains("mandarine") || p.contains("lime3ds") || l.contains("citra") -> "NINTENDO 3DS"
            p.contains("dolphin") || l.contains("dolphin") -> "GAMECUBE / WII"
            p.contains("ppsspp") || l.contains("ppsspp") -> "PSP"
            p.contains("aethersx2") || p.contains("nethersx2") || p.contains("play!") -> "PS2"
            p.contains("duckstation") || p.contains("epsxe") || p.contains("swanstation") -> "PS1"
            p.contains("yuzu") || p.contains("suyu") || p.contains("sudachi") || p.contains("skyline") || p.contains("strato") -> "STANDALONE EMU"
            p.contains("vita3k") -> "PS VITA"
            p.contains("redream") || p.contains("flycast") -> "DREAMCAST"
            p.contains("mupen") || p.contains("m64plus") -> "N64"
            p.contains("scummvm") -> "RETRO PC"
            p.contains("daijisho") || p.contains("beacon") || p.contains("pegasus") || p.contains("emulationstation") -> "FRONTEND"
            p.contains("moonlight") || p.contains("steamlink") || p.contains("geforce") || p.contains("parsec") || p.contains("psplay") -> "STREAMING"
            else -> null
        }
    }

    private fun isGameApp(appInfo: ApplicationInfo, packageName: String, label: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                return true
            }
        }
        @Suppress("DEPRECATION")
        if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
            return true
        }

        val lowered = (packageName + " " + label).lowercase()
        return lowered.contains("game") ||
                lowered.contains("steam") ||
                lowered.contains("emulator") ||
                lowered.contains("arcade") ||
                lowered.contains("playstation") ||
                lowered.contains("xbox")
    }

    private fun isMediaApp(appInfo: ApplicationInfo, packageName: String, label: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_AUDIO ||
                appInfo.category == ApplicationInfo.CATEGORY_VIDEO ||
                appInfo.category == ApplicationInfo.CATEGORY_IMAGE
            ) {
                return true
            }
        }
        val lowered = (packageName + " " + label).lowercase()
        return lowered.contains("youtube") ||
                lowered.contains("netflix") ||
                lowered.contains("spotify") ||
                lowered.contains("twitch") ||
                lowered.contains("prime video") ||
                lowered.contains("music") ||
                lowered.contains("video") ||
                lowered.contains("player") ||
                lowered.contains("gallery") ||
                lowered.contains("plex") ||
                lowered.contains("kodi") ||
                lowered.contains("vlc")
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
        val bitmap = Bitmap.createBitmap(width.coerceAtMost(256), height.coerceAtMost(256), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun extractDominantColor(bitmap: Bitmap, fallback: Color): Color {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return fallback

        var bestColor = fallback
        var maxScore = -1f

        val stepX = (w / 8).coerceAtLeast(1)
        val stepY = (h / 8).coerceAtLeast(1)
        val hsl = FloatArray(3)

        for (x in stepX / 2 until w step stepX) {
            for (y in stepY / 2 until h step stepY) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (pixel ushr 24) and 0xFF
                if (alpha < 100) continue

                ColorUtils.colorToHSL(pixel, hsl)
                val saturation = hsl[1]
                val lightness = hsl[2]

                if (lightness < 0.15f || lightness > 0.88f) continue
                if (saturation < 0.20f) continue

                val score = saturation * 2.0f + (1.0f - kotlin.math.abs(lightness - 0.5f))
                if (score > maxScore) {
                    maxScore = score
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    bestColor = Color(r, g, b, 255)
                }
            }
        }

        return bestColor
    }

    fun launchApp(appItem: AppItem): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(appItem.packageName, appItem.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppRepository", "Unable to launch ${appItem.packageName}", e)
            false
        }
    }

    fun launchEchoUpdates() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SilverShardStudios/echo-launcher/releases")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to open updates url", e)
        }
    }

    fun launchWifiBtSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (e2: Exception) {
                launchSettings()
            }
        }
    }

    fun launchSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to open settings", e)
        }
    }
}
