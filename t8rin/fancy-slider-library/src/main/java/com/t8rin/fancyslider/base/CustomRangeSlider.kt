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

import androidx.annotation.IntRange
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

/**
 * <a href="https://m3.material.io/components/sliders/overview" class="external" target="_blank">Material Design slider</a>.
 *
 * Sliders allow users to make selections from a range of values.
 *
 * Sliders reflect a range of values along a bar, from which users may select a single value.
 * They are ideal for adjusting settings such as volume, brightness, or applying image filters.
 *
 * ![Sliders image](https://developer.android.com/images/reference/androidx/compose/material3/sliders.png)
 *
 * Use continuous sliders to allow users to make meaningful selections that don't
 * require a specific value:
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @param value current value range of the slider. If outside of [valueRange] provided, value will be
 * coerced to this range.
 * @param onValueChange callback in which value should be updated
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 * respond to user input, and it will appear visually disabled and disabled to accessibility
 * services.
 * @param valueRange range of values that this slider can take. The passed [value] will be coerced
 * to this range.
 * @param steps if greater than 0, specifies the amount of discrete allowable values, evenly
 * distributed across the whole value range. If 0, the slider will behave continuously and allow any
 * value from the range specified. Must not be negative.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 * update the slider value (use [onValueChange] instead), but rather to know when the user has
 * completed selecting a new value by ending a drag or a click.
 * @param colors [com.t8rin.fancyslider.CustomSliderColors] that will be used to resolve the colors used for this slider in
 * different states. See [com.t8rin.fancyslider.CustomSliderDefaults.colors].
 * @param startInteractionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for the start thumb. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param endInteractionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for the end thumb. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param startThumb the thumb to be displayed on the start of the slider, it is placed on top of the track.
 * @param endThumb the thumb to be displayed on the end of the slider, it is placed on top of the track.
 * @param track the track to be displayed on the slider, it is placed underneath the thumb.
 */
@Composable
fun CustomRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    startThumb: @Composable () -> Unit = {
        CustomSliderDefaults.Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
            thumbSize = DpSize(20.dp, 20.dp)
        )
    },
    endThumb: @Composable () -> Unit = {
        CustomSliderDefaults.Thumb(
            interactionSource = endInteractionSource,
            colors = colors,
            enabled = enabled,
            thumbSize = DpSize(20.dp, 20.dp)
        )
    },
    track: @Composable (CustomRangeSliderState) -> Unit = { sliderState ->
        CustomSliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            rangeSliderState = sliderState
        )
    }
) {
    val state = remember(value, valueRange, steps, onValueChangeFinished) {
        CustomRangeSliderState(
            activeRangeStart = value.start,
            activeRangeEnd = value.endInclusive,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange
        )
    }

    state.onValueChange = {
        onValueChange(it.start..it.endInclusive)
    }

    CustomRangeSlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = startThumb,
        endThumb = endThumb,
        track = track
    )
}

/**
 * <a href="https://m3.material.io/components/sliders/overview" class="external" target="_blank">Material Design slider</a>.
 *
 * Sliders allow users to make selections from a range of values.
 *
 * Sliders reflect a range of values along a bar, from which users may select a single value.
 * They are ideal for adjusting settings such as volume, brightness, or applying image filters.
 *
 * ![Sliders image](https://developer.android.com/images/reference/androidx/compose/material3/sliders.png)
 *
 * Use continuous sliders to allow users to make meaningful selections that don't
 * require a specific value:
 *
 * You can allow the user to choose only between predefined set of values by specifying the amount
 * of steps between min and max values:
 *
 * @param state [CustomRangeSliderState] which contains the slider's current value range.
 * @param modifier the [Modifier] to be applied to this slider
 * @param enabled controls the enabled state of this slider. When `false`, this component will not
 * respond to user input, and it will appear visually disabled and disabled to accessibility
 * services.
 * @param colors [CustomSliderColors] that will be used to resolve the colors used for this slider in
 * different states. See [CustomSliderDefaults.colors].
 * @param startInteractionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for the start thumb. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param endInteractionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for the end thumb. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 * @param startThumb the thumb to be displayed on the start of the slider, it is placed on top of the track.
 * @param endThumb the thumb to be displayed on the end of the slider, it is placed on top of the track.
 * @param track the track to be displayed on the slider, it is placed underneath the thumb.
 */
