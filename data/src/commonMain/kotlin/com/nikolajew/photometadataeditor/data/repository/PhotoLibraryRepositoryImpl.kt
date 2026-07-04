package com.nikolajew.photometadataeditor.data.repository

import com.nikolajew.photometadataeditor.data.db.IndexedFile
import com.nikolajew.photometadataeditor.data.db.PhotoEntity
import com.nikolajew.photometadataeditor.data.db.PhotoIndexLocalDataSource
import com.nikolajew.photometadataeditor.data.filesystem.FileDeleter
import com.nikolajew.photometadataeditor.data.metadata.MetadataEngine
import com.nikolajew.photometadataeditor.data.scanner.MediaFileScanner
import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import com.nikolajew.photometadataeditor.domain.model.MediaType
import com.nikolajew.photometadataeditor.domain.model.Photo
import com.nikolajew.photometadataeditor.domain.repository.PhotoLibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class PhotoLibraryRepositoryImpl(
    private val scanner: MediaFileScanner,
    private val localDataSource: PhotoIndexLocalDataSource,
    private val metadataEngine: MetadataEngine,
    private val fileDeleter: FileDeleter,
) : PhotoLibraryRepository {

    override val photos: Flow<List<Photo>> =
        localDataSource.observeAll().map { entities ->
            entities.map(PhotoEntity::toDomain)
        }

    override suspend fun openFolder(path: String) {
        val scanned = scanner.scan(path)
        val mediaFiles = scanned.mapNotNull { file ->
            val mediaType = mediaTypeFor(file.extension) ?: return@mapNotNull null
            file.path to mediaType
        }

        // Метаданные — best effort: без ExifTool библиотека всё равно работает
        val metadata = runCatching { metadataEngine.readMetadata(mediaFiles.map { it.first }) }
            .onFailure { System.err.println("Не удалось прочитать метаданные: ${it.message}") }
            .getOrDefault(emptyMap())

        localDataSource.replaceIndex(
            files = mediaFiles.map { (filePath, mediaType) ->
                val meta = metadata[filePath]
                IndexedFile(
                    path = filePath,
                    mediaType = mediaType.name,
                    takenAtEpochMillis = meta?.takenAt?.toEpochMilliseconds(),
                    latitude = meta?.location?.latitude,
                    longitude = meta?.location?.longitude,
                )
            },
            scanTime = Clock.System.now().toEpochMilliseconds(),
        )
    }

    override suspend fun setProcessed(ids: List<String>, processed: Boolean) {
        localDataSource.setProcessed(ids, processed)
    }

    override suspend fun deletePhoto(id: String) {
        fileDeleter.delete(id)
        localDataSource.delete(id)
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

private fun PhotoEntity.toDomain(): Photo = Photo(
    id = path,
    path = path,
    fileName = path.substringAfterLast('\\').substringAfterLast('/'),
    mediaType = MediaType.valueOf(media_type),
    takenAt = taken_at?.let(Instant::fromEpochMilliseconds),
    location = if (lat != null && lon != null) GeoPoint(lat, lon) else null,
    processed = processed == 1L,
)
