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
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.common.MemoryLimitCalculator
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.rerokutosei.chimera.utils.stitch.layout.ImageDimensions
import com.rerokutosei.chimera.utils.stitch.layout.LayoutMode
import com.rerokutosei.chimera.utils.stitch.layout.LayoutOptions
import com.rerokutosei.chimera.utils.stitch.layout.LayoutOrientation
import com.rerokutosei.chimera.utils.stitch.layout.LayoutScaleMode
import com.rerokutosei.chimera.utils.stitch.layout.StitchLayout
import com.rerokutosei.chimera.utils.stitch.layout.StitchLayoutCalculator

abstract class BaseStitchingStrategy(
    context: Context,
    tag: String
) : StitchingStrategy {

    protected val logManager: LogManager = LogManager.getInstance(context)

    protected val memoryLimitCalculator by lazy {
        MemoryLimitCalculator(
            context,
            logManager,
            tag
        )
    }

    protected fun scaleBitmapsForLayout(
        bitmaps: List<Bitmap>,
        widthScale: WidthScale,
        orientation: StitchOrientation,
        tag: String
    ): List<Bitmap> {
        val targetDimensions = StitchLayoutCalculator.scale(
            images = bitmaps.map { ImageDimensions(it.width, it.height) },
            orientation = orientation.toLayoutOrientation(),
            scaleMode = widthScale.toLayoutScaleMode()
        )
        if (bitmaps.indices.all { index ->
                bitmaps[index].width == targetDimensions[index].width &&
                    bitmaps[index].height == targetDimensions[index].height
            }) {
            return bitmaps
        }

        return bitmaps.mapIndexed { index, bitmap ->
            val target = targetDimensions[index]
            if (bitmap.width == target.width && bitmap.height == target.height) {
                bitmap
            } else {
                val scaledBitmap = bitmap.scale(target.width, target.height, true)
                logManager.debug(tag) {
                    "图片缩放: ${bitmap.width}x${bitmap.height} -> ${scaledBitmap.width}x${scaledBitmap.height}"
                }
                scaledBitmap
            }
        }
    }

    protected fun calculateLayout(
        bitmaps: List<Bitmap>,
        orientation: StitchOrientation,
        mode: LayoutMode,
        spacing: Int = 0,
        overlayRatio: Int = 0
    ): StitchLayout = requireNotNull(
        StitchLayoutCalculator.calculate(
            images = bitmaps.map { ImageDimensions(it.width, it.height) },
            options = LayoutOptions(
                orientation = orientation.toLayoutOrientation(),
                mode = mode,
                spacing = spacing,
                overlayRatio = overlayRatio
            )
        )
    )

    protected fun recycleScaledIntermediates(
        originalBitmaps: List<Bitmap>,
        processedBitmaps: List<Bitmap>,
        exclude: Bitmap? = null,
        tag: String
    ) {
        if (processedBitmaps === originalBitmaps) return

        processedBitmaps.forEach { bitmap ->
            val isOriginal = originalBitmaps.any { original -> original === bitmap }
            if (!isOriginal && bitmap !== exclude && !bitmap.isRecycled) {
                val width = bitmap.width
                val height = bitmap.height
                bitmap.recycle()
                logManager.debug(tag) { "回收缩放中间位图: ${width}x${height}" }
            }
        }
    }
}

private fun StitchOrientation.toLayoutOrientation(): LayoutOrientation = when (this) {
    StitchOrientation.VERTICAL -> LayoutOrientation.VERTICAL
    StitchOrientation.HORIZONTAL -> LayoutOrientation.HORIZONTAL
}

private fun WidthScale.toLayoutScaleMode(): LayoutScaleMode = when (this) {
    WidthScale.NONE -> LayoutScaleMode.NONE
    WidthScale.MIN_WIDTH -> LayoutScaleMode.MIN
    WidthScale.MAX_WIDTH -> LayoutScaleMode.MAX
}
