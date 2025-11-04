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

package com.t8rin.fancyslider.utils.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AnimateFloatingRange(
    initialValue: Float,
    private val animationSpec: AnimationSpec<Float> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = 0.001f
    )
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val animation = Animatable(initialValue)
    
    var value by mutableFloatStateOf(initialValue)
        private set

    var targetValue by mutableFloatStateOf(initialValue)
        private set

    fun snapTo(value: Float) {
        scope.launch {
            animation.snapTo(value)
            targetValue = value
            this@AnimateFloatingRange.value = value
        }
    }

    fun updateTarget(target: Float) {
        if (target != targetValue) {
            targetValue = target
            scope.launch {
                animation.animateTo(
                    targetValue = target,
                    animationSpec = animationSpec
                ) { this@AnimateFloatingRange.value = value }
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}