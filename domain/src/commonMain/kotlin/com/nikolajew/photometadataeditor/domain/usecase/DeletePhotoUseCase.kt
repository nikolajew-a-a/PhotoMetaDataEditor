package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository

class DeletePhotoUseCase(
    private val repository: PhotoLibraryRepository,
) {

    suspend operator fun invoke(id: String) {
        repository.deletePhoto(id)
    }
}
