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

package com.rerokutosei.chimera.ui.navigation

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.data.local.StitchSettingsManager
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.ui.main.MainScreen
import com.rerokutosei.chimera.ui.main.MainViewModel
import com.rerokutosei.chimera.ui.main.OverlayMode
import com.rerokutosei.chimera.ui.main.StitchMode
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.ui.settings.SettingsScreen
import com.rerokutosei.chimera.ui.settings.SettingsViewModel
import com.rerokutosei.chimera.ui.stitch.StitchState
import com.rerokutosei.chimera.ui.stitch.StitchViewModel
import com.rerokutosei.chimera.ui.viewer.ImageViewerScreen
import com.rerokutosei.chimera.ui.viewer.ImageViewerViewModel
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import kotlinx.coroutines.flow.first

/**
 * 应用导航图
 * 使用Compose Navigation管理所有屏幕间的导航
 */
@RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Route.Main.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        // 主屏幕
        composable(Route.Main.route) {
            val mainViewModel: MainViewModel = viewModel()

            val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            val context = LocalContext.current
            val themeSettingsManager = remember { ThemeRepository.getInstance(context) }
            val darkTheme by themeSettingsManager.getDarkThemeFlow().collectAsState(initial = false)
            val followSystemTheme by themeSettingsManager.getFollowSystemThemeFlow().collectAsState(initial = true)

            val isSystemInDarkThemeVal = isSystemInDarkTheme()
            val effectiveDarkTheme = when {
                !followSystemTheme -> darkTheme
                else -> isSystemInDarkThemeVal
            }

            val settingsViewModel: SettingsViewModel = viewModel()
            
            MainScreen(
                viewModel = mainViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToStitch = {
                    // 传递图片URI列表和宽度缩放参数作为导航参数
                    val imageUris = mainUiState.selectedImages.map { it.uri }
                    // 将URI保存到ViewModel中供ImageViewerScreen使用
                    mainViewModel.setPendingStitchUris(imageUris)
                    
                    val widthScale = when (mainUiState.widthScale) {
                        WidthScale.MAX_WIDTH -> "MAX_WIDTH"
                        WidthScale.MIN_WIDTH -> "MIN_WIDTH"
                        else -> "NONE"
                    }
                    val stitchMode = when (mainUiState.stitchMode) {
                        StitchMode.DIRECT_HORIZONTAL -> "DIRECT_HORIZONTAL"
                        StitchMode.DIRECT_VERTICAL -> "DIRECT_VERTICAL"
                    }
                    val imageSpacing = mainUiState.imageSpacing
                    // 直接跳转到ImageViewer，传递参数用于拼接，但不传递URI列表
                    val route = Uri.Builder()
                        .path(Route.ImageViewer.route)
                        .appendQueryParameter("widthScale", widthScale)
                        .appendQueryParameter("stitchMode", stitchMode)
                        .appendQueryParameter("imageSpacing", imageSpacing.toString())
                        .appendQueryParameter("triggerStitch", "true")
                        .build()
                        .toString()
                    navController.navigate(route)
                    },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.route)
                }
            )
        }

        // 设置屏幕
        composable(Route.Settings.route) {
            val mainViewModel: MainViewModel = viewModel()
            SettingsScreen(
                mainViewModel = mainViewModel
            )
        }

        // 图片查看屏幕
        composable("${Route.ImageViewer.route}?widthScale={widthScale}&stitchMode={stitchMode}&imageSpacing={imageSpacing}&triggerStitch={triggerStitch}") { backStackEntry ->
            val widthScaleParam = backStackEntry.arguments?.getString("widthScale")
            val stitchModeParam = backStackEntry.arguments?.getString("stitchMode")
            val imageSpacingParam = backStackEntry.arguments?.getString("imageSpacing")
            val triggerStitchParam = backStackEntry.arguments?.getString("triggerStitch")
            
            val context = LocalContext.current
            val logManager = LogManager.getInstance(context)
            logManager.debug("NavGraph", "ImageViewer composable被调用，widthScaleParam: $widthScaleParam, stitchModeParam: $stitchModeParam, imageSpacingParam: $imageSpacingParam")

            val stitchViewModel: StitchViewModel = viewModel()
            val imageViewerViewModel: ImageViewerViewModel = viewModel()
            val mainViewModel: MainViewModel = viewModel(
                viewModelStoreOwner = remember(backStackEntry) { 
                    navController.getBackStackEntry(Route.Main.route) 
                }
            )
            
            // 从MainViewModel获取待处理的图片URI
            val pendingStitchUris by mainViewModel.pendingStitchUris.collectAsState()
            
            // 使用状态变量存储拼接结果位图
            var stitchedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var stitchErrorMessage by remember { mutableStateOf<String?>(null) }
            var isStitching by remember { mutableStateOf(false) }
            
            // 只有在参数存在且尚未拼接时才进行拼接
            LaunchedEffect(widthScaleParam, stitchModeParam, imageSpacingParam, triggerStitchParam, pendingStitchUris) {
                logManager.debug("NavGraph", "LaunchedEffect触发，isStitching: $isStitching, stitchedBitmap: $stitchedBitmap, triggerStitchParam: $triggerStitchParam")
                
                if (!isStitching && stitchedBitmap == null &&
                    triggerStitchParam == "true" && pendingStitchUris.isNotEmpty()) {
                    
                    logManager.debug("NavGraph", "开始拼接流程")
                    isStitching = true

                    val uris = pendingStitchUris
                    logManager.debug("NavGraph", "从ViewModel获取到${uris.size}个图片URI")
                    stitchViewModel.setSelectedImages(uris)

                    val stitchMode = when (stitchModeParam) {
                        "DIRECT_HORIZONTAL" -> StitchMode.DIRECT_HORIZONTAL
                        else -> StitchMode.DIRECT_VERTICAL
                    }
                    logManager.debug("NavGraph", "设置拼接模式: $stitchMode")
                    stitchViewModel.updateStitchMode(stitchMode)

                    val widthScale = when (widthScaleParam) {
                        "MAX_WIDTH" -> WidthScale.MAX_WIDTH
                        "MIN_WIDTH" -> WidthScale.MIN_WIDTH
                        else -> WidthScale.NONE
                    }
                    logManager.debug("NavGraph", "设置宽度缩放: $widthScale")

                    val imageSpacing = imageSpacingParam?.toIntOrNull() ?: 0
                    logManager.debug("NavGraph", "设置图片间隔: $imageSpacing")

                    val orientation = when (stitchMode) {
                        StitchMode.DIRECT_HORIZONTAL -> StitchOrientation.HORIZONTAL
                        else -> StitchOrientation.VERTICAL
                    }
                    logManager.debug("NavGraph", "设置拼接方向: $orientation")
                    logManager.debug("NavGraph", "调用stitchViewModel.stitchImages")

                    val overlayMode = mainViewModel.uiState.value.overlayMode
                    
                    if (overlayMode == OverlayMode.ENABLED) {
                        val stitchSettingsManager = StitchSettingsManager.getInstance(context)
                        val overlayRatio: Int = stitchSettingsManager.getOverlayAreaFlow().first()

                        val widthScale = when (mainViewModel.uiState.value.widthScale) {
                            WidthScale.MAX_WIDTH -> WidthScale.MAX_WIDTH
                            else -> WidthScale.MIN_WIDTH
                        }
                        
                        stitchViewModel.stitchOverlay(
                            overlayRatio = overlayRatio,
                            widthScale = widthScale,
                            orientation = orientation
                        )
                    } else {
                        stitchViewModel.stitchImages(
                            orientation, 
                            widthScale = widthScale, 
                            imageSpacing = imageSpacing
                        )
                    }
                } else {
                    logManager.debug("NavGraph", "未满足拼接条件")
                }
            }

            LaunchedEffect(stitchViewModel.uiState) {
                stitchViewModel.uiState.collect { uiState ->
                    when (val stitchState = uiState.stitchState) {
                        is StitchState.Success -> {
                            stitchedBitmap = stitchState.result
                            isStitching = false

                            val imageSettingsManager = ImageSettingsManager.getInstance(context)
                            val shouldAutoClear = imageSettingsManager.getAutoClearImagesFlow().first()
                            
                            if (shouldAutoClear) {
                                mainViewModel.clearImages()
                            }
                        }
                        is StitchState.Error -> {
                            stitchErrorMessage = stitchState.message
                            isStitching = false
                        }
                        else -> {
                            // 保持默认状态
                        }
                    }
                }
            }
            
            LaunchedEffect(stitchedBitmap, stitchErrorMessage, isStitching) {
                imageViewerViewModel.setProcessing(isStitching)
                if (stitchErrorMessage != null) {
                    imageViewerViewModel.setError(stitchErrorMessage!!)
                } else if (stitchedBitmap != null) {
                    imageViewerViewModel.setStitchResult(stitchedBitmap!!)
                }
            }
            
            ImageViewerScreen(
                viewModel = imageViewerViewModel,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}