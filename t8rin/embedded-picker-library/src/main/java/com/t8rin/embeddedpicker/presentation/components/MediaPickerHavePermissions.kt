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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.t8rin.embeddedpicker.R
import com.t8rin.embeddedpicker.domain.model.Album
import com.t8rin.embeddedpicker.domain.model.AlbumsState
import com.t8rin.embeddedpicker.domain.model.AllowedMedia
import com.t8rin.embeddedpicker.domain.model.Media
import com.t8rin.embeddedpicker.domain.model.MediaState

@Composable
internal fun MediaPickerHavePermissions(
    albumsState: AlbumsState,
    mediaState: MediaState,
    allowedMedia: AllowedMedia,
    allowMultiple: Boolean,
    isManagePermissionAllowed: Boolean,
    onRequestManagePermission: () -> Unit,
    onMediaSelected: (List<Media>) -> Unit,
    onDismiss: () -> Unit,
    onAlbumSelected: (Long) -> Unit,
    onMediaClick: (Media) -> Unit,
    onClearSelection: () -> Unit
) {
    var selectedAlbumId by remember { mutableStateOf(-1L) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // 显示相册选择器（如果至少有一个相册）
        AnimatedVisibility(
            visible = albumsState.albums.isNotEmpty(),
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut()
        ) {
            AlbumSelector(
                albums = albumsState.albums,
                selectedAlbumId = selectedAlbumId,
                onAlbumSelected = { albumId ->
                    selectedAlbumId = albumId
                    onAlbumSelected(albumId)
                }
            )
        }
        
        // 显示媒体网格
        if (mediaState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            MediaPickerGridWithOverlays(
                mediaState = mediaState,
                selectedMedia = mediaState.selectedMedia,
                isSearching = isSearching,
                allowMultiple = allowMultiple,
                onMediaClick = onMediaClick,
                onClearSelection = onClearSelection,
                onMediaSelected = onMediaSelected,
                onSearchingChange = { isSearching = it },
                onSearchKeywordChange = { /* 暂时留空 */ }
            )
        }
        
        // 显示操作按钮（如果选择了媒体）
        AnimatedVisibility(
            visible = mediaState.selectedMedia.isNotEmpty(),
            enter = fadeIn() + scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(durationMillis = 200)
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.Button(
                    onClick = onClearSelection
                ) {
                    Text("Clear")
                }
                
                androidx.compose.material3.Button(
                    onClick = { onMediaSelected(mediaState.selectedMedia) }
                ) {
                    Text("Select (${mediaState.selectedMedia.size})")
                }
            }
        }
    }
}

@Composable
private fun AlbumSelector(
    albums: List<Album>,
    selectedAlbumId: Long,
    onAlbumSelected: (Long) -> Unit
) {
    var showAlbumThumbnail by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (albums.isNotEmpty()) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                } else {
                    Modifier
                }
            )
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(albums) { album ->
                val isSelected = album.id == selectedAlbumId
                val isImageVisible = showAlbumThumbnail && album.uri.isNotEmpty()
                androidx.compose.material3.AssistChip(
                    onClick = { onAlbumSelected(album.id) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.animateContentSize(
                                animationSpec = tween(durationMillis = 300)
                            )
                        ) {
                            Text(
                                text = if (album.id == -1L) stringResource(R.string.all) else album.label
                            )
                            
                            AnimatedVisibility(
                                visible = isImageVisible,
                                enter = fadeIn() + expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = tween(durationMillis = 300)
                                ),
                                exit = fadeOut() + shrinkVertically(
                                    shrinkTowards = Alignment.Top,
                                    animationSpec = tween(durationMillis = 300)
                                )
                            ) {
                                Box {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(album.uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .height(60.dp)
                                            .width(60.dp)
                                            .clip(MaterialTheme.shapes.small),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .height(60.dp)
                                            .width(60.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .background(
                                                MaterialTheme
                                                    .colorScheme
                                                    .surfaceContainer
                                                    .copy(alpha = 0.6f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = album.count.toString(),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(4.dp),
                    colors = if (isSelected) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    }
                )
            }
        }
        
        if (albums.isNotEmpty()) {
            androidx.compose.material3.IconButton(
                onClick = { showAlbumThumbnail = !showAlbumThumbnail }
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (showAlbumThumbnail) 180f else 0f,
                    animationSpec = tween(durationMillis = 300)
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}