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

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

/**
 * 全局Toast提示工具类
 */
class ToastUtil {
    companion object {
        private var toast: Toast? = null

        /**
         * 显示短时间Toast提示
         */
        fun showShort(context: Context, message: String) {
            toast?.cancel()
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }
}

/**
 * Composable函数中使用的Toast提示
 */
@Composable
fun ShowToast(message: String?, onShown: () -> Unit = {}) {
    val context = LocalContext.current
    
    LaunchedEffect(message) {
        if (message != null) {
            ToastUtil.showShort(context, message)
            onShown()
        }
    }
}