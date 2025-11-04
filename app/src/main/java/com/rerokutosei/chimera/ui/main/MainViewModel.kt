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

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.data.local.StitchSettingsManager
import com.rerokutosei.chimera.data.model.ImageInfo
import com.rerokutosei.chimera.data.repository.ImageRepository
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.image.EstimateResolution
import com.rerokutosei.chimera.utils.image.ResolutionValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val imageRepository = ImageRepository.getInstance(application)
    private val stitchSettingsManager = StitchSettingsManager.getInstance(application)
    private val imageSettingsManager = ImageSettingsManager.getInstance(application)
    private val bitmapLoader = BitmapLoader(application)
    private val estimateResolution = EstimateResolution(bitmapLoader)
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private val _pendingStitchUris = MutableStateFlow<List<Uri>>(emptyList())
    val pendingStitchUris: StateFlow<List<Uri>> = _pendingStitchUris.asStateFlow()
    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()
    private val _resolutionValidationState = MutableStateFlow<ResolutionValidationState>(ResolutionValidationState.NotNeeded)
    val resolutionValidationState: StateFlow<ResolutionValidationState> = _resolutionValidationState.asStateFlow()
    private val _showResolutionErrorToast = MutableStateFlow<String?>(null)
    val showResolutionErrorToast: StateFlow<String?> = _showResolutionErrorToast.asStateFlow()
    
    init { loadSettings() }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val stitchMode = stitchSettingsManager.getStitchModeFlow().first()
            val overlayMode = stitchSettingsManager.getOverlayModeFlow().first()
            val overlayArea = stitchSettingsManager.getOverlayAreaFlow().first()
            val widthScale = stitchSettingsManager.getWidthScaleFlow().first()
            val imageSpacing = stitchSettingsManager.getImageSpacingFlow().first()
            val autoClearImages = imageSettingsManager.getAutoClearImagesFlow().first()
            
            _uiState.value = _uiState.value.copy(
                stitchMode = stitchMode,
                overlayMode = overlayMode,
                overlayArea = overlayArea,
                widthScale = widthScale,
                imageSpacing = imageSpacing,
                autoClearImages = autoClearImages
            )

            _isDataLoaded.value = true

            listenToSettingsChanges()
        }
    }
    
    /**
     * 监听设置变化以触发尺寸验证
     */
    private fun listenToSettingsChanges() {
        viewModelScope.launch {
            imageSettingsManager.getOutputImageFormatFlow().collect {
                validateResolution()
            }
        }
    }
    
    fun selectImages(uris: List<Uri>, isFromEmbeddedPicker: Boolean = false) {
        viewModelScope.launch {
            setImagePreviewLoading(true)
            try {
                val imageInfos = uris.map { uri ->
                    imageRepository.getImageInfo(uri)
                }
                // 追加新选择的图片到现有列表
                val currentImages = _uiState.value.selectedImages.toMutableList()
                // 过滤掉已存在的图片
                val newImages = imageInfos.filter { newImage ->
                    currentImages.none { existingImage ->
                        existingImage.uri == newImage.uri
                    }
                }
                // 经测试，PhotoPicker (`PickMultipleVisualMedia`) 不保证返回URI的顺序
                // 当用户快速选择大量图片时，返回的列表顺序可能是混乱的
                // 这是一个已知的平台问题，但谷歌官方一直没有修复
                // 相关问题链接: https://issuetracker.google.com/issues/264215151
                //
                // 当前的 `reversed()` 方法是一个"权宜之计"。它在某些情况下（例如，选择少量图片时）
                // 碰巧能得到正确的顺序，但并不可靠。当选择大量图片时，顺序问题会再次出现。
                // 对于Embedded Picker，图片顺序是正确的，不需要反转
                // 对于PhotoPicker和其他选择器，需要反转
                val imagesToAdd = if (isFromEmbeddedPicker) newImages else newImages.reversed()
                currentImages.addAll(0, imagesToAdd)
                _uiState.value = _uiState.value.copy(selectedImages = currentImages)

                if (newImages.isNotEmpty()) {
                    setToastMessage(getApplication<Application>().getString(R.string.images_selected, newImages.size))
                }

                validateResolution()
            } finally {
                setImagePreviewLoading(false)
            }
        }
    }

    fun setPendingStitchUris(uris: List<Uri>) {
        _pendingStitchUris.value = uris
    }

    
    fun removeImage(imageInfo: ImageInfo) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        currentImages.remove(imageInfo)
        _uiState.value = _uiState.value.copy(selectedImages = currentImages)

        validateResolution()
    }
    
    fun clearImages() {
        _uiState.value = _uiState.value.copy(selectedImages = emptyList())

        validateResolution()
    }


    fun updateStitchMode(mode: StitchMode) {
        _uiState.value = _uiState.value.copy(stitchMode = mode)
        viewModelScope.launch {
            stitchSettingsManager.setStitchMode(mode)
        }

        validateResolution()
    }

    fun updateOverlayMode(mode: OverlayMode) {
        _uiState.value = _uiState.value.copy(overlayMode = mode)
        viewModelScope.launch {
            stitchSettingsManager.setOverlayMode(mode)
        }

        validateResolution()
    }

    fun updateOverlayArea(area: Int) {
        _uiState.value = _uiState.value.copy(overlayArea = area)
        viewModelScope.launch {
            stitchSettingsManager.setOverlayArea(area)
        }

        validateResolution()
    }

    fun updateWidthScale(scale: WidthScale) {
        _uiState.value = _uiState.value.copy(widthScale = scale)
        viewModelScope.launch {
            stitchSettingsManager.setWidthScale(scale)
        }

        validateResolution()
    }

    fun updateImageSpacing(spacing: Int) {
        _uiState.value = _uiState.value.copy(imageSpacing = spacing)
        viewModelScope.launch {
            stitchSettingsManager.setImageSpacing(spacing)
        }

        validateResolution()
    }
    
    fun setErrorMessage(message: String?) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun setToastMessage(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }
    
    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun reorderImages(newUriOrder: List<Uri>) {
        val currentImages = _uiState.value.selectedImages
        if (currentImages.isEmpty() || currentImages.size != newUriOrder.size) {
            return
        }
        val imageInfoMap = currentImages.associateBy { it.uri }
        val reorderedImages = newUriOrder.mapNotNull { uri -> imageInfoMap[uri] }
        _uiState.value = _uiState.value.copy(selectedImages = reorderedImages)

        validateResolution()
    }

    /**
     * 设置图片预览加载状态
     */
    fun setImagePreviewLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isImagePreviewLoading = loading)
    }
    
    /**
     * 开始拼接
     */
    fun onStartStitching() {
        if (_uiState.value.selectedImages.size < 2) {
            setErrorMessage(getApplication<Application>().getString(R.string.select_two_or_more_images))
        }
    }

    /**
     * 验证图片尺寸是否超出格式限制
     */
    private fun validateResolution() {
        val images = _uiState.value.selectedImages
        if (images.isEmpty()) {
            _resolutionValidationState.value = ResolutionValidationState.NotNeeded
            return
        }
        
        _resolutionValidationState.value = ResolutionValidationState.InProgress
        
        viewModelScope.launch {
            val outputFormat = imageSettingsManager.getOutputImageFormatFlow().first()

            val validationResult = estimateResolution.validateResolution(
                imageUris = images.map { it.uri },
                stitchMode = _uiState.value.stitchMode,
                widthScale = _uiState.value.widthScale,
                overlayMode = _uiState.value.overlayMode,
                overlayArea = _uiState.value.overlayArea,
                imageSpacing = _uiState.value.imageSpacing,
                outputFormat = outputFormat
            )

            when (validationResult) {
                is ResolutionValidationResult.NotNeeded -> {
                    _resolutionValidationState.value = ResolutionValidationState.NotNeeded
                }
                is ResolutionValidationResult.InProgress -> {
                    _resolutionValidationState.value = ResolutionValidationState.InProgress
                }
                is ResolutionValidationResult.Valid -> {
                    _resolutionValidationState.value = ResolutionValidationState.Valid(
                        width = validationResult.width,
                        height = validationResult.height
                    )
                }
                is ResolutionValidationResult.Invalid -> {
                    _resolutionValidationState.value = ResolutionValidationState.Invalid(
                        width = validationResult.width,
                        height = validationResult.height,
                        formatName = validationResult.formatName,
                        limit = validationResult.limit
                    )

                    _showResolutionErrorToast.value = getApplication<Application>().getString(R.string.resolution_limit_exceeded)
                }
            }
        }
    }
    
    fun clearResolutionErrorToast() {
        _showResolutionErrorToast.value = null
    }
}

