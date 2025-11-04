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

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.common.MemoryLimitCalculator

/**
 * 自适应图片显示组件，根据图片大小自动选择合适的显示方式
 */
@Composable
fun AdaptiveImageDisplay(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageSettingsManager = ImageSettingsManager.getInstance(context)
    val logManager = LogManager.getInstance(context)
    val memoryLimitCalculator = remember {
        MemoryLimitCalculator(context, imageSettingsManager, logManager, "AdaptiveImageDisplay")
    }
    
    val largeImagePreviewer = remember { 
        LargeImagePreviewer(context, memoryLimitCalculator, logManager)
    }
    
    if (largeImagePreviewer.shouldUseTiledLoading(bitmap)) {
        logManager.debug("AdaptiveImageDisplay", "使用分块加载显示大图: ${bitmap.width}x${bitmap.height}")
        AndroidView(
            factory = { ctx ->
                SubsamplingScaleImageView(ctx).apply {
                    setImage(ImageSource.bitmap(bitmap))
                    maxScale = 10f
                    minScale = 0.1f
                }
            },
            modifier = modifier.fillMaxSize()
        )
    } else {
        logManager.debug("AdaptiveImageDisplay", "使用普通方式显示图片: ${bitmap.width}x${bitmap.height}")
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * 大图预览器，根据图片大小自动选择合适的预览方式
 */
private class LargeImagePreviewer(
    private val context: Context,
    private val memoryLimitCalculator: MemoryLimitCalculator,
    private val logManager: LogManager
) {
    companion object {
        private const val TAG = "LargeImagePreviewer"
        private const val FORCE_TILED_LOADING_THRESHOLD = 50 * 1024 * 1024L // 50MB
        // 大图尺寸阈值
        private const val LARGE_IMAGE_DIMENSION_THRESHOLD = 3001
    }

    /**
     * 检查图片是否过大需要使用分块加载
     */
    fun shouldUseTiledLoading(bitmap: Bitmap): Boolean {
        val maxDisplaySize = memoryLimitCalculator.calculateMaxImageSize()
        val bitmapSize = bitmap.allocationByteCount.toLong()
        
        // 果图像尺寸或体积过大(超过预设值)，不管什么情况，都使用分块加载方式显示
        val forceTiledLoading = bitmapSize > FORCE_TILED_LOADING_THRESHOLD
        // 如果图片任意一边大于3001px，也使用分块加载
        val largeDimension = bitmap.width > LARGE_IMAGE_DIMENSION_THRESHOLD || bitmap.height > LARGE_IMAGE_DIMENSION_THRESHOLD
        
        logManager.debug(TAG, "检查图片是否需要分块加载: bitmapSize=${bitmapSize}, maxDisplaySize=${maxDisplaySize}, forceTiledLoading=$forceTiledLoading, largeDimension=$largeDimension")
        return bitmapSize > maxDisplaySize || forceTiledLoading || largeDimension
    }
}