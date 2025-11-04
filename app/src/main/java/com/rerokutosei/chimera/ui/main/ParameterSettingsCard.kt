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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.ui.settings.getThumbShape
import com.t8rin.fancyslider.fancy.FancySlider

@Composable
fun ParameterSettingsCard(
    uiState: MainUiState,
    isPageEntered: Boolean,
    isDataLoaded: Boolean,
    sliderThumbShape: Int,
    onUpdateStitchMode: (StitchMode) -> Unit,
    onUpdateOverlayMode: (OverlayMode) -> Unit,
    onUpdateWidthScale: (WidthScale) -> Unit,
    onUpdateOverlayArea: (Int) -> Unit,
    onUpdateImageSpacing: (Int) -> Unit
) {
    // 拼接参数设置
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stitch_direction),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.95f
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                val stitchModes = listOf(
                    StitchMode.DIRECT_HORIZONTAL,
                    StitchMode.DIRECT_VERTICAL
                )

                CustomSegmentedButtonRow(
                    options = stitchModes,
                    selectedOption = uiState.stitchMode,
                    onOptionSelected = {
                        if (isPageEntered && isDataLoaded) {
                            onUpdateStitchMode(it)
                        }
                    },
                    optionDisplayName = { getModeDisplayName(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.image_overlay),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.95f
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                val overlayModes = listOf(
                    OverlayMode.DISABLED,
                    OverlayMode.ENABLED
                )

                CustomSegmentedButtonRow(
                    options = overlayModes,
                    selectedOption = uiState.overlayMode,
                    onOptionSelected = {
                        if (isPageEntered && isDataLoaded) {
                            onUpdateOverlayMode(it)
                        }
                    },
                    optionDisplayName = { 
                        when (it) {
                            OverlayMode.DISABLED -> stringResource(R.string.overlay_disabled)
                            OverlayMode.ENABLED -> stringResource(R.string.overlay_enabled)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.overlayMode == OverlayMode.ENABLED) {
                val scaleText = if (uiState.stitchMode == StitchMode.DIRECT_VERTICAL) {
                    stringResource(R.string.width_scaling)
                } else {
                    stringResource(R.string.height_scaling)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scaleText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.95f
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // 叠加模式下，只显示最小和最大选项，隐藏原始选项
                    val widthScales = listOf(
                        WidthScale.MIN_WIDTH,
                        WidthScale.MAX_WIDTH
                    )

                    CustomSegmentedButtonRow(
                        options = widthScales,
                        selectedOption = uiState.widthScale,
                        onOptionSelected = {
                            if (isPageEntered && isDataLoaded) {
                                onUpdateWidthScale(it)
                            }
                        },
                        optionDisplayName = {
                            if (uiState.stitchMode == StitchMode.DIRECT_VERTICAL) {
                                getWidthScaleDisplayName(it)
                            } else {
                                getHeightScaleDisplayName(it)
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.overlay_ratio),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.95f
                        )
                    )
                    Text("${uiState.overlayArea}%")
                }
                FancySlider(
                    value = uiState.overlayArea.toFloat(),
                    onValueChange = {
                        if (isPageEntered && isDataLoaded) {
                            onUpdateOverlayArea(it.toInt())
                        }
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isPageEntered && isDataLoaded,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    ),
                    thumbShape = getThumbShape(sliderThumbShape),
                    drawContainer = false
                )
            } else {
                val scaleText = if (uiState.stitchMode == StitchMode.DIRECT_VERTICAL) {
                    stringResource(R.string.width_scaling)
                } else {
                    stringResource(R.string.height_scaling)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scaleText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.95f
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // 当不使用叠加模式时，显示所有缩放选项
                    val widthScales = listOf(
                        WidthScale.MIN_WIDTH,
                        WidthScale.NONE,
                        WidthScale.MAX_WIDTH
                    )

                    CustomSegmentedButtonRow(
                        options = widthScales,
                        selectedOption = uiState.widthScale,
                        onOptionSelected = {
                            if (isPageEntered && isDataLoaded) {
                                onUpdateWidthScale(it)
                            }
                        },
                        optionDisplayName = {
                            if (uiState.stitchMode == StitchMode.DIRECT_VERTICAL) {
                                getWidthScaleDisplayName(it)
                            } else {
                                getHeightScaleDisplayName(it)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.image_spacing),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize * 0.95f
                        )
                    )
                    Text(
                        text = "${uiState.imageSpacing.toString().padStart(2, '0')}px"
                    )
                }
                
                // 图片间隔滑块
                FancySlider(
                    value = uiState.imageSpacing.toFloat(),
                    onValueChange = {
                        if (isPageEntered && isDataLoaded) {
                            onUpdateImageSpacing(it.toInt())
                        }
                    },
                    valueRange = 0f..50f,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isPageEntered && isDataLoaded,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    ),
                    thumbShape = getThumbShape(sliderThumbShape),
                    drawContainer = false
                )

            }
        }
    }
}