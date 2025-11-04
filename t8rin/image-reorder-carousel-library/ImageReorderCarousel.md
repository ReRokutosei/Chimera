# ImageReorderCarousel Library

> 本组件基于 [T8RIN](https://github.com/T8RIN) 开发的 [ImageToolbox](https://github.com/T8RIN/ImageToolbox) 项目中的图像轮播组件，仅做了部分修改以便能独立于原项目使用。感谢原作者的杰出工作！

## 组件概述

ImageReorderCarousel Library 是一个支持图片拖拽重排序的 Android Compose 组件库，从 ImageToolbox 项目中提取并独立出来。该库提供了直观的用户界面，允许用户通过简单的拖拽操作来重新排列图片顺序，并支持多种排序方式。

## 功能特性

- 图片拖拽重排序：通过长按并拖拽的方式重新排列图片顺序
- 多种排序选项：支持按名称、大小、修改时间等多种方式自动排序
- 图片预览功能：点击图片可以全屏查看
- 可定制性：支持控制添加按钮、排序按钮、删除按钮的显示与隐藏
- 触觉反馈：在拖拽操作时提供触觉反馈增强用户体验
- Material Design 3：采用最新的 Material Design 3 设计规范

## 项目结构

```
└─main
    │  AndroidManifest.xml                  # Android 清单文件
    │
    ├─java
    │  └─com
    │      └─t8rin
    │          └─imagereordercarousel       # 主包路径
    │              │  
    │              │  ImageReorderCarousel.kt      # 核心组件：可重排序图片轮播
    │              │  ImageReorderCarouselDemoActivity.kt  # 示例 Activity
    │              │  SortButton.kt          # 排序按钮组件
    │              │  SortType.kt            # 排序类型枚举
    │              │  
    │              ├─helper                 # 辅助类
    │              │      Picture.kt         # 图片加载封装
    │              │
    │              ├─modifier               # 修饰符扩展
    │              │      Haptics.kt         # 触觉反馈相关
    │              │      ShapeDefaults.kt   # 默认形状定义
    │              │
    │              └─widgets                # UI 组件
    │                      EnhancedButton.kt       # 增强按钮
    │                      EnhancedIconButton.kt   # 增强图标按钮
    │                      ImagePager.kt           # 图片查看器
    │                      EnhancedModalBottomSheet.kt  # 底部弹窗
    │
    └─res
        │                                      
        └─values         # 字符串资源
```

## 核心组件

1. **ImageReorderCarousel** - 主要的可重排序图片轮播组件
   - 支持拖拽重新排序
   - 显示图片索引
   - 提供添加、删除和排序功能

2. **SortButton** - 排序按钮组件
   - 显示排序选项的底部弹窗
   - 支持多种排序类型选择

3. **SortType** - 排序类型枚举
   - 包含日期、名称、大小、MIME类型等多种排序方式

4. **ImagePager** - 图片查看器组件
   - 全屏查看图片
   - 支持左右滑动切换图片
   - 提供分享功能

5. **辅助组件**
   - Picture: 图片加载组件
   - EnhancedButton/EnhancedIconButton: 增强版按钮组件
   - 各种工具类和扩展函数

## 使用方法

```kotlin
var images by remember {
    mutableStateOf(listOf<Uri>())
}

ImageReorderCarousel(
    images = images,
    onReorder = { reorderedImages ->
        images = reorderedImages
    },
    onNeedToAddImage = {
        // 处理添加图片逻辑，例如打开文件选择器
    },
    onNeedToRemoveImageAt = { index ->
        images = images.toMutableList().apply { removeAt(index) }
    }
)
```

参数说明：
- `images`: 要显示的图片 URI 列表
- `onReorder`: 当图片顺序发生变化时的回调函数
- `onNeedToAddImage`: 当用户点击添加按钮时的回调函数
- `onNeedToRemoveImageAt`: 当用户请求删除某个位置的图片时的回调函数
- `showAddButton`: 是否显示添加按钮（默认为 true）
- `showSortButton`: 是否显示排序按钮（默认为 true）
- `showRemoveButtons`: 是否显示删除按钮（默认为 true）
- `enableImagePreview`: 是否启用图片预览功能（默认为 true）

## 自定义配置

ImageReorderCarousel 组件提供了多个参数来自定义其行为和外观：

1. 控制按钮显示：可以通过设置 showAddButton、showSortButton 和 showRemoveButtons 参数来控制相应按钮的显示与隐藏。

2. 预览功能开关：通过 enableImagePreview 参数控制是否启用图片预览功能。

3. 样式自定义：可通过 modifier 参数应用自定义修饰符来改变组件外观。

4. 导航支持：通过 onNavigate 参数可以集成导航功能。

## 依赖项

该库依赖以下主要组件：

- Android Jetpack Compose
- Coil 图片加载库
- CalvinN/reorderable 拖拽排序库
- Material Design 3 组件
- AndroidX 核心库

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
