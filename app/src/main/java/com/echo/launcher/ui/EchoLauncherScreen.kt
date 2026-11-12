package com.echo.launcher.ui

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.echo.launcher.data.AppItem
import com.echo.launcher.data.AppRepository
import com.echo.launcher.data.BatteryInfo
import com.echo.launcher.data.DockAction
import com.echo.launcher.data.FocusZone
import com.echo.launcher.data.GamepadLayout
import com.echo.launcher.data.HandheldControllerManager
import com.echo.launcher.data.LauncherTab
import com.echo.launcher.ui.components.AllAppsModal
import com.echo.launcher.ui.components.AmbientBackground
import com.echo.launcher.ui.components.BootSequenceScreen
import com.echo.launcher.ui.components.ConsoleFooter
import com.echo.launcher.ui.components.GameCarousel
import com.echo.launcher.ui.components.GameDetailsModal
import com.echo.launcher.ui.components.TopBar
import com.echo.launcher.ui.theme.ElectricCyan

@Composable
fun EchoLauncherScreen(
    batteryInfo: BatteryInfo,
    onHandleKeyEvent: ((KeyEvent) -> Boolean) -> Unit = {},
    onHandleMotionEvent: ((MotionEvent) -> Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val repository = remember { AppRepository(context) }
    val controllerManager = remember { HandheldControllerManager() }

    var isBooting by remember { mutableStateOf(true) }
    var allApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(LauncherTab.GAMES) }
    var currentFocusZone by remember { mutableStateOf(FocusZone.CAROUSEL) }
    var gamepadLayout by remember { mutableStateOf(GamepadLayout.XBOX) }

    var selectedGameIndex by remember { mutableIntStateOf(0) }
    var selectedMediaIndex by remember { mutableIntStateOf(0) }
    var selectedDockIndex by remember { mutableIntStateOf(0) }
    var selectedModalIndex by remember { mutableIntStateOf(0) }

    var isAllAppsOpen by remember { mutableStateOf(false) }
    var isDetailsOpen by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val loaded = repository.loadInstalledApps()
        allApps = loaded
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

    fun playClickSound() {
        try { view.playSoundEffect(SoundEffectConstants.CLICK) } catch (_: Exception) {}
    }

    fun playNavSound(direction: Int) {
        try { view.playSoundEffect(direction) } catch (_: Exception) {}
    }

    BackHandler(enabled = true) {
        if (isBooting) {
            isBooting = false
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

        // Any button skips the boot screen
        if (isBooting) {
            isBooting = false
            playClickSound()
            return true
        }

        // Details Modal Key Handling
        if (isDetailsOpen) {
            if (controllerManager.isCancelKey(nativeKeyCode)) {
                isDetailsOpen = false
                playClickSound()
                return true
            }
            if (controllerManager.isConfirmKey(nativeKeyCode)) {
                activeApp?.let { repository.launchApp(it) }
                isDetailsOpen = false
                playClickSound()
                return true
            }
            return false
        }

        // Quick All Apps Shortcut via (X) Button / SELECT
        if (controllerManager.isDrawerKey(nativeKeyCode)) {
            isAllAppsOpen = !isAllAppsOpen
            if (isAllAppsOpen) currentFocusZone = FocusZone.ALL_APPS_MODAL
            playClickSound()
            return true
        }

        // Options shortcut via (Y) Button / START / MENU
        if (controllerManager.isOptionsKey(nativeKeyCode)) {
            if (activeApp != null && !isAllAppsOpen) {
                isDetailsOpen = true
                playClickSound()
                return true
            }
        }

        // Bumper L1 / R1 tab switcher
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

        // Modal navigation
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
                            repository.launchApp(it)
                            isAllAppsOpen = false
                            playClickSound()
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

        // Main Console navigation
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
                            activeApp?.let {
                                repository.launchApp(it)
                                playClickSound()
                            }
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
                        selectedDockIndex = (selectedDockIndex + 1).coerceAtMost(3)
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
                            playClickSound()
                            when (selectedDockIndex) {
                                0 -> {
                                    isAllAppsOpen = true
                                    currentFocusZone = FocusZone.ALL_APPS_MODAL
                                }
                                1 -> repository.launchEchoUpdates()
                                2 -> repository.launchWifiBtSettings()
                                3 -> repository.launchSettings()
                            }
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

    // Connect Anbernic analog stick and HAT axis events
    onHandleMotionEvent { motionEvent ->
        controllerManager.processMotionEvent(motionEvent) { directionCode ->
            handleKeyNavigation(directionCode, isActionDown = true)
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
        // PS5/Handheld dynamic radial ambient glow background with cybernetic grid
        AmbientBackground(targetAccentColor = activeAccentColor)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            TopBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    currentFocusZone = FocusZone.CAROUSEL
                    playClickSound()
                },
                currentFocusZone = currentFocusZone,
                batteryInfo = batteryInfo
            )

            Spacer(modifier = Modifier.weight(1f))

            // Center Horizontal Game & Retro Emulator Carousel
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
                    playClickSound()
                    repository.launchApp(app)
                },
                onOptions = {
                    playClickSound()
                    isDetailsOpen = true
                },
                currentFocusZone = currentFocusZone,
                gamepadLayout = gamepadLayout
            )

            Spacer(modifier = Modifier.weight(1f))

            // Console Footer: System Dock + Handheld Button Guide
            ConsoleFooter(
                selectedActionIndex = selectedDockIndex,
                onActionSelected = { index ->
                    selectedDockIndex = index
                    currentFocusZone = FocusZone.DOCK
                    playClickSound()
                },
                onActionTriggered = { action ->
                    playClickSound()
                    when (action) {
                        DockAction.ALL_APPS -> {
                            isAllAppsOpen = true
                            currentFocusZone = FocusZone.ALL_APPS_MODAL
                        }
                        DockAction.ECHO_UPDATES -> repository.launchEchoUpdates()
                        DockAction.WIFI_BT -> repository.launchWifiBtSettings()
                        DockAction.SETTINGS -> repository.launchSettings()
                    }
                },
                currentFocusZone = currentFocusZone
            )
        }

        // Game Details Options Modal
        GameDetailsModal(
            isOpen = isDetailsOpen,
            app = activeApp,
            onLaunch = {
                activeApp?.let { repository.launchApp(it) }
            },
            onDismiss = {
                isDetailsOpen = false
                playClickSound()
            }
        )

        // All Apps Modal Overlay
        AllAppsModal(
            isOpen = isAllAppsOpen,
            apps = allApps,
            selectedIndex = selectedModalIndex,
            onIndexSelected = {
                selectedModalIndex = it
                playNavSound(SoundEffectConstants.NAVIGATION_LEFT)
            },
            onLaunch = { app ->
                playClickSound()
                repository.launchApp(app)
                isAllAppsOpen = false
            },
            onDismiss = {
                isAllAppsOpen = false
                currentFocusZone = FocusZone.DOCK
                playClickSound()
            }
        )

        // Custom Console Boot Sequence
        AnimatedVisibility(
            visible = isBooting,
            enter = fadeIn(),
            exit = fadeOut(tween(400))
        ) {
            BootSequenceScreen(
                onBootFinished = {
                    isBooting = false
                    playClickSound()
                }
            )
        }
    }
}
