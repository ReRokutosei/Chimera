# Chimera — AGENTS.md

## Build & Verify

```powershell
# Full build (skip detekt static analysis)
./gradlew build -x detekt

# Lint-only failures (AGP 9.3.0 treats LocalContextGetResourceValueCall as error)
# Fix: extract context.getString(...) to composable-scoped val with stringResource(...)

# JVM unit tests (testBuildType is benchmark)
./gradlew :app:testBenchmarkUnitTest

# Compile the Android processing benchmarks
./gradlew :app:compileBenchmarkAndroidTestKotlin

# Run Android image correctness tests on a connected device
./gradlew :integrationtest:connectedDebugAndroidTest
```

- `compileSdk` = 37, `targetSdk` = 36, `minSdk` = 29, `applicationId` = `com.rerokutosei.chimera`
- Java/Kotlin toolchain = 21 (`sourceCompatibility`/`targetCompatibility` = `VERSION_21`)
- `versionName` comes from the `appVerName` Gradle property (`-PappVerName=...`); `versionCode` is derived from it (`computeVersionCode`). Dev build type appends `.dev` / `-dev` suffix.
- AGP = 9.3.0, Gradle Wrapper = 9.6.1, Kotlin = 2.4.10
- Release builds fall back to debug signing when no release keystore is available; distributable builds should provide `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`.
- JVM tests use JUnit 4 under `app/src/test`; fixed synthetic Bitmap benchmarks use AndroidX Benchmark under `app/src/androidTest` with the non-debug `benchmark` build type.
- Kotlin serialization plugin (`kotlin("plugin.serialization")`) already applied; Navigation `2.9.8` supports `@Serializable` route types via `toRoute<>()`
- HorizontalPager from `androidx.compose.foundation.pager` available (no extra dependency needed)

## Architecture

- **Single Activity** (`MainActivity`) → Jetpack Compose, no Fragments
- **No Services, no BroadcastReceivers** (except per-user request)
- Gradle modules: `:app`, `:baselineprofile` (Macrobenchmark and baseline profile generator), `:integrationtest` (device-side correctness tests), and three library modules under `t8rin/` (fancy-slider, embedded-picker, image-reorder-carousel)
- Navigation: type-safe via `@Serializable sealed class Route` in `ui/navigation/Route.kt` — routes: `Main`, `Settings`, `ImageViewer(...)`
- State: `ViewModel` + `MutableStateFlow` + DataStore Preferences; no Room, no network
- Four ViewModels: `MainViewModel`, `SettingsViewModel`, `StitchViewModel` (stitch orchestration), `ImageViewerViewModel` (viewer and cut-save orchestration)
- App theme: `AppTheme` with `shouldUseDarkTheme()` composable in `ui/theme/Theme.kt`
- Compose `Flow`/`StateFlow` values are collected with `collectAsStateWithLifecycle()`; `lifecycle-runtime-compose` is an explicit dependency

## Stitch engine (single Kotlin engine)

- `ImageStitcher` (`utils/stitch/`) is the entry point — `StitchViewModel` calls its `stitchImages()` / `stitchOverlay()`.
- Layering: `ImageStitcher` → `KotlinStitchingEngine` → a `StitchingStrategy` selected by a private engine helper (`DirectStitchingStrategy` / `OverlayStitchingStrategy`, both extend `BaseStitchingStrategy`).
- `StitchResult` is a sealed class: `BitmapResult(bitmap)` / `ErrorResult(failure: StitchFailure)`.
- Domain failures are separated into `StitchFailure`, `CutFailure`, and `SaveFailure` under `domain/error/`; UI code maps them to localized strings.
- `StitchingOptions` (defined in `strategy/StitchingStrategy.kt`) carries spacing, spacing color, overlay settings, width scale, orientation, output format, high-memory-limit flag, and multithreading flag.
- `ImageStitcher` owns one `BitmapLoader` and reuses a `KotlinStitchingEngine`; the loader is injected into the engine so bitmap tracking and recycling stay coherent.
- `StitchViewModel` is the sole owner of the stitched result Bitmap; viewer UI borrows it and must not recycle it.
- The abandoned C++/Kotlin dual-engine abstractions are fully removed: there is no `StitchingEngine` interface or `StitcherFactory`.
- Settings needed by strategies are read through suspend flows in `ImageStitcher` and passed in `StitchingOptions`; strategies and `MemoryLimitCalculator` must not use `runBlocking` for DataStore access.

