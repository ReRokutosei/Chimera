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
import android.provider.DocumentsContract
import android.provider.OpenableColumns

fun Context.getFilename(uri: Uri): String? {
    return try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

fun Uri.lastModified(context: Context): Long? = with(context.contentResolver) {
    val query = query(this@lastModified, null, null, null, null)

    query?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnNames = listOf(
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                "datetaken", // When sharing an Image from Google Photos into the app.
            )

            val millis = columnNames.firstNotNullOfOrNull {
                val index = cursor.getColumnIndex(it)
                if (!cursor.isNull(index)) {
                    cursor.getLong(index)
                } else {
                    null
                }
            }

            return millis
        }
    }

    return null
}

fun Uri.addedTime(context: Context): Long? =
    getLongColumn(context, "date_added")?.times(1000)


fun Uri.getLongColumn(context: Context, column: String): Long? =
    context.contentResolver.query(this, arrayOf(column), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(column)
            if (index != -1 && !cursor.isNull(index)) cursor.getLong(index) else null
        } else null
    }

fun Uri.getStringColumn(context: Context, column: String): String? =
    context.contentResolver.query(this, arrayOf(column), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(column)
            if (index != -1 && !cursor.isNull(index)) cursor.getString(index) else null
        } else null
    }