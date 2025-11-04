# FancySlider Library

> 本组件基于 [T8RIN](https://github.com/T8RIN) 开发的 [ImageToolbox](https://github.com/T8RIN/ImageToolbox) 项目中的 Fancy 滑块，仅做了部分修改以便能独立于原项目使用。感谢原作者的杰出工作！

## 组件概述

FancySlider Library 是一个现代化的 Android Compose 滑块组件库，从 ImageToolbox 项目中提取并独立出来。该库提供了具有丰富动画效果和视觉特效的滑块组件，包括单点滑块和范围滑块。

## 功能特性

-  **炫酷动画效果** - 滑块具有流畅的动画过渡效果
-  **Material Design 风格** - 遵循 Material Design 3 设计规范
-  **高度可定制** - 支持自定义颜色、形状和样式
-  **双向滑块** - 提供单点滑块和范围滑块两种模式
-  **响应式设计** - 支持触摸、点击和拖拽操作
-  **暗黑模式支持** - 自动适配系统主题
-  **高性能** - 优化的渲染和动画性能

## 项目结构

```
fancy-slider-library/
├── src/main/
│   ├── java/com/t8rin/fancyslider/
│   │   ├── base/                           # 基础滑块组件实现
│   │   │   ├── CustomRangeSlider.kt        # 自定义范围滑块核心实现
│   │   │   ├── CustomRangeSliderState.kt   # 范围滑块状态管理
│   │   │   ├── CustomSlider.kt             # 自定义滑块核心实现
│   │   │   ├── CustomSliderDefaults.kt     # 滑块默认样式和配置
│   │   │   ├── CustomSliderState.kt        # 滑块状态管理
│   │   │   └── CustomSliderUtils.kt        # 滑块工具类
│   │   │
│   │   ├── fancy/                          # Fancy风格滑块组件
│   │   │   ├── FancyRangeSlider.kt         # Fancy风格范围滑块
│   │   │   └── FancySlider.kt              # Fancy风格滑块
│   │   │
│   │   ├── modifier/                       # 自定义修饰符
│   │   │   ├── container.kt                # 容器修饰符
│   │   │   ├── materialShadow.kt           # 材质阴影修饰符
│   │   │   └── trackOverslide.kt           # 轨道过度滑动修饰符
│   │   │
│   │   ├── shapes/                         # 自定义形状
│   │   │   ├── DropletShape.kt
│   │   │   ├── EggShape.kt
│   │   │   ├── MaterialStarShape.kt
│   │   │   ├── MorphShape.kt
│   │   │   ├── OvalShape.kt
│   │   │   ├── PathShape.kt
│   │   │   ├── PillShape.kt
│   │   │   ├── SmallMaterialStarShape.kt
│   │   │   └── SquircleShape.kt
│   │   │
│   │   └── utils/                          # 工具类和辅助功能
│   │       ├── animation/                  # 动画相关工具
│   │       │   └── AnimateFloatingRange.kt # 浮点范围动画
│   │       │
│   │       ├── helper/                     # 辅助类
│   │       │   ├── AnimatedRangeState.kt   # 动画范围状态
│   │       │   ├── FancySliderDefaults.kt  # Fancy滑块默认配置
│   │       │   └── RippleEffectHelper.kt   # 波纹效果助手
│   │       │
│   │       ├── provider/                   # 提供者模式实现
│   │       │   ├── LocalContainer.kt       # 本地容器提供者
│   │       │   └── ProvidesValue.kt        # 值提供者
│   │       │
│   │       └── theme/                      # 主题相关
│   │           ├── Colors.kt               # 颜色定义
│   │           ├── SliderTheme.kt          # 滑块主题
│   │           └── outlineVariant.kt       # 轮廓变体扩展
│   │
│   └── res/values/                         # 资源文件
│       ├── attrs.xml                       # 自定义属性
│       ├── colors.xml                      # 颜色资源
│       └── themes.xml                      # 主题资源
│
├── build.gradle.kts                        # 构建配置文件
└── fancy.md                                # 项目文档
```

## 核心组件

### FancySlider
单点滑块组件，具有丰富的动画效果和视觉特效。支持自定义颜色、形状和交互效果。

主要特性：
- 星形滑块手柄（可自定义）
- 轨道过度滑动效果
- 材质阴影效果
- 容器背景支持
- 流畅的动画过渡

### FancyRangeSlider
范围滑块组件，允许用户选择一个数值范围。具有与 FancySlider 相同的视觉效果和动画特性。

主要特性：
- 双手柄设计
- 范围选择支持
- 与 FancySlider 相同的动画和视觉效果
- 独立的手柄交互

## 使用方法

### 基本用法

#### FancySlider
```kotlin
var sliderValue by remember { mutableStateOf(0.5f) }

FancySlider(
    value = sliderValue,
    onValueChange = { sliderValue = it },
    valueRange = 0f..1f,
    steps = 0
)
```

#### FancyRangeSlider
```kotlin
var rangeValue by remember { mutableStateOf(0.2f..0.8f) }

FancyRangeSlider(
    value = rangeValue,
    onValueChange = { rangeValue = it },
    valueRange = 0f..1f,
    steps = 0
)
```

## 自定义配置

### FancySlider 参数说明

| 参数名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| value | Float | 必需 | 滑块当前值 |
| onValueChange | (Float) -> Unit | 必需 | 值改变时的回调 |
| modifier | Modifier | Modifier | 应用于滑块的修饰符 |
| enabled | Boolean | true | 控制滑块是否启用 |
| valueRange | ClosedFloatingPointRange<Float> | 0f..1f | 滑块值范围 |
| steps | Int | 0 | 离散步数，0表示连续 |
| onValueChangeFinished | (() -> Unit)? | null | 值改变完成时的回调 |
| colors | SliderColors | SliderDefaults.colors() | 滑块颜色配置 |
| thumbShape | Shape | MaterialStarShape | 滑块手柄形状 |
| drawContainer | Boolean | true | 是否绘制滑块容器 |
| drawShadows | Boolean | true | 是否绘制阴影效果 |

### FancyRangeSlider 参数说明

| 参数名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| value | ClosedFloatingPointRange<Float> | 必需 | 滑块当前范围值 |
| onValueChange | (ClosedFloatingPointRange<Float>) -> Unit | 必需 | 范围值改变时的回调 |
| modifier | Modifier | Modifier | 应用于滑块的修饰符 |
| enabled | Boolean | true | 控制滑块是否启用 |
| valueRange | ClosedFloatingPointRange<Float> | 0f..1f | 滑块值范围 |
| steps | Int | 0 | 离散步数，0表示连续 |
| onValueChangeFinished | (() -> Unit)? | null | 值改变完成时的回调 |
| colors | SliderColors | SliderDefaults.colors() | 滑块颜色配置 |
| thumbShape | Shape | MaterialStarShape | 滑块手柄形状 |
| drawContainer | Boolean | true | 是否绘制滑块容器 |
| drawShadows | Boolean | true | 是否绘制阴影效果 |

### 自定义示例

```kotlin
// 自定义颜色的 FancySlider
FancySlider(
    value = sliderValue,
    onValueChange = { sliderValue = it },
    colors = SliderDefaults.colors(
        thumbColor = Color.Red,
        activeTrackColor = Color.Red,
        inactiveTrackColor = Color.LightGray
    ),
    thumbShape = CircleShape, // 使用圆形手柄
    drawShadows = false // 禁用阴影效果
)

// 无容器的 FancyRangeSlider
FancyRangeSlider(
    value = rangeValue,
    onValueChange = { rangeValue = it },
    drawContainer = false, // 不绘制容器
    thumbShape = PillShape // 使用胶囊形状手柄
)
```

## 依赖项

该库依赖以下主要组件：

- AndroidX Compose UI
- AndroidX Compose Foundation
- AndroidX Compose Material3
- AndroidX Core KTX
- Kotlin Coroutines

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