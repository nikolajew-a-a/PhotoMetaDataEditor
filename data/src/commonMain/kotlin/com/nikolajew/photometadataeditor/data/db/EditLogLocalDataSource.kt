package com.nikolajew.photometadataeditor.data.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Журнал правок метаданных — резервная копия старых значений. */
class EditLogLocalDataSource(
    private val database: PhotoIndexDatabase,
) {

    suspend fun append(
        path: String,
        field: String,
        oldValue: String?,
        newValue: String,
        editedAt: Long,
    ) = withContext(Dispatchers.IO) {
        database.editLogQueries.append(
            path = path,
            field = field,
            oldValue = oldValue,
            newValue = newValue,
            editedAt = editedAt,
        )
    }
}
