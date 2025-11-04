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

import android.content.Context
import android.net.Uri

internal fun List<Uri>.sortedByType(
    sortType: SortType,
    context: Context
): List<Uri> {
    return when (sortType) {
        SortType.DateModified -> sortedByDateModified(context = context)
        SortType.DateModifiedReversed -> sortedByDateModified(context = context, descending = true)
        SortType.Name -> sortedByName(context = context)
        SortType.NameReversed -> sortedByName(context = context, descending = true)
        SortType.Size -> sortedBySize(context = context)
        SortType.SizeReversed -> sortedBySize(context = context, descending = true)
        SortType.MimeType -> sortedByMimeType(context = context)
        SortType.MimeTypeReversed -> sortedByMimeType(context = context, descending = true)
        SortType.Extension -> sortedByExtension(context = context)
        SortType.ExtensionReversed -> sortedByExtension(context = context, descending = true)
        SortType.DateAdded -> sortedByDateAdded(context = context)
        SortType.DateAddedReversed -> sortedByDateAdded(context = context, descending = true)
    }
}

private fun List<Uri>.sortedByExtension(
    context: Context,
    descending: Boolean = false
): List<Uri> = sortedWith(
    if (descending) {
        compareByDescending {
            context.getFilename(it)?.substringAfterLast(
                delimiter = '.',
                missingDelimiterValue = ""
            )?.lowercase()
        }
    } else {
        compareBy {
            context.getFilename(it)?.substringAfterLast(
                delimiter = '.',
                missingDelimiterValue = ""
            )?.lowercase()
        }
    }
)

private fun List<Uri>.sortedByDateModified(
    context: Context,
    descending: Boolean = false
): List<Uri> = sortedWith(
    if (descending) {
        compareByDescending { it.lastModified(context) }
    } else {
        compareBy { it.lastModified(context) }
    }
)

private fun List<Uri>.sortedByName(
    context: Context,
    descending: Boolean = false
): List<Uri> = sortedWith(
    if (descending) {
        compareByDescending { context.getFilename(it) }
    } else {
        compareBy { context.getFilename(it) }
    }
)

private fun List<Uri>.sortedBySize(
    context: Context,
    descending: Boolean = false
): List<Uri> = sortedWith(
    if (descending) {
        compareByDescending { it.getLongColumn(context, "size") }
    } else {
        compareBy { it.getLongColumn(context, "size") }
    }
)

private fun List<Uri>.sortedByMimeType(
    context: Context,
    descending: Boolean = false
): List<Uri> = sortedWith(
    if (descending) {
        compareByDescending {
            it.getStringColumn(
                context = context,
                column = "mime_type"
            )
        }
    } else {
        compareBy {
            it.getStringColumn(
                context = context,
                column = "mime_type"
            )
        }
    }
)

private fun List<Uri>.sortedByDateAdded(
    context: Context,
    descending: Boolean = false
): List<Uri> = sortedWith(
    if (descending) {
        compareByDescending { it.addedTime(context) }
    } else {
        compareBy { it.addedTime(context) }
    }
)