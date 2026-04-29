# Chimera — AGENTS.md

## Build & Verify

```powershell
# Full build (skip detekt static analysis)
./gradlew build -x detekt

# Lint-only failures (AGP 9.2.0 treats LocalContextGetResourceValueCall as error)
# Fix: extract context.getString(...) to composable-scoped val with stringResource(...)
```

- `compileSdk` / `targetSdk` = 36, `minSdk` = 29, `applicationId` = `com.rerokutosei.chimera`
- Release build needs keystore env vars (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`)
- No tests (no test runner configured; do not add tests unless explicitly asked)
- Kotlin serialization plugin (`kotlin("plugin.serialization")`) already applied; Navigation `2.9.8` supports `@Serializable` route types via `toRoute<>()`
- HorizontalPager from `androidx.compose.foundation.pager` available (no extra dependency needed)

## Architecture

- **Single Activity** → Jetpack Compose, no Fragments
- **No Services, no BroadcastReceivers** (except per-user request)
- Navigation: type-safe via `@Serializable sealed class Route` in `ui/navigation/Route.kt`
- State: `ViewModel` + `MutableStateFlow` + DataStore Preferences; no Room, no network
- App theme: `ChimeraTheme` with `shouldUseDarkTheme()` composable in `ui/theme/Theme.kt`
- Three library modules under `t8rin/` (fancy-slider, embedded-picker, image-reorder-carousel)

## Package Layout

```
data/local/       — DataStore-based setting managers (StitchSettingsManager, ImageSettingsManager)
data/model/       — enums and data classes (ColorScheme, ThemeMode, ImageInfo)
data/repository/  — ThemeRepository
ui/main/          — MainScreen, ParameterSettingsCard, BottomActionButtons, TopAppBar
ui/settings/      — SettingsScreen, ImageOutputSettings, DisplaySettings, etc.
ui/viewer/        — ImageViewerScreen, ImageResultPreviewer, ImageViewerViewModel
ui/theme/         — ChimeraTheme, CustomColorPickerDialog, SpacingColorPickerDialog, ColorSchemePreview
ui/navigation/    — NavGraph, Route
utils/image/      — BitmapLoader, ImageSaver, ImageSharer, ImageSplitter
utils/stitch/     — ImageStitcher, StitchingStrategy, DirectStitchingStrategy, OverlayStitchingStrategy
```

## Conventions

- String resources must be added to both `values/strings.xml` (EN) and `values-zh-rCN/strings.xml` (ZH)
- `CustomSegmentedButtonRow` in `ui/main/CustomSegmentedButton.kt` is reused for mode/grid selection
- ParameterSettingsCard is hidden in cut mode; its state auto-preserves when switching back to stitch mode
- `PreviewSource.FromBitmapWithGrid(bitmap, cols, rows)` for cut grid preview
- Spacing fill color stored as hex string (e.g. `"#FF000000"`) in DataStore
- StitchSettingsManager uses generic `getPref<T>()` / `setPref<T>()` helpers for DataStore access

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

## Dark Mode

- Do NOT call `isSystemInDarkTheme()` directly for color adjustments in settings UI
- Use `shouldUseDarkTheme()` composable from `ui/theme/Theme.kt` (honors user's ThemeMode: AUTO/DARK/LIGHT)
- Or derive `isDark` via `when (uiState.themeMode) { ... }` pattern (see `DisplaySettings.kt:216-219`)
- `ColorUtils.adjustColorForDarkTheme(color)` lowers brightness; black is exempted

## Lint

- AGP 9.2.0 treats `LocalContextGetResourceValueCall` as error
- Fix pattern: extract string to composable-scoped `val` with `stringResource(R.string.xxx)` before using in non-composable lambdas
- Build command `./gradlew build -x detekt` does NOT skip lint (detekt ≠ lint)
