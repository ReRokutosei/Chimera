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

package com.t8rin.embeddedpicker.presentation.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.t8rin.embeddedpicker.R
import com.t8rin.embeddedpicker.domain.model.AlbumsState
import com.t8rin.embeddedpicker.domain.model.AllowedMedia
import com.t8rin.embeddedpicker.domain.model.Media
import com.t8rin.embeddedpicker.domain.model.MediaState
import com.t8rin.embeddedpicker.icons.BrokenImageAlt

// 将internal改为public
@Composable
fun MediaPickerRootContent(
    albumsState: AlbumsState,
    mediaState: MediaState,
    allowedMedia: AllowedMedia,
    allowMultiple: Boolean,
    onMediaSelected: (List<Media>) -> Unit,
    onDismiss: () -> Unit,
    onAlbumSelected: (Long) -> Unit,
    onMediaClick: (Media) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPermissionAllowed by remember {
        mutableStateOf(checkPermissions(context))
    }
    var isManagePermissionAllowed by remember {
        mutableStateOf(true)
    }
    var invalidator by remember {
        mutableIntStateOf(0)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionAllowed = isGranted
    }
    
    val managePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        invalidator++
    }
    
    val requestManagePermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                managePermissionLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                )
            } catch (e: Exception) {
                managePermissionLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            }
        }
    }

    // 移除Scaffold，避免与外部容器产生间距
    AnimatedContent(
        targetState = isPermissionAllowed,
        modifier = modifier.fillMaxSize()
    ) { havePermissions ->
        if (havePermissions) {
            MediaPickerHavePermissions(
                albumsState = albumsState,
                mediaState = mediaState,
                allowedMedia = allowedMedia,
                allowMultiple = allowMultiple,
                isManagePermissionAllowed = isManagePermissionAllowed,
                onRequestManagePermission = requestManagePermission,
                onMediaSelected = onMediaSelected,
                onDismiss = onDismiss,
                onAlbumSelected = onAlbumSelected,
                onMediaClick = onMediaClick,
                onClearSelection = onClearSelection
            )
        } else {
            PermissionDeniedScreen(
                onRequestPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionDeniedScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = BrokenImageAlt,
            contentDescription = null,
            modifier = Modifier.size(108.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.no_permissions),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestPermission
        ) {
            Text(stringResource(id = R.string.request))
        }
    }
}

private fun checkPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}