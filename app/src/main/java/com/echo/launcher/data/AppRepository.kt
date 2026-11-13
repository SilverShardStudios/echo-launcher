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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AppRepository(private val context: Context) {

    companion object {
        @Volatile
        private var cachedAppList: List<AppItem>? = null
        private val iconCache = ConcurrentHashMap<String, Pair<ImageBitmap, Color>>()
    }

    private val defaultPalette = listOf(
        Color(0xFF00E5FF), // Electric Cyan
        Color(0xFF7C4DFF), // Deep Violet
        Color(0xFFFF0055), // Cyber Pink
        Color(0xFF00E676), // Neon Emerald
        Color(0xFFFF9100), // Plasma Amber
        Color(0xFF2979FF)  // Azure Blue
    )

    fun getCachedApps(): List<AppItem>? = cachedAppList

    suspend fun loadInstalledApps(forceRefresh: Boolean = false): List<AppItem> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh) {
                cachedAppList?.let { if (it.isNotEmpty()) return@withContext it }
            }

            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos: List<ResolveInfo> = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(
                        mainIntent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(mainIntent, 0)
                }
            } catch (_: Throwable) {
                emptyList()
            }

            val myPackage = context.packageName
            val seenIds = HashSet<String>(resolveInfos.size)
            val apps = ArrayList<AppItem>(resolveInfos.size)

            for ((index, resolveInfo) in resolveInfos.withIndex()) {
                val activityInfo = resolveInfo.activityInfo ?: continue
                val packageName = activityInfo.packageName ?: continue
                if (packageName == myPackage) continue

                val activityName = activityInfo.name ?: continue
                val appId = "$packageName/$activityName"
                if (!seenIds.add(appId)) continue

                val label = try {
                    resolveInfo.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: packageName
                } catch (_: Throwable) {
                    packageName
                }
                val appInfo = activityInfo.applicationInfo

                val emulatorClassification = detectEmulator(packageName, label)
                val isEmulator = emulatorClassification != null
                val isGame = isEmulator || (appInfo != null && isGameApp(appInfo, packageName, label))
                val isMedia = appInfo != null && isMediaApp(appInfo, packageName, label)
                val systemTag = emulatorClassification ?: if (isGame) "CONSOLE" else if (isMedia) "MEDIA" else "SYSTEM"

                var imageBitmap: ImageBitmap? = null
                var accentColor = defaultPalette[index % defaultPalette.size]

                val cachedIcon = iconCache[appId]
                if (cachedIcon != null) {
                    imageBitmap = cachedIcon.first
                    accentColor = cachedIcon.second
                } else {
                    try {
                        val iconDrawable = resolveInfo.loadIcon(pm)
                        val softwareBitmap = drawableToSoftwareBitmap(iconDrawable)
                        if (softwareBitmap != null) {
                            val composeBmp = softwareBitmap.asImageBitmap()
                            val dominant = extractDominantColor(softwareBitmap, accentColor)
                            imageBitmap = composeBmp
                            accentColor = dominant
                            iconCache[appId] = composeBmp to dominant
                        }
                    } catch (e: Throwable) {
                        Log.w("AppRepository", "Failed to extract icon for $packageName", e)
                    }
                }

                apps.add(
                    AppItem(
                        id = appId,
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
            if (apps.isNotEmpty()) {
                cachedAppList = apps
            }
            apps
        } catch (e: Throwable) {
            Log.e("AppRepository", "Error loading installed apps", e)
            cachedAppList ?: emptyList()
        }
    }

    private fun detectEmulator(packageName: String, label: String): String? {
        val p = packageName.lowercase()
        val l = label.lowercase()
        return when {
            p.contains("retroarch") || l.contains("retroarch") -> "RETROARCH"
            p.contains("drastic") || p.contains("melonds") || l.contains("drastic") || l.contains("melonds") -> "DUAL SCREEN"
            p.contains("citra") || p.contains("mandarine") || p.contains("lime3ds") || p.contains("azahar") || l.contains("citra") -> "3D DUAL SCREEN"
            p.contains("dolphin") || l.contains("dolphin") -> "OPTICAL RETRO"
            p.contains("ppsspp") || l.contains("ppsspp") -> "PORTABLE"
            p.contains("aethersx2") || p.contains("nethersx2") || p.contains("play!") -> "128-BIT RETRO"
            p.contains("duckstation") || p.contains("epsxe") || p.contains("swanstation") -> "32-BIT RETRO"
            p.contains("yuzu") || p.contains("suyu") || p.contains("sudachi") || p.contains("skyline") || p.contains("strato") -> "STANDALONE EMU"
            p.contains("vita3k") -> "OLED PORTABLE"
            p.contains("redream") || p.contains("flycast") -> "DREAMCAST"
            p.contains("mupen") || p.contains("m64plus") -> "64-BIT RETRO"
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

        val lowered = "$packageName $label".lowercase()
        return lowered.contains("game") ||
            lowered.contains("steam") ||
            lowered.contains("emulator") ||
            lowered.contains("arcade")
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
        val lowered = "$packageName $label".lowercase()
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

    /**
     * Always renders Drawable into a software ARGB_8888 Bitmap (max 128x128) so that:
     * 1) Android HARDWARE Bitmaps never crash on bitmap.getPixel(x, y)
     * 2) GPU texture memory stays small for 120Hz zero-lag scrolling
     */
    private fun drawableToSoftwareBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        return try {
            val width = (if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128).coerceIn(1, 128)
            val height = (if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128).coerceIn(1, 128)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Throwable) {
            null
        }
    }

    private fun extractDominantColor(bitmap: Bitmap, fallback: Color): Color {
        return try {
            val safeBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return fallback
            } else {
                bitmap
            }

            val w = safeBitmap.width
            val h = safeBitmap.height
            if (w <= 0 || h <= 0) return fallback

            var bestColor = fallback
            var maxScore = -1f

            val stepX = (w / 6).coerceAtLeast(1)
            val stepY = (h / 6).coerceAtLeast(1)
            val hsl = FloatArray(3)

            for (x in stepX / 2 until w step stepX) {
                for (y in stepY / 2 until h step stepY) {
                    val pixel = safeBitmap.getPixel(x, y)
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

            bestColor
        } catch (_: Throwable) {
            fallback
        }
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
        } catch (e: Throwable) {
            try {
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(appItem.packageName)
                if (fallbackIntent != null) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                    return true
                }
            } catch (_: Throwable) {}
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
        } catch (e: Throwable) {
            Log.e("AppRepository", "Failed to open updates url", e)
        }
    }

    fun launchWifiBtSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            try {
                val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (e2: Throwable) {
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
        } catch (e: Throwable) {
            Log.e("AppRepository", "Failed to open settings", e)
        }
    }
}
