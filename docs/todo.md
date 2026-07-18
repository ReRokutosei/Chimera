# Deferred Improvements

These ideas are intentionally deferred until there is enough time for design, implementation, and device testing.

## Crash recovery

- Add an `Application`-level uncaught-exception handler and a dedicated crash-report screen.
- Run the crash screen in a separate process so it does not depend on the exhausted heap of the main process.
- Reserve a small emergency memory buffer and persist only a bounded minimal report during an OOM.
- Read and share the existing log file after the crash process starts instead of depending on asynchronous logging during the failure.
- Treat OOM reports as diagnostic information rather than automatically encouraging a GitHub issue.

ImageToolbox provides a useful reference implementation under `core/crash`, but its immediate `CrashActivity` launch remains best-effort when the heap is already exhausted.

## Logging

- Replace the current mixed asynchronous and blocking file writes with a bounded, single-writer logging pipeline.
- Add explicit log rotation, safe flush support for fatal failures, and a stable API for exporting logs.
- Keep crash reporting independent from the normal logger so logger failure cannot hide the original crash.

## Memory budgeting

- Replace limits derived from a percentage of total physical RAM with a processing budget that accounts for the app process, current allocations, source bitmaps, scaled copies, result allocation, preview, and encoding overhead.
- Validate any new budget against fixed large-image datasets on multiple memory classes before using it to block processing.

## State recovery

- Persist the selected URI list only if recovery across process death becomes a requirement.
- Revalidate persisted URI permissions and discard stale entries when restoring a session.
- Keep the existing in-memory navigation preservation for ordinary recoverable failures.

## Adaptive layouts

- Consider a width-based two-pane layout for tablets, foldables, landscape, and multi-window use.
- Avoid maintaining a separate orientation-specific UI tree.
