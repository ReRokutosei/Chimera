package com.rerokutosei.chimera.utils.image

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

object ImageSplitter {

    fun createPiece(bitmap: Bitmap, col: Int, row: Int, cols: Int, rows: Int): Bitmap {
        require(cols > 0 && rows > 0) { "Grid dimensions must be positive" }
        require(col in 0 until cols && row in 0 until rows) { "Piece coordinates are outside the grid" }
        val left = col * bitmap.width / cols
        val top = row * bitmap.height / rows
        val right = (col + 1) * bitmap.width / cols
        val bottom = (row + 1) * bitmap.height / rows
        val pieceWidth = right - left
        val pieceHeight = bottom - top
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
