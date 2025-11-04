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

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PermMedia
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R

@Composable
fun FilePickerSettingsSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 为 Embedded Picker 权限请求创建 launcher
    val embeddedPickerPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setUseEmbeddedPicker(true)
        } else {
            viewModel.setUseEmbeddedPicker(false)
            Toast.makeText(context, context.getString(R.string.storage_permission_required), Toast.LENGTH_SHORT).show()
        }
    }
    
    // 文件选择设置
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.PermMedia,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.file_picker_settings),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }

    // 自动清理已选图片开关
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.auto_clear_selected),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.clear_after_stitching),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = uiState.autoClearImages,
                onCheckedChange = { viewModel.setAutoClearImages(it) }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setAutoClearImages(!uiState.autoClearImages) }
    )

    // 使用存储访问框架选择器
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.use_saf_picker),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.use_saf_without_permission),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = uiState.useSafPicker,
                onCheckedChange = { viewModel.setUseSafPicker(it) }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setUseSafPicker(!uiState.useSafPicker) }
    )

    // 使用Embedded Picker
    // 对于SDK 29-32的设备，Embedded Picker是默认且必需的选项
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.embedded_picker),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.embedded_picker_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = uiState.useEmbeddedPicker,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (viewModel.checkEmbeddedPickerPermissions()) {
                                viewModel.setUseEmbeddedPicker(true)
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    embeddedPickerPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    embeddedPickerPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        } else {
                            viewModel.setUseEmbeddedPicker(false)
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (!uiState.useEmbeddedPicker) {
                        if (viewModel.checkEmbeddedPickerPermissions()) {
                            viewModel.setUseEmbeddedPicker(true)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                embeddedPickerPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            } else {
                                embeddedPickerPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                    } else {
                        viewModel.setUseEmbeddedPicker(false)
                    }
                }
        )
    } else {
        // 对于SDK 29-32设备，显示为默认启用状态
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.embedded_picker),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.default_picker_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = true,
                    onCheckedChange = null, // 禁用切换
                    enabled = false
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = DividerDefaults.Thickness,
        color = DividerDefaults.color.copy(alpha = 0.5f)
    )
}