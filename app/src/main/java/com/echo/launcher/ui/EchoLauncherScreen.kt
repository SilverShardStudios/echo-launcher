package com.echo.launcher.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echo.launcher.data.AppItem
import com.echo.launcher.data.AppRepository
import com.echo.launcher.data.ConsoleUiStyle
import com.echo.launcher.data.DeviceOptimizer
import com.echo.launcher.data.DockAction
import com.echo.launcher.data.DualScreenManager
import com.echo.launcher.data.FocusZone
import com.echo.launcher.data.GameStats
import com.echo.launcher.data.GameStatsRepository
import com.echo.launcher.data.GamepadLayout
import com.echo.launcher.data.HandheldControllerManager
import com.echo.launcher.data.LauncherTab
import com.echo.launcher.data.OptimizationResult
import com.echo.launcher.data.PowerInfo
import com.echo.launcher.data.UiThemePack
import com.echo.launcher.data.UiThemeRepository
import com.echo.launcher.ui.components.ALL_APPS_FILTERS
import com.echo.launcher.ui.components.AllAppsModal
import com.echo.launcher.ui.components.AmbientBackground
import com.echo.launcher.ui.components.BootSequenceScreen
import com.echo.launcher.ui.components.ConsoleFooter
import com.echo.launcher.ui.components.Dual3DLowerTouchDeck
import com.echo.launcher.ui.components.Dual3DUpperStage
import com.echo.launcher.ui.components.DualScreenBottomDeck
import com.echo.launcher.ui.components.GameCarousel
import com.echo.launcher.ui.components.GameDetailsModal
import com.echo.launcher.ui.components.HybridPortableDeckLayout
import com.echo.launcher.ui.components.NextGenHorizonLayout
import com.echo.launcher.ui.components.OptimizerModal
import com.echo.launcher.ui.components.StereoscopicDual3DLayout
import com.echo.launcher.ui.components.TopBar
import com.echo.launcher.ui.components.UI_HUB_FILTERS
import com.echo.launcher.ui.components.UiHubModal
import com.echo.launcher.ui.components.filterAllApps
import com.echo.launcher.ui.components.filterUiPacks
import com.echo.launcher.ui.components.openAndroidAppInfo
import com.echo.launcher.ui.theme.ElectricCyan
import com.echo.launcher.ui.theme.PureWhite
import com.echo.launcher.ui.theme.StatusGreen
import com.echo.launcher.ui.theme.SurfaceBorder
import com.echo.launcher.ui.theme.SurfaceCardElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var hasShownBootThisProcess = false

