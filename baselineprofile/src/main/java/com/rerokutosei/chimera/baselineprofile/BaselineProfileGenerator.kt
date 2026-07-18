package com.rerokutosei.chimera.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateStartupProfile() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        outputFilePrefix = "startup",
        includeInStartupProfile = true
    ) {
        pressHome()
        startChimeraAndWait()
        dismissWelcomeDialogIfShown()
    }

    @Test
    fun generateAppFlowProfile() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        outputFilePrefix = "app-flow"
    ) {
        pressHome()
        startChimeraAndWait()
        dismissWelcomeDialogIfShown()
        openSettingsScreen()
        scrollSettingsScreen()
        device.pressBack()
    }

}
