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

package com.rerokutosei.chimera.data.local

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * 图片设置管理器
 */
class ImageSettingsManager private constructor(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "image_settings")
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ImageSettingsManager? = null
        
        fun getInstance(context: Context): ImageSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageSettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // 图片设置键
    private object PreferencesKeys {
        val OUTPUT_IMAGE_FORMAT = intPreferencesKey("output_image_format") // 0: PNG, 1: JPEG, 2: WEBP
        val OUTPUT_IMAGE_QUALITY = intPreferencesKey("output_image_quality") // 0-100
        val DELETE_ORIGINAL_IMAGE = booleanPreferencesKey("delete_original_image")
        val AUTO_CLEAR_IMAGES = booleanPreferencesKey("auto_clear_images")
        val HIGH_MEMORY_LIMIT = booleanPreferencesKey("high_memory_limit") // 提高内存阈值
        val USE_SAF_PICKER = booleanPreferencesKey("use_saf_picker") // 使用存储访问框架选择器
        val USE_EMBEDDED_PICKER = booleanPreferencesKey("use_embedded_picker") // 使用Embedded Picker
        val SLIDER_THUMB_SHAPE = intPreferencesKey("slider_thumb_shape") // 滑块手柄形状
    }
    
    /**
     * 获取输出图片格式
     */
    fun getOutputImageFormatFlow(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.OUTPUT_IMAGE_FORMAT] ?: 1 // 默认JPEG
            }
    }
    
    /**
     * 设置输出图片格式
     */
    suspend fun setOutputImageFormat(format: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OUTPUT_IMAGE_FORMAT] = format
        }
    }
    
    /**
     * 获取输出图片质量
     */
    fun getOutputImageQualityFlow(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.OUTPUT_IMAGE_QUALITY] ?: 85 // 默认85%
            }
    }
    
    /**
     * 设置输出图片质量
     */
    suspend fun setOutputImageQuality(quality: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OUTPUT_IMAGE_QUALITY] = quality
        }
    }
    
    /**
     * 获取删除原始图片设置
     */
    fun getDeleteOriginalImageFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.DELETE_ORIGINAL_IMAGE] ?: false
            }
    }

    /**
     * 获取自动清理图片设置
     */
    fun getAutoClearImagesFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.AUTO_CLEAR_IMAGES] ?: false
            }
    }
    
    /**
     * 设置自动清理图片
     */
    suspend fun setAutoClearImages(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CLEAR_IMAGES] = enabled
        }
    }
    
    /**
     * 获取提高内存阈值设置
     */
    fun getHighMemoryLimitFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.HIGH_MEMORY_LIMIT] ?: false
            }
    }
    
    /**
     * 设置提高内存阈值
     */
    suspend fun setHighMemoryLimit(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_MEMORY_LIMIT] = enabled
        }
    }
    
    /**
     * 获取使用存储访问框架选择器设置
     */
    fun getUseSafPickerFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.USE_SAF_PICKER] ?: false
            }
    }
    
    /**
     * 设置使用存储访问框架选择器
     */
    suspend fun setUseSafPicker(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SAF_PICKER] = enabled
        }
    }
    
    /**
     * 获取使用Embedded Picker设置
     */
    fun getUseEmbeddedPickerFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.USE_EMBEDDED_PICKER] ?: false
            }
    }
    
    /**
     * 设置使用Embedded Picker
     */
    suspend fun setUseEmbeddedPicker(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_EMBEDDED_PICKER] = enabled
        }
    }
    
    /**
     * 获取滑块手柄形状
     */
    fun getSliderThumbShapeFlow(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.SLIDER_THUMB_SHAPE] ?: 0 // 默认星形
            }
    }
    
    /**
     * 设置滑块手柄形状
     */
    suspend fun setSliderThumbShape(shape: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SLIDER_THUMB_SHAPE] = shape
        }
    }
}