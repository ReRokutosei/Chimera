# Image Processing Performance Baseline

Measured on 2026-07-17 with the `benchmark` build type and AndroidX Benchmark.

## Environment

- Pixel 9 Pro AVD, Android 15 / API 35, x86_64
- 4 virtual CPU cores at 2.0 GHz, 3.1 GB RAM
- ART compilation mode: `speed`
- Emulator error explicitly suppressed; results are suitable only for before/after comparison on the same AVD

## Historical physical-device field results

The following results were reported from the release-era implementation before version 1.0.1. They were measured manually and are retained as historical capacity evidence only. The source images, thermal state, build configuration, timing boundaries, and implementation differ from the current fixed benchmarks, so these values must not be used to claim a direct performance improvement or regression.

- Xiaomi Mi 10 Ultra, Android 13
- Qualcomm Snapdragon 865, up to 2.84 GHz
- 12 GB LPDDR5 RAM

### Direct stitching

| Orientation | Historical scale label | Images | Result dimensions | Time | Result |
| --- | --- | ---: | ---: | ---: | --- |
| Vertical | Minimum height | 100 | 531 x 71,260 | ~3.43 s | Success |
| Vertical | Original height | 100 | 2,297 x 176,470 | ~5.07 s | Success |
| Vertical | Maximum height | 100 | — | — | OOM |
| Vertical | Maximum height | 75 | 2,191 x 221,489 | ~17.38 s | Success |
| Horizontal | Minimum width | 75 | 44,938 x 720 | ~2.41 s | Success |
| Horizontal | Maximum width | 75 | 255,836 x 4,096 | ~37.55 s | Success |
| Horizontal | Original width | 75 | 103,534 x 4,096 | ~1.89 s | Success |
| Vertical | Minimum height | 10 | 760 x 12,019 | ~1.83 s | Success |
| Vertical | Original height | 10 | 1,587 x 19,002 | ~0.97 s | Success |
| Vertical | Maximum height | 10 | 1,587 x 25,105 | ~6.05 s | Success |
| Horizontal | Minimum width | 10 | 7,778 x 1,200 | ~0.57 s | Success |
| Horizontal | Maximum width | 10 | 13,282 x 2,048 | ~0.66 s | Success |
| Horizontal | Original width | 10 | 12,307 x 2,048 | ~0.39 s | Success |

### Overlay stitching

| Orientation | Historical scale label | Images | Retained area | Result dimensions | Time | Result |
| --- | --- | ---: | ---: | ---: | ---: | --- |
| Horizontal | Minimum height | 10 | 23% | 2,243 x 1,200 | ~1.76 s | Success |
| Horizontal | Maximum height | 10 | 23% | 3,832 x 2,048 | ~1.93 s | Success |
| Vertical | Minimum width | 10 | 23% | 760 x 3,820 | ~1.75 s | Success |
| Vertical | Maximum width | 10 | 23% | 1,587 x 7,984 | ~5.93 s | Success |
| Horizontal | Minimum height | 75 | 10% | 3,615 x 720 | ~2.23 s | Success |
| Horizontal | Maximum height | 75 | 10% | 20,672 x 4,096 | ~32.85 s | Success |
| Vertical | Minimum width | 75 | 10% | 531 x 7,393 | ~2.19 s | Success |
| Vertical | Maximum width | 75 | 10% | 2,191 x 30,498 | ~16.50 s | Success |

## Fixed datasets

| Scenario | Images | Base dimensions |
| --- | ---: | ---: |
| Normal pair | 2 | 1080 x 1920 |
| Medium batch | 10 | 720 x 480 |
| Small stress batch | 50 | 320 x 180 |

Each image has a deterministic color and a small deterministic size variation. The benchmark covers vertical and horizontal direct stitching, overlay stitching, NONE/MIN/MAX scaling, and JPEG/PNG/WebP encoding.

## Baseline results

| Benchmark | Median | Allocations |
| --- | ---: | ---: |
| Direct horizontal, 10 medium, MIN | 78.47 ms | 380 |
| Direct vertical, 10 medium, NONE | 31.55 ms | 153 |
| Direct vertical, 50 small, MAX | 83.53 ms | 1,261 |
| Overlay vertical, 10 medium, MIN | 54.99 ms | 383 |
| JPEG encode, normal image | 27.76 ms | 9 |
| PNG encode, normal image | 68.50 ms | 9 |
| WebP encode, normal image | 114.23 ms | 7 |

