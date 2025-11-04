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

package com.rerokutosei.chimera.data.model

import androidx.compose.ui.graphics.Color
import com.rerokutosei.chimera.utils.color.ColorUtils

/**
 * 预定义颜色方案集合
 */
object PredefinedColorSchemes {
    // 动态色彩方案（基于系统壁纸）
    val dynamic = ColorScheme(
        name = "dynamic",
        primary = Color.Unspecified,
        secondary = Color.Unspecified,
        tertiary = Color.Unspecified,
        isDynamic = true
    )

    // Bocchi主题 - 粉色 (#FF6496)
    val bocchi = ColorScheme(
        name = "bocchi",
        primary = Color(0xFFFF6496),
        secondary = Color(0xFFFF96B4),
        tertiary = Color(0xFFFFB6C1)
    )

    // Nijika主题 - 黄色 (#FABE00)
    val nijika = ColorScheme(
        name = "nijika",
        primary = Color(0xFFFABE00),
        secondary = Color(0xFFFFD700),
        tertiary = Color(0xFFFFE55C)
    )

    // Ryo主题 - 蓝色 (#006EBE)
    val ryo = ColorScheme(
        name = "ryo",
        primary = Color(0xFF006EBE),
        secondary = Color(0xFF008CFF),
        tertiary = Color(0xFF5C9CFF)
    )

    // Kita主题 - 红色 (#E60046)
    val kita = ColorScheme(
        name = "kita",
        primary = Color(0xFFE60046),
        secondary = Color(0xFFFF3366),
        tertiary = Color(0xFFFF6699)
    )

    // 自定义颜色方案占位符
    val custom = ColorScheme(
        name = "custom",
        primary = Color.Unspecified,
        secondary = Color.Unspecified,
        tertiary = Color.Unspecified
    )

    // 获取所有预定义方案列表
    val allPredefined = listOf(dynamic, bocchi, nijika, kita, ryo, custom)
    
    /**
     * 根据名称查找颜色方案
     */
    fun findByName(name: String): ColorScheme {
        return when (name) {
            "bocchi" -> bocchi
            "nijika" -> nijika
            "kita" -> kita
            "ryo" -> ryo
            "custom" -> custom
            else -> dynamic // 默认返回动态色彩方案
        }
    }
    
    // 从主色调生成完整的颜色方案
    fun fromPrimaryColor(primary: Color): ColorScheme {
        return ColorScheme(
            name = "custom",
            primary = primary,
            secondary = generateSecondaryColor(primary),
            tertiary = generateTertiaryColor(primary)
        )
    }
    
    // 生成协调的次色
    private fun generateSecondaryColor(primary: Color): Color {
        return ColorUtils.generateSecondaryColor(primary)
    }
    
    // 生成协调的第三色
    private fun generateTertiaryColor(primary: Color): Color {
        return ColorUtils.generateTertiaryColor(primary)
    }
    
    // 将Color转换为HSV数组
    private fun colorToHSV(color: Color): FloatArray {
        return ColorUtils.colorToHSV(color)
    }
    
    /**
     * 调整颜色以适应深色主题
     * 降低颜色的亮度并适当增加饱和度
     */
    fun adjustColorsForDarkTheme(colorScheme: ColorScheme): ColorScheme {
        if (colorScheme.isDynamic) {
            return colorScheme
        }
        
        return ColorScheme(
            name = colorScheme.name,
            primary = adjustColorForDarkTheme(colorScheme.primary),
            secondary = adjustColorForDarkTheme(colorScheme.secondary),
            tertiary = adjustColorForDarkTheme(colorScheme.tertiary)
        )
    }
    
    /**
     * 调整单个颜色以适应深色主题
     */
    private fun adjustColorForDarkTheme(color: Color): Color {
        return ColorUtils.adjustColorForDarkTheme(color)
    }
}