package com.rerokutosei.chimera.utils.image

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

object ImageSplitter {

    fun splitBitmap(bitmap: Bitmap, cols: Int, rows: Int): List<Bitmap> {
        val pieces = mutableListOf<Bitmap>()
        val pieceWidth = bitmap.width / cols
        val pieceHeight = bitmap.height / rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val config = bitmap.config ?: Bitmap.Config.ARGB_8888
                val piece = createBitmap(pieceWidth, pieceHeight, config)
                val canvas = android.graphics.Canvas(piece)
                val srcRect = android.graphics.Rect(
                    col * pieceWidth, row * pieceHeight,
                    (col + 1) * pieceWidth, (row + 1) * pieceHeight
                )
                val destRect = android.graphics.Rect(0, 0, pieceWidth, pieceHeight)
                canvas.drawBitmap(bitmap, srcRect, destRect, null)
                canvas.setBitmap(null)
                pieces.add(piece)
            }
        }
        return pieces
    }
}
