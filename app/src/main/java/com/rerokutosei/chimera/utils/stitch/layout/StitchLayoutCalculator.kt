package com.rerokutosei.chimera.utils.stitch.layout

enum class LayoutOrientation {
    VERTICAL,
    HORIZONTAL
}

enum class LayoutScaleMode {
    NONE,
    MIN,
    MAX
}

enum class LayoutMode {
    DIRECT,
    OVERLAY
}

enum class OutputImageFormat(
    val code: Int,
    val displayName: String,
    val maxDimension: Long?
) {
    PNG(0, "PNG", Int.MAX_VALUE.toLong()),
    JPEG(1, "JPEG", 65_535L),
    WEBP(2, "WEBP", 16_383L);

    companion object {
        fun fromCode(code: Int): OutputImageFormat = entries.firstOrNull { it.code == code } ?: JPEG
    }
}

data class ImageDimensions(
    val width: Int,
    val height: Int
) {
    init {
        require(width > 0 && height > 0) { "Image dimensions must be positive" }
    }
}

data class LayoutOptions(
    val orientation: LayoutOrientation,
    val scaleMode: LayoutScaleMode = LayoutScaleMode.NONE,
    val mode: LayoutMode = LayoutMode.DIRECT,
    val spacing: Int = 0,
    val overlayRatio: Int = 0
)

data class StitchLayout(
    val width: Long,
    val height: Long,
    val scaledImages: List<ImageDimensions>,
    val overlaySteps: List<Int> = emptyList()
)

sealed interface FormatValidation {
    data class Valid(val width: Long, val height: Long) : FormatValidation

    data class ExceedsLimit(
        val width: Long,
        val height: Long,
        val format: OutputImageFormat,
        val limit: Long
    ) : FormatValidation
}

object StitchLayoutCalculator {

    fun calculate(images: List<ImageDimensions>, options: LayoutOptions): StitchLayout? {
        if (images.isEmpty()) return null
        require(options.spacing >= 0) { "Spacing must not be negative" }
        require(options.overlayRatio in 0..100) { "Overlay ratio must be between 0 and 100" }

        val scaledImages = scale(images, options.orientation, options.scaleMode)
        return when (options.mode) {
            LayoutMode.DIRECT -> calculateDirect(scaledImages, options.orientation, options.spacing)
            LayoutMode.OVERLAY -> calculateOverlay(
                scaledImages,
                options.orientation,
                options.overlayRatio
            )
        }
    }

    fun scale(
        images: List<ImageDimensions>,
        orientation: LayoutOrientation,
        scaleMode: LayoutScaleMode
    ): List<ImageDimensions> {
        if (images.isEmpty() || scaleMode == LayoutScaleMode.NONE) return images

        val target = when (orientation) {
            LayoutOrientation.VERTICAL -> when (scaleMode) {
                LayoutScaleMode.MIN -> images.minOf { it.width }
                LayoutScaleMode.MAX -> images.maxOf { it.width }
                LayoutScaleMode.NONE -> return images
            }

            LayoutOrientation.HORIZONTAL -> when (scaleMode) {
                LayoutScaleMode.MIN -> images.minOf { it.height }
                LayoutScaleMode.MAX -> images.maxOf { it.height }
                LayoutScaleMode.NONE -> return images
            }
        }

        return images.map { image ->
            when (orientation) {
                LayoutOrientation.VERTICAL -> {
                    if (image.width == target) image else ImageDimensions(
                        width = target,
                        height = (image.height * (target.toDouble() / image.width)).toInt()
                            .coerceAtLeast(1)
                    )
                }

                LayoutOrientation.HORIZONTAL -> {
                    if (image.height == target) image else ImageDimensions(
                        width = (image.width * (target.toDouble() / image.height)).toInt()
                            .coerceAtLeast(1),
                        height = target
                    )
                }
            }
        }
    }

    fun validateFormat(layout: StitchLayout, format: OutputImageFormat): FormatValidation {
        val limit =
            format.maxDimension ?: return FormatValidation.Valid(layout.width, layout.height)
        return if (layout.width > limit || layout.height > limit) {
            FormatValidation.ExceedsLimit(layout.width, layout.height, format, limit)
        } else {
            FormatValidation.Valid(layout.width, layout.height)
        }
    }

    private fun calculateDirect(
        images: List<ImageDimensions>,
        orientation: LayoutOrientation,
        spacing: Int
    ): StitchLayout {
        val spacingTotal = (images.size - 1).toLong() * spacing
        return when (orientation) {
            LayoutOrientation.VERTICAL -> StitchLayout(
                width = images.maxOf { it.width }.toLong(),
                height = images.sumOf { it.height.toLong() } + spacingTotal,
                scaledImages = images
            )

            LayoutOrientation.HORIZONTAL -> StitchLayout(
                width = images.sumOf { it.width.toLong() } + spacingTotal,
                height = images.maxOf { it.height }.toLong(),
                scaledImages = images
            )
        }
    }

    private fun calculateOverlay(
        images: List<ImageDimensions>,
        orientation: LayoutOrientation,
        overlayRatio: Int
    ): StitchLayout {
        return when (orientation) {
            LayoutOrientation.VERTICAL -> {
                val firstMajor = images.first().height
                val preferredStep = (firstMajor * overlayRatio / 100).coerceAtLeast(1)
                val steps = images.drop(1).map { minOf(preferredStep, it.height.coerceAtLeast(1)) }
                StitchLayout(
                    width = images.maxOf { it.width }.toLong(),
                    height = firstMajor.toLong() + steps.sumOf { it.toLong() },
                    scaledImages = images,
                    overlaySteps = steps
                )
            }

            LayoutOrientation.HORIZONTAL -> {
                val firstMajor = images.first().width
                val preferredStep = (firstMajor * overlayRatio / 100).coerceAtLeast(1)
                val steps = images.drop(1).map { minOf(preferredStep, it.width.coerceAtLeast(1)) }
                StitchLayout(
                    width = firstMajor.toLong() + steps.sumOf { it.toLong() },
                    height = images.maxOf { it.height }.toLong(),
                    scaledImages = images,
                    overlaySteps = steps
                )
            }
        }
    }
}