## Stage breakdown

Median of 20 measured runs using `SystemClock.elapsedRealtimeNanos()` and the same Trace sections exposed to Perfetto:

| Pipeline | Scale | Layout | Allocation | Draw |
| --- | ---: | ---: | ---: | ---: |
| Direct vertical, 10 medium, MIN | 31.06 ms | 0.05 ms | 0.09 ms | 5.38 ms |
| Overlay vertical, 10 medium, MIN | 31.00 ms | 0.06 ms | 0.04 ms | 4.14 ms |

Scaling is the only confirmed stitching bottleneck in this dataset. Layout, result allocation, and drawing do not justify optimization. Encoding cost is format-dependent and delegated to Android's Bitmap codecs.

## Confirmed optimization

When the existing multithreading setting is enabled, scaling now uses at most four fixed workers. Disabled mode remains sequential. A worker pool is used instead of one coroutine per image to keep allocation growth bounded.

| Benchmark | Sequential | Parallel | Change |
| --- | ---: | ---: | ---: |
| Direct horizontal, 10 medium, MIN | 78.47 ms | 46.28 ms | -41.0% |
| Direct vertical, 50 small, MAX | 83.53 ms | 51.45 ms | -38.4% |
| Overlay vertical, 10 medium, MIN | 54.99 ms | 32.47 ms | -41.0% |
| Direct MIN scale stage | 25.43 ms | 11.18 ms | -56.0% |
| Overlay MIN scale stage | 22.95 ms | 11.20 ms | -51.2% |

The 50-image allocation count is 1,401 with fixed workers, compared with 1,979 for the rejected one-coroutine-per-image prototype and 1,261 for sequential scaling. The measured speedup justified the remaining small coroutine allocation cost.

## Current physical-device app-flow baseline

Measured on 2026-07-18 on a Xiaomi Mi 10 Ultra (`M2007J1SC`), Android 13 / API 33, with eight iterations per case. The first-launch agreement is completed and persisted before measurement; its mandatory five-second countdown is intentionally excluded from startup timing.

The settings-flow duration is an instrumentation trace around opening Settings, two deterministic scroll gestures, returning to the main screen, and the associated UI-idle waits. It is suitable for comparisons made with the same harness, but it is not a claim about isolated frame-rendering latency.

| Scenario | No compilation | Current Baseline Profile | Median change |
| --- | ---: | ---: | ---: |
| Cold startup, time to initial display | 433.7 ms | 423.6 ms | -2.3% |
| Settings flow | 6,296.4 ms | 6,177.7 ms | -1.9% |

Observed ranges were 422.2–448.4 ms and 418.5–441.1 ms for cold startup, and 5,550.6–6,481.9 ms and 5,860.9–6,526.6 ms for the settings flow. The current profile therefore provides a small measurable benefit on this device, with substantial overlap between individual runs. Profile changes should be retained only if repeated measurements improve this baseline without regressing either scenario.

### Rejected profile regeneration

A profile regenerated from the corrected startup and Settings automation increased the baseline rules from 57,385 to 59,350 and the startup rules from 55,696 to 57,246. In the identical four-case follow-up run, cold startup with that profile measured 452.4 ms versus 396.5 ms without compilation, while the settings flow measured 6,192.9 ms versus 6,209.2 ms. The settings difference was negligible and the startup result regressed, so the regenerated profile files were not retained. This rejection also avoids accepting a larger profile without demonstrated benefit.

## Commands

```powershell
./gradlew :app:assembleBenchmark :app:assembleBenchmarkAndroidTest -x detekt
adb shell cmd package compile -m speed -f com.rerokutosei.chimera
adb shell am instrument -w -r com.rerokutosei.chimera.test/androidx.benchmark.junit4.AndroidBenchmarkRunner
./gradlew :baselineprofile:connectedNonMinifiedReleaseAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.rerokutosei.chimera.baselineprofile.StartupBenchmark" "-Pandroidx.baselineprofile.dontdisablerules=true"
```

Run performance comparisons on the same physical device when release-grade numbers are required.
