/*
 * Based on ImageToolbox, an image editor for android
 * Original work Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Modified work Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * This file contains modifications from the original source code.
 * Original source: https://github.com/T8RIN/ImageToolbox
 */

package com.t8rin.embeddedpicker.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class CacheManager(private val context: Context) {
    private val memoryCache = mutableMapOf<String, CachedData<*>>()
    private val diskCacheDir = File(context.cacheDir, "embedded_picker_cache").apply { 
        if (!exists()) mkdirs() 
    }
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 缓存策略枚举
    enum class CacheStrategy(val expiryTime: Long) {
        SHORT_LIVED(5 * 60 * 1000),     // 5分钟
        MEDIUM_LIVED(30 * 60 * 1000),   // 30分钟
        LONG_LIVED(60 * 60 * 1000),     // 1小时
        NEVER_EXPIRE(-1)                // 永不过期
    }
    
    // 带版本控制的缓存数据类
    data class CachedData<T>(
        val data: T,
        val timestamp: Long,
        val strategy: CacheStrategy
    ) : Serializable
    
    // 获取缓存数据
    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> get(key: String): T? {
        val cached = memoryCache[key] as? CachedData<T>
        return if (cached != null && (cached.strategy == CacheStrategy.NEVER_EXPIRE || 
                    System.currentTimeMillis() - cached.timestamp < cached.strategy.expiryTime)) {
            cached.data
        } else {
            // 尝试从磁盘缓存获取
            getFromDiskCache(key)
        }
    }
    
    // 保存数据到缓存
    fun <T : Serializable> put(key: String, data: T, strategy: CacheStrategy = CacheStrategy.MEDIUM_LIVED) {
        val cachedData = CachedData(data, System.currentTimeMillis(), strategy)
        memoryCache[key] = cachedData
        // 异步保存到磁盘缓存，对中等及以上缓存时间的数据进行磁盘缓存
        if (strategy != CacheStrategy.SHORT_LIVED) {
            saveToDiskCache(key, cachedData)
        }
    }
    
    // 清除所有缓存
    fun clear() {
        memoryCache.clear()
        diskCacheDir.listFiles()?.forEach { it.delete() }
    }
    
    // 清除过期缓存
    fun clearExpired() {
        val now = System.currentTimeMillis()
        memoryCache.entries.removeIf { entry ->
            val cachedData = entry.value
            cachedData.strategy != CacheStrategy.NEVER_EXPIRE && 
            now - cachedData.timestamp >= cachedData.strategy.expiryTime
        }
        
        diskCacheDir.listFiles()?.forEach { file ->
            try {
                ObjectInputStream(FileInputStream(file)).use { ois ->
                    val cachedData = ois.readObject() as CachedData<*>
                    if (cachedData.strategy != CacheStrategy.NEVER_EXPIRE && 
                        now - cachedData.timestamp >= cachedData.strategy.expiryTime) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                file.delete() // 删除损坏的缓存文件
            }
        }
    }
    
    // 保存到磁盘缓存
    private fun saveToDiskCache(key: String, data: CachedData<*>) {
        // 只对长期缓存的数据进行磁盘缓存
        if (data.strategy == CacheStrategy.LONG_LIVED || data.strategy == CacheStrategy.NEVER_EXPIRE) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val file = File(diskCacheDir, key)
                        ObjectOutputStream(FileOutputStream(file)).use { oos ->
                            oos.writeObject(data)
                        }
                    } catch (e: Exception) {
                        // 忽略磁盘缓存失败
                    }
                }
            }
        }
    }
    
    // 从磁盘缓存读取
    @Suppress("UNCHECKED_CAST")
    private fun <T : Serializable> getFromDiskCache(key: String): T? {
        return try {
            val file = File(diskCacheDir, key)
            if (file.exists()) {
                ObjectInputStream(FileInputStream(file)).use { ois ->
                    val cachedData = ois.readObject() as CachedData<T>
                    // 检查是否过期
                    if (cachedData.strategy == CacheStrategy.NEVER_EXPIRE || 
                        System.currentTimeMillis() - cachedData.timestamp < cachedData.strategy.expiryTime) {
                        // 放入内存缓存
                        memoryCache[key] = cachedData
                        cachedData.data
                    } else {
                        file.delete() // 删除过期文件
                        null
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}