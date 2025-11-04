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

package com.t8rin.fancyslider.utils.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.t8rin.fancyslider.R

/**
 * Holder class for slider theme attributes
 */
data class SliderThemeAttributes(
    val activeTrackColor: Color,
    val inactiveTrackColor: Color,
    val thumbColor: Color,
    val disabledActiveTrackColor: Color,
    val disabledInactiveTrackColor: Color,
    val disabledThumbColor: Color
)

/**
 * Composable function to get slider theme attributes with Material3 support
 */
@Composable
fun rememberSliderThemeAttributes(
    useMaterial3: Boolean = true
): SliderThemeAttributes {
    val context = LocalContext.current
    
    return if (useMaterial3) {
        // Use Material3 theme colors
        SliderThemeAttributes(
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            thumbColor = MaterialTheme.colorScheme.primary,
            disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    } else {
        // Use theme attributes
        getThemeAttributes(context)
    }
}

/**
 * Get slider theme attributes from the current theme
 */
@SuppressLint("ResourceType")
private fun getThemeAttributes(context: Context): SliderThemeAttributes {
    val attributes = intArrayOf(
        R.attr.sliderActiveTrackColor,
        R.attr.sliderInactiveTrackColor,
        R.attr.sliderThumbColor,
        R.attr.sliderDisabledActiveTrackColor,
        R.attr.sliderDisabledInactiveTrackColor,
        R.attr.sliderDisabledThumbColor
    )
    
    val typedArray: TypedArray = context.theme.obtainStyledAttributes(attributes)
    
    return try {
        SliderThemeAttributes(
            activeTrackColor = Color(typedArray.getColor(0, Color.Magenta.toArgb())),
            inactiveTrackColor = Color(typedArray.getColor(1, Color.Magenta.copy(alpha = 0.5f).toArgb())),
            thumbColor = Color(typedArray.getColor(2, Color.Magenta.toArgb())),
            disabledActiveTrackColor = Color(typedArray.getColor(3, Color.Gray.copy(alpha = 0.12f).toArgb())),
            disabledInactiveTrackColor = Color(typedArray.getColor(4, Color.Gray.copy(alpha = 0.12f).toArgb())),
            disabledThumbColor = Color(typedArray.getColor(5, Color.Gray.copy(alpha = 0.38f).toArgb()))
        )
    } finally {
        typedArray.recycle()
    }
}