package com.nikolajew.photometadataeditor.data.scanner

interface MediaFileScanner {

    suspend fun scan(folderPath: String): List<ScannedFile>
}

data class ScannedFile(
    val path: String,
    val fileName: String,
    val extension: String,
)
