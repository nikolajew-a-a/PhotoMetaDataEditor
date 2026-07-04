package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import kotlinx.coroutines.flow.Flow

class ObserveLibraryUseCase(
    private val repository: PhotoLibraryRepository,
) {

    operator fun invoke(): Flow<List<Photo>> = repository.photos
}
