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

package com.t8rin.embeddedpicker.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.t8rin.embeddedpicker.R
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object DateUtils {
    
    fun getRelativeDateLabel(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val date = Date(timestamp * 1000) // Convert seconds to milliseconds
        val diffInDays = TimeUnit.MILLISECONDS.toDays(now - (timestamp * 1000))
        
        return when {
            diffInDays == 0L -> context.getString(R.string.date_section_today)
            diffInDays == 1L -> context.getString(R.string.date_section_yesterday)
            diffInDays < 7L -> context.getString(R.string.date_section_this_week)
            diffInDays < 30L -> context.getString(R.string.date_section_this_month)
            diffInDays < 365L -> context.getString(R.string.date_section_this_year)
            else -> {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.get(Calendar.YEAR).toString()
            }
        }
    }
    
    @Composable
    fun getRelativeDateLabel(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val date = Date(timestamp * 1000) // Convert seconds to milliseconds
        val diffInDays = TimeUnit.MILLISECONDS.toDays(now - (timestamp * 1000))
        
        return when {
            diffInDays == 0L -> stringResource(R.string.date_section_today)
            diffInDays == 1L -> stringResource(R.string.date_section_yesterday)
            diffInDays < 7L -> stringResource(R.string.date_section_this_week)
            diffInDays < 30L -> stringResource(R.string.date_section_this_month)
            diffInDays < 365L -> stringResource(R.string.date_section_this_year)
            else -> {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.get(Calendar.YEAR).toString()
            }
        }
    }
    
    fun groupMediaByDate(context: Context, mediaList: List<com.t8rin.embeddedpicker.domain.model.Media>): Map<String, List<com.t8rin.embeddedpicker.domain.model.Media>> {
        return mediaList.groupBy { media ->
            getRelativeDateLabel(context, media.dateModified)
        }
    }
}