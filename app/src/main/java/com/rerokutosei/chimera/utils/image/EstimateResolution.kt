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
import com.rerokutosei.chimera.data.model.ImageInfo
import com.rerokutosei.chimera.ui.main.OverlayMode
import com.rerokutosei.chimera.ui.main.StitchMode
import com.rerokutosei.chimera.ui.main.WidthScale

/**
 * 图片尺寸验证器
 * 用于计算和验证拼接图片的预期尺寸是否超出格式限制
 */
class EstimateResolution(private val bitmapLoader: BitmapLoader) {

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

            // 根据格式检查限制
            val formatLimit = when (outputFormat) {
                0 -> "PNG" to Int.MAX_VALUE.toLong() // PNG 无实际限制
                1 -> "JPEG" to 65535L // JPEG 限制
                2 -> "WEBP" to 16383L // WEBP 限制
                else -> "JPEG" to 65535L
            }

            val (formatName, limit) = formatLimit

            return if (width > limit || height > limit) {
                ResolutionValidationResult.Invalid(
                    width = width,
                    height = height,
                    formatName = formatName,
                    limit = limit.toInt()
                )
            } else {
                ResolutionValidationResult.Valid(
                    width = width,
                    height = height
                )
            }
        } catch (e: Exception) {
            // 发生错误时，默认为有效状态
            return ResolutionValidationResult.Valid(0, 0)
        }
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
        if (imageUris.isEmpty()) return 0L to 0L

        val images = imageUris.mapNotNull { uri ->
            bitmapLoader.getSampledDimensions(uri)?.let { (w, h) ->
                ImageInfo(uri = uri, width = w, height = h)
            }
        }

        return when {
            overlayMode == OverlayMode.ENABLED -> {
                calculateOverlayResolution(images, stitchMode, widthScale, overlayArea)
            }
            else -> {
                calculateDirectResolution(images, stitchMode, widthScale, imageSpacing)
            }
        }
    }

    /**
     * 通用缩放方法
     */
    private fun scaleImages(
        images: List<ImageInfo>,
        widthScale: WidthScale,
        stitchMode: StitchMode
    ): List<ImageInfo> {
        return when (widthScale) {
            WidthScale.MAX_WIDTH -> {
                if (stitchMode == StitchMode.DIRECT_VERTICAL) {
                    scaleToMaxWidth(images)
                } else {
                    scaleToMaxHeight(images)
                }
            }
            WidthScale.MIN_WIDTH -> {
                if (stitchMode == StitchMode.DIRECT_VERTICAL) {
                    scaleToMinWidth(images)
                } else {
                    scaleToMinHeight(images)
                }
            }
            else -> images
        }
    }

    /**
     * 计算普通拼接模式的预期尺寸
     */
    private fun calculateDirectResolution(
        images: List<ImageInfo>,
        stitchMode: StitchMode,
        widthScale: WidthScale,
        imageSpacing: Int
    ): Pair<Long, Long> {
        if (images.isEmpty()) return 0L to 0L

        val scaledImages = scaleImages(images, widthScale, stitchMode)

        return when (stitchMode) {
            StitchMode.DIRECT_VERTICAL -> {
                val totalHeight = scaledImages.sumOf { it.height.toLong() } + (scaledImages.size - 1) * imageSpacing
                val maxWidth = scaledImages.maxOf { it.width.toLong() }
                maxWidth to totalHeight
            }
            StitchMode.DIRECT_HORIZONTAL -> {
                val totalWidth = scaledImages.sumOf { it.width.toLong() } + (scaledImages.size - 1) * imageSpacing
                val maxHeight = scaledImages.maxOf { it.height.toLong() }
                totalWidth to maxHeight
            }
        }
    }

    /**
     * 计算叠加模式的预期尺寸
     */
    private fun calculateOverlayResolution(
        images: List<ImageInfo>,
        stitchMode: StitchMode,
        widthScale: WidthScale,
        overlayArea: Int
    ): Pair<Long, Long> {
        if (images.isEmpty()) return 0L to 0L

        val scaledImages = scaleImages(images, widthScale, stitchMode)

        return when (stitchMode) {
            StitchMode.DIRECT_VERTICAL -> {
                val firstImageHeight = scaledImages[0].height
                val overlayHeight = (firstImageHeight * overlayArea / 100).coerceAtLeast(1)
                val totalHeight = firstImageHeight + (scaledImages.size - 1) * overlayHeight
                val maxWidth = scaledImages.maxOf { it.width }
                maxWidth.toLong() to totalHeight.toLong()
            }
            StitchMode.DIRECT_HORIZONTAL -> {
                val firstImageWidth = scaledImages[0].width
                val overlayWidth = (firstImageWidth * overlayArea / 100).coerceAtLeast(1)
                val totalWidth = firstImageWidth + (scaledImages.size - 1) * overlayWidth
                val maxHeight = scaledImages.maxOf { it.height }
                totalWidth.toLong() to maxHeight.toLong()
            }
        }
    }

    /**
     * 缩放到最大宽度
     */
    private fun scaleToMaxWidth(images: List<ImageInfo>): List<ImageInfo> {
        if (images.isEmpty()) return images
        val maxWidth = images.maxOf { it.width }
        return images.map { image ->
            if (image.width == maxWidth) {
                image
            } else {
                val scale = maxWidth.toDouble() / image.width.toDouble()
                val newHeight = (image.height * scale).toInt()
                image.copy(width = maxWidth, height = newHeight)
            }
        }
    }

    /**
     * 缩放到最小宽度
     */
    private fun scaleToMinWidth(images: List<ImageInfo>): List<ImageInfo> {
        if (images.isEmpty()) return images
        val minWidth = images.minOf { it.width }
        return images.map { image ->
            if (image.width == minWidth) {
                image
            } else {
                val scale = minWidth.toDouble() / image.width.toDouble()
                val newHeight = (image.height * scale).toInt()
                image.copy(width = minWidth, height = newHeight)
            }
        }
    }

    /**
     * 缩放到最大高度
     */
    private fun scaleToMaxHeight(images: List<ImageInfo>): List<ImageInfo> {
        if (images.isEmpty()) return images
        val maxHeight = images.maxOf { it.height }
        return images.map { image ->
            if (image.height == maxHeight) {
                image
            } else {
                val scale = maxHeight.toDouble() / image.height.toDouble()
                val newWidth = (image.width * scale).toInt()
                image.copy(width = newWidth, height = maxHeight)
            }
        }
    }

    /**
     * 缩放到最小高度
     */
    private fun scaleToMinHeight(images: List<ImageInfo>): List<ImageInfo> {
        if (images.isEmpty()) return images
        val minHeight = images.minOf { it.height }
        return images.map { image ->
            if (image.height == minHeight) {
                image
            } else {
                val scale = minHeight.toDouble() / image.height.toDouble()
                val newWidth = (image.width * scale).toInt()
                image.copy(width = newWidth, height = minHeight)
            }
        }
    }
}

/**
 * 尺寸验证结果
 */
sealed class ResolutionValidationResult {
    object NotNeeded : ResolutionValidationResult()
    object InProgress : ResolutionValidationResult()
    data class Valid(val width: Long, val height: Long) : ResolutionValidationResult()
    data class Invalid(val width: Long, val height: Long, val formatName: String, val limit: Int) : ResolutionValidationResult()
}