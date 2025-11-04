/*
 * Chimera is an image stitching tool
 * Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the GNU General Public License v3.0 (the "License");
 * you may redistribute and/or modify this program under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.en.html>.
 */

package com.rerokutosei.chimera.utils.stitch.strategy

import android.graphics.Bitmap
import com.rerokutosei.chimera.ui.main.WidthScale
import com.rerokutosei.chimera.utils.stitch.StitchOrientation

/**
 * 拼接策略接口
 */
interface StitchingStrategy {
    /**
     * 执行图片拼接
     * @param bitmaps 要拼接的位图列表
     * @param options 拼接选项
     * @return 拼接后的Bitmap，失败返回null
     */
    suspend fun stitch(bitmaps: List<Bitmap>, options: StitchingOptions): Bitmap?
}

/**
 * 拼接选项数据类
 */
data class StitchingOptions(
    val spacing: Int = 0,
    val overlayRatio: Int = 0,
    val widthScale: WidthScale = WidthScale.NONE,
    val orientation: StitchOrientation = StitchOrientation.VERTICAL,
    val quality: Int = 90,
    val outputFormat: Int = 1 // 0:PNG, 1:JPEG, 2:WEBP
)
