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

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.LruCache
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.utils.common.LogManager
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

/**
 * 图片加载器，负责从Uri加载Bitmap并管理缓存
 */
class BitmapLoader(private val context: Context) {
    
    companion object {
        private const val TAG = "BitmapLoader"
        private const val DEFAULT_LARGE_IMAGE_THRESHOLD = 10L * 1024 * 1024 // 10MB
        private const val DEFAULT_LARGE_IMAGE_DIMENSION_THRESHOLD = 3000
        private const val DEFAULT_REGION_DECODER_THRESHOLD = 50L * 1024 * 1024 // 50MB 使用区域解码器的阈值
        private const val DEFAULT_TARGET_SIZE_LARGE = 1200
        private const val DEFAULT_TARGET_SIZE_NORMAL = 1920
    }
    
    private val logManager = LogManager.getInstance(context)
    private val imageSettingsManager = ImageSettingsManager.getInstance(context)
    
    // LRU缓存，用于缓存加载的图片
    // Coil有自己的缓存机制，这里的缓存主要用于快速访问
    private val memoryCache: LruCache<String, Bitmap> by lazy {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val maxMemory = (memoryInfo.totalMem / 1024).toInt()
        val cacheSize = maxMemory / 16 // 使用最大内存的1/16作为缓存大小，以免与Coil冲突
        
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.allocationByteCount / 1024
            }
        }
    }
    
    // 用于跟踪正在使用的位图
    private val activeBitmaps = ConcurrentHashMap<String, Bitmap>()
    
    // 读写锁
    private val bitmapLock = ReentrantReadWriteLock()
    
    /**
     * 检查URI权限
     */
    private fun checkUriPermission(uri: Uri) {
        try {
            val permissions = context.contentResolver.persistedUriPermissions
            val hasPermission = permissions.any { it.uri == uri && it.isReadPermission }
            logManager.debug(TAG, "URI权限检查: $uri, 有读取权限: $hasPermission")

            if (!hasPermission) {
                logManager.debug(TAG, "当前应用拥有的持久化权限数量: ${permissions.size}")
                permissions.forEachIndexed { index, permission ->
                    logManager.debug(TAG, "权限 $index: URI=${permission.uri}, 读权限=${permission.isReadPermission}, 写权限=${permission.isWritePermission}")
                }
                
                // 尝试重新获取权限
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    logManager.debug(TAG, "重新获取权限成功: $uri")
                } catch (e: SecurityException) {
                    logManager.error(TAG, "重新获取权限失败: $uri", e)
                }
            }
        } catch (e: Exception) {
            logManager.error(TAG, "检查URI权限时出错: $uri", e)
        }
    }
    
    /**
     * 预估图片尺寸，仅读取图片头部信息
     */
    fun estimateImageSize(uri: Uri): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            return if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else {
                null
            }
        } catch (e: Exception) {
            logManager.error(TAG, "预估图片尺寸失败: $uri", e)
            return null
        }
    }

    /**
     * 预估经过采样（缩放）后的图片尺寸，模拟真实加载的逻辑但仅读取元数据。
     * 以最小的内存开销，更准确地获取到图片加载到内存后的实际尺寸。
     *
     * @param uri 图片的Uri
     * @return 返回预估的宽度和高度，如果失败则返回 null。
     */
    fun getSampledDimensions(uri: Uri): Pair<Int, Int>? {
        try {
            // 只获取图片原始尺寸和类型，不加载图片本身
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                logManager.warn(TAG, "无法获取图片尺寸: $uri")
                return null
            }

            // 模拟真实加载逻辑，判断是否为大图，并计算 inSampleSize
            val isLargeImage = isLargeImage(options)
            val targetSize = if (isLargeImage) DEFAULT_TARGET_SIZE_LARGE else DEFAULT_TARGET_SIZE_NORMAL
            val inSampleSize = calculateInSampleSize(options, targetSize, targetSize)

            // 根据 inSampleSize 计算最终加载到内存的尺寸
            val finalWidth = options.outWidth / inSampleSize
            val finalHeight = options.outHeight / inSampleSize

            logManager.debug(TAG, "预估采样后尺寸 for $uri: ${finalWidth}x$finalHeight (inSampleSize=$inSampleSize)")

            return finalWidth to finalHeight
        } catch (e: Exception) {
            logManager.error(TAG, "预估采样后尺寸失败: $uri", e)
            return null
        }
    }
    
    /**
     * 从Uri加载Bitmap，使用适当的缩放以避免内存问题
     * 统一转换为ARGB_8888格式以确保格式兼容性
     * 支持大图片分块处理（>10MB或任意一边像素>3000px）
     */
    @SuppressLint("Recycle")
    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        val uriString = uri.toString()
        
        logManager.debug(TAG, "开始加载图片: $uriString")

        memoryCache.get(uriString)?.let { 
            logManager.debug(TAG, "从缓存中获取图片: $uriString")
            if (!it.isRecycled) {
                activeBitmaps[uriString] = it
                return it 
            } else {
                memoryCache.remove(uriString)
                logManager.warn(TAG, "缓存中的位图已被回收，已从缓存中移除: $uriString")
            }
        }
        
        // 添加一个简短的延迟，给Coil一点时间来处理缓存
        try {
            Thread.sleep(10)
        } catch (e: InterruptedException) {
            // 忽略中断异常
        }
        
        return try {
            logManager.debug(TAG, "打开输入流")
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            logManager.debug(TAG, "准备打开输入流")

            checkUriPermission(uri)
            
            val inputStream: InputStream? = try {
                logManager.debug(TAG, "尝试打开输入流: $uri")
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    logManager.error(TAG, "无法打开输入流: $uri | 返回null")
                } else {
                    logManager.debug(TAG, "成功打开输入流: $uri")
                }
                inputStream
            } catch (e: SecurityException) {
                logManager.error(TAG, "安全异常，无法打开输入流: $uri | ${e.message}", e)
                return null
            } catch (e: OutOfMemoryError) {
                logManager.error(TAG, "内存不足，无法打开输入流: $uri | ${e.message}", e)
                return null
            } catch (e: Exception) {
                logManager.error(TAG, "无法打开输入流: $uri | ${e.message}", e)
                return null
            }

            if (inputStream == null) {
                logManager.error(TAG, "输入流为空: $uri")
                return null
            }

            inputStream.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            logManager.debug(TAG, "图片尺寸信息: ${options.outWidth}x${options.outHeight}, MIME类型: ${options.outMimeType}")

            val isLargeImage = isLargeImage(options)
            logManager.debug(TAG, "图片尺寸: ${options.outWidth}x${options.outHeight}, 是否为大图片: $isLargeImage")

            val estimatedSize = options.outWidth.toLong() * options.outHeight.toLong() * 4
            if (estimatedSize > DEFAULT_REGION_DECODER_THRESHOLD) {
                logManager.debug(TAG, "使用 BitmapRegionDecoder 处理超大图片")
                return loadBitmapWithRegionDecoder(uri, options)
            }

            val targetSize = if (isLargeImage) {
                DEFAULT_TARGET_SIZE_LARGE
            } else {
                DEFAULT_TARGET_SIZE_NORMAL
            }

            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
            
            // 根据是否需要透明度选择合适的格式
            val mimeType = options.outMimeType?.lowercase() ?: ""
            val hasAlpha = mimeType.contains("png") || 
                          mimeType.contains("webp") || 
                          mimeType.contains("avif") ||
                          mimeType.contains("gif")

            options.inPreferredConfig = if (hasAlpha) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565

            logManager.debug(TAG, "缩放比例: ${options.inSampleSize}, 格式: ${options.inPreferredConfig}, MIME类型: $mimeType")

            logManager.debug(TAG, "加载缩放后的图片")
            val bitmap = try {
                context.contentResolver.openInputStream(uri)?.use { scaledInputStream ->
                    BitmapFactory.decodeStream(scaledInputStream, null, options)
                }
            } catch (e: OutOfMemoryError) {
                logManager.error(TAG, "内存不足，加载缩放后的图片时出错: $uri | ${e.message}", e)
                null
            } catch (e: Exception) {
                logManager.error(TAG, "加载缩放后的图片时出错: $uri | ${e.message}", e)
                null
            } catch (e: SecurityException) {
                logManager.error(TAG, "加载缩放后的图片时出现安全异常: $uri | ${e.message}", e)
                null
            }
            
            logManager.debug(TAG, "图片加载完成: ${bitmap?.width}x${bitmap?.height}")
            
            // 将加载的图片放入缓存和活跃位图集合
            bitmap?.let {
                if (!it.isRecycled) {
                    memoryCache.put(uriString, it)
                    activeBitmaps[uriString] = it
                    logManager.debug(TAG, "将图片放入缓存: $uriString")
                } else {
                    logManager.warn(TAG, "尝试将已回收的位图放入缓存: $uriString")
                }
            }
            
            bitmap
        } catch (e: OutOfMemoryError) {
            logManager.error(TAG, "内存不足，无法加载位图: $uri", e)
            null
        } catch (e: Exception) {
            logManager.error(TAG, "Error loading bitmap from URI: $uri", e)
            null
        }
    }
    
    /**
     * 使用 BitmapRegionDecoder 加载大图片
     */
    private fun loadBitmapWithRegionDecoder(uri: Uri, options: BitmapFactory.Options): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BitmapRegionDecoder.newInstance(inputStream)
            } else {
                @Suppress("DEPRECATION")
                BitmapRegionDecoder.newInstance(inputStream, false)
            }

            if (decoder == null) {
                inputStream.close()
                logManager.error(TAG, "无法创建 BitmapRegionDecoder")
                return null
            }
            
            inputStream.close()

            val targetSize = DEFAULT_TARGET_SIZE_LARGE
            val inSampleSize = calculateInSampleSize(options, targetSize, targetSize)

            val mimeType = options.outMimeType?.lowercase() ?: ""
            val hasAlpha = mimeType.contains("png") || 
                          mimeType.contains("webp") || 
                          mimeType.contains("avif") ||
                          mimeType.contains("gif")

            val config = if (hasAlpha) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            
            val regionOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                this.inPreferredConfig = config
            }

            val rect = Rect(0, 0, options.outWidth, options.outHeight)
            val bitmap = decoder.decodeRegion(rect, regionOptions)
            decoder.recycle()

            bitmap?.let { activeBitmaps[uri.toString()] = it }
            bitmap
        } catch (e: OutOfMemoryError) {
            logManager.error(TAG, "内存不足，使用 BitmapRegionDecoder 加载图片失败: $uri", e)
            null
        } catch (e: Exception) {
            logManager.error(TAG, "使用 BitmapRegionDecoder 加载图片失败: $uri", e)
            null
        }
    }
    
    /**
     * 检查是否为大图片
     */
    private fun isLargeImage(options: BitmapFactory.Options): Boolean {
        val width = options.outWidth
        val height = options.outHeight

        if (width > DEFAULT_LARGE_IMAGE_DIMENSION_THRESHOLD || height > DEFAULT_LARGE_IMAGE_DIMENSION_THRESHOLD) {
            return true
        }

        // 估算内存占用（ARGB_8888格式，每个像素4字节）
        val estimatedSize = width * height * 4
        return estimatedSize > DEFAULT_LARGE_IMAGE_THRESHOLD
    }
    
    /**
     * 计算合适的图片缩放比例
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // 原始图片的宽度和高度
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // 计算最大inSampleSize值，使得图片尺寸大于等于请求的尺寸
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
    
    /**
     * 清空内存缓存
     */
    fun clearCache() {
        bitmapLock.write {
            memoryCache.evictAll()
            activeBitmaps.clear()
            logManager.debug(TAG, "清空图片缓存和活跃位图集合")
        }
    }
    
    /**
     * 回收位图列表
     */
    fun recycleBitmaps(bitmaps: List<Bitmap>, exclude: Bitmap? = null) {
        bitmapLock.write {
            bitmaps.forEach { bitmap ->
                if (bitmap != exclude && !bitmap.isRecycled) {
                    activeBitmaps.entries.removeIf { it.value == bitmap }

                    memoryCache.snapshot().entries.find { it.value == bitmap }?.let { entry ->
                        memoryCache.remove(entry.key)
                        logManager.debug(TAG, "从缓存中移除位图: ${entry.key}")
                    }

                    bitmap.recycle()
                    logManager.debug(TAG, "回收位图: ${bitmap.width}x${bitmap.height}")
                }
            }
        }
    }
}
