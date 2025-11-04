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

package com.rerokutosei.chimera.utils.stitch

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.data.local.StitchSettingsManager
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.stitch.engine.KotlinStitchingEngine
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * 图片拼接工具类
 */
class ImageStitcher(private val context: Context) {

    companion object {
        private const val TAG = "ImageStitcher"
    }

    private val bitmapLoader = BitmapLoader(context)
    private val logManager = LogManager.getInstance(context)
    private val stitchSettingsManager = StitchSettingsManager.getInstance(context)
    private val imageSettingsManager = ImageSettingsManager.getInstance(context)

    /**
     * 预估拼接结果尺寸
     * @param images 图片列表
     * @param orientation 拼接方向
     * @param widthScale 宽度缩放模式
     * @param imageSpacing 图片间隔（像素）
     * @return 预估的尺寸（宽度和高度）
     */
    fun estimateResultSize(
        images: List<Bitmap>,
        orientation: StitchOrientation,
        widthScale: WidthScale,
        imageSpacing: Int
    ): Pair<Int, Int> {
        if (images.isEmpty()) return Pair(0, 0)

        // 根据宽度缩放模式和拼接方向计算处理后的图片尺寸
        val processedSizes = when (widthScale) {
            WidthScale.NONE -> images.map { Pair(it.width, it.height) }
            WidthScale.MAX_WIDTH -> {
                if (orientation == StitchOrientation.VERTICAL) {
                    val maxWidth = images.maxOf { it.width }
                    images.map { bitmap ->
                        if (bitmap.width == maxWidth) Pair(bitmap.width, bitmap.height)
                        else {
                            val scale = maxWidth.toFloat() / bitmap.width.toFloat()
                            Pair(maxWidth, (bitmap.height * scale).toInt())
                        }
                    }
                } else {
                    val maxHeight = images.maxOf { it.height }
                    images.map { bitmap ->
                        if (bitmap.height == maxHeight) Pair(bitmap.width, bitmap.height)
                        else {
                            val scale = maxHeight.toFloat() / bitmap.height.toFloat()
                            Pair((bitmap.width * scale).toInt(), maxHeight)
                        }
                    }
                }
            }
            WidthScale.MIN_WIDTH -> {
                if (orientation == StitchOrientation.VERTICAL) {
                    val minWidth = images.minOf { it.width }
                    images.map { bitmap ->
                        if (bitmap.width == minWidth) Pair(bitmap.width, bitmap.height)
                        else {
                            val scale = minWidth.toFloat() / bitmap.width.toFloat()
                            Pair(minWidth, (bitmap.height * scale).toInt())
                        }
                    }
                } else {
                    val minHeight = images.minOf { it.height }
                    images.map { bitmap ->
                        if (bitmap.height == minHeight) Pair(bitmap.width, bitmap.height)
                        else {
                            val scale = minHeight.toFloat() / bitmap.height.toFloat()
                            Pair((bitmap.width * scale).toInt(), minHeight)
                        }
                    }
                }
            }
        }

        return when (orientation) {
            StitchOrientation.VERTICAL -> Pair(processedSizes.maxOf { it.first }, processedSizes.sumOf { it.second } + (processedSizes.size - 1) * imageSpacing)
            StitchOrientation.HORIZONTAL -> Pair(processedSizes.sumOf { it.first } + (processedSizes.size - 1) * imageSpacing, processedSizes.maxOf { it.second })
        }
    }

    /**
     * 拼接图片
     * @param imageUris 图片URI列表
     * @param orientation 拼接方向 (vertical: 垂直, horizontal: 水平)
     * @param widthScale 宽度缩放模式
     * @param imageSpacing 图片间隔（像素）
     * @param progressCallback 进度回调
     * @return 拼接结果
     */
    suspend fun stitchImages(
        imageUris: List<Uri>,
        orientation: StitchOrientation = StitchOrientation.VERTICAL,
        widthScale: WidthScale = WidthScale.NONE,
        imageSpacing: Int = 0,
        progressCallback: (progress: Int) -> Unit = {}
    ): StitchResult = withContext(Dispatchers.IO) {
        if (imageUris.isEmpty()) return@withContext StitchResult.ErrorResult("图片列表为空")
        logManager.debug(TAG, "开始直接拼接 ${imageUris.size} 张图片.")

        try {
            val engine = KotlinStitchingEngine(context)
            val quality = imageSettingsManager.getOutputImageQualityFlow().first()
            val format = imageSettingsManager.getOutputImageFormatFlow().first()

            val options = StitchingOptions(
                spacing = imageSpacing,
                widthScale = widthScale,
                orientation = orientation,
                quality = quality,
                outputFormat = format
            )

            engine.stitchImages(context, imageUris, options, progressCallback)
        } catch (e: Exception) {
            logManager.error(TAG, "直接拼接过程中出错", e)
            StitchResult.ErrorResult("拼接失败: ${e.message}")
        }
    }

    /**
     * 按照叠加拼接模式拼接图片
     * @param imageUris 图片URI列表
     * @param overlayRatio 被叠加区域占比，例如30表示30%
     * @param widthScale 宽度缩放模式
     * @param progressCallback 进度回调
     * @return 拼接结果
     */
    suspend fun stitchOverlay(
        imageUris: List<Uri>,
        overlayRatio: Int,
        widthScale: WidthScale = WidthScale.MIN_WIDTH,
        orientation: StitchOrientation = StitchOrientation.VERTICAL,
        progressCallback: (progress: Int) -> Unit = {}
    ): StitchResult = withContext(Dispatchers.IO) {
        if (imageUris.isEmpty()) return@withContext StitchResult.ErrorResult("图片列表为空")
        logManager.debug(TAG, "开始叠加拼接 ${imageUris.size} 张图片.")

        try {
            val engine = KotlinStitchingEngine(context)
            val quality = imageSettingsManager.getOutputImageQualityFlow().first()
            val format = imageSettingsManager.getOutputImageFormatFlow().first()

            val options = StitchingOptions(
                overlayRatio = overlayRatio,
                widthScale = widthScale,
                orientation = orientation,
                quality = quality,
                outputFormat = format
            )

            engine.stitchImages(context, imageUris, options, progressCallback)
        } catch (e: Exception) {
            logManager.error(TAG, "叠加拼接过程中出错", e)
            StitchResult.ErrorResult("叠加拼接失败: ${e.message}")
        }
    }

    fun clearCache() {
        bitmapLoader.clearCache()
    }
}

/**
 * 拼接方向枚举
 */
enum class StitchOrientation {
    VERTICAL,    // 垂直拼接
    HORIZONTAL   // 水平拼接
}