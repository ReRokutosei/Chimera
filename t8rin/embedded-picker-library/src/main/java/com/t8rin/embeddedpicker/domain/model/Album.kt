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

package com.t8rin.embeddedpicker.domain.model

import java.io.Serializable

data class Album(
    val id: Long = 0,
    val label: String,
    val uri: String,
    val pathToThumbnail: String,
    val relativePath: String,
    val timestamp: Long,
    val count: Long = 0,
    val selected: Boolean = false,
    val isPinned: Boolean = false,
) : Serializable {

    val volume: String =
        pathToThumbnail.substringBeforeLast("/").removeSuffix(relativePath.removeSuffix("/"))

    val isOnSdcard: Boolean =
        volume.lowercase().matches(".*[0-9a-f]{4}-[0-9a-f]{4}".toRegex())

    companion object {

        val NewAlbum = Album(
            id = -200,
            label = "New Album",
            uri = "",
            pathToThumbnail = "",
            relativePath = "",
            timestamp = 0,
            count = 0,
        )
    }
}