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

package com.t8rin.imagereordercarousel.widgets

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.t8rin.imagereordercarousel.helper.Picture
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ImagePager(
    visible: Boolean,
    selectedUri: Uri?,
    uris: List<Uri>?,
    onUriSelected: (Uri) -> Unit,
    onShare: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var initialPage by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedUri) {
        initialPage = uris?.indexOf(selectedUri)?.takeIf { it >= 0 } ?: 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage
    ) {
        uris?.size ?: 0
    }

    LaunchedEffect(pagerState.currentPage) {
        uris?.getOrNull(pagerState.currentPage)?.let { onUriSelected(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                Picture(
                    model = uris?.getOrNull(page),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        IconButton(
            onClick = { onDismiss() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        ) {
            Text(
                text = "X",
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.titleMedium
            )
        }

        IconButton(
            onClick = {
                scope.launch {
                    uris?.getOrNull(pagerState.currentPage)?.let { onShare(it) }
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.surface
            )
        }
    }
}