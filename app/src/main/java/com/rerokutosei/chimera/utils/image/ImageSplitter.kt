package com.rerokutosei.chimera.utils.image

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

data class SplitSegment(val start: Int, val size: Int)

object ImageSplitter {

    fun computeSegments(total: Int, count: Int): List<SplitSegment> {
        require(count > 0 && total >= count) { "Invalid segment parameters: total=$total, count=$count" }
        val base = total / count
        val rem = total % count
        var pos = 0
        return List(count) { i ->
            val size = base + if (i < rem) 1 else 0
            val seg = SplitSegment(pos, size)
            pos += size
            seg
        }
    }

    fun createPiece(bitmap: Bitmap, col: Int, row: Int, cols: Int, rows: Int): Bitmap {
        require(cols > 0 && rows > 0) { "Grid dimensions must be positive" }
        require(col in 0 until cols && row in 0 until rows) { "Piece coordinates are outside the grid" }

        val colSegs = computeSegments(bitmap.width, cols)
        val rowSegs = computeSegments(bitmap.height, rows)

        val colSeg = colSegs[col]
        val rowSeg = rowSegs[row]

        val left = colSeg.start
        val top = rowSeg.start
        val pieceWidth = colSeg.size
        val pieceHeight = rowSeg.size
        val right = left + pieceWidth
        val bottom = top + pieceHeight

        require(pieceWidth > 0 && pieceHeight > 0) { "Grid is larger than the source bitmap" }

        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val piece = createBitmap(pieceWidth, pieceHeight, config)
        val canvas = android.graphics.Canvas(piece)
        try {
            val srcRect = android.graphics.Rect(
                left,
                top,
                right,
                bottom
            )
            val destRect = android.graphics.Rect(0, 0, pieceWidth, pieceHeight)
            canvas.drawBitmap(bitmap, srcRect, destRect, null)
            return piece
        } catch (failure: Throwable) {
            piece.recycle()
            throw failure
        } finally {
            canvas.setBitmap(null)
        }
    }
}
