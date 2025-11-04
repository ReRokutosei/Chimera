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
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.stitch.StitchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class ImageViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val logManager = LogManager.getInstance(application)

    private val _previewSource = MutableStateFlow<PreviewSource?>(null)
    val previewSource = _previewSource.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private var tempStitchFile: File? = null

    fun setStitchResult(result: Any) {
        _isProcessing.value = false
        when (result) {
            is Bitmap -> {
                logManager.debug("ImageViewerViewModel", "设置新的位图结果.")
                clearBitmap()
                releaseTempFile()
                _previewSource.value = PreviewSource.FromBitmap(result)
            }
            is StitchResult.BitmapResult -> {
                logManager.debug("ImageViewerViewModel", "设置Bitmap结果.")
                clearBitmap()
                releaseTempFile()
                _previewSource.value = PreviewSource.FromBitmap(result.bitmap)
            }
            else -> {
                setError("未知的拼接结果类型: ${result::class.java.name}")
            }
        }
    }

    fun setProcessing(processing: Boolean = true) {
        if (processing) {
            _isProcessing.value = true
            _error.value = null
            _previewSource.value = null
        } else {
            _isProcessing.value = false
        }
    }

    fun setError(message: String) {
        logManager.error("ImageViewerViewModel", "设置错误状态: $message")
        _isProcessing.value = false
        _error.value = message
        _previewSource.value = null
    }

    fun releaseTempFile() {
        tempStitchFile?.let {
             logManager.debug("ImageViewerViewModel", "释放临时文件跟踪: ${it.absolutePath}")
        }
        tempStitchFile = null
    }

    private fun clearBitmap() {
        (_previewSource.value as? PreviewSource.FromBitmap)?.bitmap?.let {
            if (!it.isRecycled) {
                logManager.debug("ImageViewerViewModel", "回收位图.")
                it.recycle()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        logManager.debug("ImageViewerViewModel", "ViewModel已清除.")
        tempStitchFile?.let {
            if (it.exists()) {
                logManager.debug("ImageViewerViewModel", "删除未保存的临时文件: ${it.absolutePath}")
                it.delete()
            }
        }
        clearBitmap()
    }
}