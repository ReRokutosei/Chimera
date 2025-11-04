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

package com.t8rin.fancyslider.base

import androidx.compose.ui.util.lerp
import kotlin.math.abs

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
internal fun calcFraction(
    a: Float,
    b: Float,
    pos: Float
) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

// Scale x1 from a1..b1 range to a2..b2 range
internal fun scale(
    a1: Float,
    b1: Float,
    x1: Float,
    a2: Float,
    b2: Float
) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

// Scale x.start, x.endInclusive from a1..b1 range to a2..b2 range
internal fun scale(
    a1: Float,
    b1: Float,
    x: CustomSliderRange,
    a2: Float,
    b2: Float
) = CustomSliderRange(
    scale(a1 = a1, b1 = b1, x1 = x.start, a2 = a2, b2 = b2),
    scale(a1, b1, x.endInclusive, a2, b2)
)

internal fun snapValueToTick(
    current: Float,
    tickFractions: FloatArray,
    minPx: Float,
    maxPx: Float
): Float {
    // target is a closest anchor to the `current`, if exists
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

internal fun stepsToTickFractions(steps: Int): FloatArray {
    return if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
}