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
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.rerokutosei.chimera.utils.stitch.layout.LayoutMode

/**
 * 叠加拼接策略实现
 */
class OverlayStitchingStrategy(context: Context) : BaseStitchingStrategy(context, TAG) {
    
    companion object {
        private const val TAG = "OverlayStitchingStrategy"
    }
    
    override suspend fun stitch(bitmaps: List<Bitmap>, options: StitchingOptions): Bitmap? {
        if (bitmaps.isEmpty()) {
            logManager.error(TAG, "图片列表为空")
            return null
        }

        val processedBitmaps = scaleBitmapsForLayout(bitmaps, options.widthScale, options.orientation, TAG)

        var resultBitmap: Bitmap? = null
        try {
            val layout = calculateLayout(
                bitmaps = processedBitmaps,
                orientation = options.orientation,
                mode = LayoutMode.OVERLAY,
                overlayRatio = options.overlayRatio
            )
            val overlaySteps = layout.overlaySteps

            // 检查内存限制（最大支持32MB的图片）
            val estimatedSize = layout.width * layout.height * 4 // ARGB_8888
            val maxImageSize = memoryLimitCalculator.calculateMaxImageSize(options.highMemoryLimitEnabled)
            if (estimatedSize > maxImageSize || layout.width > Int.MAX_VALUE || layout.height > Int.MAX_VALUE) {
                logManager.error(TAG, "拼接结果图片过大: ${estimatedSize / (1024 * 1024)}MB，超过限制: ${maxImageSize / (1024 * 1024)}MB")
                return null
            }
            val totalWidth = layout.width.toInt()
            val totalHeight = layout.height.toInt()

            // 根据用户设置的输出图片格式选择合适的格式
            // 如果输出格式为PNG或WEBP，使用ARGB_8888格式
            // 如果输出格式为JPEG，使用RGB_565格式
            val config = when (options.outputFormat) {
                0, 2 -> Bitmap.Config.ARGB_8888 // PNG或WEBP
                else -> Bitmap.Config.RGB_565 // JPEG
            }
            
            // 创建结果位图
            val result = createBitmap(totalWidth, totalHeight, config)
            logManager.debug(TAG, "创建结果位图成功，格式：$config")
            
            val canvas = Canvas(result)
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                isDither = true
            }
            
            // 画笔用于绘制间隔区域
            val spacingPaint = Paint().apply { 
                this.color = options.spacingColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            when (options.orientation) {
                StitchOrientation.VERTICAL -> {
                    // 绘制第一张图片（完整显示）
                    logManager.debug(TAG, "绘制第一张图片")
                    canvas.drawBitmap(processedBitmaps[0], 0f, 0f, paint)
                    
                    // 计算第一张图片的高度（完整显示）
                    val firstImageHeight = processedBitmaps[0].height

                    // 绘制后续图片的叠加区域
                    var currentY = firstImageHeight
                    for (i in 1 until processedBitmaps.size) {
                        logManager.debug(TAG) { "处理第${i + 1}张图片的叠加区域" }

                        val overlayHeight = overlaySteps[i - 1]
                        // 计算叠加区域在当前图片中的位置（底部）
                        val overlayStartY = (processedBitmaps[i].height - overlayHeight).coerceAtLeast(0)

                        // 在结果图片上绘制黑色背景
                        canvas.drawRect(
                            0f, 
                            currentY.toFloat(), 
                            totalWidth.toFloat(), 
                            (currentY + overlayHeight).toFloat(), 
                            spacingPaint
                        )
                        
                        // 从当前图片中裁剪出叠加区域并绘制到结果图片
                        val overlayBitmap = Bitmap.createBitmap(
                            processedBitmaps[i], 
                            0, 
                            overlayStartY, 
                            processedBitmaps[i].width, 
                            overlayHeight
                        )
                        try {
                            canvas.drawBitmap(overlayBitmap, 0f, currentY.toFloat(), paint)
                        } finally {
                            if (!overlayBitmap.isRecycled) {
                                overlayBitmap.recycle()
                            }
                        }
                        
                        currentY += overlayHeight
                    }
                }
                StitchOrientation.HORIZONTAL -> {
                    // 绘制第一张图片（完整显示）
                    logManager.debug(TAG, "绘制第一张图片")
                    canvas.drawBitmap(processedBitmaps[0], 0f, 0f, paint)

                    val firstImageWidth = processedBitmaps[0].width

                    var currentX = firstImageWidth
                    for (i in 1 until processedBitmaps.size) {
                        logManager.debug(TAG) { "处理第${i + 1}张图片的叠加区域" }

                        val overlayWidth = overlaySteps[i - 1]
                        // 计算叠加区域在当前图片中的位置（右侧）
                        val overlayStartX = (processedBitmaps[i].width - overlayWidth).coerceAtLeast(0)

                        canvas.drawRect(
                            currentX.toFloat(), 
                            0f, 
                            (currentX + overlayWidth).toFloat(), 
                            totalHeight.toFloat(), 
                            spacingPaint
                        )

                        val overlayBitmap = Bitmap.createBitmap(
                            processedBitmaps[i], 
                            overlayStartX, 
                            0, 
                            overlayWidth, 
                            processedBitmaps[i].height
                        )
                        try {
                            canvas.drawBitmap(overlayBitmap, currentX.toFloat(), 0f, paint)
                        } finally {
                            if (!overlayBitmap.isRecycled) {
                                overlayBitmap.recycle()
                            }
                        }
                        
                        currentX += overlayWidth
                    }
                }
            }

            logManager.debug(TAG, "叠加拼接模式拼接完成")
            
            canvas.setBitmap(null)
            paint.reset()
            spacingPaint.reset()
            
            resultBitmap = result
            return result
        } catch (e: Exception) {
            logManager.error(TAG, "叠加拼接模式拼接出错", e)
            return null
        } finally {
            recycleScaledIntermediates(
                originalBitmaps = bitmaps,
                processedBitmaps = processedBitmaps,
                exclude = resultBitmap,
                tag = TAG
            )
        }
    }
}
