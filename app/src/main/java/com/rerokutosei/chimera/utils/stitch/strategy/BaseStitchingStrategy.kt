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

package com.rerokutosei.chimera.utils.stitch.strategy

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.common.MemoryLimitCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

abstract class BaseStitchingStrategy(
    context: Context,
    tag: String
) : StitchingStrategy {

    protected val logManager: LogManager = LogManager.getInstance(context)
    protected val imageSettingsManager: ImageSettingsManager = ImageSettingsManager.getInstance(context)

    protected val memoryLimitCalculator by lazy {
        MemoryLimitCalculator(
            context,
            imageSettingsManager,
            logManager,
            tag
        )
    }

    protected fun scaleToMaxWidth(bitmaps: List<Bitmap>, tag: String): List<Bitmap> {
        if (bitmaps.isEmpty()) return bitmaps
        val maxWidth = bitmaps.maxOf { it.width }
        logManager.debug(tag, "缩放到最大宽度: $maxWidth")
        return scaleBitmaps(
            bitmaps,
            maxWidth,
            { bitmap, targetSize -> bitmap.width == targetSize },
            { bitmap, targetSize ->
                val scale = targetSize.toFloat() / bitmap.width.toFloat()
                val newHeight = (bitmap.height * scale).toInt()
                Pair(targetSize, newHeight)
            },
            tag
        )
    }

    protected fun scaleToMinWidth(bitmaps: List<Bitmap>, tag: String): List<Bitmap> {
        if (bitmaps.isEmpty()) return bitmaps
        val minWidth = bitmaps.minOf { it.width }
        logManager.debug(tag, "缩放到最小宽度: $minWidth")
        return scaleBitmaps(
            bitmaps,
            minWidth,
            { bitmap, targetSize -> bitmap.width == targetSize },
            { bitmap, targetSize ->
                val scale = targetSize.toFloat() / bitmap.width.toFloat()
                val newHeight = (bitmap.height * scale).toInt()
                Pair(targetSize, newHeight)
            },
            tag
        )
    }

    protected fun scaleToMaxHeight(bitmaps: List<Bitmap>, tag: String): List<Bitmap> {
        if (bitmaps.isEmpty()) return bitmaps
        val maxHeight = bitmaps.maxOf { it.height }
        logManager.debug(tag, "缩放到最大高度: $maxHeight")
        return scaleBitmaps(
            bitmaps,
            maxHeight,
            { bitmap, targetSize -> bitmap.height == targetSize },
            { bitmap, targetSize ->
                val scale = targetSize.toFloat() / bitmap.height.toFloat()
                val newWidth = (bitmap.width * scale).toInt()
                Pair(newWidth, targetSize)
            },
            tag
        )
    }
    
    protected fun scaleToMinHeight(bitmaps: List<Bitmap>, tag: String): List<Bitmap> {
        if (bitmaps.isEmpty()) return bitmaps
        val minHeight = bitmaps.minOf { it.height }
        logManager.debug(tag, "缩放到最小高度: $minHeight")
        return scaleBitmaps(
            bitmaps,
            minHeight,
            { bitmap, targetSize -> bitmap.height == targetSize },
            { bitmap, targetSize ->
                val scale = targetSize.toFloat() / bitmap.height.toFloat()
                val newWidth = (bitmap.width * scale).toInt()
                Pair(newWidth, targetSize)
            },
            tag
        )
    }

    private fun scaleBitmaps(
        bitmaps: List<Bitmap>,
        targetSize: Int,
        isMatch: (Bitmap, Int) -> Boolean,
        calculateDimensions: (Bitmap, Int) -> Pair<Int, Int>,
        tag: String
    ): List<Bitmap> {
        return bitmaps.map { bitmap ->
            if (isMatch(bitmap, targetSize)) {
                bitmap
            } else {
                val (newWidth, newHeight) = calculateDimensions(bitmap, targetSize)
                val scaledBitmap = bitmap.scale(newWidth, newHeight, true)
                logManager.debug(tag, "图片缩放: ${bitmap.width}x${bitmap.height} -> ${scaledBitmap.width}x${scaledBitmap.height}")
                scaledBitmap
            }
        }
    }

    protected fun getCurrentOutputFormat(): Int = runBlocking {
        imageSettingsManager.getOutputImageFormatFlow().first()
    }
}
