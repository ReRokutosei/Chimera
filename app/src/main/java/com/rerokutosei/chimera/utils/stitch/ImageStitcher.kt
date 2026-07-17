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
import com.rerokutosei.chimera.domain.error.StitchFailure
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.stitch.engine.KotlinStitchingEngine
import com.rerokutosei.chimera.utils.stitch.layout.OutputImageFormat
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingOptions
import kotlinx.coroutines.CancellationException
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

    private val logManager = LogManager.getInstance(context)
    private val stitchSettingsManager = StitchSettingsManager.getInstance(context)
    private val imageSettingsManager = ImageSettingsManager.getInstance(context)
    private val bitmapLoader = BitmapLoader(context)
    private val engine = KotlinStitchingEngine(context, bitmapLoader)

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
        if (imageUris.isEmpty()) return@withContext StitchResult.ErrorResult(StitchFailure.NoImages)
        logManager.debug(TAG, "开始直接拼接 ${imageUris.size} 张图片.")

        try {
            val format = imageSettingsManager.getOutputImageFormatFlow().first()
            val highMemoryLimitEnabled = imageSettingsManager.getHighMemoryLimitFlow().first()
            val spacingColorHex = stitchSettingsManager.getImageSpacingColorFlow().first()
            val spacingColor = try {
                android.graphics.Color.parseColor(spacingColorHex)
            } catch (_: Exception) {
                android.graphics.Color.BLACK
            }

            val options = StitchingOptions(
                spacing = imageSpacing,
                spacingColor = spacingColor,
                isOverlayEnabled = false,
                widthScale = widthScale,
                orientation = orientation,
                outputFormat = OutputImageFormat.fromCode(format),
                highMemoryLimitEnabled = highMemoryLimitEnabled
            )

            engine.stitchImages(imageUris, options, progressCallback)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logManager.error(TAG, "直接拼接过程中出错", e)
            StitchResult.ErrorResult(StitchFailure.Unexpected(e))
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
        if (imageUris.isEmpty()) return@withContext StitchResult.ErrorResult(StitchFailure.NoImages)
        logManager.debug(TAG, "开始叠加拼接 ${imageUris.size} 张图片.")

        try {
            val format = imageSettingsManager.getOutputImageFormatFlow().first()
            val highMemoryLimitEnabled = imageSettingsManager.getHighMemoryLimitFlow().first()

            val options = StitchingOptions(
                isOverlayEnabled = true,
                overlayRatio = overlayRatio,
                widthScale = widthScale,
                orientation = orientation,
                outputFormat = OutputImageFormat.fromCode(format),
                highMemoryLimitEnabled = highMemoryLimitEnabled
            )

            engine.stitchImages(imageUris, options, progressCallback)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logManager.error(TAG, "叠加拼接过程中出错", e)
            StitchResult.ErrorResult(StitchFailure.Unexpected(e))
        }
    }
}
