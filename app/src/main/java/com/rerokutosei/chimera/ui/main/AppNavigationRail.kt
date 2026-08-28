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

package com.rerokutosei.chimera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.model.PredefinedColorSchemes
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.utils.color.ColorUtils
import com.rerokutosei.chimera.utils.common.ToastUtil

@Composable
fun AppNavigationRail(
    isCutMode: Boolean,
    onToggleCutMode: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    isSettingsSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showHelpSheet by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    val themeRepository = ThemeRepository.getInstance(context)
    val selectedColorScheme by themeRepository.getSelectedColorSchemeFlow()
        .collectAsStateWithLifecycle(initialValue = "bocchi")
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .clickable {
                        if (selectedColorScheme in listOf("bocchi", "nijika", "kita", "ryo")) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime > 1000) {
                                lastClickTime = currentTime
                                clickCount = 1
                            } else {
                                clickCount++
                                if (clickCount >= 4) {
                                    ToastUtil.showShort(context, "Thank You!")
                                    clickCount = 0
                                }
                            }
                        }
                    }
            ) {
                val circleColors: List<Color> = when (selectedColorScheme) {
                    "bocchi", "nijika", "kita", "ryo" -> {
                        val baseColors = listOf(
                            PredefinedColorSchemes.bocchi.primary,
                            PredefinedColorSchemes.nijika.primary,
                            PredefinedColorSchemes.kita.primary,
                            PredefinedColorSchemes.ryo.primary
                        )
                        if (isDarkTheme) baseColors.map { ColorUtils.adjustColorForDarkTheme(it) } else baseColors
                    }
                    else -> List(4) { MaterialTheme.colorScheme.primary }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    circleColors.take(2).forEach { color ->
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(
                                color = color,
                                radius = size.minDimension / 2,
                                center = Offset(size.width / 2, size.height / 2),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    circleColors.drop(2).forEach { color ->
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(
                                color = color,
                                radius = size.minDimension / 2,
                                center = Offset(size.width / 2, size.height / 2),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        NavigationRailItem(
            selected = !isSettingsSelected && !isCutMode,
            onClick = {
                onToggleCutMode(false)
            },
            icon = { Icon(Icons.Rounded.Layers, contentDescription = stringResource(R.string.stitch_mode)) },
            label = { Text(stringResource(R.string.stitch_mode)) }
        )

        NavigationRailItem(
            selected = !isSettingsSelected && isCutMode,
            onClick = {
                onToggleCutMode(true)
            },
            icon = { Icon(Icons.Rounded.ContentCut, contentDescription = stringResource(R.string.cut_mode)) },
            label = { Text(stringResource(R.string.cut_mode)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { showHelpSheet = true },
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Rounded.Help, contentDescription = stringResource(R.string.help))
        }

        if (isSettingsSelected) {
            FilledTonalIconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings))
            }
        } else {
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings))
            }
        }
    }

    if (showHelpSheet) {
        HelpBottomSheet(onDismiss = { showHelpSheet = false })
    }
}
