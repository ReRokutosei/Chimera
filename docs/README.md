<p align="center">
  <img src="../assets/cover.webp" width="960" alt="Chimera application preview" />
</p>

<h1 align="center">Chimera</h1>

<p align="center">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-CD15ED?logo=kotlin&logoColor=white&style=for-the-badge"/>
  <img alt="Release" src="https://img.shields.io/github/v/release/ReRokutosei/Chimera?color=FF6598&include_prereleases&logo=github&style=for-the-badge&labelColor=FF6598"/>
  <br>
  <img alt="GPLv3" src="https://img.shields.io/badge/GPL%20v3-008033?style=for-the-badge&logo=gnu&logoColor=white"/>
  <img alt="API" src="https://img.shields.io/badge/ANdroid%2010+-34A853?logo=android&logoColor=white&style=for-the-badge"/>
  <br>
  <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label="/>
  <img alt="material" src="https://custom-icon-badges.demolab.com/badge/material%20you-6442D6?style=for-the-badge&logoColor=white&logo=material-you"/>
  <br>
  <a href="https://deepwiki.com/ReRokutosei/Chimera"><img alt="DeepWiki" src="https://img.shields.io/badge/Ask%20DeepWiki-0D7FC0?style=for-the-badge&logoColor=white"/></a>
</p>

<p align="center"><strong>English | <a href="README_CN.md">简体中文</a></strong></p>

## Overview

Chimera is an Android application for stitching and cutting images. It is written in Kotlin with Jetpack Compose and supports Android 10 and later.

Image decoding, stitching, cutting, and saving are performed on the device. The application does not declare the Android `INTERNET` permission and does not transmit images to a developer-operated service.

