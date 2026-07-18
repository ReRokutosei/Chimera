package com.rerokutosei.chimera.utils.image

object ResolutionMemoryRisk {
    const val HIGH_RISK_BYTES: Long = 512L * 1024L * 1024L

    private const val ARGB_8888_BYTES_PER_PIXEL = 4L
    private const val HIGH_RISK_PIXELS = HIGH_RISK_BYTES / ARGB_8888_BYTES_PER_PIXEL

    fun isHighRisk(width: Long, height: Long): Boolean {
        require(width > 0 && height > 0) { "Resolution dimensions must be positive" }
        return width >= divideRoundingUp(HIGH_RISK_PIXELS, height)
    }

    private fun divideRoundingUp(value: Long, divisor: Long): Long {
        return value / divisor + if (value % divisor == 0L) 0L else 1L
    }
}
