/*
 * Chimera is an image stitching tool
 * Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the GNU General Public License v3.0 (the "License");
 * you may redistribute and/or modify this program under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.en.html>.
 */

package com.rerokutosei.chimera.utils.image

import android.net.Uri
import android.util.LruCache
import com.rerokutosei.chimera.domain.error.StitchFailure
import com.rerokutosei.chimera.ui.main.OverlayMode
import com.rerokutosei.chimera.ui.main.StitchMode
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.stitch.layout.FormatValidation
import com.rerokutosei.chimera.utils.stitch.layout.ImageDimensions
import com.rerokutosei.chimera.utils.stitch.layout.LayoutMode
import com.rerokutosei.chimera.utils.stitch.layout.LayoutOptions
import com.rerokutosei.chimera.utils.stitch.layout.LayoutOrientation
import com.rerokutosei.chimera.utils.stitch.layout.LayoutScaleMode
import com.rerokutosei.chimera.utils.stitch.layout.OutputImageFormat
import com.rerokutosei.chimera.utils.stitch.layout.StitchLayout
import com.rerokutosei.chimera.utils.stitch.layout.StitchLayoutCalculator

/**
 * 图片尺寸验证器
 * 用于计算和验证拼接图片的预期尺寸是否超出格式限制
 */
class EstimateResolution(private val bitmapLoader: BitmapLoader) {

    // LRU缓存存储最近计算的尺寸结果
    private val resolutionCache = LruCache<String, Pair<Long, Long>>(50)

    /**
     * 验证图片尺寸是否超出格式限制
     *
     * @param imageUris 图片URI列表
     * @param stitchMode 拼接模式
     * @param widthScale 宽度缩放模式
     * @param overlayMode 叠加模式
     * @param imageSpacing 图片间隔
     * @param outputFormat 输出格式 (0: PNG, 1: JPEG, 2: WEBP)
     * @return 验证结果
     */
    fun validateResolution(
        imageUris: List<Uri>,
        stitchMode: StitchMode,
        widthScale: WidthScale,
        overlayMode: OverlayMode,
        overlayArea: Int,
        imageSpacing: Int,
        outputFormat: Int
    ): ResolutionValidationResult {
        if (imageUris.isEmpty()) {
            return ResolutionValidationResult.NotNeeded
        }

        try {
            // 计算预期尺寸
            val (width, height) = calculateExpectedResolution(
                imageUris, stitchMode, widthScale, overlayMode, overlayArea, imageSpacing
            )

            val layout = StitchLayout(
                width = width,
                height = height,
                scaledImages = emptyList()
            )
            return when (val validation = StitchLayoutCalculator.validateFormat(layout, OutputImageFormat.fromCode(outputFormat))) {
                is FormatValidation.Valid -> ResolutionValidationResult.Valid(validation.width, validation.height)
                is FormatValidation.ExceedsLimit -> ResolutionValidationResult.Invalid(
                    width = validation.width,
                    height = validation.height,
                    formatName = validation.format.displayName,
                    limit = validation.limit.toInt()
                )
            }
        } catch (e: Exception) {
            return ResolutionValidationResult.Unavailable(StitchFailure.MetadataUnavailable(e))
        }
    }

    /**
     * 生成缓存键
     */
    private fun generateCacheKey(
        imageUris: List<Uri>,
        stitchMode: StitchMode,
        widthScale: WidthScale,
        overlayMode: OverlayMode,
        overlayArea: Int,
        imageSpacing: Int
    ): String {
        return "${imageUris.joinToString(",") { it.toString() }}|$stitchMode|$widthScale|$overlayMode|$overlayArea|$imageSpacing"
    }

    /**
     * 根据拼接参数计算预期尺寸
     */
    private fun calculateExpectedResolution(
        imageUris: List<Uri>,
        stitchMode: StitchMode,
        widthScale: WidthScale,
        overlayMode: OverlayMode,
        overlayArea: Int,
        imageSpacing: Int
    ): Pair<Long, Long> {
        // 生成缓存键
        val cacheKey = generateCacheKey(imageUris, stitchMode, widthScale, overlayMode, overlayArea, imageSpacing)

        // 尝试从缓存中获取结果
        resolutionCache.get(cacheKey)?.let {
            return it
        }

        if (imageUris.isEmpty()) return 0L to 0L

        val images = imageUris.map { uri ->
            val (width, height) = requireNotNull(bitmapLoader.getSampledDimensions(uri)) {
                "Unable to read image dimensions: $uri"
            }
            ImageDimensions(width, height)
        }

        val layout = requireNotNull(
            StitchLayoutCalculator.calculate(
                images = images,
                options = LayoutOptions(
                    orientation = stitchMode.toLayoutOrientation(),
                    scaleMode = widthScale.toLayoutScaleMode(),
                    mode = if (overlayMode == OverlayMode.ENABLED) LayoutMode.OVERLAY else LayoutMode.DIRECT,
                    spacing = imageSpacing,
                    overlayRatio = overlayArea
                )
            )
        )
        val result = layout.width to layout.height

        // 将结果存入缓存
        resolutionCache.put(cacheKey, result)

        return result
    }

}

private fun StitchMode.toLayoutOrientation(): LayoutOrientation = when (this) {
    StitchMode.DIRECT_VERTICAL -> LayoutOrientation.VERTICAL
    StitchMode.DIRECT_HORIZONTAL -> LayoutOrientation.HORIZONTAL
}

private fun WidthScale.toLayoutScaleMode(): LayoutScaleMode = when (this) {
    WidthScale.NONE -> LayoutScaleMode.NONE
    WidthScale.MIN_WIDTH -> LayoutScaleMode.MIN
    WidthScale.MAX_WIDTH -> LayoutScaleMode.MAX
}

/**
 * 尺寸验证结果
 */
sealed class ResolutionValidationResult {
    object NotNeeded : ResolutionValidationResult()
    data class Valid(val width: Long, val height: Long) : ResolutionValidationResult()
    data class Invalid(val width: Long, val height: Long, val formatName: String, val limit: Int) : ResolutionValidationResult()
    data class Unavailable(val failure: StitchFailure.MetadataUnavailable) : ResolutionValidationResult()
}
