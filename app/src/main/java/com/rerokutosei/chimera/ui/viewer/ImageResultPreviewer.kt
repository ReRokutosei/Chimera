/*
 * Chimera is an image stitching tool
 * Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the GNU General Public License v3.0 (the "License");
 * you may redistribute and/or modify this program under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3 of the License.
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.graphics.get
import androidx.core.graphics.scale

private fun chooseGridLineColor(bitmap: Bitmap): Int {
    val thumb = bitmap.scale(16, 16)
    var totalBrightness = 0L
    for (y in 0 until 16) {
        for (x in 0 until 16) {
            val pixel = thumb[x, y]
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
                val colSegs = com.rerokutosei.chimera.utils.image.ImageSplitter.computeSegments(copy.width, source.cols)
                val rowSegs = com.rerokutosei.chimera.utils.image.ImageSplitter.computeSegments(copy.height, source.rows)
                for (i in 1 until source.cols) {
                    val x = colSegs[i].start
                    canvas.drawLine(x.toFloat(), 0f, x.toFloat(), copy.height.toFloat(), paint)
                }
                for (i in 1 until source.rows) {
                    val y = rowSegs[i].start
                    canvas.drawLine(0f, y.toFloat(), copy.width.toFloat(), y.toFloat(), paint)
                }

                // Draw sequence index badges (01, 02, 03, ...)
                val badgeHeight = (copy.width.coerceAtMost(copy.height) * 0.05f).coerceIn(36f, 120f)
                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = badgeHeight * 0.62f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
                val bgPaint = Paint().apply {
                    color = Color.argb(180, 0, 0, 0)
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }

                for (r in 0 until source.rows) {
                    for (c in 0 until source.cols) {
                        val segX = colSegs[c]
                        val segY = rowSegs[r]
                        val idx = r * source.cols + c + 1
                        val badgeText = "%02d".format(idx)

                        val pad = badgeHeight * 0.25f
                        val bx = segX.start + pad
                        val by = segY.start + pad
                        val bw = badgeHeight * 1.3f
                        val bh = badgeHeight
                        val rect = android.graphics.RectF(bx, by, bx + bw, by + bh)
                        canvas.drawRoundRect(rect, 10f, 10f, bgPaint)

                        val textY = by + (bh / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                        canvas.drawText(badgeText, bx + (bw / 2f), textY, textPaint)
                    }
                }

                canvas.setBitmap(null)
                copy
            }
            DisposableEffect(displayBitmap) {
                onDispose {
                    if (!displayBitmap.isRecycled) displayBitmap.recycle()
                }
            }
            AdaptiveImageDisplay(bitmap = displayBitmap, modifier = modifier)
        }
    }
}
