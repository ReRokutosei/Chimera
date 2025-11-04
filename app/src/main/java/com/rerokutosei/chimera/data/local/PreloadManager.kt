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
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.utils.common.LogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okio.IOException

/**
 * 预加载管理器，用于在应用启动时预加载所有必要数据，避免UI闪烁
 */
class PreloadManager private constructor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: PreloadManager? = null
        
        fun getInstance(context: Context): PreloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 预加载所有必要数据
     */
    fun preloadAllData() {
        scope.launch {
            try {
                coroutineScope {
                    val themeSettingsDeferred = async { preloadThemeSettings() }
                    val stitchSettingsDeferred = async { preloadStitchSettings() }
                    val imageSettingsDeferred = async { preloadImageSettings() }

                    themeSettingsDeferred.await()
                    stitchSettingsDeferred.await()
                    imageSettingsDeferred.await()
                }
            } catch (e: IOException) {
                LogManager.Companion.getInstance(context).error("PreloadManager", "预加载数据时出错", e)
            }
        }
    }
    
    /**
     * 预加载主题设置
     */
    private suspend fun preloadThemeSettings() {
        try {
            val themeSettingsManager = ThemeRepository.getInstance(context)
            themeSettingsManager.getDarkThemeFlow().collect { }
            themeSettingsManager.getDynamicColorFlow().collect { }
            themeSettingsManager.getLogLevelFlow().collect { }
            themeSettingsManager.getFollowSystemThemeFlow().collect { }
        } catch (e: IOException) {
            LogManager.Companion.getInstance(context).error("PreloadManager", "预加载主题设置时出错", e)
        }
    }
    
    /**
     * 预加载拼接设置
     */
    private suspend fun preloadStitchSettings() {
        try {
            val stitchSettingsManager = StitchSettingsManager.Companion.getInstance(context)
            stitchSettingsManager.getStitchModeFlow().collect { }
            stitchSettingsManager.getOverlayModeFlow().collect { }
            stitchSettingsManager.getOverlayAreaFlow().collect { }
            stitchSettingsManager.getWidthScaleFlow().collect { }
            stitchSettingsManager.getImageSpacingFlow().collect { }
        } catch (e: IOException) {
            LogManager.Companion.getInstance(context).error("PreloadManager", "预加载拼接设置时出错", e)
        }
    }
    
    /**
     * 预加载图片设置
     */
    private suspend fun preloadImageSettings() {
        try {
            val imageSettingsManager = ImageSettingsManager.getInstance(context)
            imageSettingsManager.getOutputImageFormatFlow().collect { }
            imageSettingsManager.getOutputImageQualityFlow().collect { }
            imageSettingsManager.getDeleteOriginalImageFlow().collect { }
            imageSettingsManager.getAutoClearImagesFlow().collect { }
            imageSettingsManager.getHighMemoryLimitFlow().collect { }
            imageSettingsManager.getUseSafPickerFlow().collect { }
        } catch (e: IOException) {
            LogManager.Companion.getInstance(context).error("PreloadManager", "预加载图片设置时出错", e)
        }
    }
}