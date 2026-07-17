package com.rerokutosei.chimera.utils.performance

import android.os.SystemClock
import android.os.Trace
import java.util.concurrent.atomic.AtomicInteger

data class StageTiming(val stage: String, val durationNanos: Long)

object ProcessingPerformance {
    @PublishedApi
    internal const val TRACE_PREFIX = "Chimera:"
    private val nextCookie = AtomicInteger()

    @Volatile
    var observer: ((StageTiming) -> Unit)? = null

    inline fun <T> measure(stage: String, block: () -> T): T {
        val startedAt = SystemClock.elapsedRealtimeNanos()
        Trace.beginSection("$TRACE_PREFIX$stage")
        try {
            return block()
        } finally {
            Trace.endSection()
            record(stage, SystemClock.elapsedRealtimeNanos() - startedAt)
        }
    }

    suspend fun <T> measureSuspend(stage: String, block: suspend () -> T): T {
        val cookie = nextCookie.incrementAndGet()
        val startedAt = SystemClock.elapsedRealtimeNanos()
        Trace.beginAsyncSection("$TRACE_PREFIX$stage", cookie)
        try {
            return block()
        } finally {
            Trace.endAsyncSection("$TRACE_PREFIX$stage", cookie)
            record(stage, SystemClock.elapsedRealtimeNanos() - startedAt)
        }
    }

    @PublishedApi
    internal fun record(stage: String, durationNanos: Long) {
        observer?.invoke(StageTiming(stage, durationNanos))
    }
}

object ProcessingStage {
    const val TOTAL = "total"
    const val METADATA = "metadata"
    const val DECODE = "decode"
    const val EXIF = "exif"
    const val SCALE = "scale"
    const val LAYOUT = "layout"
    const val ALLOCATION = "allocation"
    const val DRAW = "draw"
    const val ENCODE_SAVE = "encode-save"
}
