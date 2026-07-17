package com.rerokutosei.chimera.domain.error

sealed interface StitchFailure {
    val cause: Throwable?
        get() = null

    data object NoImages : StitchFailure
    data object DecodeFailed : StitchFailure
    data class MetadataUnavailable(override val cause: Throwable) : StitchFailure

    data class ResultTooLarge(
        val width: Long,
        val height: Long,
        val estimatedBytes: Long,
        val maximumBytes: Long
    ) : StitchFailure

    data class AllocationFailed(override val cause: Throwable) : StitchFailure
    data class Unexpected(override val cause: Throwable) : StitchFailure
}

sealed interface CutFailure {
    val cause: Throwable?
        get() = null

    data object NoImages : CutFailure
    data object DecodeFailed : CutFailure
    data object InvalidGrid : CutFailure
    data class SplitFailed(override val cause: Throwable) : CutFailure
    data class Unexpected(override val cause: Throwable) : CutFailure
}

sealed interface SaveFailure {
    val cause: Throwable?
        get() = null

    data object StorageUnavailable : SaveFailure
    data object EncodingFailed : SaveFailure
    data class WriteFailed(override val cause: Throwable) : SaveFailure
    data class Unexpected(override val cause: Throwable) : SaveFailure
}
