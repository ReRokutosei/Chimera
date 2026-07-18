package com.rerokutosei.chimera.stitch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rerokutosei.chimera.domain.error.StitchFailure
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.image.ImageSplitter
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.rerokutosei.chimera.utils.stitch.StitchResult
import com.rerokutosei.chimera.utils.stitch.layout.OutputImageFormat
import com.rerokutosei.chimera.utils.stitch.strategy.DirectStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.OverlayStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingOptions
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StitchingCorrectnessTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun directVerticalCentersImagesAndFillsSpacing() = runBlocking {
        val first = solidBitmap(4, 2, Color.RED)
        val second = solidBitmap(2, 2, Color.BLUE)
        val result = DirectStitchingStrategy(true, context).stitch(
            listOf(first, second),
            StitchingOptions(
                spacing = 1,
                spacingColor = Color.GREEN,
                outputFormat = OutputImageFormat.PNG,
                highMemoryLimitEnabled = true
            )
        ).requireBitmap()

        try {
            assertEquals(4, result.width)
            assertEquals(5, result.height)
            assertEquals(Color.RED, result.getPixel(0, 0))
            assertEquals(Color.GREEN, result.getPixel(3, 2))
            assertEquals(Color.BLUE, result.getPixel(1, 3))
            assertEquals(Color.TRANSPARENT, result.getPixel(0, 3))
            assertFalse(first.isRecycled)
            assertFalse(second.isRecycled)
        } finally {
            recycleAll(result, first, second)
        }
    }

    @Test
    fun directScalingPreservesSourcesAndProducesExpectedSize() = runBlocking {
        val first = solidBitmap(2, 4, Color.RED)
        val second = solidBitmap(4, 4, Color.BLUE)
        val result = DirectStitchingStrategy(true, context).stitch(
            listOf(first, second),
            StitchingOptions(
                widthScale = WidthScale.MAX_WIDTH,
                outputFormat = OutputImageFormat.PNG,
                highMemoryLimitEnabled = true
            )
        ).requireBitmap()

        try {
            assertEquals(4, result.width)
            assertEquals(12, result.height)
            assertFalse(first.isRecycled)
            assertFalse(second.isRecycled)
        } finally {
            recycleAll(result, first, second)
        }
    }

    @Test
    fun directEmptyInputReturnsTypedFailure() = runBlocking {
        val result = DirectStitchingStrategy(true, context).stitch(
            emptyList(),
            StitchingOptions(highMemoryLimitEnabled = true)
        )

        assertEquals(StitchFailure.NoImages, (result as StitchResult.ErrorResult).failure)
    }

    @Test
    fun overlayVerticalUsesBottomSliceOfFollowingImage() = runBlocking {
        val first = solidBitmap(3, 4, Color.RED)
        val second = createBitmap(3, 4, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
            for (y in 2 until 4) {
                for (x in 0 until 3) {
                    setPixel(x, y, Color.GREEN)
                }
            }
        }
        val result = OverlayStitchingStrategy(context).stitch(
            listOf(first, second),
            StitchingOptions(
                isOverlayEnabled = true,
                overlayRatio = 50,
                orientation = StitchOrientation.VERTICAL,
                outputFormat = OutputImageFormat.PNG,
                highMemoryLimitEnabled = true
            )
        ).requireBitmap()

        try {
            assertEquals(3, result.width)
            assertEquals(6, result.height)
            assertEquals(Color.RED, result.getPixel(1, 3))
            assertEquals(Color.GREEN, result.getPixel(1, 4))
            assertEquals(Color.GREEN, result.getPixel(1, 5))
        } finally {
            recycleAll(result, first, second)
        }
    }

    @Test
    fun splitterDistributesRemainderPixelsWithoutDroppingEdges() {
        val source = createBitmap(5, 3, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    setPixel(x, y, Color.rgb(x * 30, y * 60, 0))
                }
            }
        }
        val topLeft = ImageSplitter.createPiece(source, 0, 0, 2, 2)
        val bottomRight = ImageSplitter.createPiece(source, 1, 1, 2, 2)

        try {
            assertEquals(2, topLeft.width)
            assertEquals(1, topLeft.height)
            assertEquals(3, bottomRight.width)
            assertEquals(2, bottomRight.height)
            assertEquals(source.getPixel(2, 1), bottomRight.getPixel(0, 0))
            assertEquals(source.getPixel(4, 2), bottomRight.getPixel(2, 1))
        } finally {
            recycleAll(topLeft, bottomRight, source)
        }
    }

    @Test
    fun splitterRejectsInvalidGridAndCoordinates() {
        val source = solidBitmap(2, 2, Color.RED)
        try {
            assertThrows(IllegalArgumentException::class.java) {
                ImageSplitter.createPiece(source, 0, 0, 0, 1)
            }
            assertThrows(IllegalArgumentException::class.java) {
                ImageSplitter.createPiece(source, 2, 0, 2, 1)
            }
            assertThrows(IllegalArgumentException::class.java) {
                ImageSplitter.createPiece(source, 0, 0, 3, 1)
            }
        } finally {
            source.recycle()
        }
    }

    private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap =
        createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    private fun StitchResult.requireBitmap(): Bitmap {
        assertTrue("Expected BitmapResult but was $this", this is StitchResult.BitmapResult)
        return (this as StitchResult.BitmapResult).bitmap
    }

    private fun recycleAll(vararg bitmaps: Bitmap) {
        bitmaps.forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }
}
