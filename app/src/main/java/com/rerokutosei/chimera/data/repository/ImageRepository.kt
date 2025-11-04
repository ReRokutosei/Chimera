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

package com.rerokutosei.chimera.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import coil.ImageLoader
import coil.request.ImageRequest
import com.rerokutosei.chimera.data.model.ImageInfo
import com.rerokutosei.chimera.utils.common.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileNotFoundException
import okio.IOException

/**
 * 图片处理仓库
 */
class ImageRepository(private val context: Context) {

    private val imageLoader by lazy { ImageLoader(context) }
    private val logManager = LogManager.getInstance(context)

    /**
     * 从Uri获取图片信息
     */
    suspend fun getImageInfo(uri: Uri): ImageInfo {
        return withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .build()

                val result = imageLoader.execute(request)

                if (result.drawable != null) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream, null, options)
                        }
                    } catch (e: OutOfMemoryError) {
                        logManager.error("ImageRepository", "内存不足: ${e.message}", e)
                    } catch (e: FileNotFoundException) {
                        logManager.error("ImageRepository", "图片文件不存在: ${e.message}", e)
                    } catch (e: SecurityException) {
                        logManager.error("ImageRepository", "没有读取图片的权限: ${e.message}", e)
                    } catch (e: IOException) {
                        logManager.error("ImageRepository", "读取图片时发生IO错误: ${e.message}", e)
                    }
                    
                    ImageInfo(
                        uri = uri,
                        name = uri.lastPathSegment ?: "Unknown",
                        size = 0L,
                        mimeType = context.contentResolver.getType(uri) ?: "image/*",
                        width = options.outWidth,
                        height = options.outHeight
                    )
                } else {
                    logManager.warn("ImageRepository", "无法加载图片信息: $uri")
                    ImageInfo(uri = uri)
                }
            } catch (e: OutOfMemoryError) {
                logManager.error("ImageRepository", "内存不足: ${e.message}", e)
                ImageInfo(uri = uri)
            } catch (e: Exception) {
                logManager.error("ImageRepository", "获取图片信息时出错: ${e.message}", e)
                ImageInfo(uri = uri)
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ImageRepository? = null

        fun getInstance(context: Context): ImageRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageRepository(context).also { INSTANCE = it }
            }
        }
    }
}