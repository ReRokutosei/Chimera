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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.t8rin.fancyslider.utils.animation.AnimateFloatingRange

@Composable
fun rememberAnimatedRangeState(
    positionFraction: Float,
    isPressed: Boolean,
    onValueChanged: (Float) -> Unit
): AnimatedRangeState = remember {
    AnimatedRangeState(
        positionFraction = positionFraction,
        isPressed = isPressed,
        onValueChanged = onValueChanged
    )
}

class AnimatedRangeState(
    positionFraction: Float,
    private val isPressed: Boolean,
    private val onValueChanged: (Float) -> Unit
) {
    var floatingRange = AnimateFloatingRange(
        initialValue = positionFraction
    )
        private set
    
    var isDragging = false
        private set
    
    var pressOffset = Offset.Zero
        private set

    fun updateFloatingRange(positionFraction: Float) {
        floatingRange.snapTo(positionFraction)
    }

    fun onPress(offset: Offset) {
        isDragging = true
        pressOffset = offset
    }

    fun onRelease() {
        isDragging = false
        pressOffset = Offset.Zero
    }

    fun onDrag(offset: Offset) {
        onValueChanged(offset.x)
    }

    fun updateFloatingRangeTarget(positionFraction: Float) {
        floatingRange.updateTarget(positionFraction)
    }
}