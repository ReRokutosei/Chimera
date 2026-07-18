package com.rerokutosei.chimera.baselineprofile

import android.os.Trace
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@OptIn(ExperimentalMetricApi::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Before
    fun prepareApp() {
        prepareAppForBenchmark()
    }

    @Test
    fun coldStartupWithoutCompilation() = benchmarkStartup(CompilationMode.None())

    @Test
    fun coldStartupWithBaselineProfile() = benchmarkStartup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    @Test
    fun settingsFlowWithoutCompilation() = benchmarkSettingsFlow(CompilationMode.None())

    @Test
    fun settingsFlowWithBaselineProfile() = benchmarkSettingsFlow(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    private fun benchmarkStartup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 8,
            setupBlock = { pressHome() }
        ) {
            startChimeraAndWait()
        }
    }

    private fun benchmarkSettingsFlow(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(
                TraceSectionMetric(
                    sectionName = "ChimeraSettingsFlow",
                    targetPackageOnly = false
                )
            ),
            compilationMode = compilationMode,
            iterations = 8,
            setupBlock = {
                pressHome()
                startChimeraAndWait()
            }
        ) {
            Trace.beginSection("ChimeraSettingsFlow")
            try {
                openSettingsScreen()
                scrollSettingsScreen()
                device.pressBack()
                device.waitForIdle()
            } finally {
                Trace.endSection()
            }
        }
    }
}
