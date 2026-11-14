package com.echo.launcher

import android.app.Activity
import android.os.Bundle

/**
 * Disabled dummy activity used by the Android launcher-reset trick.
 * Briefly enabling and disabling this component invalidates Android's cached default Home app
 * and forces the native "Select Home App (ECHO OS / Always)" system dialog to pop up.
 */
class FakeHomeTriggerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
