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

    /**
     * Записывает в файл поля, заданные в патче (null-поля не трогаются).
     * Бросает исключение, если запись не удалась.
     */
    suspend fun writeMetadata(path: String, patch: MetadataPatch)
}

data class FileMetadata(
    val takenAt: Instant?,
    val location: GeoPoint?,
)

data class MetadataPatch(
    val takenAt: Instant? = null,
    val location: GeoPoint? = null,
)

class MetadataWriteException(message: String) : Exception(message)
