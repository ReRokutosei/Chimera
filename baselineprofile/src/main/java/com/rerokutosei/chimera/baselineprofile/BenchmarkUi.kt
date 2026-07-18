package com.rerokutosei.chimera.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

internal const val PACKAGE_NAME = "com.rerokutosei.chimera"

internal fun MacrobenchmarkScope.dismissWelcomeDialogIfShown() {
    device.dismissWelcomeDialogIfShown()
}

internal fun MacrobenchmarkScope.startChimeraAndWait() {
    device.startChimeraAndWait()
}

internal fun MacrobenchmarkScope.openSettingsScreen() {
    val settingsButton = device.findAnyObject(
        SETTINGS_DESCRIPTIONS.map(By::desc),
        timeoutMs = 3_000
    )
    if (settingsButton != null) {
        settingsButton.click()
    } else {
        device.click(
            (device.displayWidth * 0.9f).toInt(),
            (device.displayHeight * 0.085f).toInt()
        )
    }
    device.waitForIdle()
}

internal fun MacrobenchmarkScope.scrollSettingsScreen() {
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

internal fun prepareAppForBenchmark() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.wakeUp()
    device.executeShellCommand("wm dismiss-keyguard")
    device.executeShellCommand("svc power stayon true")
    device.pressHome()
    device.executeShellCommand("am force-stop $PACKAGE_NAME")
    repeat(3) {
        device.startChimeraAndWait()
        val acceptedWelcomeDialog = device.dismissWelcomeDialogIfShown()
        if (acceptedWelcomeDialog) {
            Thread.sleep(2_000)
        }
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        device.pressHome()
        if (!acceptedWelcomeDialog) {
            return
        }
    }
    error("Welcome dialog acceptance was not persisted after three attempts")
}

private fun UiDevice.startChimeraAndWait() {
    val output = executeShellCommand(
        "am start -W --windowingMode 1 -n $PACKAGE_NAME/.MainActivity"
    )
    check("Error:" !in output) { "Unable to launch Chimera: $output" }
    check(wait(Until.hasObject(By.pkg(PACKAGE_NAME)), 10_000)) {
        "Chimera UI did not become visible"
    }
    waitForIdle()
}

private fun UiDevice.dismissWelcomeDialogIfShown(): Boolean {
    if (findAnyObject(SETTINGS_DESCRIPTIONS.map(By::desc), timeoutMs = 2_000) != null) {
        return false
    }
    val countdownButton = findAnyObject(
        AGREE_TEXTS.map(By::textStartsWith),
        timeoutMs = 2_000
    ) ?: return false
    val enabledAgreeButton = findAnyObject(
        AGREE_TEXTS.map(By::text),
        timeoutMs = 7_000
    )
    checkNotNull(enabledAgreeButton) {
        "Welcome dialog agree button did not become enabled in time: ${countdownButton.text}"
    }
    enabledAgreeButton.click()
    check(findAnyObject(SETTINGS_DESCRIPTIONS.map(By::desc), timeoutMs = 5_000) != null) {
        "Main screen did not become visible after accepting the welcome dialog"
    }
    waitForIdle()
    return true
}

private fun UiDevice.findAnyObject(
    selectors: List<BySelector>,
    timeoutMs: Long
): UiObject2? {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        selectors.forEach { selector ->
            findObject(selector)?.let { return it }
        }
        Thread.sleep(250)
    }
    return null
}

private val AGREE_TEXTS = listOf(
    "Agree",
    "同意"
)

private val SETTINGS_DESCRIPTIONS = listOf(
    "Settings",
    "设置"
)
