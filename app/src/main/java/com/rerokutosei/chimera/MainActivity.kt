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

package com.rerokutosei.chimera

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.rerokutosei.chimera.data.local.PreloadManager
import com.rerokutosei.chimera.data.local.UserPreferencesManager
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.ui.main.WelcomeDialog
import com.rerokutosei.chimera.ui.navigation.AppNavGraph
import com.rerokutosei.chimera.ui.theme.AppTheme
import com.rerokutosei.chimera.ui.theme.shouldUseDarkTheme
import com.rerokutosei.chimera.utils.common.LogManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val logManager by lazy { LogManager.getInstance(this) }
    private lateinit var userPreferencesManager: UserPreferencesManager

    @RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferencesManager = UserPreferencesManager.getInstance(this)

        val isFirstLaunch = userPreferencesManager.checkFirstLaunchSync()

        PreloadManager.getInstance(this).preloadAllData()

        cleanupExpiredTempFiles()

        cleanupResidualTempFiles()

        setContent {
            val context = LocalContext.current
            val themeSettingsManager = remember { ThemeRepository.getInstance(context) }

            val logManager = remember { LogManager.getInstance(context) }

            val useDarkTheme = shouldUseDarkTheme()
            val dynamicColor by themeSettingsManager.getDynamicColorFlow().collectAsState(initial = true)
            val logLevel by themeSettingsManager.getLogLevelFlow().collectAsState(initial = 1)

            logManager.setLogLevel(logLevel)

            var showWelcomeDialog by remember { mutableStateOf(isFirstLaunch) }
            var agreedToTerms by remember { mutableStateOf(false) }

            // 如果是首次启动，显示欢迎对话框
            if (showWelcomeDialog) {
                WelcomeDialog(
                    onAgree = {
                        // 用户同意条款
                        agreedToTerms = true
                        showWelcomeDialog = false

                        lifecycleScope.launch {
                            userPreferencesManager.setFirstLaunch(false)
                        }
                    },
                    onDisagree = {
                        // 用户不同意条款，退出应用
                        finish()
                    }
                )
            }

            AppTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColor
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) {
                        val navController = rememberNavController()

                        if (agreedToTerms || !isFirstLaunch) {
                            AppNavGraph(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 清理过期的临时文件
     * 清理24小时前创建的临时文件
     */
    private fun cleanupExpiredTempFiles() {
        try {
            val now = System.currentTimeMillis()
            val expirationTime = 24 * 60 * 60 * 1000L // 24小时

            cacheDir.listFiles { file ->
                file.name.startsWith("temp_") &&
                now - file.lastModified() > expirationTime
            }?.forEach { file ->
                try {
                    file.delete()
                } catch (e: SecurityException) {
                    logManager.error("MainActivity", "安全异常，无法删除过期文件: ${file.absolutePath}", e)
                }
            }
        } catch (e: SecurityException) {
            logManager.error("MainActivity", "安全异常，无法清理过期临时文件", e)
        }
    }

    /**
     * 清理残留的临时文件
     * 清理所有临时文件，无论创建时间，确保系统干净
     */
    private fun cleanupResidualTempFiles() {
        try {
            // 清理cache目录下的临时文件
            cacheDir.listFiles { file ->
                file.name.startsWith("temp_") ||
                file.name.startsWith("shared_image") ||
                file.name.startsWith("cropped_image") ||
                file.name.startsWith("temp_image")
            }?.forEach { file ->
                try {
                    file.delete()
                } catch (e: SecurityException) {
                    logManager.error("MainActivity", "安全异常，无法删除残留文件: ${file.absolutePath}", e)
                }
            }

            // 清理外部缓存目录下的临时文件
            externalCacheDir?.listFiles { file ->
                file.name.startsWith("temp_") ||
                file.name.startsWith("shared_image") ||
                file.name.startsWith("cropped_image") ||
                file.name.startsWith("temp_image")
            }?.forEach { file ->
                try {
                    file.delete()
                } catch (e: SecurityException) {
                    logManager.error("MainActivity", "安全异常，无法删除外部缓存残留文件: ${file.absolutePath}", e)
                }
            }
        } catch (e: SecurityException) {
            logManager.error("MainActivity", "安全异常，无法清理残留临时文件", e)
        }
    }
}