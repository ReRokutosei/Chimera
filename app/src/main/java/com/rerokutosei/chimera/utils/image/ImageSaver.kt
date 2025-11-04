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

package com.rerokutosei.chimera.utils.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.utils.common.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 图片保存工具类
 */
class ImageSaver(private val context: Context) {

    companion object {
        private const val TAG = "ImageSaver"
    }

    private val imageSettingsManager = ImageSettingsManager.getInstance(context)
    private val logManager = LogManager.getInstance(context)

    /**
     * 将Bitmap保存到设备相册
     */
    suspend fun saveToGallery(
        bitmap: Bitmap,
        onSaved: (Uri?) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            logManager.debug(TAG, "开始保存Bitmap到相册")

            val format = imageSettingsManager.getOutputImageFormatFlow().first()
            val quality = imageSettingsManager.getOutputImageQualityFlow().first()

            logManager.debug(TAG, "输出格式: $format, 质量: $quality")

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Chimera_$timeStamp.${getImageExtension(format)}"

            logManager.debug(TAG, "生成文件名: $fileName")

            val uri = saveImage(bitmap, fileName, format, quality)

            if (uri != null) {
                logManager.debug(TAG, "Bitmap保存成功: $uri")
                withContext(Dispatchers.Main) {
                    onSaved(uri)
                }
            } else {
                logManager.error(TAG, "Bitmap保存失败")
                withContext(Dispatchers.Main) {
                    onError(Exception("保存失败"))
                }
            }
        } catch (e: Exception) {
            logManager.error(TAG, "保存Bitmap时出错", e)
            withContext(Dispatchers.Main) {
                onError(e)
            }
        }
    }

    /**
     * 保存Bitmap到指定文件名
     */
    private fun saveImage(bitmap: Bitmap, fileName: String, format: Int, quality: Int): Uri? {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(format))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Chimera")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    val compressFormat = when (format) {
                        0 -> Bitmap.CompressFormat.PNG
                        1 -> Bitmap.CompressFormat.JPEG
                        2 -> Bitmap.CompressFormat.WEBP
                        else -> Bitmap.CompressFormat.JPEG
                    }
                    bitmap.compress(compressFormat, quality, outputStream)
                }
                return uri
            }
        } catch (e: Exception) {
            logManager.error(TAG, "保存Bitmap到MediaStore失败", e)
        }
        return null
    }

    /**
     * 根据格式获取文件扩展名
     */
    private fun getImageExtension(format: Int): String {
        return when (format) {
            0 -> "png"
            1 -> "jpg"
            2 -> "webp"
            else -> "jpg"
        }
    }

    /**
     * 根据格式获取MIME类型
     */
    private fun getMimeType(format: Int): String {
        return when (format) {
            0 -> "image/png"
            1 -> "image/jpeg"
            2 -> "image/webp"
            else -> "image/jpeg"
        }
    }
}