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

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.utils.common.LogManager
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 图片分享工具类
 */
class ImageSharer(private val context: Context) {
    private val logManager = LogManager.getInstance(context)
    private val imageSettingsManager = ImageSettingsManager.getInstance(context)

    /**
     * 分享Bitmap
     * @param bitmap 要分享的Bitmap
     * @param title 分享标题
     * @return 分享是否成功
     */
    suspend fun shareBitmap(bitmap: Bitmap, title: String = context.getString(R.string.share_stitched_image)): Boolean {
        return try {
            if (bitmap.isRecycled) {
                logManager.error("ImageSharer", "尝试分享已回收的位图")
                return false
            }

            // 从DataStore获取设置
            val formatCode = imageSettingsManager.getOutputImageFormatFlow().first()
            val quality = imageSettingsManager.getOutputImageQualityFlow().first()

            val (extension, compressFormat) = when (formatCode) {
                1 -> Pair("jpg", Bitmap.CompressFormat.JPEG)
                2 -> Pair("webp", Bitmap.CompressFormat.WEBP)
                else -> Pair("png", Bitmap.CompressFormat.PNG)
            }

            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_image.$extension")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(compressFormat, quality, outputStream)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                type = "image/${extension}"
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(android.content.Intent.createChooser(shareIntent, title))
            true
        } catch (e: IOException) {
            logManager.error("ImageSharer", "位图分享失败", e)
            false
        }
    }
}