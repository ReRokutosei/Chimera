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

package com.rerokutosei.chimera.utils.common

import android.app.ActivityManager
import android.content.Context
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 内存限制计算器
 * 用于根据设备内存情况动态计算图片处理的内存限制
 */
class MemoryLimitCalculator(
    private val context: Context,
    private val imageSettingsManager: ImageSettingsManager,
    private val logManager: LogManager,
    private val tag: String
) {
    
    companion object {
        private const val DEFAULT_MEMORY_LIMIT_RATIO = 0.5 // 默认内存限制比例 50%
        private const val HIGH_MEMORY_LIMIT_RATIO = 0.8 // 高内存限制比例 80%
    }
    
    /**
     * 根据设备内存和用户设置计算最大图片大小限制
     */
    fun calculateMaxImageSize(): Long {
        // 使用ActivityManager获取内存信息
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val isHighMemoryLimitEnabled = try {
            runBlocking {
                imageSettingsManager.getHighMemoryLimitFlow().first()
            }
        } catch (e: Exception) {
            logManager.error(tag, "获取highMemoryLimit设置失败", e)
            false
        }

        val ratio = if (isHighMemoryLimitEnabled) {
            HIGH_MEMORY_LIMIT_RATIO
        } else {
            DEFAULT_MEMORY_LIMIT_RATIO
        }
        
        val limit = (memoryInfo.totalMem * ratio).toLong()
        logManager.debug(tag, "根据设备内存计算限制: ${limit / (1024 * 1024)}MB，总内存: ${memoryInfo.totalMem / (1024 * 1024 * 1024)}GB，阈值: ${ratio * 100}%")
        return limit
    }
}