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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rerokutosei.chimera.ui.main.OverlayMode
import com.rerokutosei.chimera.ui.main.StitchMode
import com.rerokutosei.chimera.ui.main.WidthScale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * 拼接设置管理器
 */
class StitchSettingsManager private constructor(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stitch_settings")
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: StitchSettingsManager? = null
        
        fun getInstance(context: Context): StitchSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StitchSettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // 拼接设置键
    private object PreferencesKeys {
        val STITCH_MODE = stringPreferencesKey("stitch_mode")
        val WIDTH_SCALE = stringPreferencesKey("width_scale")
        val OVERLAY_AREA = intPreferencesKey("overlay_area")
        val OVERLAY_MODE = stringPreferencesKey("overlay_mode")
        val IMAGE_SPACING = intPreferencesKey("image_spacing")
        val MULTI_THREAD_ENABLED = booleanPreferencesKey("multi_thread_enabled")
    }
    
    /**
     * 获取拼接模式
     */
    fun getStitchModeFlow(): Flow<StitchMode> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val modeString = preferences[PreferencesKeys.STITCH_MODE] ?: "DIRECT_VERTICAL"
                when (modeString) {
                    "DIRECT_HORIZONTAL" -> StitchMode.DIRECT_HORIZONTAL
                    else -> StitchMode.DIRECT_VERTICAL
                }
            }
    }
    
    /**
     * 设置拼接模式
     */
    suspend fun setStitchMode(mode: StitchMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STITCH_MODE] = mode.name
        }
    }
    
    /**
     * 获取宽度缩放模式
     */
    fun getWidthScaleFlow(): Flow<WidthScale> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val scaleString = preferences[PreferencesKeys.WIDTH_SCALE] ?: "MIN_WIDTH"
                when (scaleString) {
                    "MAX_WIDTH" -> WidthScale.MAX_WIDTH
                    "MIN_WIDTH" -> WidthScale.MIN_WIDTH
                    else -> WidthScale.NONE
                }
            }
    }
    
    /**
     * 设置宽度缩放模式
     */
    suspend fun setWidthScale(scale: WidthScale) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDTH_SCALE] = scale.name
        }
    }
    
    /**
     * 获取被叠加区域占比
     */
    fun getOverlayAreaFlow(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.OVERLAY_AREA] ?: 10
            }
    }
    
    /**
     * 获取叠加模式
     */
    fun getOverlayModeFlow(): Flow<OverlayMode> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val modeString = preferences[PreferencesKeys.OVERLAY_MODE] ?: "DISABLED"
                when (modeString) {
                    "ENABLED" -> OverlayMode.ENABLED
                    else -> OverlayMode.DISABLED
                }
            }
    }

    /**
     * 设置叠加模式
     */
    suspend fun setOverlayMode(mode: OverlayMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_MODE] = mode.name
        }
    }

    /**
     * 设置被叠加区域占比
     */
    suspend fun setOverlayArea(area: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_AREA] = area
        }
    }
    
    /**
     * 获取图片间隔
     */
    fun getImageSpacingFlow(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.IMAGE_SPACING] ?: 0
            }
    }
    
    /**
     * 设置图片间隔
     */
    suspend fun setImageSpacing(spacing: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMAGE_SPACING] = spacing
        }
    }

    /**
     * 获取多线程加速计算设置
     */
    fun getMultiThreadEnabledFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.MULTI_THREAD_ENABLED] ?: false
            }
    }
    
    /**
     * 设置多线程加速计算
     */
    suspend fun setMultiThreadEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MULTI_THREAD_ENABLED] = enabled
        }
    }
}