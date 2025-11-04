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

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rerokutosei.chimera.data.model.ColorScheme
import com.rerokutosei.chimera.data.model.PredefinedColorSchemes
import com.rerokutosei.chimera.data.model.ThemeMode
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.utils.color.ColorUtils

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeSettingsManager = ThemeRepository.getInstance(context)
    
    // 获取颜色方案设置
    val selectedColorScheme by themeSettingsManager.getSelectedColorSchemeFlow().collectAsState(initial = "bocchi")
    
    val colorScheme = getColorScheme(
        selectedColorScheme = selectedColorScheme,
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        context = context,
        themeSettingsManager = themeSettingsManager
    )
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val insetsController = WindowCompat.getInsetsController(window, view)

            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun getColorScheme(
    selectedColorScheme: String,
    darkTheme: Boolean,
    dynamicColor: Boolean,
    context: Context,
    themeSettingsManager: ThemeRepository
): androidx.compose.material3.ColorScheme {
    return when {
        dynamicColor && selectedColorScheme == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

            val originalScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            
            // 创建一个新的色彩方案，其中主色、次色、第三色使用相反主题的值
            val invertedScheme = if (darkTheme) {
                // 在深色主题下，使用浅色主题的主色系列，但保持深色主题的背景色
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
                // 在浅色主题下，使用深色主题的主色系列，但保持浅色主题的背景色
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
        }

        // 自定义颜色方案
        selectedColorScheme == "custom" -> getCustomColorScheme(themeSettingsManager, darkTheme)

        else -> getPredefinedColorScheme(selectedColorScheme, darkTheme)
    }
}

@Composable
private fun getCustomColorScheme(
    themeSettingsManager: ThemeRepository,
    darkTheme: Boolean
): androidx.compose.material3.ColorScheme {
    val customPrimary by themeSettingsManager.getCustomPrimaryColorFlow().collectAsState(initial = "")
    val customSecondary by themeSettingsManager.getCustomSecondaryColorFlow().collectAsState(initial = "")
    val customTertiary by themeSettingsManager.getCustomTertiaryColorFlow().collectAsState(initial = "")
    
    // 如果自定义颜色为空，则使用默认的 Bocchi 颜色方案作为后备
    val primary = if (customPrimary.isNotEmpty()) {
        parseColorSafely(customPrimary, PredefinedColorSchemes.bocchi.primary)
    } else {
        PredefinedColorSchemes.bocchi.primary
    }
    
    val secondary = if (customSecondary.isNotEmpty()) {
        parseColorSafely(customSecondary, PredefinedColorSchemes.bocchi.secondary)
    } else {
        PredefinedColorSchemes.bocchi.secondary
    }
    
    val tertiary = if (customTertiary.isNotEmpty()) {
        parseColorSafely(customTertiary, PredefinedColorSchemes.bocchi.tertiary)
    } else {
        PredefinedColorSchemes.bocchi.tertiary
    }
    
    // 调整自定义颜色以适应深色主题
    val adjustedScheme = if (darkTheme) {
        PredefinedColorSchemes.adjustColorsForDarkTheme(
            ColorScheme(
                name = "custom",
                primary = primary,
                secondary = secondary,
                tertiary = tertiary
            )
        )
    } else {
        ColorScheme(
            name = "custom",
            primary = primary,
            secondary = secondary,
            tertiary = tertiary
        )
    }
    
    return if (darkTheme) {
        darkColorScheme(
            primary = adjustedScheme.primary,
            secondary = adjustedScheme.secondary,
            tertiary = adjustedScheme.tertiary
        )
    } else {
        lightColorScheme(
            primary = adjustedScheme.primary,
            secondary = adjustedScheme.secondary,
            tertiary = adjustedScheme.tertiary
        )
    }
}

/**
 * 解析颜色字符串
 * @param colorString 颜色字符串
 * @param defaultColor 解析失败时的默认颜色
 * @return 解析后的颜色或默认颜色
 */
private fun parseColorSafely(colorString: String, defaultColor: Color): Color {
    return ColorUtils.parseColorSafely(colorString, defaultColor)
}

private fun getPredefinedColorScheme(
    selectedColorScheme: String,
    darkTheme: Boolean
): androidx.compose.material3.ColorScheme {
    val scheme = PredefinedColorSchemes.findByName(selectedColorScheme)
    val adjustedScheme = if (darkTheme) {
        PredefinedColorSchemes.adjustColorsForDarkTheme(scheme)
    } else {
        scheme
    }
    
    return if (darkTheme) {
        darkColorScheme(
            primary = adjustedScheme.primary,
            secondary = adjustedScheme.secondary,
            tertiary = adjustedScheme.tertiary
        )
    } else {
        lightColorScheme(
            primary = adjustedScheme.primary,
            secondary = adjustedScheme.secondary,
            tertiary = adjustedScheme.tertiary
        )
    }
}

/**
 * 根据主题设置确定是否使用深色主题
 * @param context 上下文
 * @return 是否使用深色主题
 */
@Composable
fun shouldUseDarkTheme(): Boolean {
    val context = LocalContext.current
    val themeSettingsManager = ThemeRepository.getInstance(context)
    val themeMode by themeSettingsManager.getThemeModeFlow().collectAsState(initial = ThemeMode.AUTO)
    
    return when (themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme() // 自动模式，跟随系统
        ThemeMode.DARK -> true // 深色模式
        ThemeMode.LIGHT -> false // 浅色模式
    }
}