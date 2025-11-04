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

package com.rerokutosei.chimera.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.model.ColorScheme
import com.rerokutosei.chimera.data.model.PredefinedColorSchemes
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.utils.color.ColorUtils

/**
 * 自定义颜色选择对话框
 * @param onColorSelected 颜色选择回调
 * @param onDismissRequest 关闭对话框回调
 */
@Composable
fun CustomColorPickerDialog(
    onColorSelected: (ColorScheme) -> Unit,
    onDismissRequest: () -> Unit
) {
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(Color.Red) }
    val context = LocalContext.current
    val themeSettingsManager = ThemeRepository.getInstance(context)
    val isDarkTheme by themeSettingsManager.getThemeModeFlow().collectAsState(initial = com.rerokutosei.chimera.data.model.ThemeMode.AUTO)
    val isCurrentlyDark = when (isDarkTheme) {
        com.rerokutosei.chimera.data.model.ThemeMode.AUTO -> isSystemInDarkTheme()
        com.rerokutosei.chimera.data.model.ThemeMode.DARK -> true
        com.rerokutosei.chimera.data.model.ThemeMode.LIGHT -> false
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.custom_color))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HSV 颜色选择器
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(10.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        selectedColor = colorEnvelope.color
                    }
                )
                
                // Alpha调节滑块
                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(35.dp),
                    controller = controller,
                    borderRadius = 17.dp
                )
                
                // Brightness调节滑块
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(top = 8.dp)
                        .height(35.dp),
                    controller = controller,
                    borderRadius = 17.dp
                )
                
                // 颜色预览和方案预览
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 当前选中颜色预览
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AlphaTile(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            controller = controller
                        )
                        
                        Text(
                            text = ColorUtils.formatColorToHex(selectedColor),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // 生成的完整色彩方案预览
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 根据当前主题模式调整颜色预览
                        val baseColorScheme = PredefinedColorSchemes.fromPrimaryColor(selectedColor)
                        val colorScheme = if (isCurrentlyDark) {
                            PredefinedColorSchemes.adjustColorsForDarkTheme(baseColorScheme)
                        } else {
                            baseColorScheme
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.secondary)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.tertiary)
                            )
                        }
                        
                        Text(
                            text = stringResource(R.string.preview),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val baseColorScheme = PredefinedColorSchemes.fromPrimaryColor(selectedColor)
                    // 保存时使用原始颜色，适配工作在主题应用时进行
                    val colorScheme = baseColorScheme
                    onColorSelected(colorScheme)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}