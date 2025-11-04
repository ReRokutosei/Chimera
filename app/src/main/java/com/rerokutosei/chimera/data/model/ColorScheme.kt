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

package com.rerokutosei.chimera.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

// Color序列化器
object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val argb = value.toArgb()
        encoder.encodeString(String.format("#%08X", argb))
    }

    override fun deserialize(decoder: Decoder): Color {
        val hexString = decoder.decodeString()
        return Color(hexString.toColorInt())
    }
}

/**
 * 颜色方案数据类
 * @param name 方案名称
 * @param primary 主色
 * @param secondary 次色
 * @param tertiary 第三色
 * @param isDynamic 是否为动态色彩
 */
@Serializable
data class ColorScheme(
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val primary: Color,
    @Serializable(with = ColorSerializer::class)
    val secondary: Color,
    @Serializable(with = ColorSerializer::class)
    val tertiary: Color,
    val isDynamic: Boolean = false,
) {
    fun toSerializedString(): String {
        return Json.encodeToString(this)
    }
    
    companion object {
        fun fromSerializedString(serialized: String): ColorScheme {
            return Json.decodeFromString(serialized)
        }
    }
}