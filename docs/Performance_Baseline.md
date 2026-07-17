# Image Processing Performance Baseline

Measured on 2026-07-17 with the `benchmark` build type and AndroidX Benchmark.

## Environment

- Pixel 9 Pro AVD, Android 15 / API 35, x86_64
- 4 virtual CPU cores at 2.0 GHz, 3.1 GB RAM
- ART compilation mode: `speed`
- Emulator error explicitly suppressed; results are suitable only for before/after comparison on the same AVD

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

## Commands

```powershell
./gradlew :app:assembleBenchmark :app:assembleBenchmarkAndroidTest -x detekt
adb shell cmd package compile -m speed -f com.rerokutosei.chimera
adb shell am instrument -w -r com.rerokutosei.chimera.test/androidx.benchmark.junit4.AndroidBenchmarkRunner
```

Run performance comparisons on the same physical device when release-grade numbers are required.
