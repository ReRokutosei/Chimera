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

package com.rerokutosei.chimera.utils.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import kotlin.math.max
import kotlin.math.min

/**
 * 颜色处理工具类
 */
object ColorUtils {

    /**
     * 解析颜色字符串
     * @param colorString 颜色字符串
     * @param defaultColor 解析失败时的默认颜色
     * @return 解析后的颜色或默认颜色
     */
    fun parseColorSafely(colorString: String, defaultColor: Color): Color {
        return try {
            Color(colorString.toColorInt())
        } catch (e: Exception) {
            defaultColor
        }
    }

    /**
     * 解析颜色字符串
     * 支持多种格式：#AARRGGBB, #RRGGBB, Color(0.0, 1.0, 0.29411766, 1.0, sRGB IEC61966-2.1)
     */
    fun parseColorString(colorString: String): Color {
        return try {
            // 如果是十六进制格式
            if (colorString.startsWith("#")) {
                return Color(colorString.toColorInt())
            }
            
            // 如果是 Color(r, g, b, a, ...) 格式
            if (colorString.startsWith("Color(")) {
                val values = colorString.substring(6, colorString.indexOfLast { it == ',' })
                    .split(",")
                    .map { it.trim().toFloat() }
                
                if (values.size >= 4) {
                    // RGBA 格式 (0-1 范围)
                    return Color(
                        red = values[0],
                        green = values[1],
                        blue = values[2],
                        alpha = values[3]
                    )
                }
            }
            
            // 默认尝试解析
            Color(colorString.toColorInt())
        } catch (e: Exception) {
            // 如果所有方法都失败，返回默认颜色
            Color.Red
        }
    }

    /**
     * 将颜色格式化为十六进制字符串
    */
    fun formatColorToHex(color: Color): String {
        return String.format("#%08X", color.toArgb())
    }

    /**
     * 将Color转换为HSV数组
     */
    fun colorToHSV(color: Color): FloatArray {
        val hsv = FloatArray(3)
        val rgb = color.toArgb()
        val r = ((rgb shr 16) and 0xFF) / 255f
        val g = ((rgb shr 8) and 0xFF) / 255f
        val b = (rgb and 0xFF) / 255f
        
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val delta = max - min
        
        // 计算色相
        hsv[0] = when (max) {
            min -> 0f
            r -> 60 * ((g - b) / delta % 6)
            g -> 60 * ((b - r) / delta + 2)
            else -> 60 * ((r - g) / delta + 4)
        }
        if (hsv[0] < 0) hsv[0] += 360f
        
        // 计算饱和度
        hsv[1] = if (max == 0f) 0f else delta / max
        
        // 计算明度
        hsv[2] = max
        
        return hsv
    }

    /**
     * 调整颜色以适应深色主题
     * 降低亮度并在必要时增加饱和度
     */
    fun adjustColorForDarkTheme(color: Color): Color {
        val hsv = colorToHSV(color)
        
        // 降低明度
        hsv[2] = max(0.4f, hsv[2] * 0.8f)
        
        // 增加饱和度以补偿亮度降低
        hsv[1] = min(1f, hsv[1] * 1.2f)
        
        return Color.hsv(hsv[0], hsv[1], hsv[2])
    }

    /**
     * 生成协调的次色
     */
    fun generateSecondaryColor(primary: Color): Color {
        val hsv = colorToHSV(primary)
        // 在 Material Design 中，次色通常是主色的补色或类似色
        // 这里我们使用一个固定的色相偏移来生成次色
        val newHue = (hsv[0] + 30f) % 360f
        // 保持相似的饱和度和明度
        return Color.hsv(newHue, hsv[1], hsv[2])
    }

    /**
     * 生成协调的第三色
     */
    fun generateTertiaryColor(primary: Color): Color {
        val hsv = colorToHSV(primary)
        // 第三色通常是主色的另一个类似色，色相偏移更大
        val newHue = (hsv[0] + 60f) % 360f
        val newSaturation = max(0.2f, min(1f, hsv[1] * 0.9f))
        val newValue = max(0.4f, min(1f, hsv[2] * 1.1f))
        return Color.hsv(newHue, newSaturation, newValue)
    }
}