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

package com.rerokutosei.chimera.utils.image

import androidx.core.net.toUri
import com.rerokutosei.chimera.data.model.ImageInfo
import com.t8rin.embeddedpicker.domain.model.Media

/**
 * 将Embedded Picker的Media对象转换为Chimera的ImageInfo对象
 */
fun Media.toImageInfo(): ImageInfo {
    return ImageInfo(
        uri = this.uri.toUri(),
        name = this.displayName,
        size = this.size,
        mimeType = this.mimeType,
        width = 0, // Embedded Picker没有提供宽度信息，将在ImageRepository中获取
        height = 0 // 同理
    )
}