@Composable
fun CustomRangeSlider(
    state: CustomRangeSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    startThumb: @Composable () -> Unit = {
        CustomSliderDefaults.Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
            thumbSize = DpSize(20.dp, 20.dp)
        )
    },
    endThumb: @Composable () -> Unit = {
        CustomSliderDefaults.Thumb(
            interactionSource = endInteractionSource,
            colors = colors,
            enabled = enabled,
            thumbSize = DpSize(20.dp, 20.dp)
        )
    },
    track: @Composable (CustomRangeSliderState) -> Unit = { sliderState ->
        CustomSliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            rangeSliderState = sliderState
        )
    }
) {
    require(state.steps >= 0) { "steps should be >= 0" }
    state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val startDrag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = state.isRtl,
        enabled = enabled,
        interactionSource = startInteractionSource,
        onDragStopped = { velocity: Float -> state.gestureEndAction(false) },
        startDragImmediately = state.startSteps > 0,
        state = remember(state) {
            object : DraggableState {
                override suspend fun drag(
                    dragPriority: MutatePriority,
                    block: suspend DragScope.() -> Unit
                ) = coroutineScope { 
                    block(object : DragScope {
                        override fun dragBy(pixels: Float) {
                            dispatchRawDelta(pixels)
                        }
                    })
                }

                override fun dispatchRawDelta(delta: Float) {
                    state.onDrag(true, delta)
                }
            }
        }
    )

    val endDrag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = state.isRtl,
        enabled = enabled,
        interactionSource = endInteractionSource,
        onDragStopped = { velocity: Float -> state.gestureEndAction(true) },
        startDragImmediately = state.endSteps > 0,
        state = remember(state) {
            object : DraggableState {
                override suspend fun drag(
                    dragPriority: MutatePriority,
                    block: suspend DragScope.() -> Unit
                ) = coroutineScope { 
                    block(object : DragScope {
                        override fun dragBy(pixels: Float) {
                            dispatchRawDelta(pixels)
                        }
                    })
                }

                override fun dispatchRawDelta(delta: Float) {
                    state.onDrag(false, delta)
                }
            }
        }
    )

    Layout(
        {
            Box(modifier = Modifier.layoutId(RangeSliderComponents.TRACK)) {
                track(state)
            }
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.START_THUMB)
                    .then(startDrag)
            ) {
                startThumb()
            }
            Box(
                modifier = Modifier
                    .layoutId(RangeSliderComponents.END_THUMB)
                    .then(endDrag)
            ) {
                endThumb()
            }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = 48.dp,
                minHeight = 48.dp
            )
            .progressSemantics(
                value = (state.activeRangeStart + state.activeRangeEnd) / 2f,
                valueRange = state.valueRange.start..state.valueRange.endInclusive,
                steps = state.steps
            )
    ) { measurables, constraints ->
        val startThumbPlaceable = measurables.fastFirst {
            it.layoutId == RangeSliderComponents.START_THUMB
        }.measure(constraints)

        val endThumbPlaceable = measurables.fastFirst {
            it.layoutId == RangeSliderComponents.END_THUMB
        }.measure(constraints)

        val trackPlaceable = measurables.fastFirst {
            it.layoutId == RangeSliderComponents.TRACK
        }.measure(
            constraints.offset(
                horizontal = -maxOf(
                    startThumbPlaceable.width,
                    endThumbPlaceable.width
                )
            ).copy(minHeight = 0)
        )

        val sliderWidth = maxOf(
            startThumbPlaceable.width,
            endThumbPlaceable.width
        ) + trackPlaceable.width + maxOf(
            startThumbPlaceable.width,
            endThumbPlaceable.width
        )
        val sliderHeight = maxOf(
            trackPlaceable.height,
            startThumbPlaceable.height,
            endThumbPlaceable.height
        )

        state.startThumbWidth = startThumbPlaceable.width.toFloat()
        state.endThumbWidth = endThumbPlaceable.width.toFloat()
        state.totalWidth = sliderWidth
        state.updateMinMaxPx()

        val trackOffsetX = maxOf(startThumbPlaceable.width, endThumbPlaceable.width)
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2

        val startThumbOffsetX = ((trackPlaceable.width) * state.coercedActiveRangeStartAsFraction).roundToInt()
        val startThumbOffsetY = (sliderHeight - startThumbPlaceable.height) / 2

        val endThumbOffsetX = ((trackPlaceable.width) * state.coercedActiveRangeEndAsFraction).roundToInt()
        val endThumbOffsetY = (sliderHeight - endThumbPlaceable.height) / 2

        layout(sliderWidth, sliderHeight) {
            trackPlaceable.placeRelative(
                trackOffsetX,
                trackOffsetY
            )
            startThumbPlaceable.placeRelative(
                startThumbOffsetX,
                startThumbOffsetY
            )
            endThumbPlaceable.placeRelative(
                trackOffsetX + endThumbOffsetX,
                endThumbOffsetY
            )
        }
    }
}

private enum class RangeSliderComponents {
    START_THUMB,
    END_THUMB,
    TRACK
}