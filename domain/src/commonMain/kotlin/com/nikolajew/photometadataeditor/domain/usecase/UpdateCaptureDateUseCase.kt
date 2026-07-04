package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.repository.MetadataRepository
import kotlinx.datetime.Instant

class UpdateCaptureDateUseCase(
    private val repository: MetadataRepository,
) {

    suspend operator fun invoke(photoId: String, takenAt: Instant) {
        repository.updateCaptureDate(photoId, takenAt)
    }
}
