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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rerokutosei.chimera.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * 主题设置仓库
 */
class ThemeRepository private constructor(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ThemeRepository? = null

        fun getInstance(context: Context): ThemeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // 主题设置键
    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val FOLLOW_SYSTEM_THEME = booleanPreferencesKey("follow_system_theme")
        val LOG_LEVEL = intPreferencesKey("log_level")
        val THEME_MODE = intPreferencesKey("theme_mode")
        val SELECTED_COLOR_SCHEME = stringPreferencesKey("selected_color_scheme")
        val CUSTOM_PRIMARY_COLOR = stringPreferencesKey("custom_primary_color")
        val CUSTOM_SECONDARY_COLOR = stringPreferencesKey("custom_secondary_color")
        val CUSTOM_TERTIARY_COLOR = stringPreferencesKey("custom_tertiary_color")
    }

    /**
     * 获取深色主题设置
     */
    fun getDarkThemeFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.DARK_THEME] ?: false
            }
    }

    /**
     * 获取动态色彩设置
     */
    fun getDynamicColorFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
            }
    }

    /**
     * 设置动态色彩
     */
    suspend fun setDynamicColor(isDynamicColor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = isDynamicColor
        }
    }

    /**
     * 获取跟随系统主题设置
     */
    fun getFollowSystemThemeFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.FOLLOW_SYSTEM_THEME] ?: true
            }
    }

    /**
     * 获取主题模式设置
     */
    fun getThemeModeFlow(): Flow<ThemeMode> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val modeValue = preferences[PreferencesKeys.THEME_MODE] ?: 0
                ThemeMode.fromValue(modeValue)
            }
    }

    /**
     * 设置主题模式
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.value
        }
    }

    /**
     * 获取选中的颜色方案
     */
    fun getSelectedColorSchemeFlow(): Flow<String> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.SELECTED_COLOR_SCHEME] ?: "bocchi"
            }
    }

    /**
     * 设置选中的颜色方案
     */
    suspend fun setSelectedColorScheme(schemeName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_COLOR_SCHEME] = schemeName
        }
    }

    /**
     * 获取自定义主色
     */
    fun getCustomPrimaryColorFlow(): Flow<String> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.CUSTOM_PRIMARY_COLOR] ?: ""
            }
    }

    /**
     * 设置自定义主色
     */
    suspend fun setCustomPrimaryColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRIMARY_COLOR] = color
        }
    }

    /**
     * 获取自定义次色
     */
    fun getCustomSecondaryColorFlow(): Flow<String> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.CUSTOM_SECONDARY_COLOR] ?: ""
            }
    }

    /**
     * 设置自定义次色
     */
    suspend fun setCustomSecondaryColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_SECONDARY_COLOR] = color
        }
    }

    /**
     * 获取自定义第三色
     */
    fun getCustomTertiaryColorFlow(): Flow<String> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.CUSTOM_TERTIARY_COLOR] ?: ""
            }
    }

    /**
     * 设置自定义第三色
     */
    suspend fun setCustomTertiaryColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_TERTIARY_COLOR] = color
        }
    }

    /**
     * 获取日志等级
     */
    fun getLogLevelFlow(): Flow<Int> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.LOG_LEVEL] ?: 1 // 默认INFO级别
            }
    }

    /**
     * 设置日志等级
     */
    suspend fun setLogLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOG_LEVEL] = level
        }
    }
}