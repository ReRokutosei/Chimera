/*
 * Chimera is an image stitching tool
 * Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the GNU General Public License v3.0 (the "License");
 * you may redistribute and/or modify this program under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3 of the License.
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

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.model.CutPreset
import com.rerokutosei.chimera.data.model.ImageInfo
import com.rerokutosei.chimera.ui.viewer.AdaptiveImageDisplay
import com.rerokutosei.chimera.ui.viewer.ImageResultPreviewer
import com.rerokutosei.chimera.ui.viewer.PreviewSource

@Composable
fun WorkstationCanvas(
    isCutMode: Boolean,
    selectedImages: List<ImageInfo>,
    cutPreset: CutPreset,
    stitchedBitmap: Bitmap?,
    cutPreviewBitmap: Bitmap?,
    isStitching: Boolean,
    stitchProgress: Int,
    onSaveStitched: () -> Unit,
    onSaveCut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = Triple(
                    isCutMode,
                    isStitching,
                    if (isCutMode) cutPreviewBitmap != null else stitchedBitmap != null
                ),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "canvas_content"
            ) { (cutMode, stitching, hasResult) ->
                when {
                    stitching -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { stitchProgress / 100f },
                                modifier = Modifier.size(56.dp),
                                strokeWidth = 5.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.generating_stitched_image, stitchProgress),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    cutMode && cutPreviewBitmap != null -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ImageResultPreviewer(
                                source = PreviewSource.FromBitmapWithGrid(
                                    bitmap = cutPreviewBitmap,
                                    cols = cutPreset.cols,
                                    rows = cutPreset.rows
                                ),
                                modifier = Modifier.fillMaxSize()
                            )

                            // 扁平保存按钮（无多余边框、无多余投影白底）
                            Button(
                                onClick = onSaveCut,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.save),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    !cutMode && stitchedBitmap != null -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AdaptiveImageDisplay(
                                bitmap = stitchedBitmap,
                                modifier = Modifier.fillMaxSize()
                            )

                            // 扁平保存按钮（无多余边框、无多余投影白底）
                            Button(
                                onClick = onSaveStitched,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.save),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    else -> {
                        // 空态画布占位
                        CanvasEmptyPlaceholder(
                            isCutMode = cutMode,
                            hasSelectedImages = selectedImages.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasEmptyPlaceholder(
    isCutMode: Boolean,
    hasSelectedImages: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isCutMode) Icons.Rounded.ContentCut else Icons.Rounded.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val hintText = if (hasSelectedImages) {
            if (isCutMode) stringResource(R.string.canvas_hint_cut_ready) else stringResource(R.string.canvas_hint_stitch_ready)
        } else {
            if (isCutMode) stringResource(R.string.canvas_hint_cut_empty) else stringResource(R.string.canvas_hint_stitch_empty)
        }

        Text(
            text = hintText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
