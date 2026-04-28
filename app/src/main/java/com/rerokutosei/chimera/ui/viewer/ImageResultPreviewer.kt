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

package com.rerokutosei.chimera.ui.viewer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb

private fun chooseGridLineColor(bitmap: Bitmap): Int {
    val thumb = Bitmap.createScaledBitmap(bitmap, 16, 16, true)
    var totalBrightness = 0L
    for (y in 0 until 16) {
        for (x in 0 until 16) {
            val pixel = thumb.getPixel(x, y)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b).toLong()
        }
    }
    thumb.recycle()
    val avgBrightness = totalBrightness / 256
    return if (avgBrightness < 128) Color.WHITE else Color.BLACK
}

@Composable
fun ImageResultPreviewer(
    source: PreviewSource,
    modifier: Modifier = Modifier
) {
    when (source) {
        is PreviewSource.FromBitmap -> {
            AdaptiveImageDisplay(bitmap = source.bitmap, modifier = modifier)
        }
        is PreviewSource.FromBitmapWithGrid -> {
            val displayBitmap = remember(source.bitmap, source.cols, source.rows) {
                val copy = source.bitmap.copy(source.bitmap.config ?: Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(copy)
                val gridColor = chooseGridLineColor(source.bitmap)
                val paint = Paint().apply {
                    color = gridColor
                    strokeWidth = 20f
                    style = Paint.Style.STROKE
                }
                val cellW = copy.width / source.cols
                val cellH = copy.height / source.rows
                for (i in 1 until source.cols) {
                    val x = i * cellW
                    canvas.drawLine(x.toFloat(), 0f, x.toFloat(), copy.height.toFloat(), paint)
                }
                for (i in 1 until source.rows) {
                    val y = i * cellH
                    canvas.drawLine(0f, y.toFloat(), copy.width.toFloat(), y.toFloat(), paint)
                }
                canvas.setBitmap(null)
                copy
            }
            AdaptiveImageDisplay(bitmap = displayBitmap, modifier = modifier)
        }
    }
}