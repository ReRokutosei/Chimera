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

package com.rerokutosei.chimera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.model.PredefinedColorSchemes
import com.rerokutosei.chimera.data.repository.ThemeRepository
import com.rerokutosei.chimera.utils.color.ColorUtils
import com.rerokutosei.chimera.utils.common.ToastUtil

@Composable
fun TopAppBar(
    onNavigateToSettings: () -> Unit
) {
    var showHelpSheet by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    val themeRepository = ThemeRepository.getInstance(context)
    val selectedColorScheme by themeRepository.getSelectedColorSchemeFlow().collectAsState(initial = "bocchi")
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable {
                    // 只有在预设主题下点击才触发彩蛋
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
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 每个圆的间隔
            ) {
                val circleColors = when (selectedColorScheme) {
                    "bocchi", "nijika", "kita", "ryo" -> {
                        val baseColors = listOf(
                            PredefinedColorSchemes.bocchi.primary,
                            PredefinedColorSchemes.nijika.primary,
                            PredefinedColorSchemes.kita.primary,
                            PredefinedColorSchemes.ryo.primary
                        )
                        if (isDarkTheme) {
                            baseColors.map { ColorUtils.adjustColorForDarkTheme(it) }
                        } else {
                            baseColors
                        }
                    }
                    else -> {
                        List(4) { MaterialTheme.colorScheme.primary }
                    }
                }
                
                circleColors.forEach { color ->
                    Canvas(
                        modifier = Modifier.size(20.dp) // 整个圆的大小
                    ) {
                        drawCircle(
                            color = color,
                            radius = size.minDimension / 2,
                            center = Offset(size.width / 2, size.height / 2),
                            style = Stroke(width = 4.dp.toPx()) // 圆线条的粗细
                        )
                    }
                }
            }
        }

        Row {
            IconButton(onClick = { showHelpSheet = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Help,
                    contentDescription = stringResource(R.string.help)
                )
            }
        }
        
        Row {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }
    }
    
    if (showHelpSheet) {
        HelpBottomSheet(
            onDismiss = { showHelpSheet = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpContent()

            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
fun HelpContent() {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.help_card_title_mode_direct),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.help_card_content_mode_direct),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.help_card_title_mode_overlay),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.help_card_content_mode_overlay),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.help_card_title_format_limit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.help_card_content_format_limit),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.help_card_title_setting_output),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.help_card_content_setting_output),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.help_card_title_setting_performance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.help_card_content_setting_performance),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.help_card_title_setting_picker),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.help_card_content_setting_picker),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}