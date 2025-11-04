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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.data.model.ColorScheme
import com.rerokutosei.chimera.utils.color.ColorUtils

/**
 * 三色圆预览组件
 * @param colorScheme 颜色方案
 * @param isSelected 是否选中
 * @param onClick 点击事件
 * @param isDarkTheme 是否为深色主题（用于调整动态色彩预览效果）
 * @param dynamicColorScheme 动态色彩方案
 * @param isColorPicker 是否为色彩选择器入口
 */
@Composable
fun ColorSchemePreview(
    colorScheme: ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    dynamicColorScheme: androidx.compose.material3.ColorScheme? = null,
    isColorPicker: Boolean = false
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 绘制三色圆
        Canvas(modifier = Modifier.size(40.dp)) {
            // 对于色彩选择器入口，始终绘制七彩圆
            if (isColorPicker) {
                rainbowColors(size, isDarkTheme)
            } else {
                drawColorScheme(colorScheme, size, dynamicColorScheme, isDarkTheme)
            }
        }

        // 对于动态色彩方案，显示水滴图标
        if (colorScheme.isDynamic) {
            IconWithBackground(
                imageVector = Icons.Outlined.WaterDrop,
                tint = dynamicColorScheme?.primary ?: Color.LightGray,
                isDarkTheme = isDarkTheme
            )
        }
        
        // 对于色彩选择器入口，显示调色盘图标
        if (isColorPicker) {
            IconWithBackground(
                imageVector = Icons.Rounded.ColorLens,
                tint = MaterialTheme.colorScheme.primary,
                isDarkTheme = isDarkTheme
            )
        }

        // 选中时打勾（对色彩选择器入口不启用）
        if (isSelected && !isColorPicker) {
            IconWithBackground(
                imageVector = Icons.Rounded.Check,
                tint = if (colorScheme.isDynamic) Color.LightGray else colorScheme.primary,
                iconSize = 16.dp,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

/**
 * 绘制颜色方案
 */
private fun DrawScope.drawColorScheme(
    colorScheme: ColorScheme,
    canvasSize: Size,
    dynamicColorScheme: androidx.compose.material3.ColorScheme? = null,
    isDarkTheme: Boolean = false
) {
    when {
        colorScheme.isDynamic -> drawDynamicColorScheme(dynamicColorScheme, canvasSize, isDarkTheme)
        colorScheme.name == "custom" && colorScheme.primary == Color.Unspecified -> rainbowColors(canvasSize, isDarkTheme)
        else -> drawRegularColorScheme(colorScheme, canvasSize)
    }
}

/**
 * 绘制动态色彩方案
 */
private fun DrawScope.drawDynamicColorScheme(
    dynamicColorScheme: androidx.compose.material3.ColorScheme?,
    canvasSize: Size,
    isDarkTheme: Boolean = false
) {
    if (dynamicColorScheme != null) {
        // 使用实际的动态色彩值绘制三色圆
        // 绘制上半圆 - 主色
        drawArc(
            color = dynamicColorScheme.primary,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(canvasSize.width, canvasSize.height),
            topLeft = Offset(0f, 0f)
        )

        // 绘制下半圆左半部分 - 次色
        drawArc(
            color = dynamicColorScheme.secondary,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(canvasSize.width, canvasSize.height),
            topLeft = Offset(0f, 0f)
        )

        // 绘制下半圆右半部分 - 第三色
        drawArc(
            color = dynamicColorScheme.tertiary,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(canvasSize.width, canvasSize.height),
            topLeft = Offset(0f, 0f)
        )
    } else {
        rainbowColors(canvasSize, isDarkTheme)
    }
}

/**
 * 绘制七等分的彩虹色圆
 */
private fun DrawScope.rainbowColors(
    canvasSize: Size,
    isDarkTheme: Boolean = false
) {
    val originalColors = listOf(
        Color(0xFFFF0000), // 红色
        Color(0xFFFF7F00), // 橙色
        Color(0xFFFFFF00), // 黄色
        Color(0xFF00FF00), // 绿色
        Color(0xFF00FFFF), // 青色
        Color(0xFF0000FF), // 蓝色
        Color(0xFF8B00FF)  // 紫色
    )
    
    // 如果是深色主题，调整颜色
    val colors = if (isDarkTheme) {
        originalColors.map { ColorUtils.adjustColorForDarkTheme(it) }
    } else {
        originalColors
    }
    
    val anglePerSection = 360f / colors.size
    var currentAngle = 0f
    
    // 绘制前6个扇形
    for (i in 0..5) {
        drawArc(
            color = colors[i],
            startAngle = currentAngle,
            sweepAngle = anglePerSection,
            useCenter = true,
            size = Size(canvasSize.width, canvasSize.height),
            topLeft = Offset(0f, 0f)
        )
        currentAngle += anglePerSection
    }
    
    // 使用剩余的角度绘制最后一个扇形
    drawArc(
        color = colors[6],
        startAngle = currentAngle,
        sweepAngle = 360f - currentAngle,
        useCenter = true,
        size = Size(canvasSize.width, canvasSize.height),
        topLeft = Offset(0f, 0f)
    )
}

/**
 * 绘制常规色彩方案
 */
private fun DrawScope.drawRegularColorScheme(
    colorScheme: ColorScheme,
    canvasSize: Size
) {
    // 绘制上半圆 - 主色
    drawArc(
        color = colorScheme.primary,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        size = Size(canvasSize.width, canvasSize.height),
        topLeft = Offset(0f, 0f)
    )

    // 绘制下半圆左半部分 - 次色
    drawArc(
        color = colorScheme.secondary,
        startAngle = 90f,
        sweepAngle = 90f,
        useCenter = true,
        size = Size(canvasSize.width, canvasSize.height),
        topLeft = Offset(0f, 0f)
    )

    // 绘制下半圆右半部分 - 第三色
    drawArc(
        color = colorScheme.tertiary,
        startAngle = 0f,
        sweepAngle = 90f,
        useCenter = true,
        size = Size(canvasSize.width, canvasSize.height),
        topLeft = Offset(0f, 0f)
    )
}

/**
 * 带背景圆圈的图标显示组件
 * @param imageVector 图标
 * @param tint 图标颜色
 * @param modifier 修饰符
 * @param iconSize 图标大小
 * @param backgroundColor 背景颜色
 * @param isDarkTheme 是否为暗色主题
 */
@Composable
private fun IconWithBackground(
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    backgroundColor: Color = Color.White,
    isDarkTheme: Boolean = false
) {
    // 根据是否为暗色主题调整背景色
    val adjustedBackgroundColor = if (isDarkTheme) {
        ColorUtils.adjustColorForDarkTheme(backgroundColor)
    } else {
        backgroundColor
    }
    
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // 背景圆圈
        Canvas(modifier = Modifier.size(20.dp)) {
            drawCircle(color = adjustedBackgroundColor)
        }
        // 图标
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
