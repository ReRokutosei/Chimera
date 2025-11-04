/*
 * Based on ImageToolbox, an image editor for android
 * Original work Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Modified work Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * This file contains modifications from the original source code.
 * Original source: https://github.com/T8RIN/ImageToolbox
 */

package com.t8rin.fancyslider.utils.helper

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object FancySliderDefaults {
    val ThumbRadius = 12.dp
    val TrackHeight = 4.dp
    
    @Composable
    fun thumbColor(
        enabled: Boolean = true
    ): Color = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    @Composable
    fun trackColor(
        enabled: Boolean = true,
        active: Boolean = true
    ): Color = if (enabled) {
        if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        else MaterialTheme.colorScheme.surfaceVariant
    } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
}