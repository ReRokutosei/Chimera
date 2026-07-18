package com.rerokutosei.chimera.utils.stitch.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StitchLayoutCalculatorTest {

    @Test
    fun emptyInputDoesNotProduceLayout() {
        assertNull(
            StitchLayoutCalculator.calculate(
                emptyList(),
                LayoutOptions(orientation = LayoutOrientation.VERTICAL)
            )
        )
    }

    @Test
    fun verticalDirectLayoutIncludesSpacing() {
        val layout = calculate(
            images = listOf(ImageDimensions(100, 200), ImageDimensions(80, 120)),
            orientation = LayoutOrientation.VERTICAL,
            spacing = 12
        )

        assertEquals(100L, layout.width)
        assertEquals(332L, layout.height)
    }

    @Test
    fun horizontalDirectLayoutIncludesSpacing() {
        val layout = calculate(
            images = listOf(ImageDimensions(100, 200), ImageDimensions(80, 120)),
            orientation = LayoutOrientation.HORIZONTAL,
            spacing = 12
        )

        assertEquals(192L, layout.width)
        assertEquals(200L, layout.height)
    }

    @Test
    fun singleImageLayoutIsUnchanged() {
        val image = ImageDimensions(123, 456)
        val layout = calculate(
            images = listOf(image),
            orientation = LayoutOrientation.VERTICAL,
            spacing = 99
        )

        assertEquals(123L, layout.width)
        assertEquals(456L, layout.height)
        assertEquals(listOf(image), layout.scaledImages)
    }

    @Test
    fun verticalMinScaleUsesSmallestWidth() {
        val layout = calculate(
            images = listOf(ImageDimensions(100, 200), ImageDimensions(50, 100)),
            orientation = LayoutOrientation.VERTICAL,
            scaleMode = LayoutScaleMode.MIN,
            spacing = 10
        )

        assertEquals(
            listOf(ImageDimensions(50, 100), ImageDimensions(50, 100)),
            layout.scaledImages
        )
        assertEquals(50L, layout.width)
        assertEquals(210L, layout.height)
    }

    @Test
    fun horizontalMaxScaleUsesLargestHeight() {
        val layout = calculate(
            images = listOf(ImageDimensions(100, 50), ImageDimensions(200, 100)),
            orientation = LayoutOrientation.HORIZONTAL,
            scaleMode = LayoutScaleMode.MAX
        )

        assertEquals(
            listOf(ImageDimensions(200, 100), ImageDimensions(200, 100)),
            layout.scaledImages
        )
        assertEquals(400L, layout.width)
        assertEquals(100L, layout.height)
    }

    @Test
    fun verticalMaxScaleUsesLargestWidth() {
        val layout = calculate(
            images = listOf(ImageDimensions(50, 75), ImageDimensions(100, 100)),
            orientation = LayoutOrientation.VERTICAL,
            scaleMode = LayoutScaleMode.MAX
        )

        assertEquals(
            listOf(ImageDimensions(100, 150), ImageDimensions(100, 100)),
            layout.scaledImages
        )
        assertEquals(100L, layout.width)
        assertEquals(250L, layout.height)
    }

    @Test
    fun horizontalMinScaleUsesSmallestHeight() {
        val layout = calculate(
            images = listOf(ImageDimensions(100, 50), ImageDimensions(300, 100)),
            orientation = LayoutOrientation.HORIZONTAL,
            scaleMode = LayoutScaleMode.MIN
        )

        assertEquals(
            listOf(ImageDimensions(100, 50), ImageDimensions(150, 50)),
            layout.scaledImages
        )
        assertEquals(250L, layout.width)
        assertEquals(50L, layout.height)
    }

    @Test
    fun overlayStepsAreClampedForEachImage() {
        val layout = calculate(
            images = listOf(
                ImageDimensions(100, 1_000),
                ImageDimensions(100, 100),
                ImageDimensions(100, 600)
            ),
            orientation = LayoutOrientation.VERTICAL,
            mode = LayoutMode.OVERLAY,
            overlayRatio = 50
        )

        assertEquals(listOf(100, 500), layout.overlaySteps)
        assertEquals(1_600L, layout.height)
    }

    @Test
    fun horizontalOverlayUsesRightEdgeSteps() {
        val layout = calculate(
            images = listOf(
                ImageDimensions(100, 20),
                ImageDimensions(20, 30),
                ImageDimensions(200, 10)
            ),
            orientation = LayoutOrientation.HORIZONTAL,
            mode = LayoutMode.OVERLAY,
            overlayRatio = 50
        )

        assertEquals(listOf(20, 50), layout.overlaySteps)
        assertEquals(170L, layout.width)
        assertEquals(30L, layout.height)
    }

    @Test
    fun overlayRatioZeroRetainsOnePixelPerFollowingImage() {
        val layout = calculate(
            images = listOf(
                ImageDimensions(10, 20),
                ImageDimensions(10, 30),
                ImageDimensions(10, 40)
            ),
            orientation = LayoutOrientation.VERTICAL,
            mode = LayoutMode.OVERLAY,
            overlayRatio = 0
        )

        assertEquals(listOf(1, 1), layout.overlaySteps)
        assertEquals(22L, layout.height)
    }

    @Test
    fun overlayRatioHundredIsClampedToFollowingImages() {
        val layout = calculate(
            images = listOf(
                ImageDimensions(10, 100),
                ImageDimensions(10, 40),
                ImageDimensions(10, 120)
            ),
            orientation = LayoutOrientation.VERTICAL,
            mode = LayoutMode.OVERLAY,
            overlayRatio = 100
        )

        assertEquals(listOf(40, 100), layout.overlaySteps)
        assertEquals(240L, layout.height)
    }

    @Test
    fun formatValidationUsesInclusiveLimit() {
        val valid = StitchLayout(65_535, 1, emptyList())
        val invalid = StitchLayout(65_536, 1, emptyList())

        assertTrue(
            StitchLayoutCalculator.validateFormat(
                valid,
                OutputImageFormat.JPEG
            ) is FormatValidation.Valid
        )
        val result = StitchLayoutCalculator.validateFormat(invalid, OutputImageFormat.JPEG)
        assertTrue(result is FormatValidation.ExceedsLimit)
        assertEquals(65_535L, (result as FormatValidation.ExceedsLimit).limit)
    }

    @Test
    fun pngAndWebpLimitsAreValidatedIndependently() {
        assertTrue(
            StitchLayoutCalculator.validateFormat(
                StitchLayout(Int.MAX_VALUE.toLong(), 1, emptyList()),
                OutputImageFormat.PNG
            ) is FormatValidation.Valid
        )
        assertTrue(
            StitchLayoutCalculator.validateFormat(
                StitchLayout(16_383, 16_383, emptyList()),
                OutputImageFormat.WEBP
            ) is FormatValidation.Valid
        )
        val invalidWebp = StitchLayoutCalculator.validateFormat(
            StitchLayout(16_384, 1, emptyList()),
            OutputImageFormat.WEBP
        )
        assertTrue(invalidWebp is FormatValidation.ExceedsLimit)
        assertEquals(16_383L, (invalidWebp as FormatValidation.ExceedsLimit).limit)
    }

    @Test
    fun directLayoutUsesLongArithmeticForLargeDimensions() {
        val layout = calculate(
            images = listOf(
                ImageDimensions(Int.MAX_VALUE, 1),
                ImageDimensions(Int.MAX_VALUE, 1)
            ),
            orientation = LayoutOrientation.HORIZONTAL,
            spacing = Int.MAX_VALUE
        )

        assertEquals(Int.MAX_VALUE.toLong() * 3, layout.width)
        assertEquals(1L, layout.height)
    }

    @Test
    fun invalidLayoutOptionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            calculate(
                images = listOf(ImageDimensions(10, 10)),
                orientation = LayoutOrientation.VERTICAL,
                spacing = -1
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            calculate(
                images = listOf(ImageDimensions(10, 10)),
                orientation = LayoutOrientation.VERTICAL,
                mode = LayoutMode.OVERLAY,
                overlayRatio = 101
            )
        }
    }

    @Test
    fun invalidImageDimensionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { ImageDimensions(0, 1) }
        assertThrows(IllegalArgumentException::class.java) { ImageDimensions(1, 0) }
        assertThrows(IllegalArgumentException::class.java) { ImageDimensions(-1, 1) }
    }

    @Test
    fun scaleNoneReturnsOriginalList() {
        val images = listOf(ImageDimensions(10, 20), ImageDimensions(30, 40))

        assertSame(
            images,
            StitchLayoutCalculator.scale(
                images,
                LayoutOrientation.VERTICAL,
                LayoutScaleMode.NONE
            )
        )
    }

    @Test
    fun unknownOutputFormatFallsBackToJpeg() {
        assertEquals(OutputImageFormat.JPEG, OutputImageFormat.fromCode(99))
    }

    private fun calculate(
        images: List<ImageDimensions>,
        orientation: LayoutOrientation,
        scaleMode: LayoutScaleMode = LayoutScaleMode.NONE,
        mode: LayoutMode = LayoutMode.DIRECT,
        spacing: Int = 0,
        overlayRatio: Int = 0
    ): StitchLayout = requireNotNull(
        StitchLayoutCalculator.calculate(
            images,
            LayoutOptions(
                orientation = orientation,
                scaleMode = scaleMode,
                mode = mode,
                spacing = spacing,
                overlayRatio = overlayRatio
            )
        )
    )
}
