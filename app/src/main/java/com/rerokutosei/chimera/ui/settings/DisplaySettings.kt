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

import android.os.Build
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.model.ColorScheme
import com.rerokutosei.chimera.data.model.PredefinedColorSchemes
import com.rerokutosei.chimera.data.model.ThemeMode
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.ui.theme.ColorSchemePreview
import com.rerokutosei.chimera.ui.theme.CustomColorPickerDialog
import com.rerokutosei.chimera.utils.color.ColorUtils
import com.t8rin.fancyslider.shapes.DropletShape
import com.t8rin.fancyslider.shapes.EggShape
import com.t8rin.fancyslider.shapes.MaterialStarShape
import com.t8rin.fancyslider.shapes.OvalShape
import com.t8rin.fancyslider.shapes.PillShape
import com.t8rin.fancyslider.shapes.SmallMaterialStarShape
import com.t8rin.fancyslider.shapes.SquircleShape

@Composable
fun DisplaySettingsSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    DisplaySettingsHeader(modifier = modifier)
    
    ThemeModeSettings(uiState = uiState, viewModel = viewModel)
    
    ThemeColorSettings(uiState = uiState, viewModel = viewModel)
    
    SliderHandleIconSettings(uiState = uiState, viewModel = viewModel)
}

@Composable
fun DisplaySettingsHeader(modifier: Modifier = Modifier) {
    // 显示设置
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.BrightnessMedium,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.display_settings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ThemeModeSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    // 主题模式设置
    ListItem(
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.theme_mode),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 自动模式按钮
                    ThemeModeIconButton(
                        selected = uiState.themeMode == ThemeMode.AUTO,
                        onClick = { viewModel.setThemeMode(ThemeMode.AUTO) },
                        selectedIcon = if (isSystemInDarkTheme()) Icons.Rounded.BrightnessMedium else Icons.Rounded.BrightnessHigh,
                        unselectedIcon = Icons.Rounded.BrightnessAuto,
                        contentDescription = stringResource(R.string.auto_mode)
                    )
                        
                    // 浅色模式按钮
                    ThemeModeIconButton(
                        selected = uiState.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        selectedIcon = Icons.Rounded.Flare,
                        unselectedIcon = Icons.Rounded.LightMode,
                        contentDescription = stringResource(R.string.light_mode)
                    )
                        
                    // 深色模式按钮
                    ThemeModeIconButton(
                        selected = uiState.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        selectedIcon = Icons.Rounded.DarkMode,
                        unselectedIcon = Icons.Rounded.Bedtime,
                        contentDescription = stringResource(R.string.dark_mode)
                    )
                }
            }
        },
        supportingContent = null,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ThemeColorSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val themeSettingsManager = ThemeRepository.getInstance(context)
    val coroutineScope = rememberCoroutineScope()
    
    // 主题颜色设置
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.theme_color),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            var showCustomColorPicker by remember { mutableStateOf(false) }
            
            if (showCustomColorPicker) {
                CustomColorPickerDialog(
                    onColorSelected = { colorScheme ->
                        // 使用十六进制格式保存自定义色彩方案
                        viewModel.setCustomPrimaryColor(ColorUtils.formatColorToHex(colorScheme.primary))
                        viewModel.setCustomSecondaryColor(ColorUtils.formatColorToHex(colorScheme.secondary))
                        viewModel.setCustomTertiaryColor(ColorUtils.formatColorToHex(colorScheme.tertiary))
                        viewModel.setSelectedColorScheme("custom")
                    },
                    onDismissRequest = { showCustomColorPicker = false }
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val shouldUseDarkThemePreview = when (uiState.themeMode) {
                    ThemeMode.AUTO -> isSystemInDarkTheme() // 自动模式，跟随系统
                    ThemeMode.DARK -> true // 深色模式
                    ThemeMode.LIGHT -> false // 浅色模式
                }
                
                // 预定义颜色方案网格
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colorSchemes = getColorSchemes()
                    val context = LocalContext.current
                    
                    colorSchemes.forEach { (name, colorScheme) ->
                        val dynamicColorScheme = if (name == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val originalScheme = if (shouldUseDarkThemePreview) {
                                dynamicDarkColorScheme(context)
                            } else {
                                dynamicLightColorScheme(context)
                            }
                            
                            // 创建一个新的色彩方案，其中主色、次色、第三色使用相反主题的值
                            // 我也不知道为什么获取的动态色彩值在深浅色模式下颠倒
                            val invertedScheme = if (shouldUseDarkThemePreview) {
                                // 在深色主题预览下，使用浅色主题的主色系列
                                val lightScheme = dynamicLightColorScheme(context)
                                originalScheme.copy(
                                    primary = lightScheme.primary,
                                    onPrimary = lightScheme.onPrimary,
                                    primaryContainer = lightScheme.primaryContainer,
                                    onPrimaryContainer = lightScheme.onPrimaryContainer,
                                    inversePrimary = lightScheme.inversePrimary,
                                    secondary = lightScheme.secondary,
                                    onSecondary = lightScheme.onSecondary,
                                    secondaryContainer = lightScheme.secondaryContainer,
                                    onSecondaryContainer = lightScheme.onSecondaryContainer,
                                    tertiary = lightScheme.tertiary,
                                    onTertiary = lightScheme.onTertiary,
                                    tertiaryContainer = lightScheme.tertiaryContainer,
                                    onTertiaryContainer = lightScheme.onTertiaryContainer
                                )
                            } else {
                                // 在浅色主题预览下，使用深色主题的主色系列
                                val darkScheme = dynamicDarkColorScheme(context)
                                originalScheme.copy(
                                    primary = darkScheme.primary,
                                    onPrimary = darkScheme.onPrimary,
                                    primaryContainer = darkScheme.primaryContainer,
                                    onPrimaryContainer = darkScheme.onPrimaryContainer,
                                    inversePrimary = darkScheme.inversePrimary,
                                    secondary = darkScheme.secondary,
                                    onSecondary = darkScheme.onSecondary,
                                    secondaryContainer = darkScheme.secondaryContainer,
                                    onSecondaryContainer = darkScheme.onSecondaryContainer,
                                    tertiary = darkScheme.tertiary,
                                    onTertiary = darkScheme.onTertiary,
                                    tertiaryContainer = darkScheme.tertiaryContainer,
                                    onTertiaryContainer = darkScheme.onTertiaryContainer
                                )
                            }
                            
                            invertedScheme
                        } else {
                            null
                        }

                        val isColorPicker = name == "custom"
                        
                        ColorSchemeItem(
                            modifier = Modifier.weight(1f, fill = false),
                            colorScheme = colorScheme,
                            isSelected = uiState.selectedColorScheme == name,
                            isDarkThemePreview = shouldUseDarkThemePreview,
                            onClick = { 
                                if (isColorPicker) {
                                    showCustomColorPicker = true
                                } else {
                                    viewModel.setSelectedColorScheme(name)
                                }
                            },
                            dynamicColorScheme = dynamicColorScheme,
                            isColorPicker = isColorPicker
                        )
                    }
                    
                    // 显示当前保存的自定义色彩方案
                    val hasCustomColors = uiState.customPrimaryColor.isNotEmpty() && 
                                         uiState.customSecondaryColor.isNotEmpty() && 
                                         uiState.customTertiaryColor.isNotEmpty()
                    
                    // 只有在已有自定义颜色时才显示自定义颜色选择器入口和已保存的方案
                    if (hasCustomColors) {
                        val customScheme = ColorScheme(
                            name = "custom",
                            primary = parseColorString(uiState.customPrimaryColor),
                            secondary = parseColorString(uiState.customSecondaryColor),
                            tertiary = parseColorString(uiState.customTertiaryColor)
                        )
                        
                        ColorSchemeItem(
                            modifier = Modifier.weight(1f, fill = false),
                            colorScheme = customScheme,
                            isSelected = uiState.selectedColorScheme == "custom",
                            isDarkThemePreview = shouldUseDarkThemePreview,
                            onClick = { 
                                viewModel.setSelectedColorScheme("custom")
                            },
                            isColorPicker = false
                        )
                        
                        // 显示自定义颜色选择器入口
                        ColorSchemeItem(
                            modifier = Modifier.weight(1f, fill = false),
                            colorScheme = PredefinedColorSchemes.custom,
                            isSelected = false,
                            isDarkThemePreview = shouldUseDarkThemePreview,
                            onClick = { 
                                showCustomColorPicker = true
                            },
                            isColorPicker = true
                        )
                    } else {
                        // 如果没有自定义颜色，则只显示选择器入口
                        ColorSchemeItem(
                            modifier = Modifier.weight(1f, fill = false),
                            colorScheme = PredefinedColorSchemes.custom,
                            isSelected = uiState.selectedColorScheme == "custom",
                            isDarkThemePreview = shouldUseDarkThemePreview,
                            onClick = { 
                                showCustomColorPicker = true
                            },
                            isColorPicker = true
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * 解析颜色字符串
 * 支持多种格式：#AARRGGBB, #RRGGBB, Color(0.0, 1.0, 0.29411766, 1.0, sRGB IEC61966-2.1)
 */
private fun parseColorString(colorString: String): androidx.compose.ui.graphics.Color {
    return ColorUtils.parseColorString(colorString)
}

@Composable
fun SliderHandleIconSettings(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    // 滑块手柄图标
    val onPrimary = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.tertiary
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.slider_handle_icon),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shapes = listOf(
                    MaterialStarShape to stringResource(R.string.material_star_shape),
                    SmallMaterialStarShape to stringResource(R.string.small_material_star_shape),
                    DropletShape to stringResource(R.string.droplet_shape),
                    EggShape to stringResource(R.string.egg_shape),
                    OvalShape to stringResource(R.string.oval_shape),
                    PillShape to stringResource(R.string.pill_shape),
                    SquircleShape to stringResource(R.string.squircle_shape)
                )

                // 按钮和图标的大小
                val buttonSize = 40.dp
                val iconSize = buttonSize * 0.65f
                
                shapes.forEachIndexed { index, (shape, _) ->
                    OutlinedButton(
                        onClick = { 
                            viewModel.setSliderThumbShape(index)
                        },
                        modifier = Modifier.size(buttonSize),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp), // 0边距，让图标占据全部空间
                        colors = if (uiState.sliderThumbShape == index) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = primary,
                                contentColor = onPrimary
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = surface,
                                contentColor = onSurface
                            )
                        },
                        border = if (uiState.sliderThumbShape == index) {
                            null
                        } else {
                            BorderStroke(1.dp, outline)
                        }
                    ) {
                        val color = if (uiState.sliderThumbShape == index) onPrimary else onSurface
                        Canvas(modifier = Modifier.size(iconSize)) {
                            val outlineShape = shape.createOutline(size, layoutDirection, this)
                            if (outlineShape is Outline.Generic) {
                                if (!outlineShape.path.isEmpty) {
                                    drawPath(
                                        path = outlineShape.path,
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
    )

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = DividerDefaults.Thickness,
        color = DividerDefaults.color.copy(alpha = 0.5f)
    )
}

@Composable
fun ThemeModeIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    val transition = updateTransition(selected, label = "iconTransition")
    val containerColor by transition.animateColor(label = "containerColor") { selected ->
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    }
    val contentColor by transition.animateColor(label = "contentColor") { selected ->
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = contentDescription,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun ColorSchemeItem(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme,
    isSelected: Boolean,
    isDarkThemePreview: Boolean,
    onClick: () -> Unit,
    dynamicColorScheme: androidx.compose.material3.ColorScheme? = null,
    isColorPicker: Boolean = false
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val adjustedScheme = if (isDarkThemePreview && !colorScheme.isDynamic) {
            PredefinedColorSchemes.adjustColorsForDarkTheme(colorScheme)
        } else {
            colorScheme
        }
        ColorSchemePreview(
            colorScheme = adjustedScheme,
            isSelected = isSelected,
            isDarkTheme = isDarkThemePreview,
            onClick = onClick,
            dynamicColorScheme = dynamicColorScheme,
            isColorPicker = isColorPicker
        )
    }
}

private fun getColorSchemes(): List<Triple<String, ColorScheme, Boolean>> {
    val schemes = mutableListOf<Triple<String, ColorScheme, Boolean>>()
    
    schemes.add(Triple("bocchi", PredefinedColorSchemes.bocchi, false))
    schemes.add(Triple("nijika", PredefinedColorSchemes.nijika, false))
    schemes.add(Triple("kita", PredefinedColorSchemes.kita, false))
    schemes.add(Triple("ryo", PredefinedColorSchemes.ryo, false))
    
    // 动态色彩 (仅在 Android 12+ 可见)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        schemes.add(Triple("dynamic", PredefinedColorSchemes.dynamic, false))
    }
    return schemes
}