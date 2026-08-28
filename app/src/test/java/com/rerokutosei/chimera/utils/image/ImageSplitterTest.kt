package com.rerokutosei.chimera.utils.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageSplitterTest {

    @Test
    fun computeSegmentsEvenDivision() {
        val segs = ImageSplitter.computeSegments(2000, 4)
        assertEquals(4, segs.size)
        assertEquals(SplitSegment(0, 500), segs[0])
        assertEquals(SplitSegment(500, 500), segs[1])
        assertEquals(SplitSegment(1000, 500), segs[2])
        assertEquals(SplitSegment(1500, 500), segs[3])
        assertEquals(2000, segs.sumOf { it.size })
    }

    @Test
    fun computeSegmentsOddRemainderDistribution() {
        val segs = ImageSplitter.computeSegments(1983, 4)
        assertEquals(4, segs.size)
        assertEquals(listOf(496, 496, 496, 495), segs.map { it.size })

        // Check zero gap and zero overlap
        for (i in 0 until segs.size - 1) {
            assertEquals(segs[i].start + segs[i].size, segs[i + 1].start)
        }
        assertEquals(1983, segs.sumOf { it.size })
    }

    @Test
    fun computeSegmentsVariousDimensions() {
        val testCases = listOf(
            Pair(1001, 3),
            Pair(2049, 4),
            Pair(3840, 3),
            Pair(777, 2),
            Pair(1080, 3)
        )

        for ((total, count) in testCases) {
            val segs = ImageSplitter.computeSegments(total, count)
            assertEquals(count, segs.size)
            assertEquals(0, segs.first().start)

            for (i in 0 until count - 1) {
                assertEquals(segs[i].start + segs[i].size, segs[i + 1].start)
            }

            val last = segs.last()
            assertEquals(total, last.start + last.size)
            assertEquals(total, segs.sumOf { it.size })
        }
    }
}
