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

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp

fun Modifier.fancySliderRippleEffect(
    bounded: Boolean = true,
    radius: Dp? = null,
    color: Color? = null
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "fancySliderRippleEffect"
        properties["bounded"] = bounded
        properties["radius"] = radius
        properties["color"] = color
    }
) {
    val interactionSource = remember { MutableInteractionSource() }
    Modifier.then(
        Modifier.rippleEffect(
            bounded = bounded,
            radius = radius ?: Dp.Unspecified,
            color = color ?: Color.Unspecified,
            interactionSource = interactionSource
        )
    )
}

@Composable
private fun rememberFancySliderRipple(
    bounded: Boolean = true,
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified
) = ripple(
    bounded = bounded,
    radius = radius,
    color = color
)

private fun Modifier.rippleEffect(
    bounded: Boolean = true,
    radius: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "ripple"
        properties["bounded"] = bounded
        properties["radius"] = radius
        properties["color"] = color
    }
) {
    this.then(
        Modifier.indication(
            interactionSource = interactionSource,
            indication = rememberFancySliderRipple(
                bounded = bounded,
                radius = radius,
                color = color
            )
        )
    )
}