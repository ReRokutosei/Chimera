<div align="center">
</br>
<img src="../assets/cover.webp" width="2323" />
</div>


<div align="center">

# Chimera
</div>

<p align="center">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-CD15ED?logo=kotlin&logoColor=white&style=for-the-badge"/>
  <img alt="Release" src="https://img.shields.io/github/v/release/ReRokutosei/Chimera?color=FF6598&include_prereleases&logo=github&style=for-the-badge&labelColor=FF6598"/>
  </br>
  <img alt="GPLv3" src="https://img.shields.io/badge/GPL%20v3-008033?style=for-the-badge&logo=gnu&logoColor=white"/>
  <img alt="API" src="https://img.shields.io/badge/Api%2029+-34A853?logo=android&logoColor=white&style=for-the-badge"/>
  </br>
  <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label="/>
  <img alt="material" src="https://custom-icon-badges.demolab.com/badge/material%20you-6442D6?style=for-the-badge&logoColor=white&logo=material-you"/>
  </br>
  <a href="https://deepwiki.com/ReRokutosei/Chimera"><img alt="DeepWiki" src="https://img.shields.io/badge/Ask%20DeepWiki-0D7FC0?style=for-the-badge&logoColor=white"/></a>
  </br>
  

<div align="center">

**[English](README.md) | 简体中文**

## 🗺️ 项目概述

Chimera 是一个现代化的 Android 图片拼接应用

使用 Kotlin 和 Jetpack Compose 开发

允许将多张图片按照不同的模式拼接成一张长图或宽图

支持 JPEG、PNG、WEBP

支持英语、西班牙语、日语、简体中文、繁体中文(HK)、繁体中文(TW)

</div>

## 🌟 核心功能

### 🖼️ 多种图片选择方式

![选择器](../assets/picker.webp)

应用支持多种选择器，即使不授予任何权限也能使用：

1. 系统照片选择器 (Photo Picker)
	  - **无需任何权限**
	  - 适用于 SDK 31+ 或能接收谷歌组件更新的设备
	  - 谷歌推荐的现代化选择方式
	  - 但选择多张图片存在Url顺序错乱问题，并且只能查看存放在Picture公共目录的相册(影集)页

2. 嵌入式选择器 (Embedded Picker)
	  - **需要存储权限**：`READ_MEDIA_IMAGES` (SDK 33+) 或 `READ_EXTERNAL_STORAGE` (SDK 32-)
	  - 从 ImageToolbox 项目提取的强大选择器，支持搜索、查看相册
	  - 界面美观，解决了Photo Picker的问题

3. 存储访问框架选择器 (SAF)
	  - **无需任何权限**
	  - 适用于所有 SDK 版本 (29-36)
	  - 通过系统文件选择器安全访问文件


### 🔧 拼接模式

#### 📐 直接拼接
- **横向拼接**：将图片从左到右水平排列
- **纵向拼接**：将图片从上到下垂直排列
- **图片间隔**：支持 0-50px 的图片间隔设置，使用黑色像素填充
- **宽度/高度缩放**：
  - 最小：将所有图片缩放到最小宽度/高度
  - 原始：保持图片原始尺寸
  - 最大：将所有图片缩放到最大宽度/高度
- **图片重排序**：选择图片之后，可以长按拖动已选图片改变顺序

#### 🎯 叠加拼接
- **图片叠加拼接**：将一张图片的一部分区域与下一张图片叠加显示，可用于制作视频字幕截图
- **被叠加区域占比**：可调节 0-100% 的被叠加区域占比

### 🎨 个性化主题
- **🌙 深色模式**：支持手动切换和跟随系统设置
- **🌈 动态色彩主题**：基于系统壁纸颜色生成的动态主题色（仅适用Android 12+）
  - 注意：需要使用安卓原生壁纸应用，第三方包括部分OEM厂家的壁纸程序都无法被识别
- **🎀 预定义配色方案**：Bocchi、Nijika、Ryo、Kita 等主题
- **🖌️ 自定义配色**：可自定义创建颜色方案

### ⚙️ 设置选项
- **💾 图片输出设置**：支持 PNG、JPEG 和 WEBP 格式，可调节输出质量(PNG默认无损、不可调)
- **⚡ 多线程加速计算**：开启后将使用并行加速流程
- **🧠 提高内存阈值**：默认内存阈值为设备 RAM 的 50%，开启后将提升到 80%

## 📲 使用方法

1. 🚀 打开 Chimera 应用
2. 🖼️ 点击"选择图片"按钮选择要拼接的图片
3. ⚙️ 调整拼接参数（方向、缩放模式、间隔等）
4. 🧱 点击"开始拼接"按钮
5. 👀 在结果页面预览拼接效果，可选择保存到相册或分享给其他应用

