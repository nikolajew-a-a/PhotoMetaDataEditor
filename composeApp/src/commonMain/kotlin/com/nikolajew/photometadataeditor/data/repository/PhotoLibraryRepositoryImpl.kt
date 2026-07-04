package com.nikolajew.photometadataeditor.data.repository

import com.nikolajew.photometadataeditor.data.scanner.MediaFileScanner
import com.nikolajew.photometadataeditor.domain.model.MediaType
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PhotoLibraryRepositoryImpl(
    private val scanner: MediaFileScanner,
) : PhotoLibraryRepository {

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    override val photos: Flow<List<Photo>> get() = _photos

    override suspend fun openFolder(path: String) {
        val scanned = scanner.scan(path)
        _photos.value = scanned
            .mapNotNull { file ->
                val mediaType = mediaTypeFor(file.extension) ?: return@mapNotNull null
                Photo(
                    id = file.path,
                    path = file.path,
                    fileName = file.fileName,
                    mediaType = mediaType,
                    takenAt = null,
                    location = null,
                    processed = false,
                )
            }
            .sortedBy { it.fileName }
    }

    private fun mediaTypeFor(extension: String): MediaType? = when (extension) {
        in PHOTO_EXTENSIONS -> MediaType.PHOTO
        in VIDEO_EXTENSIONS -> MediaType.VIDEO
        else -> null
    }

    private companion object {
        val PHOTO_EXTENSIONS = setOf("jpg", "jpeg", "png", "heic")
        val VIDEO_EXTENSIONS = setOf("mp4", "mov")
    }
}
