package com.echo.launcher.data

import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

enum class GamepadLayout {
    ABXY,      // Standard Android / South Confirm (A = Confirm, B = Cancel)
    BAYX       // Retro / East Confirm (B = Confirm, A = Cancel)
}

/**
 * Robust controller & analog stick interpreter for handheld and gamepad devices.
 * Translates analog sticks, D-pad HAT axes, and button layout preferences into clean navigation actions.
 */
class HandheldControllerManager {

    var layout: GamepadLayout = GamepadLayout.ABXY

    private var lastStickMoveTime = 0L
    private val stickDeadzone = 0.50f
    private val stickRepeatInterval = 180L

    fun processMotionEvent(event: MotionEvent, onDirectionAction: (directionKeyCode: Int) -> Boolean): Boolean {
        if ((event.source and InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK &&
            (event.source and InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD
        ) {
            return false
        }

        val now = SystemClock.uptimeMillis()
        if (now - lastStickMoveTime < stickRepeatInterval) {
            return false
        }

        val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

        if (hatX < -stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_LEFT)
        } else if (hatX > stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_RIGHT)
        }

        if (hatY < -stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_UP)
        } else if (hatY > stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_DOWN)
        }

        val stickX = event.getAxisValue(MotionEvent.AXIS_X)
        val stickY = event.getAxisValue(MotionEvent.AXIS_Y)

        if (stickX < -stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_LEFT)
        } else if (stickX > stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_RIGHT)
        }

        if (stickY < -stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_UP)
        } else if (stickY > stickDeadzone) {
            lastStickMoveTime = now
            return onDirectionAction(KeyEvent.KEYCODE_DPAD_DOWN)
        }

        return false
    }

    fun isConfirmKey(keyCode: Int): Boolean {
        return when (layout) {
            GamepadLayout.ABXY -> keyCode == KeyEvent.KEYCODE_BUTTON_A ||
                    keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
            GamepadLayout.BAYX -> keyCode == KeyEvent.KEYCODE_BUTTON_B ||
                    keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    keyCode == KeyEvent.KEYCODE_ENTER
        }
    }

    fun isCancelKey(keyCode: Int): Boolean {
        return when (layout) {
            GamepadLayout.ABXY -> keyCode == KeyEvent.KEYCODE_BUTTON_B ||
                    keyCode == KeyEvent.KEYCODE_BACK ||
                    keyCode == KeyEvent.KEYCODE_ESCAPE
            GamepadLayout.BAYX -> keyCode == KeyEvent.KEYCODE_BUTTON_A ||
                    keyCode == KeyEvent.KEYCODE_BACK ||
                    keyCode == KeyEvent.KEYCODE_ESCAPE
        }
    }

    fun isOptionsKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_BUTTON_Y ||
                keyCode == KeyEvent.KEYCODE_Y ||
                keyCode == KeyEvent.KEYCODE_MENU ||
                keyCode == KeyEvent.KEYCODE_BUTTON_START
    }

    fun isDrawerKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_BUTTON_X ||
                keyCode == KeyEvent.KEYCODE_X ||
                keyCode == KeyEvent.KEYCODE_BUTTON_SELECT
    }

    fun isTabLeft(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_BUTTON_L1 ||
                keyCode == KeyEvent.KEYCODE_BUTTON_L2 ||
                keyCode == KeyEvent.KEYCODE_Q ||
                keyCode == KeyEvent.KEYCODE_PAGE_UP
    }

    fun isTabRight(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_BUTTON_R1 ||
                keyCode == KeyEvent.KEYCODE_BUTTON_R2 ||
                keyCode == KeyEvent.KEYCODE_E ||
                keyCode == KeyEvent.KEYCODE_PAGE_DOWN
    }
}
