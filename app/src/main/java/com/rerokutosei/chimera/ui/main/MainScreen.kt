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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rerokutosei.chimera.R
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.data.model.CutPreset
import com.rerokutosei.chimera.data.model.ImageListDirectionMode
import com.rerokutosei.chimera.ui.settings.SettingsScreen
import com.rerokutosei.chimera.ui.stitch.StitchState
import com.rerokutosei.chimera.ui.stitch.StitchViewModel
import com.rerokutosei.chimera.ui.viewer.CutSaveState
import com.rerokutosei.chimera.ui.viewer.ImageViewerViewModel
import com.rerokutosei.chimera.ui.viewer.SaveResultDialog
import com.rerokutosei.chimera.ui.viewer.cutSaveIssueMessage
import com.rerokutosei.chimera.utils.common.ShowToast
import com.rerokutosei.chimera.utils.common.ToastUtil
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.image.ImageSaveResult
import com.rerokutosei.chimera.utils.image.ImageSaver
import com.rerokutosei.chimera.utils.stitch.StitchOrientation
import com.t8rin.imagereordercarousel.CarouselScrollDirection
import com.t8rin.imagereordercarousel.ImageReorderCarousel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStitch: () -> Unit,
    onNavigateToCut: () -> Unit = {},
) {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val containerWidth = with(density) { windowInfo.containerSize.width.toDp() }
    val isWideScreen = containerWidth >= 600.dp
    val isWorkstationMode = containerWidth >= 840.dp

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDataLoaded by viewModel.isDataLoaded.collectAsStateWithLifecycle()
    val showSettingsInCanvas by viewModel.showSettingsInCanvas.collectAsStateWithLifecycle()
    var isPageEntered by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val imageSaver = remember { ImageSaver(context) }
    val bitmapLoader = remember { BitmapLoader(context) }
    val stitchViewModel: StitchViewModel = viewModel()
    val imageViewerViewModel: ImageViewerViewModel = viewModel()
    val stitchUiState by stitchViewModel.uiState.collectAsStateWithLifecycle()
    val cutSaveState by imageViewerViewModel.cutSaveState.collectAsStateWithLifecycle()

    var isCutPreviewActive by remember { mutableStateOf(false) }
    val workstationStitchedBitmap =
        (stitchUiState.stitchState as? StitchState.Success)?.result

    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        isPageEntered = true
    }

    // 边界情况处理：如果在工作台内嵌设置打开状态下，窗口被拖拽缩窄到 < 840dp，平滑迁移到全屏设置页
    LaunchedEffect(isWorkstationMode) {
        if (!isWorkstationMode && showSettingsInCanvas) {
            viewModel.setShowSettingsInCanvas(false)
            onNavigateToSettings()
        }
    }

    // 获取设置项状态
    val imageSettingsManager = ImageSettingsManager.getInstance(context)
    val useSafPicker by imageSettingsManager.getUseSafPickerFlow()
        .collectAsStateWithLifecycle(initialValue = false)
    val useEmbeddedPicker by imageSettingsManager.getUseEmbeddedPickerFlow()
        .collectAsStateWithLifecycle(initialValue = false)
    val sliderThumbShape by imageSettingsManager.getSliderThumbShapeFlow()
        .collectAsStateWithLifecycle(initialValue = 0)
    val imageListDirection by imageSettingsManager.getImageListDirectionFlow()
        .collectAsStateWithLifecycle(initialValue = ImageListDirectionMode.HORIZONTAL)

    // 添加尺寸验证状态
    val resolutionValidationState by viewModel.resolutionValidationState.collectAsStateWithLifecycle()
    val showResolutionErrorToast by viewModel.showResolutionErrorToast.collectAsStateWithLifecycle()

    // Embedded Picker状态
    var showEmbeddedPicker by remember { mutableStateOf(false) }

    // 添加标识，用于跟踪当前使用的图片选择器类型
    var isUsingEmbeddedPicker by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var isCarouselInteracting by remember { mutableStateOf(false) }

    // 显示Toast消息
    ShowToast(
        message = uiState.toastMessage,
        onShown = { viewModel.clearToast() }
    )

    // 当图片列表不为空且正在加载时，关闭加载状态
    LaunchedEffect(uiState.selectedImages) {
        if (uiState.selectedImages.isNotEmpty() && uiState.isImagePreviewLoading) {
            viewModel.setImagePreviewLoading(false)
        }
        if (uiState.selectedImages.isEmpty()) {
            isCarouselInteracting = false
        }
    }

    // 当图片列表变动或核心参数修改时，自动重置工作台大画布的旧预览，回到空态
    LaunchedEffect(
        uiState.selectedImages,
        uiState.isCutMode,
        uiState.cutPreset,
        uiState.stitchMode,
        uiState.overlayMode,
        uiState.widthScale,
        uiState.overlayArea,
        uiState.imageSpacing,
        uiState.imageSpacingColor
    ) {
        isCutPreviewActive = false
        stitchViewModel.clearStitchState()
    }

    // 工作台模式切图保存完成弹窗
    val cutSaveMessage = when (val state = cutSaveState) {
        CutSaveState.Idle, CutSaveState.Saving -> null
        is CutSaveState.Success -> when {
            uiState.cutPreset == CutPreset.GRID_4 -> stringResource(R.string.cut_completed_4)
            uiState.cutPreset == CutPreset.GRID_9 -> stringResource(R.string.cut_completed_9)
            uiState.cutPreset.rows == 1 -> stringResource(R.string.cut_completed_x)
            else -> stringResource(R.string.cut_completed_generic)
        }
        is CutSaveState.Failure -> cutSaveIssueMessage(state.issue)
    }
    cutSaveMessage?.let {
        SaveResultDialog(it, imageViewerViewModel::clearCutSaveState)
    }

    // 显示Embedded Picker对话框
    if (showEmbeddedPicker) {
        EmbeddedPickerDialog(
            onImagesSelected = { uris ->
                viewModel.selectImages(uris, isFromEmbeddedPicker = isUsingEmbeddedPicker)
                showEmbeddedPicker = false
                isUsingEmbeddedPicker = false
            },
            onDismiss = {
                showEmbeddedPicker = false
                isUsingEmbeddedPicker = false
            }
        )
    }

    if (isWideScreen) {
        // 平板 / 折叠屏展开：左侧导航轨 + 内容区
        Row(modifier = modifier.fillMaxSize()) {
            AppNavigationRail(
                isCutMode = uiState.isCutMode,
                onToggleCutMode = { isCut ->
                    viewModel.setShowSettingsInCanvas(false)
                    if (isCut != uiState.isCutMode) viewModel.toggleCutMode()
                },
                onNavigateToSettings = {
                    if (isWorkstationMode) {
                        viewModel.setShowSettingsInCanvas(!showSettingsInCanvas)
                    } else {
                        onNavigateToSettings()
                    }
                },
                isSettingsSelected = isWorkstationMode && showSettingsInCanvas
            )

            if (isWorkstationMode) {
                // 大屏沉浸式工作台：左控制台 (360dp) + 右大画布/设置面板 (自适应)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            userScrollEnabled = !isCarouselInteracting
                        ) {
                            item {
                                ErrorDialog(
                                    errorMessage = uiState.errorMessage,
                                    isVisible = uiState.errorMessage != null,
                                    onDismiss = { viewModel.clearError() }
                                )

                                ImagePickerButton(
                                    context = context,
                                    isPageEntered = isPageEntered,
                                    isDataLoaded = isDataLoaded,
                                    useSafPicker = useSafPicker,
                                    useEmbeddedPicker = useEmbeddedPicker,
                                    onImagesSelected = { uris ->
                                        viewModel.selectImages(uris, isFromEmbeddedPicker = isUsingEmbeddedPicker)
                                        isUsingEmbeddedPicker = false
                                    },
                                    showEmbeddedPicker = {
                                        showEmbeddedPicker = true
                                        isUsingEmbeddedPicker = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.isCutMode) {
                                    CutPresetSettingsCard(
                                        selectedPreset = uiState.cutPreset,
                                        onPresetSelected = { viewModel.updateCutPreset(it) }
                                    )
                                } else {
                                    ParameterSettingsCard(
                                        uiState = uiState,
                                        isPageEntered = isPageEntered,
                                        isDataLoaded = isDataLoaded,
                                        sliderThumbShape = sliderThumbShape,
                                        onUpdateStitchMode = { viewModel.updateStitchMode(it) },
                                        onUpdateOverlayMode = { viewModel.updateOverlayMode(it) },
                                        onUpdateWidthScale = { viewModel.updateWidthScale(it) },
                                        onUpdateOverlayArea = { viewModel.updateOverlayArea(it) },
                                        onUpdateImageSpacing = { viewModel.updateImageSpacing(it) },
                                        onUpdateImageSpacingColor = { viewModel.updateImageSpacingColor(it) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (!uiState.isCutMode) {
                                    EstimatedResolutionCard(
                                        resolutionValidationState = resolutionValidationState,
                                        showResolutionErrorToast = showResolutionErrorToast,
                                        onToastShown = { viewModel.clearResolutionErrorToast() }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                if (uiState.selectedImages.isNotEmpty()) {
                                    BottomActionButtons(
                                        uiState = uiState,
                                        isCutMode = uiState.isCutMode,
                                        isPageEntered = isPageEntered,
                                        isDataLoaded = isDataLoaded,
                                        onClearImages = { viewModel.clearImages() },
                                        onStartStitching = {
                                            viewModel.setShowSettingsInCanvas(false)
                                            viewModel.onStartStitching()
                                            val uris = uiState.selectedImages.map { it.uri }
                                            if (uris.size >= 2) {
                                                val orientation = when (uiState.stitchMode) {
                                                    StitchMode.DIRECT_HORIZONTAL -> StitchOrientation.HORIZONTAL
                                                    StitchMode.DIRECT_VERTICAL -> StitchOrientation.VERTICAL
                                                }
                                                if (uiState.overlayMode == OverlayMode.ENABLED) {
                                                    stitchViewModel.stitchOverlay(
                                                        imageUris = uris,
                                                        overlayRatio = uiState.overlayArea,
                                                        widthScale = uiState.widthScale,
                                                        orientation = orientation
                                                    )
                                                } else {
                                                    stitchViewModel.stitchImages(
                                                        orientation = orientation,
                                                        imageUris = uris,
                                                        widthScale = uiState.widthScale,
                                                        imageSpacing = uiState.imageSpacing
                                                    )
                                                }
                                            }
                                        },
                                        onStartCutting = {
                                            viewModel.setShowSettingsInCanvas(false)
                                            viewModel.onStartCutting()
                                            isCutPreviewActive = true
                                        },
                                        onNavigateToStitch = {},
                                        isStartButtonEnabled = if (uiState.isCutMode) true else resolutionValidationState !is ResolutionValidationState.Invalid
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // 选图缩略序列
                            val imageUris = uiState.selectedImages.map { it.uri }
                            if (uiState.selectedImages.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        SortMenuButton(
                                            sortMenuExpanded = sortMenuExpanded,
                                            onToggleSortMenu = { sortMenuExpanded = !sortMenuExpanded },
                                            currentSortMode = uiState.currentSortMode,
                                            onSortModeSelected = {
                                                sortMenuExpanded = false
                                                viewModel.sortSelectedImages(it)
                                            }
                                        )
                                    }

                                    ImageReorderCarousel(
                                        images = imageUris,
                                        onReorder = { reorderedUris -> viewModel.reorderImages(reorderedUris) },
                                        onNeedToAddImage = {},
                                        onNeedToRemoveImageAt = { index ->
                                            if (index in uiState.selectedImages.indices) {
                                                viewModel.removeImage(uiState.selectedImages[index])
                                            }
                                        },
                                        showAddButton = false,
                                        showSortButton = false,
                                        enableImagePreview = false,
                                        scrollDirection = when (imageListDirection) {
                                            ImageListDirectionMode.HORIZONTAL -> CarouselScrollDirection.HORIZONTAL
                                            ImageListDirectionMode.VERTICAL -> CarouselScrollDirection.VERTICAL
                                            ImageListDirectionMode.AUTO -> if (uiState.stitchMode == StitchMode.DIRECT_HORIZONTAL) {
                                                CarouselScrollDirection.HORIZONTAL
                                            } else {
                                                CarouselScrollDirection.VERTICAL
                                            }
                                        },
                                        onInteractionStateChanged = { interacting ->
                                            isCarouselInteracting = interacting
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
                                    )
                                }
                            }
                        }
                    }

                    // 右侧面板：带有流畅缩放淡入淡出动画的 内嵌设置页 OR 沉浸式大画布
                    AnimatedContent(
                        targetState = showSettingsInCanvas,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250))) togetherWith
                            (fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
                        },
                        label = "workstation_right_panel",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) { isSettingsOpen ->
                        if (isSettingsOpen) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { viewModel.setShowSettingsInCanvas(false) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = stringResource(R.string.back)
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        SettingsScreen(
                                            mainViewModel = viewModel,
                                            modifier = Modifier.widthIn(max = 680.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            WorkstationCanvas(
                                isCutMode = uiState.isCutMode,
                                selectedImages = uiState.selectedImages,
                                cutPreset = uiState.cutPreset,
                                stitchedBitmap = workstationStitchedBitmap,
                                isCutPreviewActive = isCutPreviewActive,
                                isStitching = stitchUiState.stitchState is StitchState.Processing,
                                stitchProgress = stitchUiState.progress,
                                onSaveStitched = {
                                    workstationStitchedBitmap?.let { bmp ->
                                        coroutineScope.launch {
                                            when (imageSaver.saveToGallery(bmp)) {
                                                is ImageSaveResult.Success -> ToastUtil.showShort(
                                                    context,
                                                    context.getString(R.string.image_saved_to_album)
                                                )
                                                is ImageSaveResult.Failure -> ToastUtil.showShort(
                                                    context,
                                                    context.getString(R.string.save_failed)
                                                )
                                            }
                                        }
                                    }
                                },
                                onSaveCutAll = {
                                    val uris = uiState.selectedImages.map { it.uri }
                                    if (uris.isNotEmpty()) {
                                        imageViewerViewModel.setCutMode(
                                            imageUris = uris,
                                            gridCols = uiState.cutPreset.cols,
                                            gridRows = uiState.cutPreset.rows
                                        )
                                        imageViewerViewModel.saveAllCutImages()
                                    }
                                },
                                onSaveCutCurrent = { page ->
                                    val uris = uiState.selectedImages.map { it.uri }
                                    if (uris.isNotEmpty() && page in uris.indices) {
                                        imageViewerViewModel.setCutMode(
                                            imageUris = uris,
                                            gridCols = uiState.cutPreset.cols,
                                            gridRows = uiState.cutPreset.rows,
                                            startIndex = page
                                        )
                                        imageViewerViewModel.saveCurrentCutImage(page)
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // 中等宽度（600dp ~ 839dp，例如竖屏平板）：居中单列
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        userScrollEnabled = !isCarouselInteracting
                    ) {
                        item {
                            TopAppBar(onNavigateToSettings = onNavigateToSettings)

                            Spacer(modifier = Modifier.height(8.dp))

                            ErrorDialog(
                                errorMessage = uiState.errorMessage,
                                isVisible = uiState.errorMessage != null,
                                onDismiss = { viewModel.clearError() }
                            )

                            ImagePickerButton(
                                context = context,
                                isPageEntered = isPageEntered,
                                isDataLoaded = isDataLoaded,
                                useSafPicker = useSafPicker,
                                useEmbeddedPicker = useEmbeddedPicker,
                                onImagesSelected = { uris ->
                                    viewModel.selectImages(uris, isFromEmbeddedPicker = isUsingEmbeddedPicker)
                                    isUsingEmbeddedPicker = false
                                },
                                showEmbeddedPicker = {
                                    showEmbeddedPicker = true
                                    isUsingEmbeddedPicker = true
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.isCutMode) {
                                CutPresetSettingsCard(
                                    selectedPreset = uiState.cutPreset,
                                    onPresetSelected = { viewModel.updateCutPreset(it) }
                                )
                            } else {
                                ParameterSettingsCard(
                                    uiState = uiState,
                                    isPageEntered = isPageEntered,
                                    isDataLoaded = isDataLoaded,
                                    sliderThumbShape = sliderThumbShape,
                                    onUpdateStitchMode = { viewModel.updateStitchMode(it) },
                                    onUpdateOverlayMode = { viewModel.updateOverlayMode(it) },
                                    onUpdateWidthScale = { viewModel.updateWidthScale(it) },
                                    onUpdateOverlayArea = { viewModel.updateOverlayArea(it) },
                                    onUpdateImageSpacing = { viewModel.updateImageSpacing(it) },
                                    onUpdateImageSpacingColor = { viewModel.updateImageSpacingColor(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!uiState.isCutMode) {
                                EstimatedResolutionCard(
                                    resolutionValidationState = resolutionValidationState,
                                    showResolutionErrorToast = showResolutionErrorToast,
                                    onToastShown = { viewModel.clearResolutionErrorToast() }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.selectedImages.isNotEmpty()) {
                                BottomActionButtons(
                                    uiState = uiState,
                                    isCutMode = uiState.isCutMode,
                                    isPageEntered = isPageEntered,
                                    isDataLoaded = isDataLoaded,
                                    onClearImages = { viewModel.clearImages() },
                                    onStartStitching = { viewModel.onStartStitching() },
                                    onStartCutting = { viewModel.onStartCutting(); onNavigateToCut() },
                                    onNavigateToStitch = onNavigateToStitch,
                                    isStartButtonEnabled = if (uiState.isCutMode) true else resolutionValidationState !is ResolutionValidationState.Invalid
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val imageUris = uiState.selectedImages.map { it.uri }
                        if (uiState.selectedImages.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    SortMenuButton(
                                        sortMenuExpanded = sortMenuExpanded,
                                        onToggleSortMenu = { sortMenuExpanded = !sortMenuExpanded },
                                        currentSortMode = uiState.currentSortMode,
                                        onSortModeSelected = {
                                            sortMenuExpanded = false
                                            viewModel.sortSelectedImages(it)
                                        }
                                    )
                                }

                                ImageReorderCarousel(
                                    images = imageUris,
                                    onReorder = { reorderedUris -> viewModel.reorderImages(reorderedUris) },
                                    onNeedToAddImage = {},
                                    onNeedToRemoveImageAt = { index ->
                                        if (index in uiState.selectedImages.indices) {
                                            viewModel.removeImage(uiState.selectedImages[index])
                                        }
                                    },
                                    showAddButton = false,
                                    showSortButton = false,
                                    enableImagePreview = false,
                                    scrollDirection = when (imageListDirection) {
                                        ImageListDirectionMode.HORIZONTAL -> CarouselScrollDirection.HORIZONTAL
                                        ImageListDirectionMode.VERTICAL -> CarouselScrollDirection.VERTICAL
                                        ImageListDirectionMode.AUTO -> if (uiState.stitchMode == StitchMode.DIRECT_HORIZONTAL) {
                                            CarouselScrollDirection.HORIZONTAL
                                        } else {
                                            CarouselScrollDirection.VERTICAL
                                        }
                                    },
                                    onInteractionStateChanged = { interacting ->
                                        isCarouselInteracting = interacting
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
                                    )
                            }
                        } else {
                            item {
                                EmptyImageSelectionPlaceholder(
                                    isCutMode = uiState.isCutMode,
                                    isPageEntered = isPageEntered,
                                    isDataLoaded = isDataLoaded,
                                    isLoading = uiState.isImagePreviewLoading
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // 手机常规竖屏模式：单列流 + 底部悬浮胶囊导航
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isPageEntered) 1f else 0f
                        translationX = if (isPageEntered) 0f else 100f
                    }
                    .animateContentSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                userScrollEnabled = !isCarouselInteracting
            ) {
                item {
                    TopAppBar(
                        onNavigateToSettings = onNavigateToSettings
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ErrorDialog(
                        errorMessage = uiState.errorMessage,
                        isVisible = uiState.errorMessage != null,
                        onDismiss = { viewModel.clearError() }
                    )

                    ImagePickerButton(
                        context = context,
                        isPageEntered = isPageEntered,
                        isDataLoaded = isDataLoaded,
                        useSafPicker = useSafPicker,
                        useEmbeddedPicker = useEmbeddedPicker,
                        onImagesSelected = { uris ->
                            viewModel.selectImages(uris, isFromEmbeddedPicker = isUsingEmbeddedPicker)
                            isUsingEmbeddedPicker = false
                        },
                        showEmbeddedPicker = {
                            showEmbeddedPicker = true
                            isUsingEmbeddedPicker = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isCutMode) {
                        CutPresetSettingsCard(
                            selectedPreset = uiState.cutPreset,
                            onPresetSelected = { viewModel.updateCutPreset(it) }
                        )
                    } else {
                        ParameterSettingsCard(
                            uiState = uiState,
                            isPageEntered = isPageEntered,
                            isDataLoaded = isDataLoaded,
                            sliderThumbShape = sliderThumbShape,
                            onUpdateStitchMode = { viewModel.updateStitchMode(it) },
                            onUpdateOverlayMode = { viewModel.updateOverlayMode(it) },
                            onUpdateWidthScale = { viewModel.updateWidthScale(it) },
                            onUpdateOverlayArea = { viewModel.updateOverlayArea(it) },
                            onUpdateImageSpacing = { viewModel.updateImageSpacing(it) },
                            onUpdateImageSpacingColor = { viewModel.updateImageSpacingColor(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!uiState.isCutMode) {
                        EstimatedResolutionCard(
                            resolutionValidationState = resolutionValidationState,
                            showResolutionErrorToast = showResolutionErrorToast,
                            onToastShown = { viewModel.clearResolutionErrorToast() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.selectedImages.isNotEmpty()) {
                        BottomActionButtons(
                            uiState = uiState,
                            isCutMode = uiState.isCutMode,
                            isPageEntered = isPageEntered,
                            isDataLoaded = isDataLoaded,
                            onClearImages = { viewModel.clearImages() },
                            onStartStitching = { viewModel.onStartStitching() },
                            onStartCutting = { viewModel.onStartCutting(); onNavigateToCut() },
                            onNavigateToStitch = onNavigateToStitch,
                            isStartButtonEnabled = if (uiState.isCutMode) true else resolutionValidationState !is ResolutionValidationState.Invalid
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                val imageUris = uiState.selectedImages.map { it.uri }
                if (uiState.selectedImages.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            SortMenuButton(
                                sortMenuExpanded = sortMenuExpanded,
                                onToggleSortMenu = { sortMenuExpanded = !sortMenuExpanded },
                                currentSortMode = uiState.currentSortMode,
                                onSortModeSelected = {
                                    sortMenuExpanded = false
                                    viewModel.sortSelectedImages(it)
                                }
                            )
                        }

                        ImageReorderCarousel(
                            images = imageUris,
                            onReorder = { reorderedUris -> viewModel.reorderImages(reorderedUris) },
                            onNeedToAddImage = {},
                            onNeedToRemoveImageAt = { index ->
                                if (index in uiState.selectedImages.indices) {
                                    viewModel.removeImage(uiState.selectedImages[index])
                                }
                            },
                            showAddButton = false,
                            showSortButton = false,
                            enableImagePreview = false,
                            scrollDirection = when (imageListDirection) {
                                ImageListDirectionMode.HORIZONTAL -> CarouselScrollDirection.HORIZONTAL
                                ImageListDirectionMode.VERTICAL -> CarouselScrollDirection.VERTICAL
                                ImageListDirectionMode.AUTO -> if (uiState.stitchMode == StitchMode.DIRECT_HORIZONTAL) {
                                    CarouselScrollDirection.HORIZONTAL
                                } else {
                                    CarouselScrollDirection.VERTICAL
                                }
                            },
                            onInteractionStateChanged = { interacting ->
                                isCarouselInteracting = interacting
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
                        )
                    }
                } else {
                    item {
                        EmptyImageSelectionPlaceholder(
                            isCutMode = uiState.isCutMode,
                            isPageEntered = isPageEntered,
                            isDataLoaded = isDataLoaded,
                            isLoading = uiState.isImagePreviewLoading
                        )
                    }
                }
            }

            FloatingModeNavigationBar(
                isCutMode = uiState.isCutMode,
                onToggleCutMode = { isCut ->
                    if (isCut != uiState.isCutMode) {
                        viewModel.toggleCutMode()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CutPresetSettingsCard(
    selectedPreset: CutPreset,
    onPresetSelected: (CutPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CustomSegmentedButtonRow(
                options = listOf(CutPreset.GRID_4, CutPreset.GRID_9),
                selectedOption = selectedPreset,
                onOptionSelected = onPresetSelected,
                optionDisplayName = { stringResource(it.titleRes) }
            )

            CustomSegmentedButtonRow(
                options = listOf(CutPreset.X_3, CutPreset.X_4),
                selectedOption = selectedPreset,
                onOptionSelected = onPresetSelected,
                optionDisplayName = { stringResource(it.titleRes) }
            )

            Text(
                text = stringResource(selectedPreset.hintRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SortMenuButton(
    sortMenuExpanded: Boolean,
    onToggleSortMenu: () -> Unit,
    currentSortMode: com.rerokutosei.chimera.ui.main.ImageSortMode?,
    onSortModeSelected: (com.rerokutosei.chimera.ui.main.ImageSortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        IconButton(onClick = onToggleSortMenu) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = stringResource(R.string.sort_images)
            )
        }

        AnimatedVisibility(visible = sortMenuExpanded) {
            Card(
                modifier = Modifier.width(250.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sort_by_time),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    CustomSegmentedButtonRow(
                        options = listOf(
                            com.rerokutosei.chimera.ui.main.ImageSortMode.TIME_ASC,
                            com.rerokutosei.chimera.ui.main.ImageSortMode.TIME_DESC
                        ),
                        selectedOption = currentSortMode,
                        onOptionSelected = onSortModeSelected,
                        optionDisplayName = {
                            when (it) {
                                com.rerokutosei.chimera.ui.main.ImageSortMode.TIME_ASC -> stringResource(R.string.sort_old_to_new)
                                com.rerokutosei.chimera.ui.main.ImageSortMode.TIME_DESC -> stringResource(R.string.sort_new_to_old)
                                else -> ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.sort_by_name),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    CustomSegmentedButtonRow(
                        options = listOf(
                            com.rerokutosei.chimera.ui.main.ImageSortMode.NAME_ASC,
                            com.rerokutosei.chimera.ui.main.ImageSortMode.NAME_DESC
                        ),
                        selectedOption = currentSortMode,
                        onOptionSelected = onSortModeSelected,
                        optionDisplayName = {
                            when (it) {
                                com.rerokutosei.chimera.ui.main.ImageSortMode.NAME_ASC -> stringResource(R.string.sort_a_to_z)
                                com.rerokutosei.chimera.ui.main.ImageSortMode.NAME_DESC -> stringResource(R.string.sort_z_to_a)
                                else -> ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyImageSelectionPlaceholder(
    isCutMode: Boolean,
    isPageEntered: Boolean,
    isDataLoaded: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            ContainedLoadingIndicator()
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isCutMode) stringResource(R.string.please_select_cut_images) else stringResource(
                        R.string.please_select_images
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.alpha(if (isPageEntered && isDataLoaded) 1f else 0f)
                )
            }
        }
    }
}