## Package Layout

```
data/local/       — DataStore-based managers: StitchSettingsManager, ImageSettingsManager,
                    UserPreferencesManager, LogSettingsManager, PreloadManager
data/model/       — enums/data classes: ColorScheme, PredefinedColorSchemes, ThemeMode,
                    ImageInfo, ImageListDirectionMode
data/repository/  — ThemeRepository, ImageRepository
domain/error/      — StitchFailure, CutFailure, SaveFailure
domain/usecase/    — SaveCutImagesUseCase
ui/main/          — MainScreen, MainViewModel, ParameterSettingsCard, BottomActionButtons,
                    TopAppBar, CustomSegmentedButton, ImagePickerButton, EmbeddedPickerDialog,
                    EstimatedResolutionCard, WelcomeDialog, ErrorDialog
ui/settings/      — SettingsScreen, SettingsViewModel, ImageOutputSettings, DisplaySettings,
                    PerformanceSettings, FilePickerSettings, OtherSettings, About,
                    OpenSourceLicenses, PrivacyPolicy, ComButton
ui/viewer/        — ImageViewerScreen, ImageViewerViewModel, ImageResultPreviewer,
                    AdaptiveDisplay, PreviewSource
ui/theme/         — Theme (AppTheme, shouldUseDarkTheme), Type, CustomColorPickerDialog,
                    SpacingColorPickerDialog, ColorSchemePreview
ui/navigation/    — NavGraph, Route
ui/stitch/        — StitchViewModel (drives ImageStitcher)
utils/stitch/         — ImageStitcher, StitchResult (+ StitchOrientation enum)
utils/stitch/engine/  — KotlinStitchingEngine
utils/stitch/layout/  — Pure Kotlin layout calculation and output-format validation
utils/stitch/strategy/— StitchingStrategy + StitchingOptions, BaseStitchingStrategy,
                        DirectStitchingStrategy, OverlayStitchingStrategy
utils/image/      — BitmapLoader, ImageSaver, ImageSharer, ImageSplitter, EstimateResolution,
                    ResolutionMemoryRisk
utils/performance/— ProcessingPerformance stage timing and Trace sections
utils/color/      — ColorUtils
utils/common/     — LogManager, MemoryLimitCalculator, ToastUtil, LinkTextUtil
```

## Conventions

- String resources must be added to both `values/strings.xml` (English) and `values-zh-rCN/strings.xml` (Simplified Chinese)
- English and Simplified Chinese are the only maintained app languages; do not add Traditional Chinese, Japanese, Spanish, or other locale resource directories
- `CustomSegmentedButtonRow` in `ui/main/CustomSegmentedButton.kt` is reused for mode/grid selection
- ParameterSettingsCard is hidden in cut mode; its state auto-preserves when switching back to stitch mode
- `PreviewSource.FromBitmapWithGrid(bitmap, cols, rows)` for cut grid preview
- Cut preview keeps only the current page and adjacent pages cached; recycle evicted bitmaps through `BitmapLoader`
- Cut-save decoding belongs on `Dispatchers.IO`; bitmap splitting belongs on `Dispatchers.Default`
- Cut saving is driven by `ImageViewerViewModel` through `SaveCutImagesUseCase`; process and recycle one generated piece at a time.
- `ImageSaver.saveToGallery()` returns `ImageSaveResult`; do not reintroduce callback-based save APIs.
- Embedded Picker permission decisions must use `MediaAccessPermissions`: Android 12L and below use `READ_EXTERNAL_STORAGE`, Android 13 uses `READ_MEDIA_IMAGES`, and Android 14+ accepts either full access or `READ_MEDIA_VISUAL_USER_SELECTED` partial access.
- Embedded Picker remains available on every supported Android version; do not hide or disable it solely because the system Photo Picker is available.
- Performance changes must be supported by the fixed benchmark datasets and documented in `docs/Performance_Baseline.md`.
- Estimated result memory uses a conservative ARGB_8888 calculation; `ResolutionMemoryRisk` marks results at or above 512 MiB as `Risky`.
- `Risky` changes the estimated-resolution capsule to the theme error color and adds an accessibility state description, but must not disable stitching; only `Invalid` blocks the start action.
- Deferred crash recovery, logging, memory-budget, process-death recovery, and adaptive-layout ideas are tracked in `docs/todo.md`.
- Spacing fill color stored as hex string (e.g. `"#FF000000"`) in DataStore
- StitchSettingsManager uses generic `getPref<T>()` / `setPref<T>()` helpers for DataStore access
- Use `LogManager.debug(tag) { ... }` for interpolated messages in loops or other hot paths

