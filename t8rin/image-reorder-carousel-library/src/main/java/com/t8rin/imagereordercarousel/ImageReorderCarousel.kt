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

package com.t8rin.imagereordercarousel

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagereordercarousel.helper.Picture
import com.t8rin.imagereordercarousel.widgets.EnhancedButton
import com.t8rin.imagereordercarousel.widgets.EnhancedIconButton
import com.t8rin.imagereordercarousel.widgets.ImagePager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * A carousel component that allows reordering images through drag and drop.
 *
 * @param images List of image URIs to display
 * @param onReorder Callback when images are reordered
 * @param modifier Modifier to apply to the carousel
 * @param onNeedToAddImage Callback when the add image button is clicked
 * @param onNeedToRemoveImageAt Callback when an image removal is requested
 * @param onNavigate Callback for navigation to other screens
 * @param showAddButton Whether to show the add button, default is true
 * @param showSortButton Whether to show the sort button, default is true
 * @param showRemoveButtons Whether to show the remove buttons, default is true
 * @param enableImagePreview Whether to enable image preview, default is true
 */
@Composable
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
fun ImageReorderCarousel(
    images: List<Uri>?,
    onReorder: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    onNeedToAddImage: () -> Unit,
    onNeedToRemoveImageAt: (Int) -> Unit,
    onNavigate: ((Any) -> Unit)? = null,
    showAddButton: Boolean = true,
    showSortButton: Boolean = true,
    showRemoveButtons: Boolean = true,
    enableImagePreview: Boolean = true
) {
    val data = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    LaunchedEffect(images) {
        data.clear()
        images?.let { data.addAll(it) }
    }

    val state = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            data.apply {
                add(to.index, removeAt(from.index))
            }
        }
    )

    val isDragging = state.isAnyItemDragging
    val wasDragging = remember { mutableStateOf(isDragging) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isDragging) {
        if (wasDragging.value && !isDragging) {
            onReorder(data.toList())
        }
        if (isDragging) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasDragging.value = isDragging
    }

    var previewUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (showAddButton) {
                EnhancedIconButton(
                    onClick = onNeedToAddImage,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(30.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            val scope = rememberCoroutineScope()
            if (showSortButton) {
                SortButton(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(30.dp),
                    onSortTypeSelected = { sortType ->
                        scope.launch(Dispatchers.Default) {
                            val newValue = images
                                .orEmpty()
                                .sortedByType(
                                    sortType = sortType,
                                    context = context
                                )

                            withContext(Dispatchers.Main.immediate) {
                                data.clear()
                                data.addAll(newValue)
                                onReorder(newValue)
                            }
                        }
                    }
                )
            }
        }
        BoxWithConstraints {
            val maxWidth = constraints.maxWidth
            val density = LocalDensity.current
            val itemSize = with(density) { (maxWidth * 0.75f).toDp().coerceAtMost(165.dp) }

            Box {
                val showButton = showRemoveButtons && !state.isAnyItemDragging
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .padding(12.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = data,
                        key = { _, uri -> uri.toString() + uri.hashCode() }
                    ) { index, uri ->
                        ReorderableItem(
                            state = state,
                            key = uri.toString() + uri.hashCode()
                        ) { isDragging ->
                            val alpha by animateFloatAsState(if (isDragging) 0.3f else 0.6f)
                            val scale by animateFloatAsState(if (isDragging) 1.05f else 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(itemSize)
                                        .scale(scale)
                                        .clip(
                                            if (showButton) MaterialTheme.shapes.medium else MaterialTheme.shapes.small
                                        )
                                        .background(Color.Transparent)
                                ) {
                                    Picture(
                                        model = uri,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .combinedClickable(
                                                onClick = {},
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            )
                                            .longPressDraggableHandle(), // 使用长按拖拽而不是普通拖拽
                                        shape = RectangleShape,
                                        contentScale = ContentScale.Fit
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(itemSize)
                                            .background(
                                                MaterialTheme.colorScheme
                                                    .surface
                                                    .copy(alpha)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                AnimatedVisibility(
                                    visible = showButton,
                                    enter = expandVertically(tween(300)) + fadeIn(),
                                    exit = shrinkVertically(tween(300)) + fadeOut(),
                                    modifier = Modifier.width(itemSize)
                                ) {
                                    EnhancedButton(
                                        contentPadding = PaddingValues(),
                                        onClick = { onNeedToRemoveImageAt(index) },
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                            0.5f
                                        ),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        shape = MaterialTheme.shapes.large,
                                        modifier = Modifier
                                            .padding(top = 10.dp)
                                            .height(30.dp)
                                            .width(itemSize * 0.65f)
                                    ) {
                                        Text(text = stringResource(R.string.remove),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (enableImagePreview) {
        ImagePager(
            visible = previewUri != null,
            selectedUri = previewUri,
            uris = images,
            onUriSelected = { previewUri = it },
            onShare = { /* Share functionality would go here */ },
            onDismiss = { previewUri = null }
        )
    }
}