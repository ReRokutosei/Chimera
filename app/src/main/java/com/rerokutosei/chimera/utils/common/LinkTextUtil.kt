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

package com.rerokutosei.chimera.utils.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import java.util.regex.Pattern

/**
 * 可点击链接文本组件
 * 自动识别文本中的URL并使其可点击跳转
 */
@Composable
fun AnnotatedLinkText(
    text: String,
    style: TextStyle = TextStyle(),
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val urlPattern = Pattern.compile("https?://[\\w.-]+(?:/[\\w.-]*)*")
    val matcher = urlPattern.matcher(text)
    
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        while (matcher.find()) {
            append(text.substring(lastIndex, matcher.start()))
            
            val url = matcher.group()
            withLink(
                link = LinkAnnotation.Url(url) { 
                    uriHandler.openUri(url)
                }
            ) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(url)
                }
            }
            
            lastIndex = matcher.end()
        }
        append(text.substring(lastIndex))
    }
    
    Text(
        text = annotatedString,
        style = style,
        modifier = modifier
    )
}