data class MainUiState(
    val selectedImages: List<ImageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isImagePreviewLoading: Boolean = false, // 图片预览加载状态
    val stitchMode: StitchMode = StitchMode.DIRECT_VERTICAL, // 拼接模式
    val overlayMode: OverlayMode = OverlayMode.DISABLED, // 叠加模式
    val overlayArea: Int = 10, // 叠加区域占比
    val widthScale: WidthScale = WidthScale.MIN_WIDTH, // 宽/高度缩放
    val errorMessage: String? = null,
    val toastMessage: String? = null,
    val imageSpacing: Int = 0,  // 添加图片间隔参数
    val autoClearImages: Boolean = true // 是否自动清理已选图片
)

// 添加尺寸验证状态的密封类
sealed class ResolutionValidationState {
    object NotNeeded : ResolutionValidationState()
    object InProgress : ResolutionValidationState()
    data class Valid(val width: Long, val height: Long) : ResolutionValidationState()
    data class Invalid(val width: Long, val height: Long, val formatName: String, val limit: Int) : ResolutionValidationState()
}

enum class StitchMode {
    DIRECT_HORIZONTAL,  // 横向拼接
    DIRECT_VERTICAL     // 纵向拼接
}

enum class OverlayMode {
    DISABLED,  // 不叠加
    ENABLED    // 叠加
}

enum class WidthScale {
    NONE,       // 不缩放
    MAX_WIDTH,  // 缩放到最大宽度
    MIN_WIDTH   // 缩放到最小宽度
}