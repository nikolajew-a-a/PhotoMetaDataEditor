package com.nikolajew.photometadataeditor.data.metadata

import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import kotlinx.datetime.Instant

/**
 * Движок чтения/записи метаданных медиафайлов.
 * Desktop-реализация — внешний процесс ExifTool, Android (план) — ExifInterface.
 */
interface MetadataEngine {

    /**
     * Читает метаданные для набора файлов.
     * Ключи результата — те же строки путей, что переданы на вход.
     * Файлы, для которых ничего не удалось прочитать, в результате отсутствуют.
     */
    suspend fun readMetadata(paths: List<String>): Map<String, FileMetadata>
}

data class FileMetadata(
    val takenAt: Instant?,
    val location: GeoPoint?,
)
