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
import com.echo.launcher.data.GamepadLayout
import com.echo.launcher.data.HandheldControllerManager
import com.echo.launcher.data.LauncherTab
import com.echo.launcher.data.OptimizationResult
import com.echo.launcher.data.PowerInfo
import com.echo.launcher.data.UiThemePack
import com.echo.launcher.data.UiThemeRepository
import com.echo.launcher.ui.components.AllAppsModal
import com.echo.launcher.ui.components.AmbientBackground
import com.echo.launcher.ui.components.BootSequenceScreen
import com.echo.launcher.ui.components.ConsoleFooter
import com.echo.launcher.ui.components.DualScreenBottomDeck
import com.echo.launcher.ui.components.GameCarousel
import com.echo.launcher.ui.components.GameDetailsModal
import com.echo.launcher.ui.components.HybridPortableDeckLayout
import com.echo.launcher.ui.components.NextGenHorizonLayout
import com.echo.launcher.ui.components.OptimizerModal
import com.echo.launcher.ui.components.StereoscopicDual3DLayout
import com.echo.launcher.ui.components.TopBar
import com.echo.launcher.ui.components.UiHubModal
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
    var selectedTab by remember { mutableStateOf(LauncherTab.GAMES) }
    var currentFocusZone by remember { mutableStateOf(FocusZone.CAROUSEL) }
    var gamepadLayout by remember { mutableStateOf(GamepadLayout.ABXY) }

    var selectedGameIndex by remember { mutableIntStateOf(0) }
    var selectedMediaIndex by remember { mutableIntStateOf(0) }
    var selectedDockIndex by remember { mutableIntStateOf(0) }
    var selectedModalIndex by remember { mutableIntStateOf(0) }

    var isAllAppsOpen by remember { mutableStateOf(false) }
    var isDetailsOpen by remember { mutableStateOf(false) }
    var isOptimizerOpen by remember { mutableStateOf(false) }
    var isUiHubOpen by remember { mutableStateOf(false) }

    val uiPacks = remember { uiThemeRepository.availablePacks }
    var downloadedPackIds by remember { mutableStateOf(uiThemeRepository.getDownloadedIds()) }
    var activeUiStyle by remember { mutableStateOf(uiThemeRepository.getActiveUiStyle()) }
    var selectedUiHubIndex by remember { mutableIntStateOf(0) }
    var downloadingPackId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    var telemetry by remember { mutableStateOf(deviceOptimizer.getTelemetry()) }
    var isOptimizing by remember { mutableStateOf(false) }
    var lastOptimizationResult by remember { mutableStateOf<OptimizationResult?>(null) }
    var autoBoostEnabled by remember { mutableStateOf(deviceOptimizer.isAutoBoostEnabled) }
    var toastNotificationText by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    fun playClickSound() {
        try { view.playSoundEffect(SoundEffectConstants.CLICK) } catch (_: Exception) {}
    }

    fun playNavSound(direction: Int) {
        try { view.playSoundEffect(direction) } catch (_: Exception) {}
    }

    fun handleDownloadOrApplyUiPack(pack: UiThemePack) {
        playClickSound()
        if (downloadingPackId != null) return

        val isAlreadyDownloaded = downloadedPackIds.contains(pack.id)
        if (isAlreadyDownloaded) {
            activeUiStyle = pack.style
            uiThemeRepository.setActiveUiStyle(pack.style)
            toastNotificationText = "❖ Activated ${pack.title}"
            isUiHubOpen = false
            coroutineScope.launch {
                delay(2800)
                if (toastNotificationText == "❖ Activated ${pack.title}") {
                    toastNotificationText = null
                }
            }
        } else {
            coroutineScope.launch {
                downloadingPackId = pack.id
                downloadProgress = 0.08f
                val steps = 8
                for (step in 1..steps) {
                    delay(75)
                    downloadProgress = (step.toFloat() / steps.toFloat()).coerceIn(0.1f, 1.0f)
                }
                downloadedPackIds = uiThemeRepository.markPackDownloaded(pack.id)
                activeUiStyle = pack.style
                uiThemeRepository.setActiveUiStyle(pack.style)
                downloadingPackId = null
                downloadProgress = 0f
                toastNotificationText = "⬇ Installed & Applied ${pack.title}"
                delay(2800)
                if (toastNotificationText == "⬇ Installed & Applied ${pack.title}") {
                    toastNotificationText = null
                }
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
        toastNotificationText = "Removed ${pack.title} Pack"
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
            toastNotificationText = "⚡ ${result.statusMessage}"
            onDone?.invoke()
            delay(3600)
            if (toastNotificationText?.contains(result.statusMessage) == true) {
                toastNotificationText = null
            }
        }
    }

    fun launchAppWithOptionalBoost(app: AppItem, forceBoost: Boolean = false) {
        playClickSound()
        if (forceBoost || autoBoostEnabled) {
            coroutineScope.launch {
                val res = deviceOptimizer.optimizeDevice(allApps)
                telemetry = res.telemetryAfter
                lastOptimizationResult = res
                toastNotificationText = "⚡ Boosted ${res.freedRamMb} MB RAM for ${app.label}"
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
            toastNotificationText = "↻ Library Rescanned: ${refreshed.size} Apps & Emulators Ready"
            delay(2800)
            toastNotificationText = null
        }
    }

    LaunchedEffect(Unit) {
        val loaded = repository.loadInstalledApps()
        allApps = loaded
        isDefaultHomeLauncher = checkIsDefaultLauncher(context)
        telemetry = deviceOptimizer.getTelemetry()
    }

    LaunchedEffect(resumeTick) {
        isDefaultHomeLauncher = checkIsDefaultLauncher(context)
        telemetry = deviceOptimizer.getTelemetry()
    }

    val currentTabApps = remember(allApps, selectedTab) {
        when (selectedTab) {
            LauncherTab.GAMES -> {
                val games = allApps.filter { it.isGame }
                if (games.isNotEmpty()) games else allApps
            }
            LauncherTab.MEDIA -> {
                val media = allApps.filter { it.isMedia }
                if (media.isNotEmpty()) media else allApps
            }
        }
    }

    val activeCarouselIndex = when (selectedTab) {
        LauncherTab.GAMES -> selectedGameIndex.coerceIn(0, (currentTabApps.size - 1).coerceAtLeast(0))
        LauncherTab.MEDIA -> selectedMediaIndex.coerceIn(0, (currentTabApps.size - 1).coerceAtLeast(0))
    }

    val activeApp = currentTabApps.getOrNull(activeCarouselIndex)
    val activeAccentColor = activeApp?.accentColor ?: ElectricCyan

    // Hardware dual display support (Anbernic RG DS when secondary screen is exposed as Display 2)
    LaunchedEffect(
        allApps,
        activeCarouselIndex,
        isAllAppsOpen,
        dualScreenManager.hasPhysicalSecondaryDisplay,
        isDualScreenMode
    ) {
        if (dualScreenManager.hasPhysicalSecondaryDisplay && isDualScreenMode && allApps.isNotEmpty()) {
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

    fun toggleDualScreen() {
        val next = dualScreenManager.toggleDualScreenMode()
        isDualScreenMode = next
        playClickSound()
    }

    fun triggerDockAction(action: DockAction) {
        playClickSound()
        when (action) {
            DockAction.OPTIMIZE -> triggerDeviceOptimization(openModal = true)
            DockAction.UI_HUB -> isUiHubOpen = true
            DockAction.ALL_APPS -> {
                isAllAppsOpen = true
                currentFocusZone = FocusZone.ALL_APPS_MODAL
            }
            DockAction.ECHO_UPDATES -> repository.launchEchoUpdates()
            DockAction.WIFI_BT -> repository.launchWifiBtSettings()
            DockAction.SETTINGS -> repository.launchSettings()
        }
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
            playClickSound()
        } else if (isAllAppsOpen) {
            isAllAppsOpen = false
            currentFocusZone = FocusZone.DOCK
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

        if (isUiHubOpen) {
            when (nativeKeyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    selectedUiHubIndex = (selectedUiHubIndex - 1).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    selectedUiHubIndex = (selectedUiHubIndex + 1).coerceAtMost(uiPacks.size - 1)
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                else -> {
                    if (controllerManager.isCancelKey(nativeKeyCode)) {
                        isUiHubOpen = false
                        playClickSound()
                        return true
                    }
                    if (controllerManager.isConfirmKey(nativeKeyCode)) {
                        uiPacks.getOrNull(selectedUiHubIndex)?.let { handleDownloadOrApplyUiPack(it) }
                        return true
                    }
                }
            }
            return false
        }

        if (isOptimizerOpen) {
            if (controllerManager.isCancelKey(nativeKeyCode)) {
                isOptimizerOpen = false
                playClickSound()
                return true
            }
            if (controllerManager.isConfirmKey(nativeKeyCode)) {
                triggerDeviceOptimization(openModal = true)
                return true
            }
            return false
        }

        if (isDetailsOpen) {
            if (controllerManager.isCancelKey(nativeKeyCode)) {
                isDetailsOpen = false
                playClickSound()
                return true
            }
            if (controllerManager.isConfirmKey(nativeKeyCode)) {
                activeApp?.let { launchAppWithOptionalBoost(it) }
                isDetailsOpen = false
                return true
            }
            return false
        }

        if (controllerManager.isDrawerKey(nativeKeyCode)) {
            isAllAppsOpen = !isAllAppsOpen
            if (isAllAppsOpen) currentFocusZone = FocusZone.ALL_APPS_MODAL
            playClickSound()
            return true
        }

        if (controllerManager.isOptionsKey(nativeKeyCode)) {
            if (activeApp != null && !isAllAppsOpen) {
                isDetailsOpen = true
                playClickSound()
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

        if (isAllAppsOpen) {
            when (nativeKeyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    selectedModalIndex = (selectedModalIndex - 1).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    selectedModalIndex = (selectedModalIndex + 1).coerceAtMost(allApps.size - 1)
                    playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    selectedModalIndex = (selectedModalIndex - 4).coerceAtLeast(0)
                    playNavSound(SoundEffectConstants.NAVIGATION_UP)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    selectedModalIndex = (selectedModalIndex + 4).coerceAtMost(allApps.size - 1)
                    playNavSound(SoundEffectConstants.NAVIGATION_DOWN)
                    return true
                }
                else -> {
                    if (controllerManager.isConfirmKey(nativeKeyCode)) {
                        allApps.getOrNull(selectedModalIndex)?.let {
                            isAllAppsOpen = false
                            launchAppWithOptionalBoost(it)
                        }
                        return true
                    }
                    if (controllerManager.isCancelKey(nativeKeyCode)) {
                        isAllAppsOpen = false
                        currentFocusZone = FocusZone.DOCK
                        playClickSound()
                        return true
                    }
                }
            }
            return false
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
                        if (selectedTab == LauncherTab.GAMES) {
                            selectedGameIndex = (selectedGameIndex - 1).coerceAtLeast(0)
                        } else {
                            selectedMediaIndex = (selectedMediaIndex - 1).coerceAtLeast(0)
                        }
                        playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (selectedTab == LauncherTab.GAMES) {
                            selectedGameIndex = (selectedGameIndex + 1).coerceAtMost(currentTabApps.size - 1)
                        } else {
                            selectedMediaIndex = (selectedMediaIndex + 1).coerceAtMost(currentTabApps.size - 1)
                        }
                        playNavSound(SoundEffectConstants.NAVIGATION_RIGHT)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        currentFocusZone = FocusZone.TOP_TABS
                        playNavSound(SoundEffectConstants.NAVIGATION_UP)
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        currentFocusZone = FocusZone.DOCK
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
                            val actions = DockAction.entries
                            actions.getOrNull(selectedDockIndex)?.let { triggerDockAction(it) }
                            return true
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
    fun RenderActiveConsoleBody(modifier: Modifier = Modifier) {
        when (activeUiStyle) {
            ConsoleUiStyle.NEXTGEN_MEDIA_BAR -> {
                NextGenHorizonLayout(
                    apps = currentTabApps,
                    selectedIndex = activeCarouselIndex,
                    onIndexSelected = { index ->
                        if (selectedTab == LauncherTab.GAMES) selectedGameIndex = index else selectedMediaIndex = index
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    onLaunch = { launchAppWithOptionalBoost(it) },
                    onOptions = {
                        playClickSound()
                        isDetailsOpen = true
                    },
                    onOpenUiHub = {
                        playClickSound()
                        isUiHubOpen = true
                    },
                    onOptimize = { triggerDeviceOptimization(openModal = true) },
                    onAllApps = {
                        playClickSound()
                        isAllAppsOpen = true
                        currentFocusZone = FocusZone.ALL_APPS_MODAL
                    },
                    modifier = modifier
                )
            }
            ConsoleUiStyle.HYBRID_DECK -> {
                HybridPortableDeckLayout(
                    apps = currentTabApps,
                    selectedIndex = activeCarouselIndex,
                    onIndexSelected = { index ->
                        if (selectedTab == LauncherTab.GAMES) selectedGameIndex = index else selectedMediaIndex = index
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    onLaunch = { launchAppWithOptionalBoost(it) },
                    onOptions = {
                        playClickSound()
                        isDetailsOpen = true
                    },
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
                    onIndexSelected = { index ->
                        if (selectedTab == LauncherTab.GAMES) selectedGameIndex = index else selectedMediaIndex = index
                        currentFocusZone = FocusZone.CAROUSEL
                        playClickSound()
                    },
                    onLaunch = { launchAppWithOptionalBoost(it) },
                    onOptions = {
                        playClickSound()
                        isDetailsOpen = true
                    },
                    onOpenUiHub = {
                        playClickSound()
                        isUiHubOpen = true
                    },
                    onOptimize = { triggerDeviceOptimization(openModal = true) },
                    modifier = modifier
                )
            }
            ConsoleUiStyle.ECHO_DEFAULT -> {
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    GameCarousel(
                        apps = currentTabApps,
                        selectedIndex = activeCarouselIndex,
                        onIndexSelected = { index ->
                            if (selectedTab == LauncherTab.GAMES) {
                                selectedGameIndex = index
                            } else {
                                selectedMediaIndex = index
                            }
                            currentFocusZone = FocusZone.CAROUSEL
                            playClickSound()
                        },
                        onLaunch = { app ->
                            launchAppWithOptionalBoost(app)
                        },
                        onOptions = {
                            playClickSound()
                            isDetailsOpen = true
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
                            currentFocusZone = currentFocusZone
                        )
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
            // ANBERNIC RG DS / DUAL-SCREEN SPANNED CLAMSHELL MODE (Covers Both Screens)
            Column(modifier = Modifier.fillMaxSize()) {
                // UPPER SCREEN: Active Console Shell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
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

                        RenderActiveConsoleBody(modifier = Modifier.weight(1f))
                    }
                }

                // Physical Clamshell Hinge Seam Guard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Black)
                        .border(1.dp, SurfaceBorder)
                )

                // LOWER SCREEN: Touch Command Deck & Quick Launch Matrix
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    DualScreenBottomDeck(
                        apps = currentTabApps,
                        selectedIndex = activeCarouselIndex,
                        onSelectIndex = { idx ->
                            if (selectedTab == LauncherTab.GAMES) {
                                selectedGameIndex = idx
                            } else {
                                selectedMediaIndex = idx
                            }
                            playClickSound()
                        },
                        onLaunch = { app ->
                            launchAppWithOptionalBoost(app)
                        },
                        onToggleAllApps = {
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
                            isUiHubOpen = true
                        },
                        ramUsagePercent = telemetry.ramUsagePercent,
                        onToggleDualScreenMode = ::toggleDualScreen
                    )
                }
            }
        } else {
            // SINGLE SCREEN OR PHYSICAL DISPLAY-2 PRESENTATION MODE
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

                RenderActiveConsoleBody(modifier = Modifier.weight(1f))
            }
        }

        GameDetailsModal(
            isOpen = isDetailsOpen,
            app = activeApp,
            onLaunch = {
                activeApp?.let { launchAppWithOptionalBoost(it, forceBoost = false) }
            },
            onBoostAndLaunch = {
                activeApp?.let { launchAppWithOptionalBoost(it, forceBoost = true) }
            },
            onToggleFavorite = {
                activeApp?.let { app ->
                    playClickSound()
                    val nowFav = repository.toggleFavorite(app.packageName)
                    allApps = repository.getCachedApps() ?: allApps
                    toastNotificationText = if (nowFav) "★ Pinned ${app.label} to Front" else "Removed ${app.label} from Favorites"
                }
            },
            onDismiss = {
                isDetailsOpen = false
                playClickSound()
            }
        )

        AllAppsModal(
            isOpen = isAllAppsOpen,
            apps = allApps,
            selectedIndex = selectedModalIndex,
            onIndexSelected = {
                selectedModalIndex = it
                playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
            },
            onLaunch = { app ->
                isAllAppsOpen = false
                launchAppWithOptionalBoost(app)
            },
            onRefreshApps = ::refreshAppLibrary,
            onOptimizeClick = {
                isAllAppsOpen = false
                triggerDeviceOptimization(openModal = true)
            },
            onDismiss = {
                isAllAppsOpen = false
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
            onSelectIndex = { idx ->
                selectedUiHubIndex = idx
                playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
            },
            onDownloadOrApply = ::handleDownloadOrApplyUiPack,
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
