package com.nikolajew.photometadataeditor.data.repository

import com.nikolajew.photometadataeditor.data.db.EditLogLocalDataSource
import com.nikolajew.photometadataeditor.data.db.PhotoIndexLocalDataSource
import com.nikolajew.photometadataeditor.data.metadata.MetadataEngine
import com.nikolajew.photometadataeditor.data.metadata.MetadataPatch
import com.nikolajew.photometadataeditor.data.metadata.MetadataWriteException
import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import com.nikolajew.photometadataeditor.domain.repository.MetadataRepository
import kotlin.math.abs
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Оркестрация записи метаданных, общая для всех платформ:
 * старое значение в журнал → запись движком → контрольное чтение → обновление индекса.
 */
class MetadataRepositoryImpl(
    private val engine: MetadataEngine,
    private val photoIndex: PhotoIndexLocalDataSource,
    private val editLog: EditLogLocalDataSource,
) : MetadataRepository {

    override suspend fun updateCaptureDate(photoId: String, takenAt: Instant) {
        val before = engine.readMetadata(listOf(photoId))[photoId]

        editLog.append(
            path = photoId,
            field = FIELD_TAKEN_AT,
            oldValue = before?.takenAt?.toString(),
            newValue = takenAt.toString(),
            editedAt = Clock.System.now().toEpochMilliseconds(),
        )

        engine.writeMetadata(photoId, MetadataPatch(takenAt = takenAt))

        val after = engine.readMetadata(listOf(photoId))[photoId]
            ?: throw MetadataWriteException("Не удалось перечитать файл после записи")
        if (after.takenAt != takenAt) {
            throw MetadataWriteException(
                "Контрольное чтение вернуло другую дату: ${after.takenAt}",
            )
        }

        photoIndex.updateTakenAt(photoId, takenAt.toEpochMilliseconds())
    }

    override suspend fun updateLocation(photoId: String, location: GeoPoint) {
        val before = engine.readMetadata(listOf(photoId))[photoId]

        editLog.append(
            path = photoId,
            field = FIELD_LOCATION,
            oldValue = before?.location?.let { "${it.latitude},${it.longitude}" },
            newValue = "${location.latitude},${location.longitude}",
            editedAt = Clock.System.now().toEpochMilliseconds(),
        )

        engine.writeMetadata(photoId, MetadataPatch(location = location))

        val after = engine.readMetadata(listOf(photoId))[photoId]?.location
            ?: throw MetadataWriteException("После записи в файле не нашлась локация")
        if (abs(after.latitude - location.latitude) > GPS_TOLERANCE ||
            abs(after.longitude - location.longitude) > GPS_TOLERANCE
        ) {
            throw MetadataWriteException(
                "Контрольное чтение вернуло другие координаты: " +
                    "${after.latitude}, ${after.longitude}",
            )
        }

        photoIndex.updateLocation(photoId, location.latitude, location.longitude)
    }

    private companion object {
        const val FIELD_TAKEN_AT = "taken_at"
        const val FIELD_LOCATION = "location"

        /** EXIF хранит координаты рациональными числами — допускаем ошибку округления. */
        const val GPS_TOLERANCE = 1e-4
    }
}
