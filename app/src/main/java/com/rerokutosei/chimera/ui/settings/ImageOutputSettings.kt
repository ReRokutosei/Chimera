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

package com.rerokutosei.chimera.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R
import com.t8rin.fancyslider.fancy.FancySlider

@Composable
fun ImageOutputSettingsSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    // 图片输出设置
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Image,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.image_output_settings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }

    // 输出图片格式选择
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.output_format),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableOutlinedButton(
                    selected = uiState.outputImageFormat == 0,
                    text = "PNG",
                    onClick = { viewModel.setOutputImageFormat(0) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                )
                
                SelectableOutlinedButton(
                    selected = uiState.outputImageFormat == 1,
                    text = "JPEG",
                    onClick = { viewModel.setOutputImageFormat(1) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                )
                
                SelectableOutlinedButton(
                    selected = uiState.outputImageFormat == 2,
                    text = "WEBP",
                    onClick = { viewModel.setOutputImageFormat(2) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                )
            }
        }
    )

    // 输出图片质量滑块 (仅在 JPEG 或 WEBP 格式时显示)
    if (uiState.outputImageFormat == 1 || uiState.outputImageFormat == 2) {
        ListItem(
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.output_quality),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${uiState.outputImageQuality}%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            },
            supportingContent = {
                FancySlider(
                    value = uiState.outputImageQuality.toFloat(),
                    onValueChange = { viewModel.setOutputImageQuality(it.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    ),
                    thumbShape = getThumbShape(uiState.sliderThumbShape),
                    drawContainer = false
                )
            }
        )
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = DividerDefaults.Thickness,
        color = DividerDefaults.color.copy(alpha = 0.5f)
    )
}

fun getThumbShape(shapeIndex: Int): androidx.compose.ui.graphics.Shape {
    return when (shapeIndex) {
        0 -> com.t8rin.fancyslider.shapes.MaterialStarShape
        1 -> com.t8rin.fancyslider.shapes.SmallMaterialStarShape
        2 -> com.t8rin.fancyslider.shapes.DropletShape
        3 -> com.t8rin.fancyslider.shapes.EggShape
        4 -> com.t8rin.fancyslider.shapes.OvalShape
        5 -> com.t8rin.fancyslider.shapes.PillShape
        6 -> com.t8rin.fancyslider.shapes.SquircleShape
        else -> com.t8rin.fancyslider.shapes.MaterialStarShape
    }
}