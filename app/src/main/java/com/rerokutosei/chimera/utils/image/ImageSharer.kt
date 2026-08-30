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

package com.rerokutosei.chimera.utils.image

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.FileProvider
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.stitch.layout.OutputImageFormat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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
    @Suppress("TooGenericExceptionCaught") // Sharing is a best-effort UI boundary.
    suspend fun shareBitmap(
        bitmap: Bitmap,
        title: String = context.getString(R.string.share_stitched_image)
    ): Boolean {
        return try {
            if (bitmap.isRecycled) {
                logManager.error("ImageSharer", "尝试分享已回收的位图")
                return false
            }

            // 从DataStore获取设置
            val formatCode = imageSettingsManager.getOutputImageFormatFlow().first()
            val quality = imageSettingsManager.getOutputImageQualityFlow().first()

            val format = OutputImageFormat.fromCode(formatCode)
            val compressFormat = when (format) {
                OutputImageFormat.PNG -> Bitmap.CompressFormat.PNG
                OutputImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
                OutputImageFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }

            val cachePath = File(context.cacheDir, "images")
            check(cachePath.exists() || cachePath.mkdirs()) {
                "Unable to create image sharing cache directory"
            }
            val file = withContext(Dispatchers.IO) {
                val f = File(cachePath, "shared_image.${format.fileExtension}")
                val encoded = FileOutputStream(f).use { outputStream ->
                    bitmap.compress(compressFormat, quality, outputStream)
                }
                if (!encoded) {
                    f.delete()
                    error("Bitmap encoder rejected the image")
                }
                f
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                type = format.mimeType
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(android.content.Intent.createChooser(shareIntent, title))
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logManager.error("ImageSharer", "位图分享失败", e)
            false
        }
    }
}
