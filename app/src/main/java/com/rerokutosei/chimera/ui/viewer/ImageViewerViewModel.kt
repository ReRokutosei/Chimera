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

package com.rerokutosei.chimera.ui.viewer

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rerokutosei.chimera.domain.error.CutFailure
import com.rerokutosei.chimera.domain.error.SaveFailure
import com.rerokutosei.chimera.domain.usecase.CutSaveResult
import com.rerokutosei.chimera.domain.usecase.SaveCutImagesUseCase
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.image.ImageSaver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val logManager = LogManager.getInstance(application)
    private val saveCutImagesUseCase =
        SaveCutImagesUseCase(BitmapLoader(application), ImageSaver(application))

    // 切割模式状态
    private val _isCutMode = MutableStateFlow(false)
    val isCutMode = _isCutMode.asStateFlow()

    private val _cutImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val cutImageUris = _cutImageUris.asStateFlow()

    private val _cutGridCols = MutableStateFlow(3)
    val cutGridCols = _cutGridCols.asStateFlow()

    private val _cutGridRows = MutableStateFlow(3)
    val cutGridRows = _cutGridRows.asStateFlow()

    private val _currentCutIndex = MutableStateFlow(0)
    val currentCutIndex = _currentCutIndex.asStateFlow()

    private val _cutSaveState = MutableStateFlow<CutSaveState>(CutSaveState.Idle)
    val cutSaveState = _cutSaveState.asStateFlow()

    fun setCutMode(
        imageUris: List<Uri>,
        gridCols: Int,
        gridRows: Int,
        startIndex: Int = 0
    ) {
        _isCutMode.value = true
        _cutImageUris.value = imageUris
        _cutGridCols.value = gridCols
        _cutGridRows.value = gridRows
        _currentCutIndex.value = startIndex
        _cutSaveState.value = CutSaveState.Idle
    }

    fun setCurrentCutIndex(index: Int) {
        _currentCutIndex.value = index
    }

    fun saveCutImages() {
        if (_cutSaveState.value is CutSaveState.Saving) return

        val imageUris = _cutImageUris.value
        val cols = _cutGridCols.value
        val rows = _cutGridRows.value
        viewModelScope.launch {
            _cutSaveState.value = CutSaveState.Saving
            try {
                _cutSaveState.value =
                    when (val result = saveCutImagesUseCase(imageUris, cols, rows)) {
                        is CutSaveResult.Success -> CutSaveState.Success(result.savedCount)
                        is CutSaveResult.CutFailed -> {
                            logFailure("切图失败", result.failure, result.failure.cause)
                            CutSaveState.Failure(
                                CutSaveIssue.Cut(result.failure),
                                result.savedCount
                            )
                        }

                        is CutSaveResult.SaveFailed -> {
                            logFailure("保存切图失败", result.failure, result.failure.cause)
                            CutSaveState.Failure(
                                CutSaveIssue.Save(result.failure),
                                result.savedCount
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (failure: Exception) {
                logFailure("切图保存发生意外错误", failure, failure)
                _cutSaveState.value = CutSaveState.Failure(
                    CutSaveIssue.Cut(CutFailure.Unexpected(failure)),
                    savedCount = 0
                )
            }
        }
    }

    fun clearCutSaveState() {
        _cutSaveState.value = CutSaveState.Idle
    }

    private fun logFailure(message: String, failure: Any, cause: Throwable?) {
        val detail = "$message: ${failure::class.simpleName}"
        cause?.let { logManager.error("ImageViewerViewModel", detail, it) }
            ?: logManager.error("ImageViewerViewModel", detail)
    }

    override fun onCleared() {
        super.onCleared()
        logManager.debug("ImageViewerViewModel", "ViewModel已清除.")
    }
}

sealed interface CutSaveState {
    data object Idle : CutSaveState
    data object Saving : CutSaveState
    data class Success(val savedCount: Int) : CutSaveState
    data class Failure(val issue: CutSaveIssue, val savedCount: Int) : CutSaveState
}

sealed interface CutSaveIssue {
    data class Cut(val failure: CutFailure) : CutSaveIssue
    data class Save(val failure: SaveFailure) : CutSaveIssue
}
