package com.rerokutosei.chimera.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(PACKAGE_NAME) {
        pressHome()
        startActivityAndWait()

        dismissWelcomeDialogIfShown()
        openSettingsScreen()
        scrollSettingsScreen()
        device.pressBack()
    }

    private fun MacrobenchmarkScope.dismissWelcomeDialogIfShown() {
        val agreeSelectors = AGREE_TEXT_PREFIXES.map(By::textStartsWith)
        val agreeButton = findAnyObject(
            agreeSelectors,
            timeoutMs = 2_000
        ) ?: return

        repeat(8) {
            val refreshedButton = findAnyObject(agreeSelectors, timeoutMs = 500)
            if (refreshedButton?.isEnabled == true) {
                refreshedButton.click()
                device.waitForIdle()
                return
            }
            if (agreeButton.isEnabled) {
                agreeButton.click()
                device.waitForIdle()
                return
            }
            Thread.sleep(1_000)
        }

        val enabledAgreeButton = findAnyObject(
            agreeSelectors,
            timeoutMs = 1_000
        )
        check(enabledAgreeButton?.isEnabled == true) {
            "Welcome dialog agree button did not become enabled in time"
        }
        enabledAgreeButton.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.openSettingsScreen() {
        val settingsSelectors = SETTINGS_DESCRIPTIONS.map(By::desc)
        val settingsButton = findAnyObject(
            settingsSelectors,
            timeoutMs = 10_000
        )
        checkNotNull(settingsButton) { "Settings button not found" }
        settingsButton.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.scrollSettingsScreen() {
        repeat(2) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.8f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.2f).toInt(),
                24
            )
            device.waitForIdle()
        }
    }

    private fun MacrobenchmarkScope.findAnyObject(
        selectors: List<BySelector>,
        timeoutMs: Long
    ): UiObject2? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            selectors.forEach { selector ->
                val match = device.findObject(selector)
                if (match != null) {
                    return match
                }
            }
            Thread.sleep(250)
        }
        return null
    }

    companion object {
        private const val PACKAGE_NAME = "com.rerokutosei.chimera"

        private val AGREE_TEXT_PREFIXES = listOf(
            "Agree",
            "同意",
            "同意する",
            "Aceptar"
        )

        private val SETTINGS_DESCRIPTIONS = listOf(
            "Settings",
            "设置",
            "設定",
            "Configuración"
        )
    }
}
