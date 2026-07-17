package com.rerokutosei.chimera.utils.stitch.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
