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

package com.rerokutosei.chimera.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.ui.settings.SettingsViewModel
import com.rerokutosei.chimera.utils.common.ShowToast
import com.t8rin.imagereordercarousel.ImageReorderCarousel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isDataLoaded by viewModel.isDataLoaded.collectAsState()
    var isPageEntered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isPageEntered = true
    }

    DisposableEffect(Unit) { onDispose {} }

    // 获取设置项状态
    val imageSettingsManager = ImageSettingsManager.getInstance(context)
    val themeSettingsManager = ThemeRepository.getInstance(context)
    val useSafPicker by imageSettingsManager.getUseSafPickerFlow().collectAsState(initial = false)
    val useEmbeddedPicker by imageSettingsManager.getUseEmbeddedPickerFlow().collectAsState(initial = false)
    val sliderThumbShape by imageSettingsManager.getSliderThumbShapeFlow().collectAsState(initial = 0)
    
    // 添加尺寸验证状态
    val resolutionValidationState by viewModel.resolutionValidationState.collectAsState()
    val showResolutionErrorToast by viewModel.showResolutionErrorToast.collectAsState()
    
    // Embedded Picker状态
    var showEmbeddedPicker by remember { mutableStateOf(false) }
    
    // 添加标识，用于跟踪当前使用的图片选择器类型
    var isUsingEmbeddedPicker by remember { mutableStateOf(false) }

    // 显示Toast消息
    ShowToast(
        message = uiState.toastMessage,
        onShown = { viewModel.clearToast() }
    )

    // 当图片列表不为空且正在加载时，关闭加载状态
    LaunchedEffect(uiState.selectedImages) {
        if (uiState.selectedImages.isNotEmpty() && uiState.isImagePreviewLoading) {
            viewModel.setImagePreviewLoading(false)
        }
    }

    // 显示Embedded Picker对话框
    if (showEmbeddedPicker) {
        EmbeddedPickerDialog(
            onImagesSelected = { uris ->
                viewModel.selectImages(uris, isFromEmbeddedPicker = isUsingEmbeddedPicker)
                showEmbeddedPicker = false
                isUsingEmbeddedPicker = false
            },
            onDismiss = { 
                showEmbeddedPicker = false
                isUsingEmbeddedPicker = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = if (isPageEntered) 1f else 0f
                    translationX = if (isPageEntered) 0f else 100f
                }
                .animateContentSize()
        ) {
            item {
                TopAppBar(
                    onNavigateToSettings = onNavigateToSettings
                )

                Spacer(modifier = Modifier.height(8.dp))

                ErrorDialog(
                    errorMessage = uiState.errorMessage,
                    isVisible = uiState.errorMessage != null,
                    onDismiss = { viewModel.clearError() }
                )

                ImagePickerButton(
                    context = context,
                    isPageEntered = isPageEntered,
                    isDataLoaded = isDataLoaded,
                    useSafPicker = useSafPicker,
                    useEmbeddedPicker = useEmbeddedPicker,
                    onImagesSelected = { uris -> 
                        // 对于SAF和PhotoPicker，仍然需要反转顺序
                        viewModel.selectImages(uris, isFromEmbeddedPicker = false) 
                    },
                    showEmbeddedPicker = { 
                        showEmbeddedPicker = true
                        isUsingEmbeddedPicker = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ParameterSettingsCard(
                    uiState = uiState,
                    isPageEntered = isPageEntered,
                    isDataLoaded = isDataLoaded,
                    sliderThumbShape = sliderThumbShape,
                    onUpdateStitchMode = { viewModel.updateStitchMode(it) },
                    onUpdateOverlayMode = { viewModel.updateOverlayMode(it) },
                    onUpdateWidthScale = { viewModel.updateWidthScale(it) },
                    onUpdateOverlayArea = { viewModel.updateOverlayArea(it) },
                    onUpdateImageSpacing = { viewModel.updateImageSpacing(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 尺寸预览卡片
                EstimatedResolutionCard(
                    resolutionValidationState = resolutionValidationState,
                    showResolutionErrorToast = showResolutionErrorToast,
                    onToastShown = { viewModel.clearResolutionErrorToast() }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // 按钮组
                if (uiState.selectedImages.isNotEmpty()) {
                    BottomActionButtons(
                        uiState = uiState,
                        isPageEntered = isPageEntered,
                        isDataLoaded = isDataLoaded,
                        onClearImages = { viewModel.clearImages() },
                        onStartStitching = { viewModel.onStartStitching() },
                        onNavigateToStitch = onNavigateToStitch,
                        isStartButtonEnabled = resolutionValidationState !is ResolutionValidationState.Invalid
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ImageReorderCarousel 图片预览
            val imageUris = uiState.selectedImages.map { it.uri }
            
            if (uiState.selectedImages.isNotEmpty()) {
                item {
                    ImageReorderCarousel(
                        images = imageUris,
                        onReorder = { reorderedUris -> viewModel.reorderImages(reorderedUris) },
                        onNeedToAddImage = {},
                        onNeedToRemoveImageAt = { index ->
                            if (index in uiState.selectedImages.indices) {
                                viewModel.removeImage(uiState.selectedImages[index])
                            }
                        },
                        showAddButton = false,
                        showSortButton = false,
                        enableImagePreview = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isImagePreviewLoading) { ContainedLoadingIndicator()
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.please_select_images),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}