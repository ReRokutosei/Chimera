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
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * 用户偏好设置管理器
 */
class UserPreferencesManager private constructor(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UserPreferencesManager? = null

        fun getInstance(context: Context): UserPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // 用户偏好设置键
    private object PreferencesKeys {
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    /**
     * 是否首次启动应用
     */
    fun getFirstLaunchFlow(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
            }
    }

    /**
     * 设置是否首次启动应用
     */
    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = isFirstLaunch
        }
        
        sharedPreferences.edit()
            .putBoolean("first_launch", isFirstLaunch)
            .apply()
    }

    /**
     * 同步检查是否首次启动（用于在setContent之前快速检查）
     */
    fun checkFirstLaunchSync(): Boolean {
        return sharedPreferences.getBoolean("first_launch", true)
    }
}