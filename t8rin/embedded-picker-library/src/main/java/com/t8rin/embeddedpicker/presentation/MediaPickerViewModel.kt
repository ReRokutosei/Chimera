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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t8rin.embeddedpicker.domain.MediaRetriever
import com.t8rin.embeddedpicker.domain.model.AlbumsState
import com.t8rin.embeddedpicker.domain.model.AllowedMedia
import com.t8rin.embeddedpicker.domain.model.Media
import com.t8rin.embeddedpicker.domain.model.MediaState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaPickerViewModel(
    private val mediaRetriever: MediaRetriever
) : ViewModel() {

    private val _albumsState = MutableStateFlow(AlbumsState())
    val albumsState: StateFlow<AlbumsState> = _albumsState.asStateFlow()

    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private var currentAlbumId: Long = -1
    private lateinit var allowedMedia: AllowedMedia

    fun init(allowedMedia: AllowedMedia) {
        this.allowedMedia = allowedMedia
        loadAlbums()
        loadMedia()
    }

    fun getAlbum(albumId: Long) {
        currentAlbumId = albumId
        loadMedia()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            _albumsState.value = _albumsState.value.copy(isLoading = true)
            try {
                mediaRetriever.getAlbums().collect { albums ->
                    _albumsState.value = _albumsState.value.copy(
                        isLoading = false,
                        albums = albums
                    )
                }
            } catch (e: Exception) {
                _albumsState.value = _albumsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load albums"
                )
            }
        }
    }

    private fun loadMedia() {
        viewModelScope.launch {
            _mediaState.value = _mediaState.value.copy(isLoading = true)
            try {
                mediaRetriever.getMedia(currentAlbumId, allowedMedia).collect { media ->
                    _mediaState.value = _mediaState.value.copy(
                        isLoading = false,
                        media = media
                    )
                }
            } catch (e: Exception) {
                _mediaState.value = _mediaState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load media"
                )
            }
        }
    }

    fun selectMedia(media: Media) {
        val currentSelected = _mediaState.value.selectedMedia.toMutableList()
        if (currentSelected.contains(media)) {
            currentSelected.remove(media)
        } else {
            currentSelected.add(media)
        }
        _mediaState.value = _mediaState.value.copy(selectedMedia = currentSelected)
    }

    fun clearSelection() {
        _mediaState.value = _mediaState.value.copy(selectedMedia = emptyList())
    }
    
    // 添加清除缓存的方法，用于刷新数据
    fun clearCache() {
        if (mediaRetriever is com.t8rin.embeddedpicker.data.AndroidMediaRetriever) {
            mediaRetriever.clearCache()
        }
    }
}