> [!TIP]
> If you need the desktop version, please go to the [ChimeraWeb](https://github.com/ReRokutosei/ChimeraWeb).

## Features

### Image access

![Image selection interface](../assets/picker.webp)

- **System Photo Picker**: Uses the platform picker when available and grants access only to selected media, without broad storage permission.
- **Storage Access Framework (SAF)**: Selects image files through the system document provider.
- **Embedded Picker**: Provides album browsing and search through components derived from ImageToolbox. It requires the applicable media permission and may need time to index media on first use. On Android 14 and later, granting selected-photo access limits the embedded library to those photos.

### Stitching and cutting

- Direct vertical or horizontal stitching, with configurable spacing and fill color.
- Original, minimum-alignment, and maximum-alignment scaling modes.
- Overlay mode that keeps a configurable trailing section from each subsequent image; this is intended for workflows such as combining subtitle screenshots.
- Drag-and-drop ordering of selected images.
- 2 x 2 and 3 x 3 grid cutting, including batch saving.

### Display and output

- Light, dark, and automatic themes.
- Material You dynamic color on supported Android versions; results depend on the wallpaper information exposed by the operating system.
- JPEG, PNG, and WebP output. The quality setting applies to JPEG and WebP.
- Optional multithreaded decoding and scaling. The result depends on image dimensions, device cores, and available memory.

## Performance

The following results compare sequential and bounded parallel scaling on the same Pixel 9 Pro AVD, Android 15, x86_64, with ART `speed` compilation. They are medians from deterministic synthetic datasets and are intended for before-and-after comparison, not as performance guarantees for physical devices.

| Scenario | Sequential | Multithreaded | Change |
| --- | ---: | ---: | ---: |
| Direct horizontal, 10 medium images, MIN | 78.47 ms | 46.28 ms | -41.0% |
| Direct vertical, 50 small images, MAX | 83.53 ms | 51.45 ms | -38.4% |
| Overlay vertical, 10 medium images, MIN | 54.99 ms | 32.47 ms | -41.0% |

Physical-device app-flow measurements were also collected on a Xiaomi Mi 10 Ultra running Android 13, with eight iterations per case. The mandatory first-launch agreement countdown was completed before measurement and is not included in startup timing.

| Scenario | No compilation | Current Baseline Profile | Median change |
| --- | ---: | ---: | ---: |
| Cold startup, time to initial display | 433.7 ms | 423.6 ms | -2.3% |
| Settings flow | 6,296.4 ms | 6,177.7 ms | -1.9% |

The current Baseline Profile provides a small measurable benefit on this device. A larger regenerated profile was rejected because it increased the rule count without improving the Settings flow and regressed cold startup.

See [Image Processing Performance Baseline](Performance_Baseline.md) for datasets, stage timings, codec measurements, commands, and limitations.

## Technical limits

### Output dimensions

- JPEG output is validated against a maximum dimension of 65,535 pixels.
- WebP output is validated against a maximum dimension of 16,383 pixels.
- PNG is not subject to those encoder-specific limits, but Android Bitmap dimensions and available device memory still apply.

Changing the output format does not reduce the memory required by decoded source images or the stitching canvas.

### Memory

Bitmap processing commonly requires approximately 2-4 bytes per pixel, plus source images, scaled intermediates, previews, and the output bitmap. Peak memory therefore cannot be estimated from the final file size alone.

If processing fails because of memory pressure, first reduce the number or dimensions of the input images and close other memory-intensive applications. The increased memory threshold setting only relaxes Chimera's internal preflight limit; it does not add RAM and can increase the risk of process termination or an out-of-memory failure.

### Photo Picker ordering

The Android Photo Picker may return selected URIs in an order different from the visual selection order. This remains tracked as [a known bug](https://issuetracker.google.com/issues/264215151). Use the embedded picker, SAF, or reorder the images manually.

## Build and verification

Requirements:

- JDK 21
- Android SDK Platform 37
- The repository's Gradle Wrapper

```bash
git clone --depth 1 https://github.com/ReRokutosei/Chimera.git
cd Chimera
./gradlew build -x detekt
```

Release builds use the debug signing key when no release keystore is configured. For distributable builds, provide `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` through environment variables or user-level Gradle properties, then run `./gradlew assembleRelease`.

## Privacy, warranty, and license

- [Privacy Policy](PrivacyPolicy_EN.md)
- [Disclaimer](Disclaimer_EN.md)
- [GNU General Public License v3.0](../LICENSE)

## Credits

- Components derived from [ImageToolbox](https://github.com/T8RIN/ImageToolbox): Embedded Picker, Fancy Slider, and Image Reorder Carousel.
- Application icon designed by [Freepik](https://www.freepik.com/icon/animal_13228011).
- Some existing project screenshots and background assets reference *Bocchi the Rock!*. Those works remain the property of their respective rights holders. Attribution does not imply endorsement of this project.

## Dependencies

<details>
<summary><strong>Click Here to View</strong></summary>

- [AboutLibraries Core Library](https://github.com/mikepenz/AboutLibraries) 15.0.4 | Under Apache License 2.0
- [Accompanist Drawable Painter library](https://github.com/google/accompanist/) 0.32.0 | Under Apache License 2.0
- [Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.13.0) 1.13.0 | Under Apache License 2.0
- [Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity#1.13.0) 1.13.0 | Under Apache License 2.0
- [Activity Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/activity#1.13.0) 1.13.0 | Under Apache License 2.0
- [Android App Startup Runtime](https://developer.android.com/jetpack/androidx/releases/startup#1.1.1) 1.1.1 | Under Apache License 2.0
- [Android Arch-Common](https://developer.android.com/jetpack/androidx/releases/arch-core#2.2.0) 2.2.0 | Under Apache License 2.0
- [Android Arch-Runtime](https://developer.android.com/jetpack/androidx/releases/arch-core#2.2.0) 2.2.0 | Under Apache License 2.0
- [Android Graphics Path](https://developer.android.com/jetpack/androidx/releases/graphics#1.0.1) 1.0.1 | Under Apache License 2.0
- [Android Resource Inspection - Annotations](https://developer.android.com/jetpack/androidx/releases/resourceinspection#1.0.1) 1.0.1 | Under Apache License 2.0
- [AndroidX Autofill](https://developer.android.com/jetpack/androidx) 1.0.0 | Under Apache License 2.0
- [AndroidX Futures](https://developer.android.com/topic/libraries/architecture/index.html) 1.1.0 | Under Apache License 2.0
- [AndroidX Test Library](https://developer.android.com/testing) 1.0.1 | Under Apache License 2.0
- [AndroidX Widget ViewPager2](https://developer.android.com/jetpack/androidx) 1.0.0 | Under Apache License 2.0
- [androidx.core:core-viewtree](https://developer.android.com/jetpack/androidx/releases/core#1.0.0) 1.0.0 | Under Apache License 2.0
- [androidx.customview:poolingcontainer](https://developer.android.com/jetpack/androidx/releases/customview#1.0.0) 1.0.0 | Under Apache License 2.0
- [Annotation](https://developer.android.com/jetpack/androidx/releases/annotation#1.10.0) 1.10.0 | Under Apache License 2.0
- [AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat#1.7.1) 1.7.1 | Under Apache License 2.0
- [AppCompat Resources](https://developer.android.com/jetpack/androidx/releases/appcompat#1.7.1) 1.7.1 | Under Apache License 2.0
- [Benchmark - Common](https://developer.android.com/jetpack/androidx/releases/benchmark#1.5.0-alpha07) 1.5.0-alpha07 | Under Apache License 2.0
- [Benchmark TraceProcessor](https://developer.android.com/jetpack/androidx/releases/benchmark#1.5.0-alpha07) 1.5.0-alpha07 | Under Apache License 2.0
- [coil](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [coil-base](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [coil-compose](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [coil-compose-base](https://github.com/coil-kt/coil) 2.7.0 | Under Apache License 2.0
- [collections](https://developer.android.com/jetpack/androidx/releases/collection#1.5.0) 1.5.0 | Under Apache License 2.0
- [Collections Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/collection#1.5.0) 1.5.0 | Under Apache License 2.0
- [colorpicker-compose](https://github.com/skydoves/colorpicker-compose/) 1.2.0 | Under Apache License 2.0
- [Compose Animation](https://developer.android.com/jetpack/androidx/releases/compose-animation#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Animation](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Animation Core](https://developer.android.com/jetpack/androidx/releases/compose-animation#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Animation Core](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Foundation](https://developer.android.com/jetpack/androidx/releases/compose-foundation#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Foundation](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Geometry](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Geometry](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Graphics](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Graphics](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Layouts](https://developer.android.com/jetpack/androidx/releases/compose-foundation#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Layouts](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Material Icons Core](https://developer.android.com/jetpack/androidx/releases/compose-material#1.7.8) 1.7.8 | Under Apache License 2.0
- [Compose Material Icons Extended](https://developer.android.com/jetpack/androidx/releases/compose-material#1.7.8) 1.7.8 | Under Apache License 2.0
- [Compose Material Ripple](https://developer.android.com/jetpack/androidx/releases/compose-material#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Material3 Components](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-alpha24) 1.5.0-alpha24 | Under Apache License 2.0
- [Compose Material3 Ripple](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-alpha24) 1.5.0-alpha24 | Under Apache License 2.0
- [Compose Navigation](https://developer.android.com/jetpack/androidx/releases/navigation#2.9.8) 2.9.8 | Under Apache License 2.0
- [Compose Runtime](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Runtime](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Runtime Annotation](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Runtime Retain](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Saveable](https://developer.android.com/jetpack/androidx/releases/compose-runtime#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Saveable](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Testing manifest dependency](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Tooling](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Tooling Data](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose UI](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose UI](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose UI Preview Tooling](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose UI Text](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose UI Text](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Unit](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Unit](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [Compose Util](https://developer.android.com/jetpack/androidx/releases/compose-ui#1.12.0-beta01) 1.12.0-beta01 | Under Apache License 2.0
- [Compose Util](https://github.com/JetBrains/compose-multiplatform) 1.11.1 | Under Apache License 2.0
- [ConstraintLayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout#2.2.1) 2.2.1 | Under Apache License 2.0
- [ConstraintLayout Core](https://developer.android.com/jetpack/androidx/releases/constraintlayout#1.1.1) 1.1.1 | Under Apache License 2.0
- [Core](https://developer.android.com/jetpack/androidx/releases/core#1.19.0) 1.19.0 | Under Apache License 2.0
- [Core Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/core#1.19.0) 1.19.0 | Under Apache License 2.0
- [Custom View](https://developer.android.com/jetpack/androidx/releases/customview#1.2.0) 1.2.0 | Under Apache License 2.0
- [DataStore](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under Apache License 2.0
- [DataStore Core](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under Apache License 2.0
- [DataStore Core Okio](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under Apache License 2.0
- [DynamicAnimation](https://developer.android.com/jetpack/androidx/releases/dynamicanimation#1.1.0) 1.1.0 | Under Apache License 2.0
- [Emoji2](https://developer.android.com/jetpack/androidx/releases/emoji2#1.4.0) 1.4.0 | Under Apache License 2.0
- [Emoji2 Views Helper](https://developer.android.com/jetpack/androidx/releases/emoji2#1.4.0) 1.4.0 | Under Apache License 2.0
- [error-prone annotations](https://errorprone.info/error_prone_annotations) 2.15.0 | Under Apache License 2.0
- [ExifInterface](https://developer.android.com/jetpack/androidx/releases/exifinterface#1.4.2) 1.4.2 | Under Apache License 2.0
- [Experimental annotation](https://developer.android.com/jetpack/androidx/releases/annotation#1.4.1) 1.4.1 | Under Apache License 2.0
- [Graphics Shapes](https://developer.android.com/jetpack/androidx/releases/graphics#1.0.1) 1.0.1 | Under Apache License 2.0
- [Guava ListenableFuture only](https://github.com/google/guava/listenablefuture) 1.0 | Under Apache License 2.0
- [JetBrains Java Annotations](https://github.com/JetBrains/java-annotations) 23.0.0 | Under Apache License 2.0
- [Jetpack Compose Libraries BOM](https://developer.android.com/jetpack) 2026.06.01 | Under Apache License 2.0
- [JSpecify annotations](http://jspecify.org/) 1.0.0 | Under Apache License 2.0
- [Kotlin Libraries bill-of-materials](https://kotlinlang.org/) 1.8.22 | Under Apache License 2.0
- [Kotlin Stdlib](https://kotlinlang.org/) 2.4.10 | Under Apache License 2.0
- [Kotlin Stdlib Common](https://kotlinlang.org/) 2.4.10 | Under Apache License 2.0
- [Kotlin Stdlib Jdk7](https://kotlinlang.org/) 1.8.21 | Under Apache License 2.0
- [Kotlin Stdlib Jdk8](https://kotlinlang.org/) 1.8.21 | Under Apache License 2.0
- [kotlinx-coroutines-android](https://github.com/Kotlin/kotlinx.coroutines) 1.9.0 | Under Apache License 2.0
- [kotlinx-coroutines-bom](https://github.com/Kotlin/kotlinx.coroutines) 1.9.0 | Under Apache License 2.0
- [kotlinx-coroutines-core](https://github.com/Kotlin/kotlinx.coroutines) 1.9.0 | Under Apache License 2.0
- [kotlinx-serialization-bom](https://github.com/Kotlin/kotlinx.serialization) 1.11.0 | Under Apache License 2.0
- [kotlinx-serialization-core](https://github.com/Kotlin/kotlinx.serialization) 1.11.0 | Under Apache License 2.0
- [kotlinx-serialization-json](https://github.com/Kotlin/kotlinx.serialization) 1.11.0 | Under Apache License 2.0
- [Lifecycle Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle LiveData](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle LiveData Core](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle Process](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle Runtime](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle Runtime](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle Runtime Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle Runtime Compose](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle ViewModel](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle ViewModel](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle ViewModel Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle ViewModel Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle ViewModel with SavedState](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle ViewModel with SavedState](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle-Common](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Lifecycle-Common](https://github.com/JetBrains/compose-jb) 2.9.6 | Under Apache License 2.0
- [Lifecycle-Common for Java 8](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [LiveData Core Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.11.0) 2.11.0 | Under Apache License 2.0
- [Material Components for Android](https://github.com/material-components/material-components-android) 1.14.0 | Under Apache License 2.0
- [Moshi](https://github.com/square/moshi/) 1.13.0 | Under Apache License 2.0
- [Navigation Common](https://developer.android.com/jetpack/androidx/releases/navigation#2.9.8) 2.9.8 | Under Apache License 2.0
- [Navigation Event](https://developer.android.com/jetpack/androidx/releases/navigationevent#1.0.0) 1.0.0 | Under Apache License 2.0
- [Navigation Runtime](https://developer.android.com/jetpack/androidx/releases/navigation#2.9.8) 2.9.8 | Under Apache License 2.0
- [NavigationEvent Compose](https://developer.android.com/jetpack/androidx/releases/navigationevent#1.0.0) 1.0.0 | Under Apache License 2.0
- [okhttp](https://square.github.io/okhttp/) 4.12.0 | Under Apache License 2.0
- [okio](https://github.com/square/okio/) 3.17.0 | Under Apache License 2.0
- [Parcelize Runtime](https://kotlinlang.org/) 2.4.10 | Under Apache License 2.0
- [Preferences DataStore](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under Apache License 2.0
- [Preferences DataStore Core](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under Apache License 2.0
- [Preferences DataStore Proto](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under Apache License 2.0
- [Preferences External Protobuf](https://developer.android.com/jetpack/androidx/releases/datastore#1.2.1) 1.2.1 | Under BSD 3-Clause "New" or "Revised" License
- [Profile Installer](https://developer.android.com/jetpack/androidx/releases/profileinstaller#1.4.1) 1.4.1 | Under Apache License 2.0
- [Reorderable](https://github.com/Calvin-LL/Reorderable) 3.1.0 | Under Apache License 2.0
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
- [Support Drawer Layout](https://developer.android.com/jetpack/androidx) 1.1.1 | Under Apache License 2.0
- [Support fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.5.4) 1.5.4 | Under Apache License 2.0
- [Support Interpolators](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Support loader](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Support RecyclerView](https://developer.android.com/jetpack/androidx/releases/recyclerview#1.2.1) 1.2.1 | Under Apache License 2.0
- [Support VectorDrawable](https://developer.android.com/jetpack/androidx) 1.1.0 | Under Apache License 2.0
- [Support View Pager](http://developer.android.com/tools/extras/support-library.html) 1.0.0 | Under Apache License 2.0
- [Tracing](https://developer.android.com/jetpack/androidx/releases/tracing#1.3.0) 1.3.0 | Under Apache License 2.0
- [Tracing Kotlin Extensions](https://developer.android.com/jetpack/androidx/releases/tracing#1.3.0) 1.3.0 | Under Apache License 2.0
- [Tracing Perfetto Handshake](https://developer.android.com/jetpack/androidx/releases/tracing#1.0.0) 1.0.0 | Under Apache License 2.0
- [Transition](https://developer.android.com/jetpack/androidx/releases/transition#1.5.0) 1.5.0 | Under Apache License 2.0
- [VersionedParcelable](http://developer.android.com/tools/extras/support-library.html) 1.1.1 | Under Apache License 2.0
- [WindowManager](https://developer.android.com/jetpack/androidx/releases/window#1.5.0) 1.5.0 | Under Apache License 2.0
- [WindowManager Core](https://developer.android.com/jetpack/androidx/releases/window#1.5.0) 1.5.0 | Under Apache License 2.0
- [wire-runtime](https://github.com/square/wire/) 6.4.0 | Under Apache License 2.0

</details>
