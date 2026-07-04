package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository

class SetProcessedUseCase(
    private val repository: PhotoLibraryRepository,
) {

    suspend operator fun invoke(ids: List<String>, processed: Boolean) {
        if (ids.isEmpty()) return
        repository.setProcessed(ids, processed)
    }
}
