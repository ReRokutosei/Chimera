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

package com.rerokutosei.chimera.ui.viewer

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.utils.common.LogManager
import com.rerokutosei.chimera.utils.image.ImageSaver
import com.rerokutosei.chimera.utils.image.ImageSharer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageViewerScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageViewerViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val logManager = LogManager.getInstance(context)
    val imageSaver = remember { ImageSaver(context) }
    val imageSharer = remember { ImageSharer(context) }
    val coroutineScope = rememberCoroutineScope()

    val previewSource by viewModel.previewSource.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.stitching_result),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            ErrorDialog(it, onBackClick, context)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isProcessing) {
                logManager.debug("ImageViewerScreen", "显示处理中状态")
                ContainedLoadingIndicator()
            } else if (previewSource != null) {
                logManager.debug("ImageViewerScreen", "显示预览, source: $previewSource")
                ImageResultPreviewer(
                    source = previewSource!!,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (errorMessage == null) {
                Text(stringResource(R.string.no_valid_result))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        saveMessage?.let {
            SaveResultDialog(it) { saveMessage = null }
        }

        ActionButtons(
            previewSource = previewSource,
            isProcessing = isProcessing,
            isSaving = isSaving,
            onShareClick = { source ->
                coroutineScope.launch {
                    when (source) {
                        is PreviewSource.FromBitmap -> imageSharer.shareBitmap(source.bitmap, context.getString(R.string.share_stitched_image))
                    }
                    viewModel.releaseTempFile()
                }
            },
            onSaveClick = { source ->
                isSaving = true
                coroutineScope.launch {
                    val onResult: (Uri?) -> Unit = { uri ->
                        saveMessage = if (uri != null) context.getString(R.string.image_saved_to_album) else context.getString(R.string.save_failed)
                        isSaving = false
                        if (uri != null) viewModel.releaseTempFile()
                    }
                    val onError: (Exception) -> Unit = { e ->
                        saveMessage = context.getString(R.string.save_failed) + ": ${e.message}"
                        isSaving = false
                    }

                    when (source) {
                        is PreviewSource.FromBitmap -> imageSaver.saveToGallery(source.bitmap, onResult, onError)
                    }
                }
            }
        )
    }
}

@Composable
private fun ErrorDialog(errorMessage: String, onBackClick: () -> Unit, context: Context) {
    val coroutineScope = rememberCoroutineScope()
    var showLogContent by remember { mutableStateOf(false) }
    var logContent by remember { mutableStateOf("") }
    var isLogDisplayed by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.stitching_failed)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(errorMessage)
                if (showLogContent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = stringResource(R.string.log_content) + logContent, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onBackClick) { Text(stringResource(R.string.back)) }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (!isLogDisplayed) {
                        val logManager = LogManager.getInstance(context)
                        coroutineScope.launch(Dispatchers.IO) {
                            val logFile = logManager.javaClass.getDeclaredMethod("getLogFile").apply { isAccessible = true }.invoke(logManager) as File
                            logContent = if (logFile.exists()) {
                                try {
                                    filterCurrentSessionLogs(logFile.readText())
                                } catch (e: IOException) {
                                    context.getString(R.string.failed_to_read_log_file, e.message)
                                }
                            } else {
                                context.getString(R.string.log_file_not_exist)
                            }
                            showLogContent = true
                            isLogDisplayed = true
                        }
                    } else {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("日志内容", logContent)
                        clipboard.setPrimaryClip(clip)
                    }
                }) { Text(if (isLogDisplayed) stringResource(R.string.copy_log) else stringResource(R.string.view_log)) }
            }
        }
    )
}

@Composable
private fun SaveResultDialog(message: String, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_result)) },
        text = { Text(message) },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.ok)) } }
    )
}

@Composable
private fun ActionButtons(
    previewSource: PreviewSource?,
    isProcessing: Boolean,
    isSaving: Boolean,
    onShareClick: (PreviewSource) -> Unit,
    onSaveClick: (PreviewSource) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { previewSource?.let(onShareClick) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ),
            enabled = !isProcessing && previewSource != null
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.share))
        }

        Button(
            onClick = { previewSource?.let(onSaveClick) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            enabled = !isProcessing && !isSaving && previewSource != null
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
            } else {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save))
        }
    }
}

private fun filterCurrentSessionLogs(fullLogContent: String): String {
    val lines = fullLogContent.lines()
    val filteredLogs = mutableListOf<String>()
    var startIndex = -1
    for (i in lines.indices.reversed()) {
        if (lines[i].contains("开始拼接图片")) {
            startIndex = i
            break
        }
    }
    if (startIndex != -1) {
        val actualStartIndex = (startIndex - 5).coerceAtLeast(0)
        for (i in actualStartIndex until lines.size) {
            filteredLogs.add(lines[i])
        }
    } else {
        val start = (lines.size - 100).coerceAtLeast(0)
        for (i in start until lines.size) {
            filteredLogs.add(lines[i])
        }
    }
    return filteredLogs.joinToString("\n")
}