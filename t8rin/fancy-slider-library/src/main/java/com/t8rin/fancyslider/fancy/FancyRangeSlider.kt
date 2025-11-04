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

package com.t8rin.fancyslider.fancy

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.t8rin.fancyslider.base.CustomSliderDefaults
import com.t8rin.fancyslider.base.CustomRangeSlider
import com.t8rin.fancyslider.modifier.container
import com.t8rin.fancyslider.modifier.materialShadow
import com.t8rin.fancyslider.base.toCustom
import com.t8rin.fancyslider.shapes.MaterialStarShape
import com.t8rin.fancyslider.utils.provider.ProvidesValue
import com.t8rin.fancyslider.utils.theme.outlineVariant

/**
 * A custom range slider with fancy animations and visual effects.
 *
 * @param value current value range of the slider
 * @param onValueChange called when the value is changed
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider
 * @param valueRange range of values that this slider can take
 * @param steps if greater than 0, specifies the amounts of discrete steps that this slider
 * will have between [valueRange].start and [valueRange].endInclusive
 * @param onValueChangeFinished called when the value change is finished
 * @param colors [SliderColors] that will be used to resolve the colors used for this slider
 * @param thumbShape shape of the slider thumb
 * @param drawContainer whether to draw the slider container
 * @param drawShadows whether to draw shadows for the slider thumbs
 */
@Composable
fun FancyRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    thumbShape: Shape = MaterialStarShape,
    drawContainer: Boolean = true,
    drawShadows: Boolean = true
) {
    val thumbColor by animateColorAsState(
        if (enabled) colors.thumbColor else colors.disabledThumbColor
    )

    val animatedStartValue by animateFloatAsState(value.start)
    val animatedEndValue by animateFloatAsState(value.endInclusive)
    
    val startInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource = remember { MutableInteractionSource() }

    ProvidesValue(LocalDensity, LocalDensity.current) {
        var scaleX by remember { mutableFloatStateOf(1f) }
        var scaleY by remember { mutableFloatStateOf(1f) }
        var translateX by remember { mutableFloatStateOf(0f) }
        var transformOrigin by remember { mutableStateOf(TransformOrigin.Center) }

        CustomRangeSlider(
            value = value,
            onValueChange = onValueChange,
            startInteractionSource = startInteractionSource,
            endInteractionSource = endInteractionSource,
            enabled = enabled,
            modifier = modifier
                .graphicsLayer {
                    this.transformOrigin = transformOrigin
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    this.translationX = translateX
                }
                .then(
                    if (drawContainer) {
                        Modifier.Companion
                            .container(
                                shape = CircleShape,
                                autoShadowElevation = animateDpAsState(
                                    if (drawShadows) 1.dp else 0.dp
                                ).value,
                                resultPadding = 0.dp,
                                borderColor = MaterialTheme.colorScheme
                                    .outlineVariant()
                                    .copy(0.3f),
                                color = Color.Transparent
                                    .copy(0.5f)
                                    .compositeOver(MaterialTheme.colorScheme.surface)
                                    .copy(colors.activeTrackColor.alpha),
                                composeColorOnTopOfBackground = false
                            )
                            .padding(horizontal = 6.dp)
                    } else Modifier
                ),
            colors = colors.toCustom(),
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            startThumb = {
                Spacer(
                    Modifier
                        .zIndex(100f)
                        .size(26.dp)
                        .indication(
                            interactionSource = startInteractionSource,
                            indication = ripple(
                                bounded = false,
                                radius = 24.dp
                            )
                        )
                        .hoverable(interactionSource = startInteractionSource)
                        .materialShadow(
                            shape = thumbShape,
                            elevation = 1.dp,
                            enabled = drawShadows
                        )
                        .background(thumbColor, thumbShape)
                )
            },
            endThumb = {
                Spacer(
                    Modifier
                        .zIndex(100f)
                        .size(26.dp)
                        .indication(
                            interactionSource = endInteractionSource,
                            indication = ripple(
                                bounded = false,
                                radius = 24.dp
                            )
                        )
                        .hoverable(interactionSource = endInteractionSource)
                        .materialShadow(
                            shape = thumbShape,
                            elevation = 1.dp,
                            enabled = drawShadows
                        )
                        .background(thumbColor, thumbShape)
                )
            },
            track = { sliderState ->
                val density = LocalDensity.current
                val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

                CustomSliderDefaults.Track(
                    rangeSliderState = sliderState,
                    colors = colors.toCustom(),
                    trackHeight = 38.dp,
                    enabled = enabled
                )
            }
        )
    }
}