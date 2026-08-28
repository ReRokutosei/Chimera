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

import org.junit.Assert.assertEquals
import org.junit.Test

class CutPresetTest {

    @Test
    fun fromIdReturnsExpectedPreset() {
        assertEquals(CutPreset.GRID_4, CutPreset.fromId("grid_4"))
        assertEquals(CutPreset.GRID_4, CutPreset.fromId("2"))

        assertEquals(CutPreset.GRID_9, CutPreset.fromId("grid_9"))
        assertEquals(CutPreset.GRID_9, CutPreset.fromId("3"))

        assertEquals(CutPreset.X_3, CutPreset.fromId("x_3"))
        assertEquals(CutPreset.X_3, CutPreset.fromId("1x3"))

        assertEquals(CutPreset.X_4, CutPreset.fromId("x_4"))
        assertEquals(CutPreset.X_4, CutPreset.fromId("1x4"))

        // Null and invalid fall back to X_4
        assertEquals(CutPreset.X_4, CutPreset.fromId(null))
        assertEquals(CutPreset.X_4, CutPreset.fromId("unknown_id"))
    }

    @Test
    fun gridDimensionPropertiesAreCorrect() {
        assertEquals(2, CutPreset.GRID_4.rows)
        assertEquals(2, CutPreset.GRID_4.cols)

        assertEquals(3, CutPreset.GRID_9.rows)
        assertEquals(3, CutPreset.GRID_9.cols)

        assertEquals(1, CutPreset.X_3.rows)
        assertEquals(3, CutPreset.X_3.cols)

        assertEquals(1, CutPreset.X_4.rows)
        assertEquals(4, CutPreset.X_4.cols)
    }
}
