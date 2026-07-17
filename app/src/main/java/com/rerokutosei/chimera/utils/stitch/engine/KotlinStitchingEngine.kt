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

package com.rerokutosei.chimera.utils.stitch.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.rerokutosei.chimera.data.local.StitchSettingsManager
import com.rerokutosei.chimera.domain.error.StitchFailure
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.rerokutosei.chimera.utils.stitch.StitchResult
import com.rerokutosei.chimera.utils.stitch.strategy.DirectStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.OverlayStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingOptions
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Kotlin拼接引擎实现
 */
class KotlinStitchingEngine(
    private val context: Context,
    private val bitmapLoader: BitmapLoader
) {

    private val stitchSettingsManager = StitchSettingsManager.getInstance(context)
    private val logManager = LogManager.getInstance(context)

    suspend fun stitchImages(
        imageUris: List<Uri>,
        options: StitchingOptions,
        progressCallback: (progress: Int) -> Unit
    ): StitchResult {
        return withContext(Dispatchers.IO) {
            var bitmaps: List<Bitmap> = emptyList()
            try {
                val multiThreadEnabled = stitchSettingsManager.getMultiThreadEnabledFlow().first()

                bitmaps = if (multiThreadEnabled) {
                    logManager.debug("KotlinStitchingEngine", "多线程加速已开启，并行加载图片")
                    val deferredBitmaps = coroutineScope {
                        imageUris.map { uri ->
                            async {
                                bitmapLoader.loadBitmapFromUri(uri)
                            }
                        }
                    }
                    val loadedBitmaps = deferredBitmaps.awaitAll()
                    progressCallback(50)
                    loadedBitmaps.filterNotNull()
                } else {
                    logManager.debug("KotlinStitchingEngine", "多线程加速已关闭，串行加载图片")
                    val loadedBitmaps = mutableListOf<Bitmap>()
                    imageUris.forEachIndexed { index, uri ->
                        val bitmap = bitmapLoader.loadBitmapFromUri(uri)
                        if (bitmap != null) {
                            loadedBitmaps.add(bitmap)
                        }
                        progressCallback((index + 1) * 50 / imageUris.size)
                    }
                    loadedBitmaps
                }
                
                if (bitmaps.size != imageUris.size) {
                    logManager.error("KotlinStitchingEngine", "一张或多张图片加载失败")
                    bitmapLoader.recycleBitmaps(bitmaps)
                    return@withContext StitchResult.ErrorResult(StitchFailure.DecodeFailed)
                }

                val strategy = createStitcher(options.orientation, options.isOverlayEnabled)

                val result = strategy.stitch(bitmaps, options)
                progressCallback(80)

                when (result) {
                    is StitchResult.BitmapResult -> {
                        logManager.debug("KotlinStitchingEngine", "拼接完成，返回Bitmap进行预览")
                        bitmapLoader.recycleBitmaps(bitmaps.filter { it !== result.bitmap })
                        progressCallback(100)
                        result
                    }
                    is StitchResult.ErrorResult -> {
                        bitmapLoader.recycleBitmaps(bitmaps)
                        result
                    }
                }
            } catch (e: CancellationException) {
                bitmapLoader.recycleBitmaps(bitmaps)
                throw e
            } catch (e: Exception) {
                bitmapLoader.recycleBitmaps(bitmaps)
                logManager.error("KotlinStitchingEngine", "拼接过程出错", e)
                StitchResult.ErrorResult(StitchFailure.Unexpected(e))
            }
        }
    }

    /**
     * 根据拼接方向和叠加开关创建对应的拼接策略
     */
    private fun createStitcher(orientation: StitchOrientation, isOverlay: Boolean): StitchingStrategy {
        return when {
            isOverlay -> OverlayStitchingStrategy(context)
            orientation == StitchOrientation.VERTICAL -> DirectStitchingStrategy(isVertical = true, context = context)
            else -> DirectStitchingStrategy(isVertical = false, context = context)
        }
    }
}
