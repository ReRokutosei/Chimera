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

package com.rerokutosei.chimera.ui.navigation

/**
 * 应用导航路由定义
 */
sealed class Route(val route: String) {
    object Main : Route("main")
    object Settings : Route("settings")
    object ImageViewer : Route("viewer")

    companion object {
        /**
         * 获取所有路由列表，用于导航图构建
         */
        val allRoutes = listOf(Main, Settings, ImageViewer)
    }
}