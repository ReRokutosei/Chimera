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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R

@Composable
fun BottomActionButtons(
    uiState: MainUiState,
    isPageEntered: Boolean,
    isDataLoaded: Boolean,
    onClearImages: () -> Unit,
    onStartStitching: () -> Unit,
    onNavigateToStitch: () -> Unit,
    isStartButtonEnabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 清空图片按钮
        Button(
            onClick = {
                if (isPageEntered && isDataLoaded) {
                    onClearImages()
                }
            },
            modifier = Modifier.weight(1f),
            enabled = uiState.selectedImages.isNotEmpty() && isPageEntered && isDataLoaded
        ) {
            Icon(Icons.Rounded.DeleteSweep, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.clear_images))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 开始拼接按钮
        Button(
            onClick = {
                if (isPageEntered && isDataLoaded) {
                    if (uiState.selectedImages.size >= 2) {
                        onNavigateToStitch()
                        onStartStitching()
                    }
                }
            },
            modifier = Modifier.weight(1f),
            enabled = uiState.selectedImages.size >= 2 && isPageEntered && isDataLoaded && isStartButtonEnabled
        ) {
            Icon(Icons.Rounded.SlowMotionVideo, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.start_stitching))
        }
    }
}