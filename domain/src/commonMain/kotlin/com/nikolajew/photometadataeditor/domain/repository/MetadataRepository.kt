package com.nikolajew.photometadataeditor.domain.repository

import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import kotlinx.datetime.Instant

interface MetadataRepository {

    /** Записывает дату съёмки в файл. Бросает исключение, если запись не удалась. */
    suspend fun updateCaptureDate(photoId: String, takenAt: Instant)

    /** Записывает геолокацию в файл. Бросает исключение, если запись не удалась. */
    suspend fun updateLocation(photoId: String, location: GeoPoint)
}
