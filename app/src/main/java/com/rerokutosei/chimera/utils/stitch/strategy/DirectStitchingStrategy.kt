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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import com.rerokutosei.chimera.ui.main.WidthScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 直接拼接策略实现（垂直和水平拼接）
 */
class DirectStitchingStrategy(
    private val isVertical: Boolean,
    context: Context
) : BaseStitchingStrategy(context, TAG) {
    
    companion object {
        private const val TAG = "DirectStitchingStrategy"
    }
    
    override suspend fun stitch(bitmaps: List<Bitmap>, options: StitchingOptions): Bitmap? {
        logManager.debug(TAG, "开始拼接，图片数量: ${bitmaps.size}，方向: ${if (isVertical) "垂直" else "水平"}，宽度缩放: ${options.widthScale}")

        val processedBitmaps = when (options.widthScale) {
            WidthScale.MAX_WIDTH -> {
                logManager.debug(TAG, "应用最大宽度缩放")
                if (isVertical) {
                    scaleToMaxWidth(bitmaps, TAG)
                } else {
                    scaleToMaxHeight(bitmaps, TAG)
                }
            }
            WidthScale.MIN_WIDTH -> {
                logManager.debug(TAG, "应用最小宽度缩放")
                if (isVertical) {
                    scaleToMinWidth(bitmaps, TAG)
                } else {
                    scaleToMinHeight(bitmaps, TAG)
                }
            }
            else -> {
                logManager.debug(TAG, "不应用宽度缩放")
                bitmaps // WidthScale.NONE，不进行缩放
            }
        }
        
        return withContext(Dispatchers.Default) {
            if (isVertical) {
                stitchVertically(processedBitmaps, options.spacing)
            } else {
                stitchHorizontally(processedBitmaps, options.spacing)
            }
        }
    }
    
    /**
     * 垂直拼接图片
     * @param bitmaps 要拼接的位图列表
     * @param spacing 图片间隔（像素）
     */
    private fun stitchVertically(bitmaps: List<Bitmap>, spacing: Int): Bitmap? {
        return stitchImages(bitmaps, spacing, true)
    }
    
    /**
     * 水平拼接图片
     * @param bitmaps 要拼接的位图列表
     * @param spacing 图片间隔（像素）
     */
    private fun stitchHorizontally(bitmaps: List<Bitmap>, spacing: Int): Bitmap? {
        return stitchImages(bitmaps, spacing, false)
    }
    
    /**
     * 通用拼接方法
     * @param bitmaps 要拼接的位图列表
     * @param spacing 图片间隔（像素）
     * @param isVertical 是否为垂直拼接
     */
    private fun stitchImages(bitmaps: List<Bitmap>, spacing: Int, isVertical: Boolean): Bitmap? {
        logManager.debug(TAG, "开始${if (isVertical) "垂直" else "水平"}拼接，图片数量: ${bitmaps.size}，间隔: $spacing")
        
        val (totalMajor, totalMinor) = if (isVertical) {
            bitmaps.sumOf { it.height } + (bitmaps.size - 1) * spacing to bitmaps.maxOf { it.width }
        } else {
            bitmaps.sumOf { it.width } + (bitmaps.size - 1) * spacing to bitmaps.maxOf { it.height }
        }

        logManager.debug(TAG, "${if (isVertical) "垂直" else "水平"}拼接：总${if (isVertical) "高度" else "宽度"}=$totalMajor, 最大${if (isVertical) "宽度" else "高度"}=$totalMinor")

        // 检查内存限制
        val estimatedSize = totalMajor.toLong() * totalMinor.toLong() * 4 // ARGB_8888
        val maxImageSize = memoryLimitCalculator.calculateMaxImageSize()
        
        if (estimatedSize > maxImageSize) {
            logManager.error(TAG, "拼接结果图片过大: ${estimatedSize / (1024 * 1024)}MB，超过限制: ${maxImageSize / (1024 * 1024)}MB")
            return null
        }

        logManager.debug(TAG, "创建结果位图：${if (isVertical) totalMinor else totalMajor}x${if (isVertical) totalMajor else totalMinor}")
        return try {
            // 根据用户设置的输出图片格式和图片透明度需求选择合适的格式
            // 如果输出格式为PNG或WEBP，且图片中至少有一张使用了ARGB_8888格式时，结果图片才会使用ARGB_8888格式
            // 如果输出格式为JPEG，一律不使用ARGB_8888格式
            val outputFormat = getCurrentOutputFormat()
            val hasAlpha = bitmaps.any { it.config == Bitmap.Config.ARGB_8888 }
            val config = when {
                (outputFormat == 0 || outputFormat == 2) && hasAlpha -> Bitmap.Config.ARGB_8888 // PNG或WEBP且有透明度
                else -> Bitmap.Config.RGB_565 // JPEG或无透明度需求
            }
            
            val result = if (isVertical) {
                createBitmap(totalMinor, totalMajor, config)
            } else {
                createBitmap(totalMajor, totalMinor, config)
            }
            logManager.debug(TAG, "结果位图创建成功：${result.width}x${result.height}，格式：$config")

            val canvas = Canvas(result)
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                isDither = true
            }

            // 创建黑色画笔用于绘制间隔
            val blackPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }

            try {
                var currentMajor = 0
                for ((index, bitmap) in bitmaps.withIndex()) {
                    val (x, y) = if (isVertical) {
                        val xPos = (totalMinor - bitmap.width) / 2
                        xPos to currentMajor
                    } else {
                        val yPos = (totalMinor - bitmap.height) / 2
                        currentMajor to yPos
                    }
                    
                    logManager.debug(TAG, "绘制图片 $index：位置($x, $y), 尺寸(${bitmap.width}x${bitmap.height})")
                    canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)
                    
                    // 如果不是最后一张图片且有间隔，则绘制黑色间隔
                    if (index < bitmaps.size - 1 && spacing > 0) {
                        if (isVertical) {
                            currentMajor += bitmap.height
                            logManager.debug(TAG, "绘制间隔：位置(0, $currentMajor), 尺寸(${totalMinor}x${spacing})")
                            canvas.drawRect(0f, currentMajor.toFloat(), totalMinor.toFloat(), (currentMajor + spacing).toFloat(), blackPaint)
                            currentMajor += spacing
                        } else {
                            currentMajor += bitmap.width
                            logManager.debug(TAG, "绘制间隔：位置($currentMajor, 0), 尺寸(${spacing}x${totalMinor})")
                            canvas.drawRect(currentMajor.toFloat(), 0f, (currentMajor + spacing).toFloat(), totalMinor.toFloat(), blackPaint)
                            currentMajor += spacing
                        }
                    } else {
                        currentMajor += if (isVertical) bitmap.height else bitmap.width
                    }
                }
            } finally {
                canvas.setBitmap(null)
                paint.reset()
                blackPaint.reset()
            }

            logManager.debug(TAG, "${if (isVertical) "垂直" else "水平"}拼接完成，结果位图尺寸：${result.width}x${result.height}")
            logManager.debug(TAG, "结果位图内存大小: ${result.allocationByteCount} bytes")
            result
        } catch (e: OutOfMemoryError) {
            logManager.error(TAG, "内存不足，无法创建结果位图", e)
            null
        } catch (e: Exception) {
            logManager.error(TAG, "创建结果位图时发生错误", e)
            null
        }
    }
}
