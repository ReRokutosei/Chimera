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

package com.rerokutosei.chimera.ui.settings

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.rerokutosei.chimera.R

@SuppressLint("LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var openSourceLibraries by remember { mutableStateOf<List<Library>?>(null) }

    LaunchedEffect(Unit) {
        openSourceLibraries = runCatching {
            val aboutLibrariesJson = readResourceFile(context.resources, R.raw.aboutlibraries)
            Libs.Builder()
                .withJson(aboutLibrariesJson)
                .build()
                .libraries
                 .sortedBy { it.name.lowercase() }
        }.getOrElse { emptyList() }
    }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.open_source_licenses))
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                item {
                    OpenSourceLibrary(
                        name = "ImageToolbox",
                        author = "Malik Mukhametzyanov",
                        license = "Apache License 2.0",
                        version = "f5e3ada",
                        url = "https://github.com/T8RIN/ImageToolbox"
                    )
                }
                if (openSourceLibraries != null) {
                    items(openSourceLibraries!!) { library ->
                        OpenSourceLibraryItem(library)
                    }
                } else {
                    item {
                        Text(
                            text = "Loading...",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OpenSourceLibraryItem(library: Library) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                library.website?.let { 
                    uriHandler.openUri(it)
                }
            }
            .padding(16.dp)
    ) {
        // 第一行：库名称和版本号
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = library.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            library.artifactVersion?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // 第二行：作者
        if (library.developers.isNotEmpty()) {
            Text(
                text = library.developers.joinToString(", ") { it.name ?: "" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 第三行：许可证
        if (library.licenses.isNotEmpty()) {
            SuggestionChip(
                onClick = { },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Balance,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = library.licenses.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(top = 4.dp),
                shape = MaterialTheme.shapes.extraLarge
            )
        }
    }
}

// 硬编码的依赖库
@Composable
fun OpenSourceLibrary(
    name: String,
    author: String,
    license: String,
    version: String,
    url: String
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                uriHandler.openUri(url)
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = version,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SuggestionChip(
            onClick = {},
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Balance,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = license,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.padding(top = 4.dp),
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

// 读取资源文件的辅助函数
fun readResourceFile(resources: Resources, resourceId: Int): String {
    return resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
}
