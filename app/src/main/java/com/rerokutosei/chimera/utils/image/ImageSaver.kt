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
import com.rerokutosei.chimera.domain.error.SaveFailure
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.stitch.layout.OutputImageFormat
import kotlinx.coroutines.CancellationException
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
    suspend fun loadOptions(): ImageSaveOptions = withContext(Dispatchers.IO) {
        ImageSaveOptions(
            format = OutputImageFormat.fromCode(imageSettingsManager.getOutputImageFormatFlow().first()),
            quality = imageSettingsManager.getOutputImageQualityFlow().first()
        )
    }

    suspend fun saveToGallery(bitmap: Bitmap): ImageSaveResult {
        return try {
            saveToGallery(bitmap, loadOptions())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logManager.error(TAG, "读取图片保存设置失败", e)
            ImageSaveResult.Failure(SaveFailure.Unexpected(e))
        }
    }

    suspend fun saveToGallery(
        bitmap: Bitmap,
        options: ImageSaveOptions,
        nameSuffix: String? = null
    ): ImageSaveResult = withContext(Dispatchers.IO) {
        var insertedUri: Uri? = null
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val suffix = nameSuffix?.let { "_$it" }.orEmpty()
            val fileName = "Chimera_${timeStamp}$suffix.${getImageExtension(options.format)}"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(options.format))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Chimera")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext ImageSaveResult.Failure(SaveFailure.StorageUnavailable)
            insertedUri = uri
            val outputStream = resolver.openOutputStream(uri)
                ?: return@withContext failedSave(uri, SaveFailure.WriteFailed(IllegalStateException("MediaStore returned no output stream")))

            val encoded = outputStream.use { stream ->
                bitmap.compress(options.format.toCompressFormat(), options.quality, stream)
            }
            if (!encoded) {
                return@withContext failedSave(uri, SaveFailure.EncodingFailed)
            }

            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            logManager.debug(TAG, "Bitmap保存成功: $uri")
            ImageSaveResult.Success(uri)
        } catch (e: CancellationException) {
            insertedUri?.let(::deleteQuietly)
            throw e
        } catch (e: Exception) {
            logManager.error(TAG, "保存Bitmap到MediaStore失败", e)
            insertedUri?.let(::deleteQuietly)
            ImageSaveResult.Failure(SaveFailure.WriteFailed(e))
        }
    }

    private fun failedSave(uri: Uri, failure: SaveFailure): ImageSaveResult.Failure {
        deleteQuietly(uri)
        return ImageSaveResult.Failure(failure)
    }

    private fun deleteQuietly(uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
            .onFailure { logManager.error(TAG, "清理未完成的相册条目失败: $uri", it) }
    }

    /**
     * 根据格式获取文件扩展名
     */
    private fun getImageExtension(format: OutputImageFormat): String {
        return when (format) {
            OutputImageFormat.PNG -> "png"
            OutputImageFormat.JPEG -> "jpg"
            OutputImageFormat.WEBP -> "webp"
        }
    }

    /**
     * 根据格式获取MIME类型
     */
    private fun getMimeType(format: OutputImageFormat): String {
        return when (format) {
            OutputImageFormat.PNG -> "image/png"
            OutputImageFormat.JPEG -> "image/jpeg"
            OutputImageFormat.WEBP -> "image/webp"
        }
    }
}

data class ImageSaveOptions(
    val format: OutputImageFormat,
    val quality: Int
)

sealed interface ImageSaveResult {
    data class Success(val uri: Uri) : ImageSaveResult
    data class Failure(val failure: SaveFailure) : ImageSaveResult
}

private fun OutputImageFormat.toCompressFormat(): Bitmap.CompressFormat = when (this) {
    OutputImageFormat.PNG -> Bitmap.CompressFormat.PNG
    OutputImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
    OutputImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
}
