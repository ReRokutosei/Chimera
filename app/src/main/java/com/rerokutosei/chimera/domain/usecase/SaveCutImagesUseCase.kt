package com.rerokutosei.chimera.domain.usecase

import android.net.Uri
import com.rerokutosei.chimera.domain.error.CutFailure
import com.rerokutosei.chimera.domain.error.SaveFailure
import com.rerokutosei.chimera.utils.image.BitmapLoader
import com.rerokutosei.chimera.utils.image.ImageSaveResult
import com.rerokutosei.chimera.utils.image.ImageSaver
import com.rerokutosei.chimera.utils.image.ImageSplitter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveCutImagesUseCase(
    private val bitmapLoader: BitmapLoader,
    private val imageSaver: ImageSaver
) {
    suspend operator fun invoke(
        imageUris: List<Uri>,
        cols: Int,
        rows: Int
    ): CutSaveResult = withContext(Dispatchers.IO) {
        if (imageUris.isEmpty()) return@withContext CutSaveResult.CutFailed(CutFailure.NoImages, 0)
        if (cols <= 0 || rows <= 0) return@withContext CutSaveResult.CutFailed(
            CutFailure.InvalidGrid,
            0
        )

        val options = try {
            imageSaver.loadOptions()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withContext CutSaveResult.SaveFailed(SaveFailure.Unexpected(e), 0)
        }

        var savedCount = 0
        imageUris.forEachIndexed { imageIndex, uri ->
            val source = bitmapLoader.loadBitmapFromUri(uri)
                ?: return@withContext CutSaveResult.CutFailed(CutFailure.DecodeFailed, savedCount)
            try {
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        val piece = try {
                            withContext(Dispatchers.Default) {
                                ImageSplitter.createPiece(source, col, row, cols, rows)
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (failure: OutOfMemoryError) {
                            return@withContext CutSaveResult.CutFailed(
                                CutFailure.SplitFailed(
                                    failure
                                ), savedCount
                            )
                        } catch (failure: Exception) {
                            return@withContext CutSaveResult.CutFailed(
                                CutFailure.SplitFailed(
                                    failure
                                ), savedCount
                            )
                        }

                        val saveResult = try {
                            imageSaver.saveToGallery(
                                bitmap = piece,
                                options = options,
                                nameSuffix = "cut_${imageIndex + 1}_${row * cols + col + 1}"
                            )
                        } finally {
                            if (!piece.isRecycled) piece.recycle()
                        }

                        when (saveResult) {
                            is ImageSaveResult.Success -> savedCount++
                            is ImageSaveResult.Failure -> {
                                return@withContext CutSaveResult.SaveFailed(
                                    saveResult.failure,
                                    savedCount
                                )
                            }
                        }
                    }
                }
            } finally {
                bitmapLoader.recycleBitmaps(listOf(source))
            }
        }

        CutSaveResult.Success(savedCount)
    }
}

sealed interface CutSaveResult {
    data class Success(val savedCount: Int) : CutSaveResult
    data class CutFailed(val failure: CutFailure, val savedCount: Int) : CutSaveResult
    data class SaveFailed(val failure: SaveFailure, val savedCount: Int) : CutSaveResult
}
