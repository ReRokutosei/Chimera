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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.rerokutosei.chimera.utils.image.BitmapLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WorkstationCanvas(
    isCutMode: Boolean,
    selectedImages: List<ImageInfo>,
    cutPreset: CutPreset,
    stitchedBitmap: Bitmap?,
    isCutPreviewActive: Boolean,
    isStitching: Boolean,
    stitchProgress: Int,
    onSaveStitched: () -> Unit,
    onSaveCutAll: () -> Unit,
    onSaveCutCurrent: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bitmapLoader = remember { BitmapLoader(context) }

    val cutImageUris = remember(selectedImages) { selectedImages.map { it.uri } }
    val cutPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { cutImageUris.size }
    )
    val cutBitmaps = remember(cutImageUris) {
        mutableStateOf<Map<Int, Bitmap>>(emptyMap())
    }

    DisposableEffect(cutBitmaps) {
        onDispose {
            bitmapLoader.recycleBitmaps(cutBitmaps.value.values.toList())
            cutBitmaps.value = emptyMap()
        }
    }

    LaunchedEffect(cutPagerState.currentPage, cutImageUris, isCutPreviewActive) {
        if (isCutPreviewActive && cutImageUris.isNotEmpty()) {
            val idx = cutPagerState.currentPage.coerceIn(cutImageUris.indices)
            val retainedIndices = (idx - 1..idx + 1).filter { it in cutImageUris.indices }.toSet()
            val retainedBitmaps = cutBitmaps.value.filterKeys { it in retainedIndices }
            val evictedBitmaps = cutBitmaps.value.filterKeys { it !in retainedIndices }.values.toList()
            cutBitmaps.value = retainedBitmaps
            bitmapLoader.recycleBitmaps(evictedBitmaps)

            if (!cutBitmaps.value.containsKey(idx) && idx in cutImageUris.indices) {
                val bitmap = withContext(Dispatchers.IO + NonCancellable) {
                    bitmapLoader.loadBitmapFromUri(cutImageUris[idx])
                }
                var transferred = false
                try {
                    currentCoroutineContext().ensureActive()
                    if (bitmap != null) {
                        cutBitmaps.value = cutBitmaps.value + (idx to bitmap)
                        transferred = true
                    }
                } finally {
                    if (bitmap != null && !transferred) {
                        bitmapLoader.recycleBitmaps(listOf(bitmap))
                    }
                }
            }
        }
    }

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
                    if (isCutMode) (isCutPreviewActive && cutImageUris.isNotEmpty()) else stitchedBitmap != null
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

                    cutMode && isCutPreviewActive && cutImageUris.isNotEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = cutPagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val bitmap = cutBitmaps.value[page]
                                if (bitmap != null) {
                                    ImageResultPreviewer(
                                        source = PreviewSource.FromBitmapWithGrid(
                                            bitmap = bitmap,
                                            cols = cutPreset.cols,
                                            rows = cutPreset.rows
                                        ),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                                    }
                                }
                            }

                            // 页码指示徽标（多图时展示在顶部居中）
                            if (cutImageUris.size > 1) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 16.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                                    tonalElevation = 3.dp
                                ) {
                                    Text(
                                        text = "${cutPagerState.currentPage + 1} / ${cutImageUris.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // 扁平操作按钮组合（无多余边框、无多余投影白底）
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (cutImageUris.size > 1) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (cutPagerState.currentPage > 0) {
                                                coroutineScope.launch {
                                                    cutPagerState.animateScrollToPage(cutPagerState.currentPage - 1)
                                                }
                                            }
                                        },
                                        shape = CircleShape,
                                        enabled = cutPagerState.currentPage > 0,
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.prev_image),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    // 主色按钮：保存此图切片
                                    Button(
                                        onClick = { onSaveCutCurrent(cutPagerState.currentPage) },
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
                                            text = stringResource(R.string.save_current_slices),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    // 次级按钮：全部切图保存
                                    FilledTonalButton(
                                        onClick = onSaveCutAll,
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.save_all_slices),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { onSaveCutCurrent(0) },
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
                                            text = stringResource(R.string.save_slices),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                if (cutImageUris.size > 1) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (cutPagerState.currentPage < cutImageUris.size - 1) {
                                                coroutineScope.launch {
                                                    cutPagerState.animateScrollToPage(cutPagerState.currentPage + 1)
                                                }
                                            }
                                        },
                                        shape = CircleShape,
                                        enabled = cutPagerState.currentPage < cutImageUris.size - 1,
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.next_image),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
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
