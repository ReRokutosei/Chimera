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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.embeddedpicker.R
import com.t8rin.embeddedpicker.domain.model.Media
import com.t8rin.embeddedpicker.domain.model.MediaState

@Composable
internal fun MediaPickerGridWithOverlays(
    mediaState: MediaState,
    selectedMedia: List<Media>,
    isSearching: Boolean,
    allowMultiple: Boolean,
    onMediaClick: (Media) -> Unit,
    onClearSelection: () -> Unit,
    onMediaSelected: (List<Media>) -> Unit,
    onSearchingChange: (Boolean) -> Unit,
    onSearchKeywordChange: (String) -> Unit
) {
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 媒体网格
        MediaPickerGridWithSections(
            mediaList = mediaState.media.filter { media ->
                if (searchKeyword.isBlank()) {
                    true
                } else {
                    media.displayName.contains(searchKeyword, ignoreCase = true)
                }
            },
            selectedMedia = selectedMedia,
            onMediaClick = onMediaClick,
            onMediaLongClick = { /* 长按处理 */ }
        )
        
        // 浮动操作按钮
        val isButtonVisible = (!allowMultiple || selectedMedia.isNotEmpty()) && !isSearching
        AnimatedVisibility(
            visible = isButtonVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .safeDrawingPadding()
        ) {
            val enabled = selectedMedia.isNotEmpty()
            val containerColor by animateColorAsState(
                targetValue = if (enabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(durationMillis = 300)
            )
            val contentColor by animateColorAsState(
                targetValue = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 300)
            )
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(
                    visible = selectedMedia.isNotEmpty(),
                    enter = scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(durationMillis = 200)
                    ) + fadeIn(),
                    exit = scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(durationMillis = 200)
                    ) + fadeOut()
                ) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = onClearSelection,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
                BadgedBox(
                    badge = {
                        if (selectedMedia.isNotEmpty() && allowMultiple) {
                            val badgeWidth = animateDpAsState(
                                targetValue = when {
                                    selectedMedia.size > 50 -> 32.dp
                                    selectedMedia.size > 9 -> 24.dp  // 两位数时增加宽度
                                    else -> 20.dp
                                                   },
                                animationSpec = tween(durationMillis = 200)
                            ).value
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .height(20.dp)
                                    .widthIn(min = badgeWidth)
                            ) {
                                Text(
                                    text = if (selectedMedia.size > 50) "50+" else selectedMedia.size.toString(),
                                    fontSize = when {
                                        selectedMedia.size > 50 -> 10.sp
                                        selectedMedia.size > 9 -> 11.sp  // 两位数时稍微调小字体
                                    else -> 12.sp
                                    }
                                )
                            }
                        }
                    }
                ) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = {
                            if (enabled) {
                                onMediaSelected(selectedMedia)
                            }
                        },
                        containerColor = containerColor,
                        contentColor = contentColor,
                        modifier = Modifier.semantics {
                            contentDescription = "Add media"
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TaskAlt,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.pick_multiple_media))
                        }
                    }
                }
            }
        }
        
        // 搜索无结果提示
        AnimatedVisibility(
            visible = mediaState.media.none {
                it.displayName.contains(searchKeyword, ignoreCase = true)
            } && searchKeyword.isNotBlank(),
            enter = scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(),
            exit = scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.nothing_found_by_search),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.SearchOff,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(2f)
                        .sizeIn(maxHeight = 140.dp, maxWidth = 140.dp)
                        .fillMaxSize()
                )
                Spacer(Modifier.weight(1f))
            }
        }
        
        // 搜索输入框
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeDrawingPadding(),
            targetState = isSearching,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ) togetherWith fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) { searching ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                if (searching) {
                    RoundedTextField(
                        maxLines = 1,
                        hint = { Text(stringResource(id = R.string.search_here)) },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search,
                        ),
                        value = searchKeyword,
                        onValueChange = { newValue ->
                            searchKeyword = newValue
                            onSearchKeywordChange(newValue)
                        },
                        startIcon = {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    searchKeyword = ""
                                    onSearchingChange(false)
                                    onSearchKeywordChange("")
                                },
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.close),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        endIcon = {
                            AnimatedVisibility(
                                visible = searchKeyword.isNotEmpty(),
                                enter = fadeIn() + scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = tween(durationMillis = 200)
                                ),
                                exit = fadeOut() + scaleOut(
                                    targetScale = 0.8f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                            ) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        searchKeyword = ""
                                        onSearchKeywordChange("")
                                    },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = stringResource(R.string.close),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = {
                            onSearchingChange(true)
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    }
                }
            }
        }
        
        BackHandler(isSearching) {
            onSearchingChange(false)
        }
    }
}