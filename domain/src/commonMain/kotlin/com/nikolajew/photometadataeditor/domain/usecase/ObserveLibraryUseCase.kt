package com.nikolajew.photometadataeditor.domain.usecase

import com.nikolajew.photometadataeditor.domain.model.LibraryFilter
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveLibraryUseCase(
    private val repository: PhotoLibraryRepository,
) {

    operator fun invoke(filter: LibraryFilter): Flow<List<Photo>> =
        repository.photos.map { photos ->
            when (filter) {
                LibraryFilter.ALL -> photos
                LibraryFilter.UNPROCESSED -> photos.filterNot(Photo::processed)
                LibraryFilter.PROCESSED -> photos.filter(Photo::processed)
            }
        }
}
