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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rerokutosei.chimera.R

@Composable
fun <T> CustomSegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionDisplayName: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        options.forEach { option ->
            Button(
                onClick = { onOptionSelected(option) },
                modifier = Modifier
                    .weight(1f)  // 每个选项平均分配空间
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                colors = if (selectedOption == option) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            ) {
                Text(
                    text = optionDisplayName(option),
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = (-0.5).sp,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun getModeDisplayName(mode: StitchMode): String {
    return when (mode) {
        StitchMode.DIRECT_VERTICAL -> stringResource(R.string.vertical)
        StitchMode.DIRECT_HORIZONTAL -> stringResource(R.string.horizontal)
    }
}

@Composable
fun getWidthScaleDisplayName(widthScale: WidthScale): String {
    return when (widthScale) {
        WidthScale.MIN_WIDTH -> stringResource(R.string.min)
        WidthScale.NONE -> stringResource(R.string.original)
        WidthScale.MAX_WIDTH -> stringResource(R.string.max)
    }
}

@Composable
fun getHeightScaleDisplayName(widthScale: WidthScale): String {
    return when (widthScale) {
        WidthScale.MIN_WIDTH -> stringResource(R.string.min)
        WidthScale.NONE -> stringResource(R.string.original)
        WidthScale.MAX_WIDTH -> stringResource(R.string.max)
    }
}