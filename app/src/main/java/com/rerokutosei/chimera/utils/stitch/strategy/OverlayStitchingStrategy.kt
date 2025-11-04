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
import com.rerokutosei.chimera.utils.stitch.StitchOrientation

/**
 * 叠加拼接策略实现
 */
class OverlayStitchingStrategy(context: Context) : BaseStitchingStrategy(context, TAG) {
    
    companion object {
        private const val TAG = "OverlayStitchingStrategy"
    }
    
    private val MAX_IMAGE_SIZE by lazy { memoryLimitCalculator.calculateMaxImageSize() }
    
    override suspend fun stitch(bitmaps: List<Bitmap>, options: StitchingOptions): Bitmap? {
        if (bitmaps.isEmpty()) {
            logManager.error(TAG, "图片列表为空")
            return null
        }

        try {
            val processedBitmaps = when (options.orientation) {
                StitchOrientation.VERTICAL -> {
                    when (options.widthScale) {
                        WidthScale.MAX_WIDTH -> {
                            logManager.debug(TAG, "纵向叠加 - 缩放到最大宽度")
                            scaleToMaxWidth(bitmaps, TAG)
                        }
                        else -> {
                            logManager.debug(TAG, "纵向叠加 - 缩放到最小宽度")
                            scaleToMinWidth(bitmaps, TAG)
                        }
                    }
                }
                StitchOrientation.HORIZONTAL -> {
                    when (options.widthScale) {
                        WidthScale.MAX_WIDTH -> {
                            logManager.debug(TAG, "横向叠加 - 缩放到最大高度")
                            scaleToMaxHeight(bitmaps, TAG)
                        }
                        else -> {
                            logManager.debug(TAG, "横向叠加 - 缩放到最小高度")
                            scaleToMinHeight(bitmaps, TAG)
                        }
                    }
                }
            }

            val (totalWidth, totalHeight) = when (options.orientation) {
                StitchOrientation.VERTICAL -> {
                    // 计算第一张图片的高度（完整显示）
                    val firstImageHeight = processedBitmaps[0].height

                    // 计算每张图片的叠加区域高度（基于第一张图片的高度和比例）
                    // 将overlayRatio从分母转换为分子，即用户设置的百分比
                    val overlayHeight = (firstImageHeight * options.overlayRatio / 100).coerceAtLeast(1)
                    logManager.debug(TAG, "第一张图片高度: $firstImageHeight, 叠加区域高度: $overlayHeight")

                    // 计算总高度：第一张图片完整高度 + 后续每张图片的叠加区域高度
                    val height = firstImageHeight + (processedBitmaps.size - 1) * overlayHeight
                    val width = processedBitmaps.maxOf { it.width }
                    logManager.debug(TAG, "纵向叠加结果尺寸: ${width}x${height}")
                    Pair(width, height)
                }
                StitchOrientation.HORIZONTAL -> {
                    // 计算第一张图片的宽度（完整显示）
                    val firstImageWidth = processedBitmaps[0].width

                    // 计算每张图片的叠加区域宽度（基于第一张图片的宽度和比例）
                    // 将overlayRatio从分母转换为分子，即用户设置的百分比
                    val overlayWidth = (firstImageWidth * options.overlayRatio / 100).coerceAtLeast(1)
                    logManager.debug(TAG, "第一张图片宽度: $firstImageWidth, 叠加区域宽度: $overlayWidth")

                    // 计算总宽度：第一张图片完整宽度 + 后续每张图片的叠加区域宽度
                    val width = firstImageWidth + (processedBitmaps.size - 1) * overlayWidth
                    val height = processedBitmaps.maxOf { it.height }
                    logManager.debug(TAG, "横向叠加结果尺寸: ${width}x${height}")
                    Pair(width, height)
                }
            }

            // 检查内存限制（最大支持32MB的图片）
            val estimatedSize = totalWidth.toLong() * totalHeight.toLong() * 4 // ARGB_8888
            if (estimatedSize > MAX_IMAGE_SIZE) {
                logManager.error(TAG, "拼接结果图片过大: ${estimatedSize / (1024 * 1024)}MB，超过限制: ${MAX_IMAGE_SIZE / (1024 * 1024)}MB")
                return null
            }

            // 根据用户设置的输出图片格式选择合适的格式
            // 如果输出格式为PNG或WEBP，使用ARGB_8888格式
            // 如果输出格式为JPEG，使用RGB_565格式
            val outputFormat = getCurrentOutputFormat()
            val config = when (outputFormat) {
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
            
            // 黑色画笔用于绘制黑色区域
            val blackPaint = Paint().apply { 
                this.color = Color.BLACK
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
                    
                    // 计算每张图片的叠加区域高度（基于第一张图片的高度和比例）
                    val overlayHeight = (firstImageHeight * options.overlayRatio / 100).coerceAtLeast(1)
                    
                    // 绘制后续图片的叠加区域
                    var currentY = firstImageHeight
                    for (i in 1 until processedBitmaps.size) {
                        logManager.debug(TAG, "处理第${i+1}张图片的叠加区域")
                        
                        // 计算叠加区域在当前图片中的位置（底部）
                        val overlayStartY = processedBitmaps[i].height - overlayHeight
                        
                        // 在结果图片上绘制黑色背景
                        canvas.drawRect(
                            0f, 
                            currentY.toFloat(), 
                            totalWidth.toFloat(), 
                            (currentY + overlayHeight).toFloat(), 
                            blackPaint
                        )
                        
                        // 从当前图片中裁剪出叠加区域并绘制到结果图片
                        val overlayBitmap = Bitmap.createBitmap(
                            processedBitmaps[i], 
                            0, 
                            overlayStartY, 
                            processedBitmaps[i].width, 
                            overlayHeight
                        )
                        canvas.drawBitmap(overlayBitmap, 0f, currentY.toFloat(), paint)

                        if (!overlayBitmap.isRecycled) {
                            overlayBitmap.recycle()
                        }
                        
                        currentY += overlayHeight
                    }
                }
                StitchOrientation.HORIZONTAL -> {
                    // 绘制第一张图片（完整显示）
                    logManager.debug(TAG, "绘制第一张图片")
                    canvas.drawBitmap(processedBitmaps[0], 0f, 0f, paint)

                    val firstImageWidth = processedBitmaps[0].width
                    
                    // 计算每张图片的叠加区域宽度（基于第一张图片的宽度和比例）
                    val overlayWidth = (firstImageWidth * options.overlayRatio / 100).coerceAtLeast(1)

                    var currentX = firstImageWidth
                    for (i in 1 until processedBitmaps.size) {
                        logManager.debug(TAG, "处理第${i+1}张图片的叠加区域")
                        
                        // 计算叠加区域在当前图片中的位置（右侧）
                        val overlayStartX = processedBitmaps[i].width - overlayWidth

                        canvas.drawRect(
                            currentX.toFloat(), 
                            0f, 
                            (currentX + overlayWidth).toFloat(), 
                            totalHeight.toFloat(), 
                            blackPaint
                        )

                        val overlayBitmap = Bitmap.createBitmap(
                            processedBitmaps[i], 
                            overlayStartX, 
                            0, 
                            overlayWidth, 
                            processedBitmaps[i].height
                        )
                        canvas.drawBitmap(overlayBitmap, currentX.toFloat(), 0f, paint)

                        if (!overlayBitmap.isRecycled) {
                            overlayBitmap.recycle()
                        }
                        
                        currentX += overlayWidth
                    }
                }
            }

            logManager.debug(TAG, "叠加拼接模式拼接完成")

            if (processedBitmaps !== bitmaps) {
                bitmaps.forEach { bitmap ->
                    if (!bitmap.isRecycled && bitmap != result) {
                        bitmap.recycle()
                    }
                }
            }
            
            return result
        } catch (e: Exception) {
            logManager.error(TAG, "叠加拼接模式拼接出错", e)
            return null
        }
    }
}
