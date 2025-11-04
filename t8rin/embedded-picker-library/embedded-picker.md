# Embedded Picker Library 嵌入式媒体选择器库

> 本组件基于 [T8RIN](https://github.com/T8RIN) 开发的 [ImageToolbox](https://github.com/T8RIN/ImageToolbox) 项目中的媒体选择器，仅做了部分修改以便能独立于原项目使用。感谢原作者的杰出工作！

## 组件概述

Embedded Picker Library 是一个基于 Jetpack Compose 的 Android 媒体选择器库，从 ImageToolbox 项目中提取并独立出来。该库提供了一个现代化、美观且功能丰富的界面，用于从设备中选择图片。

## 功能特性

1. **现代化 UI**：使用 Jetpack Compose 构建，界面美观流畅
2. **权限管理**：自动处理存储权限请求
3. **相册分类**：支持按相册浏览图片
4. **多选支持**：可配置单选或多选模式
5. **搜索功能**：支持在媒体文件中搜索
6. **缓存机制**：内置缓存管理，提高加载性能

## 项目结构

```
src/main
├── java/com/t8rin/embeddedpicker
│   ├── data                    # 数据层
│   │   ├── AndroidMediaRetriever.kt    # 媒体数据检索器
│   │   └── CacheManager.kt             # 缓存管理器
│   ├── domain                  # 领域层
│   │   ├── model               # 数据模型
│   │   │   ├── Album.kt                # 相册模型
│   │   │   ├── AlbumsState.kt          # 相册状态模型
│   │   │   ├── AllowedMedia.kt         # 媒体类型模型
│   │   │   ├── Media.kt                # 媒体文件模型
│   │   │   └── MediaState.kt           # 媒体状态模型
│   │   └── MediaRetriever.kt           # 媒体检索接口
│   ├── icons                   # 自定义图标
│   ├── presentation            # 表现层
│   │   ├── components          # UI 组件
│   │   ├── MediaPickerActivity.kt      # 主要的 Activity
│   │   ├── MediaPickerViewModel.kt     # ViewModel
│   │   └── SendMediaAsResult.kt        # 结果返回工具
│   └── utils                   # 工具类
└── res                         # 资源文件
    └── values                  # 字符串等资源
```

## 核心组件

### 1. MediaPickerActivity
这是库的主要入口点，一个继承自 ComponentActivity 的 Compose Activity。它负责初始化 ViewModel 并显示媒体选择器界面。

### 2. MediaPickerViewModel
ViewModel 负责管理 UI 状态，包括：
- 相册列表状态 AlbumsState
- 媒体文件状态 MediaState
- 媒体文件选择逻辑

### 3. 数据模型

#### Media（媒体文件）
表示单个媒体文件，包含以下属性：
- id: 媒体文件ID
- displayName: 显示名称
- size: 文件大小
- mimeType: MIME类型
- dateAdded: 添加日期
- dateModified: 修改日期
- uri: 文件URI
- bucketId: 所属相册ID

#### Album（相册）
表示设备上的相册，包含以下属性：
- id: 相册ID
- label: 相册名称
- uri: 相册缩略图URI
- pathToThumbnail: 缩略图路径
- relativePath: 相对路径
- timestamp: 时间戳
- count: 包含的媒体文件数量

### 4. 数据检索
AndroidMediaRetriever 类负责从设备媒体存储中检索媒体文件和相册信息，并提供了缓存机制来提高性能。

### 5. 权限管理
库会自动处理必要的存储权限：
- Android 13+：`READ_MEDIA_IMAGES`
- Android 12及以下：`READ_EXTERNAL_STORAGE`

## 使用方法

1. 在 AndroidManifest.xml 中添加必要的权限：
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

1. 在需要使用媒体选择器的地方启动 MediaPickerActivity：
```kotlin
val intent = Intent(context, MediaPickerActivity::class.java)
intent.putExtra("allowMultiple", true) // 是否允许多选
startActivityForResult(intent, REQUEST_CODE)
```

1. 在 `onActivityResult` 中处理返回的结果。

## 自定义配置

可以通过修改 AllowedMedia.kt 来控制允许选择的媒体类型：
- Photos: 仅图片
- Videos: 仅视频
- Both: 图片和视频

目前库中默认只支持图片选择。

## 依赖项

该库依赖以下主要组件：
- Jetpack Compose (Material3)
- Kotlin Coroutines
- Coil 图片加载库
- AndroidX Lifecycle
- RecyclerView (用于 DiffUtil)

## 许可证

本项目基于 Apache License 2.0 开源协议：

```
Copyright 2025 T8RIN (Malik Mukhametzyanov)
Modified work Copyright 2025 ReRokutosei

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
