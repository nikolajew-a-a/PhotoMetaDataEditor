package com.nikolajew.photometadataeditor.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PhotoIndexLocalDataSource(
    private val database: PhotoIndexDatabase,
) {

    private val queries get() = database.photoEntityQueries

    fun observeAll(): Flow<List<PhotoEntity>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO)

    /**
     * Полностью синхронизирует индекс с результатом сканирования:
     * новые файлы добавляются, существующие сохраняют флаг processed,
     * исчезнувшие с диска — удаляются.
     */
    suspend fun replaceIndex(files: List<IndexedFile>, scanTime: Long) =
        withContext(Dispatchers.IO) {
            database.transaction {
                files.forEach { file ->
                    queries.upsert(
                        path = file.path,
                        mediaType = file.mediaType,
                        takenAt = file.takenAtEpochMillis,
                        lat = file.latitude,
                        lon = file.longitude,
                        indexedAt = scanTime,
                    )
                }
                queries.deleteStale(scanTime)
            }
        }

    suspend fun setProcessed(paths: List<String>, processed: Boolean) =
        withContext(Dispatchers.IO) {
            queries.setProcessed(if (processed) 1L else 0L, paths)
        }

    suspend fun updateTakenAt(path: String, takenAtEpochMillis: Long) =
        withContext(Dispatchers.IO) {
            queries.updateTakenAt(takenAtEpochMillis, path)
        }

    suspend fun updateLocation(path: String, latitude: Double, longitude: Double) =
        withContext(Dispatchers.IO) {
            queries.updateLocation(latitude, longitude, path)
        }

    suspend fun delete(path: String) =
        withContext(Dispatchers.IO) {
            queries.deleteByPath(path)
        }

    suspend fun deleteAll(paths: Collection<String>) =
        withContext(Dispatchers.IO) {
            queries.deleteByPaths(paths)
        }
}

data class IndexedFile(
    val path: String,
    val mediaType: String,
    val takenAtEpochMillis: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
