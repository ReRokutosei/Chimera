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

package com.rerokutosei.chimera.utils.stitch

import android.content.Context
import com.rerokutosei.chimera.utils.stitch.strategy.DirectStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.OverlayStitchingStrategy
import com.rerokutosei.chimera.utils.stitch.strategy.StitchingStrategy

/**
 * 拼接器工厂类
 */
class StitcherFactory(private val context: Context) {
    
    /**
     * 创建拼接策略
     * @param orientation 拼接方向
     * @param isOverlay 是否为叠加模式
     * @return 对应的拼接策略实现
     */
    fun createStitcher(orientation: StitchOrientation, isOverlay: Boolean): StitchingStrategy {
        return when {
            isOverlay -> OverlayStitchingStrategy(context)
            orientation == StitchOrientation.VERTICAL -> DirectStitchingStrategy(
                isVertical = true,
                context = context
            )
            else -> DirectStitchingStrategy(isVertical = false, context = context)
        }
    }
}