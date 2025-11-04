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

import androidx.compose.runtime.Composable

enum class SortType {
    DateModified, DateModifiedReversed,
    Name, NameReversed,
    Size, SizeReversed,
    MimeType, MimeTypeReversed,
    Extension, ExtensionReversed,
    DateAdded, DateAddedReversed
}

internal val SortType.title: String
    @Composable
    get() = when (this) {
        SortType.DateModified -> "Date Modified"
        SortType.DateModifiedReversed -> "Date Modified (Reversed)"
        SortType.Name -> "Name"
        SortType.NameReversed -> "Name (Reversed)"
        SortType.Size -> "Size"
        SortType.SizeReversed -> "Size (Reversed)"
        SortType.MimeType -> "Mime Type"
        SortType.MimeTypeReversed -> "Mime Type (Reversed)"
        SortType.Extension -> "Extension"
        SortType.ExtensionReversed -> "Extension (Reversed)"
        SortType.DateAdded -> "Date Added"
        SortType.DateAddedReversed -> "Date Added (Reversed)"
    }