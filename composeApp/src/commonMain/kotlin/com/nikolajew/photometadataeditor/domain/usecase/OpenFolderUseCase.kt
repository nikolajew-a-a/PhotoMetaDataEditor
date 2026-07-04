package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository

class OpenFolderUseCase(
    private val repository: PhotoLibraryRepository,
) {

    suspend operator fun invoke(path: String) {
        repository.openFolder(path)
    }
}
