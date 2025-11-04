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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rerokutosei.chimera.R

@Composable
fun AboutSection(
    modifier: Modifier = Modifier
) {
    // 关于
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.version) + LocalContext.current.packageManager
                    .getPackageInfo(LocalContext.current.packageName, 0).versionName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )

    // 添加开源许可证项
    var showLicensesDialog by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.open_source_licenses),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showLicensesDialog = true }
    )

    // 开源许可证
    if (showLicensesDialog) {
        OpenSourceLicensesDialog(
            onDismiss = { showLicensesDialog = false }
        )
    }

    // 隐私政策和免责声明项
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.privacy_policy_and_disclaimer),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPrivacyPolicyDialog = true }
    )

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 开发者信息卡片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(245.dp)
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val uriHandler = LocalUriHandler.current
            Card(
                modifier = Modifier
                    .size(245.dp, 245.dp)
                    .clickable {
                        uriHandler.openUri("https://github.com/ReRokutosei")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rerokutosei),
                        contentDescription = stringResource(R.string.developer_avatar),
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.developer_info),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "「君と集まって」\n「星座になれたら」",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // 图标设计者
            Card(
                modifier = Modifier
                    .size(245.dp, 245.dp)
                    .clickable {
                        uriHandler.openUri("https://www.freepik.com/icon/animal_13228011")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ZoomableAppIcon()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.icon_designer),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "Express the power of your ideas with Freepik",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
    // 结束乐队四小只背景图片
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.kessokuband),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.75f),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun ZoomableAppIcon() {
    val customZoomLevel = 1.75f

    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = stringResource(R.string.developer_information),
        modifier = Modifier
            .size(70.dp)
            .graphicsLayer(scaleX = customZoomLevel, scaleY = customZoomLevel),
        contentScale = ContentScale.Fit
    )
}