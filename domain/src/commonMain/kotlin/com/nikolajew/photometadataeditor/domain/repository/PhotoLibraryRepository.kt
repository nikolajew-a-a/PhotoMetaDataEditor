package com.nikolajew.photometadataeditor.domain.repository

import com.nikolajew.photometadataeditor.domain.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoLibraryRepository {

    val photos: Flow<List<Photo>>

    suspend fun openFolder(path: String)

    suspend fun setProcessed(ids: List<String>, processed: Boolean)

    /** Удаляет файл с диска (в корзину, если платформа умеет) и из индекса. */
    suspend fun deletePhoto(id: String)
}
