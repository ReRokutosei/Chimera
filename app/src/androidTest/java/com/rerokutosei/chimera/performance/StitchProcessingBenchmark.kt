package com.rerokutosei.chimera.performance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.rerokutosei.chimera.utils.stitch.StitchResult
import com.rerokutosei.chimera.utils.stitch.layout.OutputImageFormat
import com.rerokutosei.chimera.utils.performance.ProcessingPerformance
import com.rerokutosei.chimera.utils.performance.StageTiming
import com.rerokutosei.chimera.utils.stitch.strategy.DirectStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.OverlayStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingOptions
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class StitchProcessingBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun directVertical_10Medium_none() = benchmarkDirect(
        scenario = FixedBitmapScenario.TEN_MEDIUM,
        orientation = StitchOrientation.VERTICAL,
        scale = WidthScale.NONE
    )

    @Test
    fun directHorizontal_10Medium_min() = benchmarkDirect(
        scenario = FixedBitmapScenario.TEN_MEDIUM,
        orientation = StitchOrientation.HORIZONTAL,
        scale = WidthScale.MIN_WIDTH
    )

    @Test
    fun directVertical_50Small_max() = benchmarkDirect(
        scenario = FixedBitmapScenario.FIFTY_SMALL,
        orientation = StitchOrientation.VERTICAL,
        scale = WidthScale.MAX_WIDTH
    )

    @Test
    fun overlayVertical_10Medium_min() {
        val source = FixedBitmapData.create(FixedBitmapScenario.TEN_MEDIUM)
        val strategy = OverlayStitchingStrategy(context)
        val options = StitchingOptions(
            isOverlayEnabled = true,
            overlayRatio = 20,
            widthScale = WidthScale.MIN_WIDTH,
            orientation = StitchOrientation.VERTICAL,
            outputFormat = OutputImageFormat.JPEG,
            highMemoryLimitEnabled = true,
            multiThreadEnabled = true
        )
        try {
            benchmarkRule.measureRepeated {
                val result = runBlocking { strategy.stitch(source, options) }
                check(result is StitchResult.BitmapResult)
                result.bitmap.recycle()
            }
        } finally {
            source.forEach(Bitmap::recycle)
        }
    }

    @Test
    fun encodeJpeg_mediumResult() = benchmarkEncode(OutputImageFormat.JPEG)

    @Test
    fun encodePng_mediumResult() = benchmarkEncode(OutputImageFormat.PNG)

    @Test
    fun encodeWebp_mediumResult() = benchmarkEncode(OutputImageFormat.WEBP)

    @Test
    fun stageBreakdown_10Medium() {
        val source = FixedBitmapData.create(FixedBitmapScenario.TEN_MEDIUM)
        try {
            reportStageBreakdown("direct-min") {
                DirectStitchingStrategy(true, context).stitch(
                    source,
                    StitchingOptions(
                        widthScale = WidthScale.MIN_WIDTH,
                        orientation = StitchOrientation.VERTICAL,
                        outputFormat = OutputImageFormat.JPEG,
                        highMemoryLimitEnabled = true
                    )
                )
            }
            reportStageBreakdown("overlay-min") {
                OverlayStitchingStrategy(context).stitch(
                    source,
                    StitchingOptions(
                        isOverlayEnabled = true,
                        overlayRatio = 20,
                        widthScale = WidthScale.MIN_WIDTH,
                        orientation = StitchOrientation.VERTICAL,
                        outputFormat = OutputImageFormat.JPEG,
                        highMemoryLimitEnabled = true
                    )
                )
            }
            reportStageBreakdown("direct-min-parallel") {
                DirectStitchingStrategy(true, context).stitch(
                    source,
                    StitchingOptions(
                        widthScale = WidthScale.MIN_WIDTH,
                        orientation = StitchOrientation.VERTICAL,
                        outputFormat = OutputImageFormat.JPEG,
                        highMemoryLimitEnabled = true,
                        multiThreadEnabled = true
                    )
                )
            }
            reportStageBreakdown("overlay-min-parallel") {
                OverlayStitchingStrategy(context).stitch(
                    source,
                    StitchingOptions(
                        isOverlayEnabled = true,
                        overlayRatio = 20,
                        widthScale = WidthScale.MIN_WIDTH,
                        orientation = StitchOrientation.VERTICAL,
                        outputFormat = OutputImageFormat.JPEG,
                        highMemoryLimitEnabled = true,
                        multiThreadEnabled = true
                    )
                )
            }
        } finally {
            ProcessingPerformance.observer = null
            source.forEach(Bitmap::recycle)
        }
    }

    private fun benchmarkDirect(
        scenario: FixedBitmapScenario,
        orientation: StitchOrientation,
        scale: WidthScale
    ) {
        val source = FixedBitmapData.create(scenario)
        val strategy = DirectStitchingStrategy(orientation == StitchOrientation.VERTICAL, context)
        val options = StitchingOptions(
            widthScale = scale,
            orientation = orientation,
            outputFormat = OutputImageFormat.JPEG,
            highMemoryLimitEnabled = true,
            multiThreadEnabled = true
        )
        try {
            benchmarkRule.measureRepeated {
                val result = runBlocking { strategy.stitch(source, options) }
                check(result is StitchResult.BitmapResult)
                result.bitmap.recycle()
            }
        } finally {
            source.forEach(Bitmap::recycle)
        }
    }

    private fun benchmarkEncode(format: OutputImageFormat) {
        val bitmap = FixedBitmapData.create(FixedBitmapScenario.TWO_NORMAL).first()
        try {
            benchmarkRule.measureRepeated {
                val output = ByteArrayOutputStream()
                check(bitmap.compress(format.compressFormat, 90, output))
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun reportStageBreakdown(
        label: String,
        operation: suspend () -> StitchResult
    ) {
        repeat(5) {
            recycleResult(runBlocking { operation() })
        }

        val timings = mutableListOf<StageTiming>()
        ProcessingPerformance.observer = { timings += it }
        repeat(20) {
            recycleResult(runBlocking { operation() })
        }
        ProcessingPerformance.observer = null

        val summary = timings.groupBy(StageTiming::stage)
            .toSortedMap()
            .mapValues { (_, values) -> values.map(StageTiming::durationNanos).sorted()[values.size / 2] / 1_000_000.0 }
        Log.i("ChimeraBenchmark", "$label stage medians ms: $summary")
    }

    private fun recycleResult(result: StitchResult) {
        check(result is StitchResult.BitmapResult)
        result.bitmap.recycle()
    }
}

private enum class FixedBitmapScenario(
    val count: Int,
    val width: Int,
    val height: Int
) {
    TWO_NORMAL(2, 1_080, 1_920),
    TEN_MEDIUM(10, 720, 480),
    FIFTY_SMALL(50, 320, 180)
}

private object FixedBitmapData {
    fun create(scenario: FixedBitmapScenario): List<Bitmap> = List(scenario.count) { index ->
        Bitmap.createBitmap(
            scenario.width + index % 3 * 16,
            scenario.height + index % 5 * 12,
            Bitmap.Config.RGB_565
        ).also { bitmap ->
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.rgb((index * 47) % 255, (index * 83) % 255, (index * 131) % 255)
            }
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
            canvas.setBitmap(null)
        }
    }
}

private val OutputImageFormat.compressFormat: Bitmap.CompressFormat
    get() = when (this) {
        OutputImageFormat.PNG -> Bitmap.CompressFormat.PNG
        OutputImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        OutputImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
    }
