package com.rerokutosei.chimera.utils.image

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolutionMemoryRiskTest {

    @Test
    fun resolutionBelowThresholdIsNotHighRisk() {
        assertFalse(ResolutionMemoryRisk.isHighRisk(16_384, 8_191))
    }

    @Test
    fun resolutionAtThresholdIsHighRisk() {
        assertTrue(ResolutionMemoryRisk.isHighRisk(16_384, 8_192))
    }

    @Test
    fun veryLargeResolutionDoesNotOverflow() {
        assertTrue(ResolutionMemoryRisk.isHighRisk(Long.MAX_VALUE, Long.MAX_VALUE))
    }

    @Test
    fun nonPositiveDimensionsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ResolutionMemoryRisk.isHighRisk(0, 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ResolutionMemoryRisk.isHighRisk(1, -1)
        }
    }
}
