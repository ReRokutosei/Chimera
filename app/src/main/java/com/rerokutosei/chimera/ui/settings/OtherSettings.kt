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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R

@Composable
fun OtherSettingsSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    // 其他选项
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.other_settings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }

    // 日志等级选择
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.log_level),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        trailingContent = {
            var showLogLevelMenu by remember { mutableStateOf(false) }
            val selectedLogLevel = when (uiState.logLevel) {
                0 -> "DEBUG"
                1 -> "INFO"
                2 -> "WARN"
                3 -> "ERROR"
                else -> "INFO"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedLogLevel, style = MaterialTheme.typography.bodyMedium)
                androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 8.dp))
                androidx.compose.material3.IconButton(onClick = { showLogLevelMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = stringResource(R.string.select_log_level),
                        modifier = Modifier.size(60.dp)
                    )
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = showLogLevelMenu,
                    onDismissRequest = { showLogLevelMenu = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("DEBUG") },
                        onClick = {
                            showLogLevelMenu = false
                            viewModel.setLogLevel(0)
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("INFO") },
                        onClick = {
                            showLogLevelMenu = false
                            viewModel.setLogLevel(1)
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("WARN")},
                        onClick = {
                            showLogLevelMenu = false
                            viewModel.setLogLevel(2)
                        }
                    )
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("ERROR") },
                        onClick = {
                            showLogLevelMenu = false
                            viewModel.setLogLevel(3)
                        }
                    )
                }
            }
        }
    )

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = DividerDefaults.Thickness, 
        color = DividerDefaults.color
    )
}