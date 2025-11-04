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

package com.t8rin.embeddedpicker.presentation

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.t8rin.embeddedpicker.data.AndroidMediaRetriever
import com.t8rin.embeddedpicker.domain.model.AllowedMedia
import com.t8rin.embeddedpicker.presentation.components.MediaPickerRootContent

class MediaPickerActivity : ComponentActivity() {

    private lateinit var viewModel: MediaPickerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取传递的参数
        val allowMultiple = intent.getBooleanExtra("allowMultiple", false)
        
        viewModel = MediaPickerViewModel(AndroidMediaRetriever(this))
        viewModel.init(AllowedMedia.Photos())
        
        setContent {
            MediaPickerApp(allowMultiple)
        }
    }

    @Composable
    private fun MediaPickerApp(allowMultiple: Boolean) {
        val albumsState by viewModel.albumsState.collectAsState()
        val mediaState by viewModel.mediaState.collectAsState()
        
        MediaPickerRootContent(
            albumsState = albumsState,
            mediaState = mediaState,
            allowedMedia = AllowedMedia.Photos(),
            allowMultiple = allowMultiple,
            onMediaSelected = { media ->
                // 返回结果
                sendMediaAsResult(media.map { Uri.parse(it.uri) })
            },
            onDismiss = {
                finish()
            },
            onAlbumSelected = { albumId ->
                viewModel.getAlbum(albumId)
            },
            onMediaClick = { media ->
                viewModel.selectMedia(media)
            },
            onClearSelection = {
                viewModel.clearSelection()
            }
        )
    }
}