@Composable
fun EchoLauncherScreen(
    powerInfo: PowerInfo,
    resumeTick: Int = 0,
    onRequestDefaultHome: () -> Unit = {},
    onHandleKeyEvent: ((KeyEvent) -> Boolean) -> Unit = {},
    onHandleMotionEvent: ((MotionEvent) -> Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AppRepository(context) }
    val controllerManager = remember { HandheldControllerManager() }
    val dualScreenManager = remember { DualScreenManager(context) }
    val deviceOptimizer = remember { DeviceOptimizer(context) }
    val uiThemeRepository = remember { UiThemeRepository(context) }
    val gameStatsRepository = remember { GameStatsRepository(context) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val isTallSpannedDisplay = screenHeight > (screenWidth * 0.95f)
    var isDualScreenMode by remember(isTallSpannedDisplay, dualScreenManager.hasPhysicalSecondaryDisplay) {
        mutableStateOf(
            dualScreenManager.isDualScreenMode ||
                dualScreenManager.hasPhysicalSecondaryDisplay ||
                isTallSpannedDisplay
        )
    }

    var isDefaultHomeLauncher by remember { mutableStateOf(checkIsDefaultLauncher(context)) }
    var isBooting by remember { mutableStateOf(!hasShownBootThisProcess) }
    var allApps by remember { mutableStateOf(repository.getCachedApps() ?: emptyList()) }
    var gameStats by remember { mutableStateOf<Map<String, GameStats>>(emptyMap()) }
    var hasUsageAccess by remember { mutableStateOf(gameStatsRepository.hasUsageAccess()) }

    var selectedTab by remember { mutableStateOf(LauncherTab.GAMES) }
    var currentFocusZone by remember { mutableStateOf(FocusZone.CAROUSEL) }
    var gamepadLayout by remember { mutableStateOf(uiThemeRepository.gamepadLayout) }
    var sortByRecentlyPlayed by remember { mutableStateOf(uiThemeRepository.sortByRecentlyPlayed) }

    var selectedGameIndex by remember { mutableIntStateOf(0) }
    var selectedMediaIndex by remember { mutableIntStateOf(0) }
    var selectedDockIndex by remember { mutableIntStateOf(0) }
    var selectedModalIndex by remember { mutableIntStateOf(0) }

    var isAllAppsOpen by remember { mutableStateOf(false) }
    var allAppsFilter by remember { mutableStateOf("ALL") }
    var allAppsSearchQuery by remember { mutableStateOf("") }
    var isSearchFieldFocused by remember { mutableStateOf(false) }

    var isDetailsOpen by remember { mutableStateOf(false) }
    var detailsTargetAppId by remember { mutableStateOf<String?>(null) }
    var selectedDetailAction by remember { mutableIntStateOf(0) }

    var isOptimizerOpen by remember { mutableStateOf(false) }
    var isUiHubOpen by remember { mutableStateOf(false) }
    var uiHubFilter by remember { mutableStateOf("ALL") }

    val uiPacks = remember { uiThemeRepository.availablePacks }
    var downloadedPackIds by remember { mutableStateOf(uiThemeRepository.getDownloadedIds()) }
    var activeUiStyle by remember { mutableStateOf(uiThemeRepository.getActiveUiStyle()) }
    var isHybridLightTheme by remember { mutableStateOf(uiThemeRepository.isHybridLightTheme) }
    var dual3dGridColumns by remember { mutableIntStateOf(uiThemeRepository.dual3dGridColumns) }
    var dual3dDepthLevel by remember { mutableFloatStateOf(1.0f) }

    var showHorizonControlCenter by remember { mutableStateOf(false) }
    var horizonControlIndex by remember { mutableIntStateOf(0) }

    var selectedUiHubIndex by remember { mutableIntStateOf(0) }
    var downloadingPackId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    var telemetry by remember { mutableStateOf(deviceOptimizer.getTelemetry()) }
    var isOptimizing by remember { mutableStateOf(false) }
    var lastOptimizationResult by remember { mutableStateOf<OptimizationResult?>(null) }
    var autoBoostEnabled by remember { mutableStateOf(deviceOptimizer.isAutoBoostEnabled) }
    var toastNotificationText by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(gamepadLayout) {
        controllerManager.layout = gamepadLayout
    }

    fun playClickSound() {
        try { view.playSoundEffect(SoundEffectConstants.CLICK) } catch (_: Exception) {}
    }

    fun playNavSound(direction: Int) {
        try { view.playSoundEffect(direction) } catch (_: Exception) {}
    }

    fun showToast(msg: String, durationMs: Long = 2800L) {
        toastNotificationText = msg
        coroutineScope.launch {
            delay(durationMs)
            if (toastNotificationText == msg) {
                toastNotificationText = null
            }
        }
    }

    fun toggleGamepadLayout() {
        playClickSound()
        val next = if (gamepadLayout == GamepadLayout.ABXY) GamepadLayout.BAYX else GamepadLayout.ABXY
        gamepadLayout = next
        uiThemeRepository.gamepadLayout = next
        showToast("🎮 Controller: ${next.name}")
    }

    fun toggleSortOrder() {
        playClickSound()
        val next = !sortByRecentlyPlayed
        sortByRecentlyPlayed = next
        uiThemeRepository.sortByRecentlyPlayed = next
        if (next && !hasUsageAccess) {
            showToast("Grant Usage Access in Optimizer to sort by Recent", 3200L)
        } else {
            showToast(if (next) "🕒 Sorted by Recently Played" else "A–Z Alphabetical Sort")
        }
    }

    fun toggleDualScreen() {
        val next = dualScreenManager.toggleDualScreenMode()
        isDualScreenMode = next
        playClickSound()
    }

    fun handleInstantPreviewUiPack(pack: UiThemePack) {
        playClickSound()
        activeUiStyle = pack.style
        uiThemeRepository.setActiveUiStyle(pack.style)
        isUiHubOpen = false
        showToast("👁 Test-Driving ${pack.title}")
    }

    fun handleDownloadAllUiPacks() {
        playClickSound()
        if (downloadingPackId != null) return
        coroutineScope.launch {
            for (pack in uiPacks) {
                if (!downloadedPackIds.contains(pack.id)) {
                    downloadingPackId = pack.id
                    for (step in 1..4) {
                        delay(40)
                        downloadProgress = step / 4f
                    }
                    downloadedPackIds = uiThemeRepository.markPackDownloaded(pack.id)
                }
            }
            downloadingPackId = null
            downloadProgress = 0f
            showToast("✓ All ${uiPacks.size} Console UI Shells Installed")
        }
    }

    fun handleDownloadOrApplyUiPack(pack: UiThemePack) {
        playClickSound()
        if (downloadingPackId != null) return

        val isAlreadyDownloaded = downloadedPackIds.contains(pack.id)
        if (isAlreadyDownloaded) {
            activeUiStyle = pack.style
            uiThemeRepository.setActiveUiStyle(pack.style)
            isUiHubOpen = false
            showToast("❖ Activated ${pack.title}")
        } else {
            coroutineScope.launch {
                downloadingPackId = pack.id
                downloadProgress = 0.1f
                for (step in 1..6) {
                    delay(50)
                    downloadProgress = (step.toFloat() / 6f).coerceIn(0.1f, 1.0f)
                }
                downloadedPackIds = uiThemeRepository.markPackDownloaded(pack.id)
                activeUiStyle = pack.style
                uiThemeRepository.setActiveUiStyle(pack.style)
                downloadingPackId = null
                downloadProgress = 0f
                isUiHubOpen = false
                showToast("⬇ Installed & Applied ${pack.title}")
            }
        }
    }

    fun handleUninstallUiPack(pack: UiThemePack) {
        playClickSound()
        if (pack.isBuiltIn) return
        downloadedPackIds = uiThemeRepository.removePackDownload(pack.id)
        if (activeUiStyle == pack.style) {
            activeUiStyle = ConsoleUiStyle.ECHO_DEFAULT
            uiThemeRepository.setActiveUiStyle(ConsoleUiStyle.ECHO_DEFAULT)
        }
        showToast("Removed ${pack.title}")
    }

    fun triggerDeviceOptimization(openModal: Boolean = true, onDone: (() -> Unit)? = null) {
        playClickSound()
        if (openModal) {
            isOptimizerOpen = true
        }
        if (isOptimizing) return
        coroutineScope.launch {
            isOptimizing = true
            val result = deviceOptimizer.optimizeDevice(allApps)
            telemetry = result.telemetryAfter
            lastOptimizationResult = result
            isOptimizing = false
            showToast("⚡ ${result.statusMessage}", 3600L)
            onDone?.invoke()
        }
    }

    fun launchAppWithOptionalBoost(app: AppItem, forceBoost: Boolean = false) {
        playClickSound()
        if (forceBoost || autoBoostEnabled) {
            coroutineScope.launch {
                val res = deviceOptimizer.optimizeDevice(allApps, excludePackage = app.packageName)
                telemetry = res.telemetryAfter
                lastOptimizationResult = res
                if (res.freedRamMb > 0) {
                    showToast("⚡ Freed ${res.freedRamMb} MB RAM for ${app.label}", 2600L)
                }
                repository.launchApp(app)
            }
        } else {
            repository.launchApp(app)
        }
    }

    fun refreshAppLibrary() {
        playClickSound()
        coroutineScope.launch {
            val refreshed = repository.loadInstalledApps(forceRefresh = true)
            allApps = refreshed
            hasUsageAccess = gameStatsRepository.hasUsageAccess()
            gameStats = gameStatsRepository.loadStats(refreshed)
            showToast("↻ Library Rescanned: ${refreshed.size} Apps Ready")
        }
    }

    LaunchedEffect(Unit) {
        val loaded = repository.loadInstalledApps()
        allApps = loaded
        isDefaultHomeLauncher = checkIsDefaultLauncher(context)
        telemetry = deviceOptimizer.getTelemetry()
    }

    LaunchedEffect(resumeTick, allApps) {
        isDefaultHomeLauncher = checkIsDefaultLauncher(context)
        telemetry = deviceOptimizer.getTelemetry()
        val usageOk = gameStatsRepository.hasUsageAccess()
        hasUsageAccess = usageOk
        if (allApps.isNotEmpty()) {
            gameStats = gameStatsRepository.loadStats(allApps)
        }
    }

    val sortedAllApps = remember(allApps, sortByRecentlyPlayed, hasUsageAccess, gameStats) {
        allApps.sortedWith(
            compareByDescending<AppItem> { it.isFavorite }
                .thenByDescending {
                    if (sortByRecentlyPlayed && hasUsageAccess) gameStats[it.packageName]?.lastPlayedMs ?: 0L else 0L
                }
                .thenBy { it.label.lowercase() }
        )
    }

    val currentTabApps = remember(sortedAllApps, selectedTab) {
        when (selectedTab) {
            LauncherTab.GAMES -> {
                val games = sortedAllApps.filter { it.isGame }
                if (games.isNotEmpty()) games else sortedAllApps
            }
            LauncherTab.MEDIA -> {
                val media = sortedAllApps.filter { it.isMedia }
                if (media.isNotEmpty()) media else sortedAllApps
            }
        }
    }

    val filteredAllApps = remember(sortedAllApps, allAppsFilter, allAppsSearchQuery) {
        filterAllApps(sortedAllApps, allAppsFilter, allAppsSearchQuery)
    }

    val visibleUiPacks = remember(uiPacks, uiHubFilter, downloadedPackIds) {
        filterUiPacks(uiPacks, uiHubFilter, downloadedPackIds)
    }

    val activeCarouselIndex = when (selectedTab) {
        LauncherTab.GAMES -> selectedGameIndex.coerceIn(0, (currentTabApps.size - 1).coerceAtLeast(0))
        LauncherTab.MEDIA -> selectedMediaIndex.coerceIn(0, (currentTabApps.size - 1).coerceAtLeast(0))
    }

    val activeApp = currentTabApps.getOrNull(activeCarouselIndex)
    val activeAccentColor = activeApp?.accentColor ?: ElectricCyan
    val detailsApp = detailsTargetAppId?.let { id -> sortedAllApps.find { it.id == id } } ?: activeApp

    // Hardware dual display support
    LaunchedEffect(
        currentTabApps,
        activeCarouselIndex,
        isAllAppsOpen,
        dualScreenManager.hasPhysicalSecondaryDisplay,
        isDualScreenMode
    ) {
        if (dualScreenManager.hasPhysicalSecondaryDisplay && isDualScreenMode && currentTabApps.isNotEmpty()) {
            dualScreenManager.showSecondaryPresentation(
                activityContext = context,
                apps = currentTabApps,
                selectedIndex = activeCarouselIndex,
                onSelectIndex = { idx ->
                    if (selectedTab == LauncherTab.GAMES) selectedGameIndex = idx else selectedMediaIndex = idx
                },
                onLaunch = { launchAppWithOptionalBoost(it) },
                onToggleAllApps = { isAllAppsOpen = !isAllAppsOpen },
                onOpenSettings = { repository.launchSettings() }
            )
        } else {
            dualScreenManager.dismissSecondaryPresentation()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            dualScreenManager.release()
        }
    }

    fun setCarouselIndex(newIdx: Int) {
        val clamped = newIdx.coerceIn(0, (currentTabApps.size - 1).coerceAtLeast(0))
        if (selectedTab == LauncherTab.GAMES) selectedGameIndex = clamped else selectedMediaIndex = clamped
    }

    fun triggerDockAction(action: DockAction) {
        playClickSound()
        when (action) {
            DockAction.OPTIMIZE -> triggerDeviceOptimization(openModal = true)
            DockAction.UI_HUB -> {
                selectedUiHubIndex = 0
                isUiHubOpen = true
            }
            DockAction.ALL_APPS -> {
                selectedModalIndex = 0
                isAllAppsOpen = true
                currentFocusZone = FocusZone.ALL_APPS_MODAL
            }
            DockAction.ECHO_UPDATES -> repository.launchEchoUpdates()
            DockAction.WIFI_BT -> repository.launchWifiBtSettings()
            DockAction.SETTINGS -> repository.launchSettings()
        }
    }

    fun triggerHorizonControlAction(idx: Int) {
        playClickSound()
        when (idx) {
            0 -> {
                showHorizonControlCenter = false
                currentFocusZone = FocusZone.CAROUSEL
            }
            1 -> {
                showHorizonControlCenter = false
                selectedModalIndex = 0
                isAllAppsOpen = true
                currentFocusZone = FocusZone.ALL_APPS_MODAL
            }
            2 -> {
                showHorizonControlCenter = false
                triggerDeviceOptimization(openModal = true)
            }
            3 -> {
                showHorizonControlCenter = false
                isUiHubOpen = true
            }
            4 -> toggleDualScreen()
            5 -> repository.launchSettings()
        }
    }

    fun openDetailsForApp(app: AppItem?) {
        if (app == null) return
        playClickSound()
        detailsTargetAppId = app.id
        selectedDetailAction = 0
        isDetailsOpen = true
    }

    BackHandler(enabled = true) {
        if (isBooting) {
            isBooting = false
            hasShownBootThisProcess = true
        } else if (isUiHubOpen) {
            isUiHubOpen = false
            playClickSound()
        } else if (isOptimizerOpen) {
            isOptimizerOpen = false
            playClickSound()
        } else if (isDetailsOpen) {
            isDetailsOpen = false
            detailsTargetAppId = null
            playClickSound()
        } else if (isAllAppsOpen) {
            isAllAppsOpen = false
            isSearchFieldFocused = false
            currentFocusZone = FocusZone.CAROUSEL
            playClickSound()
        } else if (showHorizonControlCenter) {
            showHorizonControlCenter = false
            currentFocusZone = FocusZone.CAROUSEL
            playClickSound()
        }
    }

    fun handleKeyNavigation(nativeKeyCode: Int, isActionDown: Boolean): Boolean {
        if (!isActionDown) return false

        if (isBooting) {
            isBooting = false
            hasShownBootThisProcess = true
            playClickSound()
            return true
        }

        // Let soft/hardware keyboard typing work inside the Library search box without controller shortcuts hijacking Q/E/X/Y
        if (isAllAppsOpen && isSearchFieldFocused &&
            nativeKeyCode != KeyEvent.KEYCODE_ESCAPE &&
            nativeKeyCode != KeyEvent.KEYCODE_BACK
        ) {
            return false
        }

        if (isUiHubOpen) {
            when {
                nativeKeyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                    selectedUiHubIndex = (selectedUiHubIndex - 1).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                nativeKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    selectedUiHubIndex = (selectedUiHubIndex + 1).coerceAtMost((visibleUiPacks.size - 1).coerceAtLeast(0))
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                controllerManager.isTabLeft(nativeKeyCode) -> {
                    val cur = UI_HUB_FILTERS.indexOf(uiHubFilter).coerceAtLeast(0)
                    uiHubFilter = UI_HUB_FILTERS[(cur - 1 + UI_HUB_FILTERS.size) % UI_HUB_FILTERS.size]
                    selectedUiHubIndex = 0
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                controllerManager.isTabRight(nativeKeyCode) -> {
                    val cur = UI_HUB_FILTERS.indexOf(uiHubFilter).coerceAtLeast(0)
                    uiHubFilter = UI_HUB_FILTERS[(cur + 1) % UI_HUB_FILTERS.size]
                    selectedUiHubIndex = 0
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                controllerManager.isOptionsKey(nativeKeyCode) -> {
                    visibleUiPacks.getOrNull(selectedUiHubIndex)?.let { handleInstantPreviewUiPack(it) }
                    return true
                }
                controllerManager.isDrawerKey(nativeKeyCode) -> {
                    handleDownloadAllUiPacks()
                    return true
                }
                controllerManager.isCancelKey(nativeKeyCode) -> {
                    isUiHubOpen = false
                    playClickSound()
                    return true
                }
                controllerManager.isConfirmKey(nativeKeyCode) -> {
                    visibleUiPacks.getOrNull(selectedUiHubIndex)?.let { handleDownloadOrApplyUiPack(it) }
                    return true
                }
            }
            return false
        }

        if (isOptimizerOpen) {
            when {
                controllerManager.isCancelKey(nativeKeyCode) -> {
                    isOptimizerOpen = false
                    playClickSound()
                    return true
                }
                controllerManager.isConfirmKey(nativeKeyCode) -> {
                    triggerDeviceOptimization(openModal = true)
                    return true
                }
                controllerManager.isOptionsKey(nativeKeyCode) -> {
                    toggleGamepadLayout()
                    return true
                }
                controllerManager.isDrawerKey(nativeKeyCode) -> {
                    toggleSortOrder()
                    return true
                }
            }
            return false
        }

        if (isDetailsOpen) {
            when (nativeKeyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    selectedDetailAction = when (selectedDetailAction) {
                        1 -> 0
                        3 -> 2
                        else -> selectedDetailAction
                    }
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    selectedDetailAction = when (selectedDetailAction) {
                        0 -> 1
                        2 -> 3
                        else -> selectedDetailAction
                    }
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    selectedDetailAction = (selectedDetailAction - 2).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_UP)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    selectedDetailAction = (selectedDetailAction + 2).coerceAtMost(3)
                    playNavSound(SoundEffectConstants.NAVIGATION_DOWN)
                    return true
                }
                else -> {
                    if (controllerManager.isCancelKey(nativeKeyCode)) {
                        isDetailsOpen = false
                        detailsTargetAppId = null
                        playClickSound()
                        return true
                    }
                    if (controllerManager.isConfirmKey(nativeKeyCode)) {
                        val target = detailsApp
                        when (selectedDetailAction) {
                            0 -> {
                                isDetailsOpen = false
                                detailsTargetAppId = null
                                target?.let { launchAppWithOptionalBoost(it, forceBoost = true) }
                            }
                            1 -> {
                                isDetailsOpen = false
                                detailsTargetAppId = null
                                target?.let { launchAppWithOptionalBoost(it, forceBoost = false) }
                            }
                            2 -> {
                                target?.let { app ->
                                    playClickSound()
                                    val nowFav = repository.toggleFavorite(app.packageName)
                                    allApps = repository.getCachedApps() ?: allApps
                                    showToast(if (nowFav) "★ Pinned ${app.label}" else "Unpinned ${app.label}")
                                }
                            }
                            3 -> {
                                target?.let { openAndroidAppInfo(context, it.packageName) }
                            }
                        }
                        return true
                    }
                }
            }
            return false
        }

        if (isAllAppsOpen) {
            when {
                nativeKeyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                    selectedModalIndex = (selectedModalIndex - 1).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                nativeKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    selectedModalIndex = (selectedModalIndex + 1).coerceAtMost((filteredAllApps.size - 1).coerceAtLeast(0))
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                nativeKeyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                    selectedModalIndex = (selectedModalIndex - 4).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_UP)
                    return true
                }
                nativeKeyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                    selectedModalIndex = (selectedModalIndex + 4).coerceAtMost((filteredAllApps.size - 1).coerceAtLeast(0))
                    playNavSound(SoundEffectConstants.NAVIGATION_DOWN)
                    return true
                }
                controllerManager.isTabLeft(nativeKeyCode) -> {
                    val cur = ALL_APPS_FILTERS.indexOf(allAppsFilter).coerceAtLeast(0)
                    allAppsFilter = ALL_APPS_FILTERS[(cur - 1 + ALL_APPS_FILTERS.size) % ALL_APPS_FILTERS.size]
                    selectedModalIndex = 0
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                controllerManager.isTabRight(nativeKeyCode) -> {
                    val cur = ALL_APPS_FILTERS.indexOf(allAppsFilter).coerceAtLeast(0)
                    allAppsFilter = ALL_APPS_FILTERS[(cur + 1) % ALL_APPS_FILTERS.size]
                    selectedModalIndex = 0
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                controllerManager.isOptionsKey(nativeKeyCode) -> {
                    filteredAllApps.getOrNull(selectedModalIndex)?.let { openDetailsForApp(it) }
                    return true
                }
                controllerManager.isConfirmKey(nativeKeyCode) -> {
                    filteredAllApps.getOrNull(selectedModalIndex)?.let {
                        isAllAppsOpen = false
                        isSearchFieldFocused = false
                        launchAppWithOptionalBoost(it)
                    }
                    return true
                }
                controllerManager.isCancelKey(nativeKeyCode) -> {
                    isAllAppsOpen = false
                    isSearchFieldFocused = false
                    currentFocusZone = FocusZone.DOCK
                    playClickSound()
                    return true
                }
            }
            return false
        }

        if (controllerManager.isDrawerKey(nativeKeyCode)) {
            selectedModalIndex = 0
            isAllAppsOpen = true
            currentFocusZone = FocusZone.ALL_APPS_MODAL
            playClickSound()
            return true
        }

        if (controllerManager.isOptionsKey(nativeKeyCode)) {
            if (activeApp != null) {
                openDetailsForApp(activeApp)
                return true
            }
        }

        if (controllerManager.isTabLeft(nativeKeyCode)) {
            selectedTab = LauncherTab.GAMES
            playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
            return true
        }
        if (controllerManager.isTabRight(nativeKeyCode)) {
            selectedTab = LauncherTab.MEDIA
            playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
            return true
        }

        when (currentFocusZone) {
            FocusZone.TOP_TABS -> {
                when (nativeKeyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        selectedTab = LauncherTab.GAMES
                        playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        selectedTab = LauncherTab.MEDIA
                        playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        currentFocusZone = FocusZone.CAROUSEL
                        playNavSound(SoundEffectConstants.NAVIGATION_DOWN)
                        return true
                    }
                    else -> {
                        if (controllerManager.isConfirmKey(nativeKeyCode)) {
                            currentFocusZone = FocusZone.CAROUSEL
                            playClickSound()
                            return true
                        }
                    }
                }
            }

            FocusZone.CAROUSEL -> {
                when (nativeKeyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        setCarouselIndex(activeCarouselIndex - 1)
                        playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        setCarouselIndex(activeCarouselIndex + 1)
                        playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (activeUiStyle == ConsoleUiStyle.DUAL_SCREEN_3D && activeCarouselIndex >= dual3dGridColumns) {
                            setCarouselIndex(activeCarouselIndex - dual3dGridColumns)
                        } else {
                            currentFocusZone = FocusZone.TOP_TABS
                        }
                        playNavSound(SoundEffectConstants.NAVIGATION_UP)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (activeUiStyle == ConsoleUiStyle.DUAL_SCREEN_3D && activeCarouselIndex + dual3dGridColumns < currentTabApps.size) {
                            setCarouselIndex(activeCarouselIndex + dual3dGridColumns)
                        } else if (activeUiStyle == ConsoleUiStyle.NEXTGEN_MEDIA_BAR) {
                            showHorizonControlCenter = true
                            currentFocusZone = FocusZone.DOCK
                        } else {
                            currentFocusZone = FocusZone.DOCK
                        }
                        playNavSound(SoundEffectConstants.NAVIGATION_DOWN)
                        return true
                    }
                    else -> {
                        if (controllerManager.isConfirmKey(nativeKeyCode)) {
                            activeApp?.let { launchAppWithOptionalBoost(it) }
                            return true
                        }
                    }
                }
            }

            FocusZone.DOCK -> {
                if (activeUiStyle == ConsoleUiStyle.NEXTGEN_MEDIA_BAR) {
                    when {
                        nativeKeyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                            horizonControlIndex = (horizonControlIndex - 1).coerceAtLeast(0)
                            playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                            return true
                        }
                        nativeKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            horizonControlIndex = (horizonControlIndex + 1).coerceAtMost(5)
                            playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                            return true
                        }
                        nativeKeyCode == KeyEvent.KEYCODE_DPAD_UP || controllerManager.isCancelKey(nativeKeyCode) -> {
                            showHorizonControlCenter = false
                            currentFocusZone = FocusZone.CAROUSEL
                            playNavSound(SoundEffectConstants.NAVIGATION_UP)
                            return true
                        }
                        controllerManager.isConfirmKey(nativeKeyCode) -> {
                            triggerHorizonControlAction(horizonControlIndex)
                            return true
                        }
                    }
                } else {
                    when (nativeKeyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            selectedDockIndex = (selectedDockIndex - 1).coerceAtLeast(0)
                            playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                            return true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            selectedDockIndex = (selectedDockIndex + 1).coerceAtMost(5)
                            playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                            return true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            currentFocusZone = FocusZone.CAROUSEL
                            playNavSound(SoundEffectConstants.NAVIGATION_UP)
                            return true
                        }
                        else -> {
                            if (controllerManager.isConfirmKey(nativeKeyCode)) {
                                DockAction.entries.getOrNull(selectedDockIndex)?.let { triggerDockAction(it) }
                                return true
                            }
                        }
                    }
                }
            }

            FocusZone.ALL_APPS_MODAL -> Unit
        }

        return false
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    onHandleKeyEvent { event ->
        handleKeyNavigation(event.keyCode, event.action == KeyEvent.ACTION_DOWN)
    }

    onHandleMotionEvent { motionEvent ->
        controllerManager.processMotionEvent(motionEvent) { directionCode ->
            handleKeyNavigation(directionCode, isActionDown = true)
        }
    }

    val useSpannedDualScreenLayout = isDualScreenMode

    @Composable
    fun RenderConsoleShellScreen(modifier: Modifier = Modifier) {
        when (activeUiStyle) {
            ConsoleUiStyle.NEXTGEN_MEDIA_BAR -> {
                NextGenHorizonLayout(
                    apps = currentTabApps,
                    selectedIndex = activeCarouselIndex,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    gameStats = gameStats,
                    hasUsageAccess = hasUsageAccess,
                    onRequestUsageAccess = { gameStatsRepository.openUsageAccessSettings() },
                    sortByRecent = sortByRecentlyPlayed,
                    onToggleSort = ::toggleSortOrder,
                    showControlCenter = showHorizonControlCenter,
                    onToggleControlCenter = {
                        playClickSound()
                        showHorizonControlCenter = !showHorizonControlCenter
                        currentFocusZone = if (showHorizonControlCenter) FocusZone.DOCK else FocusZone.CAROUSEL
                    },
                    controlCenterIndex = horizonControlIndex,
                    onControlCenterAction = ::triggerHorizonControlAction,
                    currentFocusZone = currentFocusZone,
                    gamepadLayout = gamepadLayout,
                    powerInfo = powerInfo,
                    ramUsagePercent = telemetry.ramUsagePercent,
                    isDualScreenMode = isDualScreenMode,
                    onToggleDualScreen = ::toggleDualScreen,
                    onIndexSelected = { index ->
                        setCarouselIndex(index)
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    onLaunch = { launchAppWithOptionalBoost(it) },
                    onOptions = { openDetailsForApp(it) },
                    onOpenUiHub = {
                        playClickSound()
                        selectedUiHubIndex = 0
                        isUiHubOpen = true
                    },
                    onOptimize = { triggerDeviceOptimization(openModal = true) },
                    onAllApps = {
                        playClickSound()
                        selectedModalIndex = 0
                        isAllAppsOpen = true
                        currentFocusZone = FocusZone.ALL_APPS_MODAL
                    },
                    onOpenSettings = {
                        playClickSound()
                        repository.launchSettings()
                    },
                    modifier = modifier
                )
            }
            ConsoleUiStyle.HYBRID_DECK -> {
                HybridPortableDeckLayout(
                    apps = currentTabApps,
                    selectedIndex = activeCarouselIndex,
                    gameStats = gameStats,
                    hasUsageAccess = hasUsageAccess,
                    sortByRecent = sortByRecentlyPlayed,
                    onToggleSort = ::toggleSortOrder,
                    gamepadLayout = gamepadLayout,
                    powerInfo = powerInfo,
                    ramUsagePercent = telemetry.ramUsagePercent,
                    isLightTheme = isHybridLightTheme,
                    onToggleLightTheme = {
                        playClickSound()
                        isHybridLightTheme = !isHybridLightTheme
                        uiThemeRepository.isHybridLightTheme = isHybridLightTheme
                    },
                    isDualScreenMode = isDualScreenMode,
                    onToggleDualScreen = ::toggleDualScreen,
                    onIndexSelected = { index ->
                        setCarouselIndex(index)
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    onLaunch = { launchAppWithOptionalBoost(it) },
                    onOptions = { openDetailsForApp(it) },
                    selectedDockIndex = selectedDockIndex,
                    currentFocusZone = currentFocusZone,
                    onDockActionSelected = { index ->
                        selectedDockIndex = index
                        currentFocusZone = FocusZone.DOCK
                        playClickSound()
                    },
                    onDockActionTriggered = ::triggerDockAction,
                    modifier = modifier
                )
            }
            ConsoleUiStyle.DUAL_SCREEN_3D -> {
                StereoscopicDual3DLayout(
                    apps = currentTabApps,
                    selectedIndex = activeCarouselIndex,
                    gameStats = gameStats,
                    hasUsageAccess = hasUsageAccess,
                    gridColumns = dual3dGridColumns,
                    onGridColumnsChange = { cols ->
                        playClickSound()
                        val clamped = cols.coerceIn(2, 6)
                        dual3dGridColumns = clamped
                        uiThemeRepository.dual3dGridColumns = clamped
                    },
                    depth3dLevel = dual3dDepthLevel,
                    onToggleDepth3d = {
                        playClickSound()
                        dual3dDepthLevel = if (dual3dDepthLevel > 0.5f) 0f else 1f
                    },
                    gamepadLayout = gamepadLayout,
                    powerInfo = powerInfo,
                    ramUsagePercent = telemetry.ramUsagePercent,
                    isDualScreenMode = isDualScreenMode,
                    onToggleDualScreen = ::toggleDualScreen,
                    onIndexSelected = { index ->
                        setCarouselIndex(index)
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    onLaunch = { launchAppWithOptionalBoost(it) },
                    onOptions = { openDetailsForApp(it) },
                    onOpenUiHub = {
                        playClickSound()
                        selectedUiHubIndex = 0
                        isUiHubOpen = true
                    },
                    onOptimize = { triggerDeviceOptimization(openModal = true) },
                    onAllApps = {
                        playClickSound()
                        selectedModalIndex = 0
                        isAllAppsOpen = true
                        currentFocusZone = FocusZone.ALL_APPS_MODAL
                    },
                    onOpenSettings = {
                        playClickSound()
                        repository.launchSettings()
                    },
                    modifier = modifier
                )
            }
            ConsoleUiStyle.ECHO_DEFAULT -> {
                Box(modifier = modifier.fillMaxSize()) {
                    AmbientBackground(targetAccentColor = activeAccentColor)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TopBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                currentFocusZone = FocusZone.CAROUSEL
                                playClickSound()
                            },
                            currentFocusZone = currentFocusZone,
                            powerInfo = powerInfo,
                            ramUsagePercent = telemetry.ramUsagePercent,
                            onOptimizeClick = { triggerDeviceOptimization(openModal = true) },
                            onOpenUiHub = {
                                playClickSound()
                                selectedUiHubIndex = 0
                                isUiHubOpen = true
                            },
                            isDualScreenMode = isDualScreenMode,
                            onToggleDualScreen = ::toggleDualScreen
                        )

                        if (!isDefaultHomeLauncher) {
                            DefaultHomePromptBanner(
                                onSetDefault = {
                                    playClickSound()
                                    onRequestDefaultHome()
                                }
                            )
                        }

                        if (toastNotificationText != null) {
                            OptimizationToastBanner(
                                message = toastNotificationText!!,
                                onClick = { isOptimizerOpen = true }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        GameCarousel(
                            apps = currentTabApps,
                            selectedIndex = activeCarouselIndex,
                            onIndexSelected = { index ->
                                setCarouselIndex(index)
                                currentFocusZone = FocusZone.CAROUSEL
                                playClickSound()
                            },
                            onLaunch = { app ->
                                launchAppWithOptionalBoost(app)
                            },
                            onOptions = { app ->
                                openDetailsForApp(app)
                            },
                            currentFocusZone = currentFocusZone,
                            gamepadLayout = gamepadLayout
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (!useSpannedDualScreenLayout) {
                            ConsoleFooter(
                                selectedActionIndex = selectedDockIndex,
                                onActionSelected = { index ->
                                    selectedDockIndex = index
                                    currentFocusZone = FocusZone.DOCK
                                    playClickSound()
                                },
                                onActionTriggered = ::triggerDockAction,
                                currentFocusZone = currentFocusZone,
                                gamepadLayout = gamepadLayout
                            )
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                val isDown = keyEvent.type == KeyEventType.KeyDown
                handleKeyNavigation(keyEvent.nativeKeyEvent.keyCode, isDown)
            }
    ) {
        if (useSpannedDualScreenLayout) {
            // DUAL-SCREEN SPANNED CLAMSHELL MODE (Anbernic RG DS / Vertical Dual Display)
            Column(modifier = Modifier.fillMaxSize()) {
                if (activeUiStyle == ConsoleUiStyle.DUAL_SCREEN_3D) {
                    // Authentic 1:1 Upper 3D Stage + Lower Touch Matrix split across physical displays!
                    Dual3DUpperStage(
                        activeApp = activeApp,
                        totalAppCount = currentTabApps.size,
                        stats = activeApp?.let { gameStats[it.packageName] },
                        hasUsageAccess = hasUsageAccess,
                        powerInfo = powerInfo,
                        ramUsagePercent = telemetry.ramUsagePercent,
                        depth3dLevel = dual3dDepthLevel,
                        onToggleDepth3d = {
                            playClickSound()
                            dual3dDepthLevel = if (dual3dDepthLevel > 0.5f) 0f else 1f
                        },
                        onOptimize = { triggerDeviceOptimization(openModal = true) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF0E131D))
                            .padding(8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.Black)
                            .border(1.dp, SurfaceBorder)
                    )

                    Dual3DLowerTouchDeck(
                        apps = currentTabApps,
                        selectedIndex = activeCarouselIndex,
                        gridColumns = dual3dGridColumns,
                        onGridColumnsChange = { cols ->
                            playClickSound()
                            val clamped = cols.coerceIn(2, 6)
                            dual3dGridColumns = clamped
                            uiThemeRepository.dual3dGridColumns = clamped
                        },
                        gamepadLayout = gamepadLayout,
                        isDualScreenMode = isDualScreenMode,
                        onToggleDualScreen = ::toggleDualScreen,
                        onIndexSelected = { index ->
                            setCarouselIndex(index)
                            playClickSound()
                        },
                        onLaunch = { launchAppWithOptionalBoost(it) },
                        onOptions = { openDetailsForApp(it) },
                        onOpenUiHub = {
                            playClickSound()
                            selectedUiHubIndex = 0
                            isUiHubOpen = true
                        },
                        onOptimize = { triggerDeviceOptimization(openModal = true) },
                        onAllApps = {
                            playClickSound()
                            selectedModalIndex = 0
                            isAllAppsOpen = true
                            currentFocusZone = FocusZone.ALL_APPS_MODAL
                        },
                        onOpenSettings = {
                            playClickSound()
                            repository.launchSettings()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF0E131D))
                            .padding(8.dp)
                    )
                } else {
                    // Upper Screen = Active UI Shell; Lower Screen = RG DS Touch Command Deck
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        RenderConsoleShellScreen(modifier = Modifier.fillMaxSize())
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.Black)
                            .border(1.dp, SurfaceBorder)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        DualScreenBottomDeck(
                            apps = currentTabApps,
                            selectedIndex = activeCarouselIndex,
                            onSelectIndex = { idx ->
                                setCarouselIndex(idx)
                                playClickSound()
                            },
                            onLaunch = { app ->
                                launchAppWithOptionalBoost(app)
                            },
                            onToggleAllApps = {
                                selectedModalIndex = 0
                                isAllAppsOpen = true
                                currentFocusZone = FocusZone.ALL_APPS_MODAL
                                playClickSound()
                            },
                            onOpenSettings = {
                                repository.launchSettings()
                                playClickSound()
                            },
                            onOptimizeDevice = { triggerDeviceOptimization(openModal = true) },
                            onOpenUiHub = {
                                playClickSound()
                                selectedUiHubIndex = 0
                                isUiHubOpen = true
                            },
                            ramUsagePercent = telemetry.ramUsagePercent,
                            onToggleDualScreenMode = ::toggleDualScreen
                        )
                    }
                }
            }
        } else {
            // SINGLE SCREEN MODE
            Box(modifier = Modifier.fillMaxSize()) {
                RenderConsoleShellScreen(modifier = Modifier.fillMaxSize())

                if (activeUiStyle != ConsoleUiStyle.ECHO_DEFAULT) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 46.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isDefaultHomeLauncher) {
                            DefaultHomePromptBanner(
                                onSetDefault = {
                                    playClickSound()
                                    onRequestDefaultHome()
                                }
                            )
                        }
                        if (toastNotificationText != null) {
                            OptimizationToastBanner(
                                message = toastNotificationText!!,
                                onClick = { isOptimizerOpen = true }
                            )
                        }
                    }
                }
            }
        }

        GameDetailsModal(
            isOpen = isDetailsOpen,
            app = detailsApp,
            stats = detailsApp?.let { gameStats[it.packageName] },
            hasUsageAccess = hasUsageAccess,
            selectedActionIndex = selectedDetailAction,
            onSelectActionIndex = { selectedDetailAction = it },
            onLaunch = {
                detailsTargetAppId = null
                detailsApp?.let { launchAppWithOptionalBoost(it, forceBoost = false) }
            },
            onBoostAndLaunch = {
                detailsTargetAppId = null
                detailsApp?.let { launchAppWithOptionalBoost(it, forceBoost = true) }
            },
            onToggleFavorite = {
                detailsApp?.let { app ->
                    playClickSound()
                    val nowFav = repository.toggleFavorite(app.packageName)
                    allApps = repository.getCachedApps() ?: allApps
                    showToast(if (nowFav) "★ Pinned ${app.label} to Front" else "Removed ${app.label} from Favorites")
                }
            },
            onOpenAppInfo = {
                detailsApp?.let { openAndroidAppInfo(context, it.packageName) }
            },
            onRequestUsageAccess = {
                gameStatsRepository.openUsageAccessSettings()
            },
            onDismiss = {
                isDetailsOpen = false
                detailsTargetAppId = null
                playClickSound()
            }
        )

        AllAppsModal(
            isOpen = isAllAppsOpen,
            filteredApps = filteredAllApps,
            selectedFilter = allAppsFilter,
            onFilterChange = { cat ->
                playClickSound()
                allAppsFilter = cat
                selectedModalIndex = 0
            },
            searchQuery = allAppsSearchQuery,
            onSearchQueryChange = { q ->
                allAppsSearchQuery = q
                selectedModalIndex = 0
            },
            onSearchFocusChanged = { isSearchFieldFocused = it },
            sortByRecent = sortByRecentlyPlayed,
            onToggleSort = ::toggleSortOrder,
            selectedIndex = selectedModalIndex.coerceIn(0, (filteredAllApps.size - 1).coerceAtLeast(0)),
            onIndexSelected = {
                selectedModalIndex = it
                playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
            },
            onLaunch = { app ->
                isAllAppsOpen = false
                isSearchFieldFocused = false
                launchAppWithOptionalBoost(app)
            },
            onOptions = { app -> openDetailsForApp(app) },
            onRefreshApps = ::refreshAppLibrary,
            onOptimizeClick = {
                isAllAppsOpen = false
                isSearchFieldFocused = false
                triggerDeviceOptimization(openModal = true)
            },
            onDismiss = {
                isAllAppsOpen = false
                isSearchFieldFocused = false
                currentFocusZone = FocusZone.DOCK
                playClickSound()
            }
        )

        OptimizerModal(
            isOpen = isOptimizerOpen,
            telemetry = telemetry,
            lastResult = lastOptimizationResult,
            isOptimizing = isOptimizing,
            autoBoostEnabled = autoBoostEnabled,
            onToggleAutoBoost = { enabled ->
                playClickSound()
                autoBoostEnabled = enabled
                deviceOptimizer.isAutoBoostEnabled = enabled
            },
            gamepadLayout = gamepadLayout,
            onToggleGamepadLayout = ::toggleGamepadLayout,
            sortByRecent = sortByRecentlyPlayed,
            onToggleSortOrder = ::toggleSortOrder,
            hasUsageAccess = hasUsageAccess,
            onRequestUsageAccess = {
                playClickSound()
                gameStatsRepository.openUsageAccessSettings()
            },
            onRunOptimization = {
                triggerDeviceOptimization(openModal = true)
            },
            onDismiss = {
                isOptimizerOpen = false
                playClickSound()
            }
        )

        UiHubModal(
            isOpen = isUiHubOpen,
            packs = uiPacks,
            downloadedPackIds = downloadedPackIds,
            activeStyle = activeUiStyle,
            downloadingPackId = downloadingPackId,
            downloadProgress = downloadProgress,
            selectedIndex = selectedUiHubIndex,
            selectedFilter = uiHubFilter,
            onFilterChange = { tab ->
                playClickSound()
                uiHubFilter = tab
                selectedUiHubIndex = 0
            },
            onSelectIndex = { idx ->
                selectedUiHubIndex = idx
                playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
            },
            onDownloadOrApply = ::handleDownloadOrApplyUiPack,
            onInstantPreview = ::handleInstantPreviewUiPack,
            onDownloadAllPacks = ::handleDownloadAllUiPacks,
            onUninstallPack = ::handleUninstallUiPack,
            onDismiss = {
                isUiHubOpen = false
                playClickSound()
            }
        )

        AnimatedVisibility(
            visible = isBooting,
            enter = fadeIn(),
            exit = fadeOut(tween(260))
        ) {
            BootSequenceScreen(
                onBootFinished = {
                    isBooting = false
                    hasShownBootThisProcess = true
                    playClickSound()
                }
            )
        }
    }
}

@Composable
private fun OptimizationToastBanner(
    message: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(StatusGreen.copy(alpha = 0.16f))
            .border(1.dp, StatusGreen.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            color = StatusGreen,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DefaultHomePromptBanner(
    onSetDefault: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCardElevated)
            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onSetDefault)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "⚡ CONSOLE BOOT MODE: Tap here to set ECHO OS as Default Home so turning on the console boots straight into ECHO OS",
            color = PureWhite,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(ElectricCyan)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "ENABLE",
                color = Color.Black,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun checkIsDefaultLauncher(context: Context): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        resolveInfo?.activityInfo?.packageName == context.packageName
    } catch (_: Exception) {
        false
    }
}
