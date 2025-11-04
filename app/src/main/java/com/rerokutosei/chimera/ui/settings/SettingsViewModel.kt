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

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rerokutosei.chimera.data.local.ImageSettingsManager
import com.rerokutosei.chimera.data.local.StitchSettingsManager
import com.rerokutosei.chimera.data.model.ThemeMode
import com.rerokutosei.chimera.data.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val themeSettingsManager = ThemeRepository.getInstance(application)
    private val imageSettingsManager = ImageSettingsManager.getInstance(application)
    private val stitchSettingsManager = StitchSettingsManager.getInstance(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            kotlinx.coroutines.coroutineScope {
                launch {
                    themeSettingsManager.getDarkThemeFlow().collect { isDarkTheme ->
                        _uiState.value = _uiState.value.copy(isDarkTheme = isDarkTheme)
                    }
                }
                
                launch {
                    themeSettingsManager.getDynamicColorFlow().collect { isDynamicColor ->
                        _uiState.value = _uiState.value.copy(isDynamicColor = isDynamicColor)
                    }
                }
                
                launch {
                    stitchSettingsManager.getOverlayAreaFlow().collect { overlayArea ->
                        _uiState.value = _uiState.value.copy(overlayArea = overlayArea)
                    }
                }
                
                launch {
                    imageSettingsManager.getDeleteOriginalImageFlow().collect { deleteOriginalImage ->
                        _uiState.value = _uiState.value.copy(deleteOriginalImage = deleteOriginalImage)
                    }
                }
                
                launch {
                    stitchSettingsManager.getMultiThreadEnabledFlow().collect { multiThreadEnabled ->
                        _uiState.value = _uiState.value.copy(multiThreadEnabled = multiThreadEnabled)
                    }
                }

                launch {
                    imageSettingsManager.getOutputImageFormatFlow().collect { outputImageFormat ->
                        _uiState.value = _uiState.value.copy(outputImageFormat = outputImageFormat)
                    }
                }

                launch {
                    imageSettingsManager.getOutputImageQualityFlow().collect { outputImageQuality ->
                        _uiState.value = _uiState.value.copy(outputImageQuality = outputImageQuality)
                    }
                }

                launch {
                    themeSettingsManager.getLogLevelFlow().collect { logLevel ->
                        _uiState.value = _uiState.value.copy(logLevel = logLevel)
                    }
                }

                
                launch {
                    imageSettingsManager.getAutoClearImagesFlow().collect { autoClearImages ->
                        _uiState.value = _uiState.value.copy(autoClearImages = autoClearImages)
                    }
                }
                
                launch {
                    themeSettingsManager.getThemeModeFlow().collect { themeMode ->
                        _uiState.value = _uiState.value.copy(themeMode = themeMode)
                    }
                }
                
                launch {
                    themeSettingsManager.getSelectedColorSchemeFlow().collect { selectedColorScheme ->
                        _uiState.value = _uiState.value.copy(selectedColorScheme = selectedColorScheme)
                    }
                }
                
                launch {
                    themeSettingsManager.getCustomPrimaryColorFlow().collect { customPrimaryColor ->
                        _uiState.value = _uiState.value.copy(customPrimaryColor = customPrimaryColor)
                    }
                }
                
                launch {
                    themeSettingsManager.getCustomSecondaryColorFlow().collect { customSecondaryColor ->
                        _uiState.value = _uiState.value.copy(customSecondaryColor = customSecondaryColor)
                    }
                }
                
                launch {
                    themeSettingsManager.getCustomTertiaryColorFlow().collect { customTertiaryColor ->
                        _uiState.value = _uiState.value.copy(customTertiaryColor = customTertiaryColor)
                    }
                }
                
                launch {
                    imageSettingsManager.getHighMemoryLimitFlow().collect { highMemoryLimit ->
                        _uiState.value = _uiState.value.copy(highMemoryLimit = highMemoryLimit)
                    }
                }

                launch {
                    imageSettingsManager.getUseSafPickerFlow().collect { useSafPicker ->
                        _uiState.value = _uiState.value.copy(useSafPicker = useSafPicker)
                    }
                }
                
                launch {
                    imageSettingsManager.getUseEmbeddedPickerFlow().collect { useEmbeddedPicker ->
                        _uiState.value = _uiState.value.copy(useEmbeddedPicker = useEmbeddedPicker)
                    }
                }
                
                launch {
                    imageSettingsManager.getSliderThumbShapeFlow().collect { sliderThumbShape ->
                        _uiState.value = _uiState.value.copy(sliderThumbShape = sliderThumbShape)
                    }
                }
            }
        }
    }

    fun setDynamicColor(isDynamicColor: Boolean) {
        viewModelScope.launch {
            themeSettingsManager.setDynamicColor(isDynamicColor)
        }
    }

    fun setMultiThreadEnabled(enabled: Boolean) {
        viewModelScope.launch {
            stitchSettingsManager.setMultiThreadEnabled(enabled)
        }
    }

    fun setOutputImageFormat(format: Int) {
        viewModelScope.launch {
            imageSettingsManager.setOutputImageFormat(format)
        }
    }

    fun setOutputImageQuality(quality: Int) {
        viewModelScope.launch {
            imageSettingsManager.setOutputImageQuality(quality)
        }
    }

    fun setLogLevel(level: Int) {
        viewModelScope.launch {
            themeSettingsManager.setLogLevel(level)
        }
    }

    fun setAutoClearImages(enabled: Boolean) {
        viewModelScope.launch {
            imageSettingsManager.setAutoClearImages(enabled)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeSettingsManager.setThemeMode(mode)
        }
    }
    
    fun setSelectedColorScheme(schemeName: String) {
        viewModelScope.launch {
            themeSettingsManager.setSelectedColorScheme(schemeName)
        }
    }
    
    fun setCustomPrimaryColor(color: String) {
        viewModelScope.launch {
            themeSettingsManager.setCustomPrimaryColor(color)
        }
    }
    
    fun setCustomSecondaryColor(color: String) {
        viewModelScope.launch {
            themeSettingsManager.setCustomSecondaryColor(color)
        }
    }
    
    fun setCustomTertiaryColor(color: String) {
        viewModelScope.launch {
            themeSettingsManager.setCustomTertiaryColor(color)
        }
    }
    
    fun setHighMemoryLimit(enabled: Boolean) {
        viewModelScope.launch {
            imageSettingsManager.setHighMemoryLimit(enabled)
        }
    }

    fun setUseSafPicker(enabled: Boolean) {
        // 启用SAF Picker时禁用Embedded Picker
        if (enabled) {
            setUseEmbeddedPicker(false)
        }
        
        viewModelScope.launch {
            imageSettingsManager.setUseSafPicker(enabled)
        }
    }
    
    fun setUseEmbeddedPicker(enabled: Boolean) {
        // 启用Embedded Picker时禁用SAF Picker
        if (enabled) {
            setUseSafPicker(false)
        }
        
        viewModelScope.launch {
            imageSettingsManager.setUseEmbeddedPicker(enabled)
        }
    }
    
    /**
     * 检查Embedded Picker所需的权限是否已授予
     */
    fun checkEmbeddedPickerPermissions(): Boolean {
        val context = getApplication<Application>().applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun setSliderThumbShape(shape: Int) {
        viewModelScope.launch {
            imageSettingsManager.setSliderThumbShape(shape)
        }
    }
}

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val isDynamicColor: Boolean = true,
    val overlayArea: Int = 10,
    val autoRemoveTopBottomLines: Boolean = true,
    val autoRemoveLeftRightLines: Boolean = false,
    val cutStatusBar: Boolean = false,
    val cutNavigationBar: Boolean = false,
    val deleteOriginalImage: Boolean = false,
    val multiThreadEnabled: Boolean = false,
    val outputImageFormat: Int = 0, // 0: PNG, 1: JPEG, 2: WEBP
    val outputImageQuality: Int = 85, // 0-100
    val logLevel: Int = 1, // 0: DEBUG, 1: INFO, 2: WARN, 3: ERROR
    val autoClearImages: Boolean = true, // 是否自动清理已选图片
    val highMemoryLimit: Boolean = false, // 是否提高内存阈值
    val useSafPicker: Boolean = false, // 是否使用存储访问框架选择器
    val useEmbeddedPicker: Boolean = false, // 是否使用Embedded Picker
    val sliderThumbShape: Int = 0, // 滑块手柄形状
    val themeMode: ThemeMode = ThemeMode.AUTO, // 主题模式
    val selectedColorScheme: String = "dynamic", // 选中的颜色方案名称
    val customPrimaryColor: String = "", // 自定义主色
    val customSecondaryColor: String = "", // 自定义次色
    val customTertiaryColor: String = "" // 自定义第三色
)