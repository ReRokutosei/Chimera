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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R

@Composable
fun PerformanceSettingsSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    // 性能设置
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Speed,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.performance_settings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }

    // 多线程加速计算
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.multithreading),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.multithreading_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = uiState.multiThreadEnabled,
                onCheckedChange = { viewModel.setMultiThreadEnabled(it) }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setMultiThreadEnabled(!uiState.multiThreadEnabled) }
    )

    // 提高内存阈值
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.high_memory_threshold),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.high_memory_threshold_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = uiState.highMemoryLimit,
                onCheckedChange = { viewModel.setHighMemoryLimit(it) }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setHighMemoryLimit(!uiState.highMemoryLimit) }
    )

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = DividerDefaults.Thickness,
        color = DividerDefaults.color.copy(alpha = 0.5f)
    )
}