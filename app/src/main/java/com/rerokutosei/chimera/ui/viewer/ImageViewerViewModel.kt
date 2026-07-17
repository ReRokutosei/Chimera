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
import com.rerokutosei.chimera.utils.common.LogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImageViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val logManager = LogManager.getInstance(application)

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
    }

    fun setCurrentCutIndex(index: Int) {
        _currentCutIndex.value = index
    }

    override fun onCleared() {
        super.onCleared()
        logManager.debug("ImageViewerViewModel", "ViewModel已清除.")
    }
}
