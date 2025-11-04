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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.rerokutosei.chimera.R
import com.t8rin.embeddedpicker.data.AndroidMediaRetriever
import com.t8rin.embeddedpicker.domain.model.AllowedMedia
import com.t8rin.embeddedpicker.presentation.MediaPickerViewModel
import com.t8rin.embeddedpicker.presentation.components.MediaPickerRootContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbeddedPickerDialog(
    onImagesSelected: (List<android.net.Uri>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var hasPermission by remember { mutableStateOf(checkPermissions(context)) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        if (hasPermission) {
            EmbeddedPickerContent(
                onImagesSelected = onImagesSelected,
                onDismiss = onDismiss
            )
        } else {
            // 权限被拒绝时显示提示
            Box(modifier = Modifier.fillMaxSize()) {
                Text(stringResource(R.string.storage_permission_required))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmbeddedPickerContent(
    onImagesSelected: (List<android.net.Uri>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val mediaRetriever = remember { AndroidMediaRetriever(context) }
    val viewModel = remember { 
        MediaPickerViewModel(mediaRetriever).apply {
            init(AllowedMedia.Photos())
        }
    }
    
    val albumsState by viewModel.albumsState.collectAsState()
    val mediaState by viewModel.mediaState.collectAsState()
    val isLoading = albumsState.isLoading || mediaState.isLoading
    val hasContent = albumsState.albums.isNotEmpty() || mediaState.media.isNotEmpty()
    
    if (isLoading && !hasContent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(stringResource(com.t8rin.embeddedpicker.R.string.pick_multiple_media))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = stringResource(com.t8rin.embeddedpicker.R.string.close)
                        )
                    }
                }
            )

            MediaPickerRootContent(
                albumsState = albumsState,
                mediaState = mediaState,
                allowedMedia = AllowedMedia.Photos(),
                allowMultiple = true,
                onMediaSelected = { mediaList ->
                    val uris = mediaList.map { it.uri.toUri() }
                    onImagesSelected(uris)
                    onDismiss()
                },
                onDismiss = onDismiss,
                onAlbumSelected = { albumId ->
                    viewModel.getAlbum(albumId)
                },
                onMediaClick = { media ->
                    viewModel.selectMedia(media)
                },
                onClearSelection = {
                    viewModel.clearSelection()
                },
            )
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