/*
 * Chimera is an image stitching tool
 * Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the GNU General Public License v3.0 (the "License");
 * you may redistribute and/or modify this program under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.en.html>.
 */

package com.rerokutosei.chimera.data.model

import androidx.annotation.StringRes
import com.rerokutosei.chimera.R

enum class CutPreset(
    val id: String,
    val rows: Int,
    val cols: Int,
    @StringRes val titleRes: Int,
    @StringRes val hintRes: Int
) {
    GRID_4("grid_4", 2, 2, R.string.cut_grid_4, R.string.cut_hint_grid),
    GRID_9("grid_9", 3, 3, R.string.cut_grid_9, R.string.cut_hint_grid),
    X_3("x_3", 1, 3, R.string.cut_preset_x3, R.string.cut_hint_x),
    X_4("x_4", 1, 4, R.string.cut_preset_x4, R.string.cut_hint_x);

    companion object {
        fun fromId(id: String?): CutPreset {
            return when (id) {
                "2", "grid_4" -> GRID_4
                "3", "grid_9" -> GRID_9
                "x_3", "1x3" -> X_3
                "x_4", "1x4" -> X_4
                else -> X_4
            }
        }
    }
}
