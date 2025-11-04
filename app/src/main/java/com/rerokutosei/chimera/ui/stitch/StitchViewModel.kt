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

package com.rerokutosei.chimera.ui.stitch

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rerokutosei.chimera.ui.main.StitchMode
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.stitch.ImageStitcher
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.rerokutosei.chimera.utils.stitch.StitchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StitchViewModel(application: Application) : AndroidViewModel(application) {
    
    private val imageStitcher = ImageStitcher(application)
    private val logManager = LogManager.Companion.getInstance(application)
    
    private val _uiState = MutableStateFlow(StitchUiState())
    val uiState: StateFlow<StitchUiState> = _uiState.asStateFlow()
    
    fun stitchImages(
        orientation: StitchOrientation,
        imageUris: List<Uri>? = null,
        widthScale: WidthScale = WidthScale.NONE,
        imageSpacing: Int = 0
    ) {
        viewModelScope.launch {
            logManager.debug("StitchViewModel", "stitchImages方法被调用")
            _uiState.value = _uiState.value.copy(
                stitchState = StitchState.Processing,
                progress = 0
            )

            try {
                // 如果传入了图片URI列表，则使用该列表，否则使用全局图片列表
                val urisToUse = imageUris ?: _uiState.value.selectedImages
                logManager.debug("StitchViewModel", "使用的图片URI数量: ${urisToUse.size}")
                
                if (urisToUse.isEmpty()) {
                    logManager.debug("StitchViewModel", "未选择任何图片")
                    _uiState.value = _uiState.value.copy(
                        stitchState = StitchState.Error("未选择任何图片"),
                        progress = 0
                    )
                    return@launch
                }

                logManager.info("StitchViewModel", "开始拼接图片，方向: $orientation，数量: ${urisToUse.size}，间隔: $imageSpacing")

                // 执行拼接操作
                val result = imageStitcher.stitchImages(
                    urisToUse,
                    orientation,
                    widthScale,
                    imageSpacing
                ) { progress ->
                    // 更新进度
                    _uiState.value = _uiState.value.copy(progress = progress)
                }

                // 根据结果类型进行处理
                when (result) {
                    is StitchResult.BitmapResult -> {
                        logManager.info("StitchViewModel", "拼接成功，结果尺寸: ${result.bitmap.width}x${result.bitmap.height}")
                        logManager.debug("StitchViewModel", "拼接结果位图内存大小: ${result.bitmap.allocationByteCount} bytes")
                        _uiState.value = _uiState.value.copy(
                            stitchState = StitchState.Success(result.bitmap),
                            progress = 100
                        )
                    }
                    is StitchResult.ErrorResult -> {
                        logManager.error("StitchViewModel", "拼接失败: ${result.errorMessage}")
                        _uiState.value = _uiState.value.copy(
                            stitchState = StitchState.Error(result.errorMessage),
                            progress = 0
                        )
                    }
                }
            } catch (e: Exception) {
                logManager.error("StitchViewModel", "拼接过程出错", e)
                _uiState.value = _uiState.value.copy(
                    stitchState = StitchState.Error("拼接过程出错: ${e.message ?: "未知错误"}"),
                    progress = 0
                )
            }
        }
    }
    
    /**
     * 叠加拼接模式
     */
    fun stitchOverlay(
        imageUris: List<Uri>? = null,
        overlayRatio: Int,
        widthScale: WidthScale = WidthScale.MIN_WIDTH,
        orientation: StitchOrientation = StitchOrientation.VERTICAL
    ) {
        viewModelScope.launch {
            logManager.debug("StitchViewModel", "stitchOverlay方法被调用")
            _uiState.value = _uiState.value.copy(
                stitchState = StitchState.Processing,
                progress = 0
            )

            try {
                val urisToUse = imageUris ?: _uiState.value.selectedImages
                logManager.debug("StitchViewModel", "使用的图片URI数量: ${urisToUse.size}")
                
                if (urisToUse.isEmpty()) {
                    logManager.debug("StitchViewModel", "未选择任何图片")
                    _uiState.value = _uiState.value.copy(
                        stitchState = StitchState.Error("未选择任何图片"),
                        progress = 0
                    )
                    return@launch
                }

                logManager.info("StitchViewModel", "开始叠加拼接图片，数量: ${urisToUse.size}，被叠加区域占比: $overlayRatio%，宽度缩放: $widthScale，方向: $orientation")

                // 执行叠加拼接操作
                val result = imageStitcher.stitchOverlay(
                    urisToUse,
                    overlayRatio,
                    widthScale,
                    orientation
                ) { progress ->
                    _uiState.value = _uiState.value.copy(progress = progress)
                }

                when (result) {
                    is StitchResult.BitmapResult -> {
                        logManager.info("StitchViewModel", "叠加拼接成功，结果尺寸: ${result.bitmap.width}x${result.bitmap.height}")
                        logManager.debug("StitchViewModel", "拼接结果位图内存大小: ${result.bitmap.allocationByteCount} bytes")
                        _uiState.value = _uiState.value.copy(
                            stitchState = StitchState.Success(result.bitmap),
                            progress = 100
                        )
                    }
                    is StitchResult.ErrorResult -> {
                        logManager.error("StitchViewModel", "叠加拼接失败: ${result.errorMessage}")
                        _uiState.value = _uiState.value.copy(
                            stitchState = StitchState.Error(result.errorMessage),
                            progress = 0
                        )
                    }
                }
            } catch (e: Exception) {
                logManager.error("StitchViewModel", "叠加拼接过程出错", e)
                _uiState.value = _uiState.value.copy(
                    stitchState = StitchState.Error("叠加拼接过程出错: ${e.message ?: "未知错误"}"),
                    progress = 0
                )
            }
        }
    }

    fun setSelectedImages(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(selectedImages = uris)
    }
    
    fun updateStitchMode(mode: StitchMode) {
        _uiState.value = _uiState.value.copy(stitchMode = mode)
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.stitchState is StitchState.Success) {
            val currentBitmap = (_uiState.value.stitchState as StitchState.Success).result
            if (!currentBitmap.isRecycled) {
                logManager.debug("StitchViewModel", "ViewModel清除时回收位图: ${currentBitmap.width}x${currentBitmap.height}")
                currentBitmap.recycle()
            }
        }
        logManager.debug("StitchViewModel", "ViewModel已清除")
    }
}

data class StitchUiState(
    val selectedImages: List<Uri> = emptyList(),
    val stitchState: StitchState = StitchState.Idle,
    val progress: Int = 0,
    val stitchMode: StitchMode = StitchMode.DIRECT_VERTICAL
)

sealed class StitchState {
    object Idle : StitchState()
    object Processing : StitchState()
    data class Success(val result: Bitmap) : StitchState()
    data class Error(val message: String) : StitchState()
}