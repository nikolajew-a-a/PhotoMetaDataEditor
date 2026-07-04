package com.nikolajew.photometadataeditor.data.filesystem

interface FileExistenceChecker {

    /** Возвращает подмножество путей, которые реально существуют на диске. */
    suspend fun existingOf(paths: Collection<String>): Set<String>
}
