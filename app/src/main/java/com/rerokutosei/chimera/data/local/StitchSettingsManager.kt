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
        val IMAGE_SPACING_COLOR = stringPreferencesKey("image_spacing_color")
        val CUT_GRID = intPreferencesKey("cut_grid")
        val MULTI_THREAD_ENABLED = booleanPreferencesKey("multi_thread_enabled")
    }

    private fun <T> DataStore<Preferences>.getPref(key: Preferences.Key<T>, default: T): Flow<T> {
        return this.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[key] ?: default }
    }

    private suspend fun <T> DataStore<Preferences>.setPref(key: Preferences.Key<T>, value: T) {
        this.edit { it[key] = value }
    }

    fun getStitchModeFlow(): Flow<StitchMode> {
        return context.dataStore.getPref(PreferencesKeys.STITCH_MODE, "DIRECT_VERTICAL")
            .map { when (it) { "DIRECT_HORIZONTAL" -> StitchMode.DIRECT_HORIZONTAL; else -> StitchMode.DIRECT_VERTICAL } }
    }

    suspend fun setStitchMode(mode: StitchMode) {
        context.dataStore.setPref(PreferencesKeys.STITCH_MODE, mode.name)
    }

    fun getWidthScaleFlow(): Flow<WidthScale> {
        return context.dataStore.getPref(PreferencesKeys.WIDTH_SCALE, "MIN_WIDTH")
            .map { when (it) { "MAX_WIDTH" -> WidthScale.MAX_WIDTH; "MIN_WIDTH" -> WidthScale.MIN_WIDTH; else -> WidthScale.NONE } }
    }

    suspend fun setWidthScale(scale: WidthScale) {
        context.dataStore.setPref(PreferencesKeys.WIDTH_SCALE, scale.name)
    }

    fun getOverlayAreaFlow(): Flow<Int> = context.dataStore.getPref(PreferencesKeys.OVERLAY_AREA, 10)
    suspend fun setOverlayArea(area: Int) = context.dataStore.setPref(PreferencesKeys.OVERLAY_AREA, area)

    fun getOverlayModeFlow(): Flow<OverlayMode> {
        return context.dataStore.getPref(PreferencesKeys.OVERLAY_MODE, "DISABLED")
            .map { if (it == "ENABLED") OverlayMode.ENABLED else OverlayMode.DISABLED }
    }

    suspend fun setOverlayMode(mode: OverlayMode) {
        context.dataStore.setPref(PreferencesKeys.OVERLAY_MODE, mode.name)
    }

    fun getImageSpacingFlow(): Flow<Int> = context.dataStore.getPref(PreferencesKeys.IMAGE_SPACING, 0)
    suspend fun setImageSpacing(spacing: Int) = context.dataStore.setPref(PreferencesKeys.IMAGE_SPACING, spacing)

    fun getImageSpacingColorFlow(): Flow<String> = context.dataStore.getPref(PreferencesKeys.IMAGE_SPACING_COLOR, "#FF000000")
    suspend fun setImageSpacingColor(color: String) = context.dataStore.setPref(PreferencesKeys.IMAGE_SPACING_COLOR, color)

    fun getCutGridFlow(): Flow<Int> = context.dataStore.getPref(PreferencesKeys.CUT_GRID, 3)
    suspend fun setCutGrid(grid: Int) = context.dataStore.setPref(PreferencesKeys.CUT_GRID, grid)

    fun getMultiThreadEnabledFlow(): Flow<Boolean> = context.dataStore.getPref(PreferencesKeys.MULTI_THREAD_ENABLED, false)
    suspend fun setMultiThreadEnabled(enabled: Boolean) = context.dataStore.setPref(PreferencesKeys.MULTI_THREAD_ENABLED, enabled)
}
