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

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.utils.common.ToastUtil


@Composable
fun ImagePickerButton(
    context: Context,
    isPageEntered: Boolean,
    isDataLoaded: Boolean,

    useSafPicker: Boolean,
    useEmbeddedPicker: Boolean,
    onImagesSelected: (List<android.net.Uri>) -> Unit,
    showEmbeddedPicker: () -> Unit,
) {

    // 为 SAF 选择器创建单独的 launcher
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onImagesSelected(uris)
        }
    }

    // 为 Photo Picker 创建 launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(999)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImagesSelected(uris)
        }
    }

    // 为 Embedded Picker 权限请求创建 launcher
    val embeddedPickerPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showEmbeddedPicker()
        } else {
            ToastUtil.showShort(context, context.getString(R.string.storage_permission_required))
        }
    }

    val supportsEmbeddedPicker = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2
    val hasEmbeddedPickerPermission = supportsEmbeddedPicker &&
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    Button(
        onClick = {
            if (isPageEntered && isDataLoaded) {
                when {
                    useSafPicker -> {
                        safPickerLauncher.launch(arrayOf("image/*"))
                    }

                    useEmbeddedPicker && supportsEmbeddedPicker -> {
                        if (hasEmbeddedPickerPermission) {
                            showEmbeddedPicker()
                        } else {
                            embeddedPickerPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                    // 对于 SDK 29-32 的设备，如果 Photo Picker 不可用，则使用 Embedded Picker
                    !ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context) &&
                        supportsEmbeddedPicker -> {
                        if (hasEmbeddedPickerPermission) {
                            showEmbeddedPicker()
                        } else {
                            embeddedPickerPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context) -> {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }

                    else -> {
                        // 当所有其他选项都不可用时，使用 SAF 作为最后的备选方案
                        safPickerLauncher.launch(arrayOf("image/*"))
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = isPageEntered && isDataLoaded
    ) {
        Icon(Icons.Rounded.AddPhotoAlternate, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.select_images))
    }
}