## 🏗️ 技术架构

### 🏢 架构模式
- **MVVM架构模式**：采用 Model-View-ViewModel 架构，分离数据逻辑和界面逻辑
- **单Activity多Compose界面**：整个应用基于单一 Activity 和多个 Compose 界面构建

### 💻 技术栈
- **Kotlin & Coroutines**
- **Jetpack Compose**
- **Android Architecture Components**

## ⚠️ 技术限制和注意事项

### 📏 图片格式限制
拼接图片受文件格式的技术规范和设备内存的双重限制：

- JPEG：技术理论最大尺寸为 65,535 × 65,535 像素（16 位限制），App默认输出 JPEG 格式
- WebP：无论是无损还是有损，其最大尺寸通常被限制在 16,384 × 16,384 像素左右
- PNG：技术理论最大尺寸高达 4,294,967,295 × 4,294,967,295 像素（32 位无符号整数限制），理论上是最高的

### 💾 内存限制
> [!IMPORTANT]
> 无论格式如何，实际操作中最大的限制是**设备内存(RAM)和安卓虚拟机机制**限制。

超大图解码所需的内存（宽 × 高 × 4 字节/像素）很容易导致应用崩溃（OOM Crash）。

如果图片较多或图片较大，建议**前往设置调整为 PNG 格式，并开启提高内存阈值**。

理论上支持处理超过100张图像，但需要大量内存，并且由于Android虚拟机的限制，我们建议前往 PC 端自寻应用处理。

### 🐞 已知问题
经测试，PhotoPicker (`PickMultipleVisualMedia`) 不保证返回URI的顺序。当用户选择大量图片时，返回的列表顺序可能是混乱的。这是一个已知的平台问题，但谷歌官方一直没有修复。

相关问题讨论链接: https://issuetracker.google.com/issues/264215151

对于 Embedded Picker和 SAF 选择器，图片顺序是正确的。

## 📦 安装

您可以通过以下方式获取应用：