## Commit Convention

- All commits must follow Conventional Commits: `<type>(<optional-scope>): <description>`.
- Allowed types are `feat`, `fix`, `perf`, `refactor`, `docs`, `test`, `build`, `ci`, `chore`, `style`, and `revert`.
- Use a scope whenever it clarifies classification, especially `deps`, `release`, `lint`, `build`, `ci`, `docs`, and `test`.
- Reserve unscoped `fix:` for product defects. Internal corrections must use an explicit scope such as `fix(lint):`, `fix(build):`, or `fix(ci):` so release notes classify them as maintenance.
- Dependency updates use `fix(deps):` or `chore(deps):`; version-release commits use `chore(release):`.
- Keep the subject concise, imperative, and without a trailing period. Use the body to explain motivation, constraints, or non-obvious tradeoffs.
- Mark breaking changes with `!` after the type/scope and include a `BREAKING CHANGE:` footer.
- Codex-assisted commits must include `Co-Authored-By: Codex <noreply@openai.com>`.

## DataStore Keys (StitchSettingsManager)

| Key | Type | Default |
|-----|------|---------|
| stitch_mode | String | DIRECT_VERTICAL |
| width_scale | String | MIN_WIDTH |
| overlay_area | Int | 10 |
| overlay_mode | String | DISABLED |
| image_spacing | Int | 0 |
| image_spacing_color | String | #FF000000 |
| cut_grid | Int | 3 |
| multi_thread_enabled | Boolean | false |

## DataStore Keys (ImageSettingsManager)

| Key | Type | Note |
|-----|------|------|
| output_image_format | Int | 0: PNG, 1: JPEG, 2: WEBP |
| output_image_quality | Int | 0–100 |
| delete_original_image | Boolean | |
| auto_clear_images | Boolean | |
| high_memory_limit | Boolean | raise memory threshold |
| use_saf_picker | Boolean | use Storage Access Framework picker |
| use_embedded_picker | Boolean | use embedded-picker library |
| slider_thumb_shape | Int | FancySlider thumb shape |
| image_list_direction | Int | image list direction mode |

Other managers: `UserPreferencesManager` (`first_launch`), `LogSettingsManager`, `PreloadManager`.

## Dark Mode

- Do NOT call `isSystemInDarkTheme()` directly for color adjustments in settings UI
- Use `shouldUseDarkTheme()` composable from `ui/theme/Theme.kt` (honors user's ThemeMode: AUTO/DARK/LIGHT)
- Or derive `isDark` via `when (uiState.themeMode) { ... }` pattern (see `DisplaySettings.kt:216-219`)
- `ColorUtils.adjustColorForDarkTheme(color)` lowers brightness; black is exempted

## Lint

- AGP 9.3.0 treats `LocalContextGetResourceValueCall` as error
- Fix pattern: extract string to composable-scoped `val` with `stringResource(R.string.xxx)` before using in non-composable lambdas
- Build command `./gradlew build -x detekt` does NOT skip lint (detekt ≠ lint)