1. 📥 从 [GitHub Releases](https://github.com/ReRokutosei/Chimera/releases) 下载最新版本的 APK 文件
2. 🔧 克隆源码并自行构建项目

## 🔨 构建项目
<details>
<summary><strong>点击查看具体步骤</strong></summary>

### 🛠️ 环境要求

- **Android Studio Narwhal | 2025.1.3**（或更高版本）
- **JDK 21**（推荐使用 Android Studio 自带的 JetBrains Runtime 21）
- 通过 Android Studio 获取 **Android SDK**

### 📋 构建步骤

#### 1. 📂 浅克隆项目到本地：
```bash
git clone --depth 1 https://github.com/ReRokutosei/Chimera.git
```

#### 2. 💼 在 Android Studio 中打开项目

#### 3. 📁 在根目录创建 `local.properties`并写入你的SDK路径
```
// 例如
sdk.dir=D\:\\yourpath\\AndroidSdk
```

#### 4. 🔐 生成密钥

点击AS顶部的`Build`菜单 

  -> `Generate Signed App Bundle or APK` 

  -> 按照说明生成Key 

  -> 将密钥存储在`./keystore/Chimera.jks`

#### 5. ⚙️ 在用户目录创建 `gradle.properties`
- 在您的本地开发环境中，需要在用户目录下创建 Gradle 配置文件：
- Windows 系统：`C:\Users\{用户名}\.gradle\gradle.properties`
- macOS/Linux 系统：`~/.gradle/gradle.properties`
- 在该文件中添加您的签名配置：
```properties
KEYSTORE_PATH=../keystore/Chimera.jks
KEYSTORE_PASSWORD=yourpassword
KEY_ALIAS=chimera_release
KEY_PASSWORD=yourpassword
```

#### 6. 🔄 点击 AS 的`Gradle Sync`

#### 7. 🏗️ 构建项目：
```bash
./gradlew assembleRelease
```

</details>

## 🔐 隐私政策

本应用不请求任何网络权限，不收集、不存储、不处理、不传输您的任何个人信息，所有操作均在您的设备本地完成。

详情请查看 [隐私声明](PrivacyPolicy_CN.md) 文件。

## ⚠️ 免责声明

应用按"现状"提供，不提供任何形式的担保。我们对因使用本应用而产生的任何后果不承担任何责任。

详情请查看 [免责声明](Disclaimer_CN.md) 文件。

## 📜 许可证

本项目采用 GNU 通用公共许可证第3.0版授权。详情请查看 [LICENSE](LICENSE) 文件。

## 🙏 资源说明
### 🧰 ImageToolbox
本项目使用了[ImageToolbox (Apache License 2.0)](https://github.com/T8RIN/ImageToolbox)的 **Embedded Picker、Fancy Slider、Image Reorder Rarousel** 三个组件，感谢原项目贡献者们的杰出工作！

> 详情请查看目录`./t8rin`

如果你有更多图像处理需求，强烈建议尝试强大的 [**ImageToolbox**](https://github.com/T8RIN/ImageToolbox/releases/latest)

### 🎨 应用图标
应用图标由 [Freepik](https://www.freepik.com/icon/animal_13228011) 设计

### 🖼️ 展示图片
应用设置页背景以及截图展示的图片均来自动漫[《ぼっち・ざ・ろっく！》](https://bocchi.rocks/)，版权归属于「©はまじあき・芳文社／ぼっち・ざ・ろっく！製作委員会」

## 📚 依赖库

<details>
<summary><strong>点击查看</strong></summary>

- [AboutLibraries Core Library](https://github.com/mikepenz/AboutLibraries) 13.2.1 | Under Apache License 2.0
- [Accompanist Drawable Painter library](https://github.com/google/accompanist/) 0.32.0 | Under Apache License 2.0
- [Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.12.4) 1.12.4 | Under Apache License 2.0
- [Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity#1.12.4) 1.12.4 | Under Apache License 2.0
- [Activity Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/activity#1.12.4) 1.12.4 | Under Apache License 2.0
- [Android App Startup Runtime](https://developer.android.com/jetpack/androidx/releases/startup#1.1.1) 1.1.1 | Under Apache License 2.0
- [Android Arch-Common](https://developer.android.com/jetpack/androidx/releases/arch-core#2.2.0) 2.2.0 | Under Apache License 2.0
- [Android Arch-Runtime](https://developer.android.com/jetpack/androidx/releases/arch-core#2.2.0) 2.2.0 | Under Apache License 2.0
- [Android ConstraintLayout](http://tools.android.com) 2.1.0 | Under Apache License 2.0
- [Android ConstraintLayout Core](http://tools.android.com) 1.0.0 | Under Apache License 2.0
- [Android Graphics Path](https://developer.android.com/jetpack/androidx/releases/graphics#1.0.1) 1.0.1 | Under Apache License 2.0
- [Android Resource Inspection - Annotations](https://developer.android.com/jetpack/androidx/releases/resourceinspection#1.0.1) 1.0.1 | Under Apache License 2.0
- [Android Tracing](https://developer.android.com/jetpack/androidx/releases/tracing#1.2.0) 1.2.0 | Under Apache License 2.0
- [AndroidX Autofill](https://developer.android.com/jetpack/androidx) 1.0.0 | Under Apache License 2.0
- [AndroidX Futures](https://developer.android.com/topic/libraries/architecture/index.html) 1.1.0 | Under Apache License 2.0
- [AndroidX Widget ViewPager2](https://developer.android.com/jetpack/androidx) 1.0.0 | Under Apache License 2.0
- [androidx.core:core-viewtree](https://developer.android.com/jetpack/androidx/releases/core#1.0.0) 1.0.0 | Under Apache License 2.0
- [androidx.customview:poolingcontainer](https://developer.android.com/jetpack/androidx/releases/customview#1.0.0) 1.0.0 | Under Apache License 2.0
- [Annotation](https://developer.android.com/jetpack/androidx/releases/annotation#1.9.1) 1.9.1 | Under Apache License 2.0
- [Annotation](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat#1.7.1) 1.7.1 | Under Apache License 2.0
- [AppCompat Resources](https://developer.android.com/jetpack/androidx/releases/appcompat#1.7.1) 1.7.1 | Under Apache License 2.0
- [coil](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [coil-base](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [coil-compose](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [coil-compose-base](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [collections](https://developer.android.com/jetpack/androidx/releases/collection#1.5.0) 1.5.0 | Under Apache License 2.0
- [collections](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Collections Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/collection#1.5.0) 1.5.0 | Under Apache License 2.0
- [colorpicker-compose](https://github.com/skydoves/colorpicker-compose/) 1.1.3 | Under Apache License 2.0
- [Compose Animation](https://developer.android.com/jetpack/androidx/releases/compose-animation#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Animation](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Animation Core](https://developer.android.com/jetpack/androidx/releases/compose-animation#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Animation Core](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Foundation](https://developer.android.com/jetpack/androidx/releases/compose-foundation#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Foundation](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Geometry](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Geometry](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Graphics](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Graphics](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Layouts](https://developer.android.com/jetpack/androidx/releases/compose-foundation#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Layouts](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Material Components](https://developer.android.com/jetpack/androidx/releases/compose-material#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Material Icons Core](https://developer.android.com/jetpack/androidx/releases/compose-material#1.7.8) 1.7.8 | Under Apache License 2.0
- [Compose Material Icons Extended](https://developer.android.com/jetpack/androidx/releases/compose-material#1.7.8) 1.7.8 | Under Apache License 2.0
- [Compose Material Ripple](https://developer.android.com/jetpack/androidx/releases/compose-material#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Material3 Components](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-alpha14) 1.5.0-alpha14 | Under Apache License 2.0
- [Compose Navigation](https://developer.android.com/jetpack/androidx/releases/navigation#2.9.7) 2.9.7 | Under Apache License 2.0
- [Compose Runtime](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Runtime](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Runtime Annotation](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Runtime Retain](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Saveable](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Saveable](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Testing manifest dependency](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Tooling](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Tooling Data](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose UI](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose UI Preview Tooling](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose UI primitives](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose UI Text](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose UI Text](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Unit](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Unit](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Compose Util](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.10.3) 1.10.3 | Under Apache License 2.0
- [Compose Util](https://github.com/JetBrains/compose-jb) 1.9.3 | Under Apache License 2.0
- [Core](https://developer.android.com/jetpack/androidx/releases/core#1.17.0) 1.17.0 | Under Apache License 2.0
- [Core Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/core#1.17.0) 1.17.0 | Under Apache License 2.0
- [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under Apache License 2.0
- [DataStore Core](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under Apache License 2.0
- [DataStore Core Okio](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under Apache License 2.0
- [DynamicAnimation](https://developer.android.com/jetpack/androidx/releases/dynamicanimation#1.1.0) 1.1.0 | Under Apache License 2.0
- [Emoji2](https://developer.android.com/jetpack/androidx/releases/emoji2#1.4.0) 1.4.0 | Under Apache License 2.0
- [Emoji2 Views Helper](https://developer.android.com/jetpack/androidx/releases/emoji2#1.4.0) 1.4.0 | Under Apache License 2.0
- [error-prone annotations](https://errorprone.info/error_prone_annotations) 2.15.0 | Under Apache License 2.0
- [Experimental annotation](https://developer.android.com/jetpack/androidx/releases/annotation#1.4.1) 1.4.1 | Under Apache License 2.0
- [Graphics Shapes](https://developer.android.com/jetpack/androidx/releases/graphics#1.0.1) 1.0.1 | Under Apache License 2.0
- [Guava ListenableFuture only](https://github.com/google/guava/listenablefuture) 1.0 | Under Apache License 2.0
- [JetBrains Java Annotations](https://github.com/JetBrains/java-annotations) 23.0.0 | Under Apache License 2.0
- [Jetpack Compose Libraries BOM](https://developer.android.com/jetpack) 2026.02.00 | Under Apache License 2.0
- [JSpecify annotations](http://jspecify.org/) 1.0.0 | Under Apache License 2.0
- [Kotlin Libraries bill-of-materials](https://kotlinlang.org/) 1.8.22 | Under Apache License 2.0
- [Kotlin Stdlib](https://kotlinlang.org/) 2.3.10 | Under Apache License 2.0
- [Kotlin Stdlib Common](https://kotlinlang.org/) 2.3.10 | Under Apache License 2.0
- [Kotlin Stdlib Jdk7](https://kotlinlang.org/) 1.8.21 | Under Apache License 2.0
- [Kotlin Stdlib Jdk8](https://kotlinlang.org/) 1.8.21 | Under Apache License 2.0
- [kotlinx-collections-immutable](https://github.com/Kotlin/kotlinx.collections.immutable) 0.4.0 | Under Apache License 2.0
- [kotlinx-coroutines-android](https://github.com/Kotlin/kotlinx.coroutines) 1.9.0 | Under Apache License 2.0
- [kotlinx-coroutines-bom](https://github.com/Kotlin/kotlinx.coroutines) 1.9.0 | Under Apache License 2.0
- [kotlinx-coroutines-core](https://github.com/Kotlin/kotlinx.coroutines) 1.9.0 | Under Apache License 2.0
- [kotlinx-serialization-bom](https://github.com/Kotlin/kotlinx.serialization) 1.10.0 | Under Apache License 2.0
- [kotlinx-serialization-core](https://github.com/Kotlin/kotlinx.serialization) 1.10.0 | Under Apache License 2.0
- [kotlinx-serialization-json](https://github.com/Kotlin/kotlinx.serialization) 1.10.0 | Under Apache License 2.0
- [Lifecycle Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle LiveData](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle LiveData Core](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle Process](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle Runtime](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle Runtime](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle Runtime Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle Runtime Compose](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle ViewModel](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle ViewModel](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle ViewModel Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle ViewModel Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle ViewModel with SavedState](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle ViewModel with SavedState](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle-Common](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Lifecycle-Common](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle-Common for Java 8](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [LiveData Core Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0) 2.10.0 | Under Apache License 2.0
- [Material Components for Android](https://github.com/material-components/material-components-android) 1.13.0 | Under Apache License 2.0
- [Navigation Common](https://developer.android.com/jetpack/androidx/releases/navigation#2.9.7) 2.9.7 | Under Apache License 2.0
- [Navigation Event](https://developer.android.com/jetpack/androidx/releases/navigationevent#1.0.2) 1.0.2 | Under Apache License 2.0
- [Navigation Runtime](https://developer.android.com/jetpack/androidx/releases/navigation#2.9.7) 2.9.7 | Under Apache License 2.0
- [NavigationEvent Compose](https://developer.android.com/jetpack/androidx/releases/navigationevent#1.0.2) 1.0.2 | Under Apache License 2.0
- [okhttp](https://square.github.io/okhttp/) 4.12.0 | Under Apache License 2.0
- [okio](https://github.com/square/okio/) 3.9.1 | Under Apache License 2.0
- [Parcelize Runtime](https://kotlinlang.org/) 2.3.10 | Under Apache License 2.0
- [Preferences DataStore](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under Apache License 2.0
- [Preferences DataStore Core](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under Apache License 2.0
- [Preferences DataStore Proto](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under Apache License 2.0
- [Preferences External Protobuf](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.0) 1.2.0 | Under BSD 3-Clause "New" or "Revised" License
- [Profile Installer](https://developer.android.com/jetpack/androidx/releases/profileinstaller#1.4.0) 1.4.0 | Under Apache License 2.0
- [Reorderable](https://github.com/Calvin-LL/Reorderable) 3.0.0 | Under Apache License 2.0
- [Saved State](https://developer.android.com/jetpack/androidx/releases/savedstate#1.4.0) 1.4.0 | Under Apache License 2.0
- [Saved State](https://github.com/JetBrains/compose-jb) 1.3.6 | Under Apache License 2.0
- [Saved State Compose](https://developer.android.com/jetpack/androidx/releases/savedstate#1.4.0) 1.4.0 | Under Apache License 2.0
- [Saved State Compose](https://github.com/JetBrains/compose-jb) 1.3.6 | Under Apache License 2.0
- [SavedState Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/savedstate#1.4.0) 1.4.0 | Under Apache License 2.0
- [SubsamplingScaleImageView](https://github.com/davemorrissey/subsampling-scale-image-view) 3.10.0 | Under Apache License 2.0
- [Support AnimatedVectorDrawable](https://developer.android.com/jetpack/androidx) 1.1.0 | Under Apache License 2.0
- [Support CardView v7](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Support Coordinator Layout](https://developer.android.com/jetpack/androidx) 1.1.0 | Under Apache License 2.0
- [Support Cursor Adapter](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Support Custom View](https://developer.android.com/jetpack/androidx) 1.1.0 | Under Apache License 2.0
- [Support Drawer Layout](https://developer.android.com/jetpack/androidx) 1.1.1 | Under Apache License 2.0
- [Support ExifInterface](https://developer.android.com/jetpack/androidx) 1.1.0 | Under Apache License 2.0
- [Support fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.5.4) 1.5.4 | Under Apache License 2.0
- [Support Interpolators](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Support loader](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Support RecyclerView](https://developer.android.com/jetpack/androidx/releases/recyclerview#1.2.1) 1.2.1 | Under Apache License 2.0
- [Support VectorDrawable](https://developer.android.com/jetpack/androidx) 1.1.0 | Under Apache License 2.0
- [Support View Pager](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Transition](https://developer.android.com/jetpack/androidx/releases/transition#1.5.0) 1.5.0 | Under Apache License 2.0
- [VersionedParcelable](http://developer.android.com/tools/extras/support-library.html) 1.1.1 | Under Apache License 2.0
- [WindowManager](https://developer.android.com/jetpack/androidx/releases/window#1.5.0) 1.5.0 | Under Apache License 2.0
- [WindowManager Core](https://developer.android.com/jetpack/androidx/releases/window#1.5.0) 1.5.0 | Under Apache License 2.0

</details>