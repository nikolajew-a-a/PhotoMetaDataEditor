package com.nikolajew.photometadataeditor.platform

interface FolderPicker {

    /** Возвращает путь к выбранной папке или null, если пользователь отменил выбор. */
    suspend fun pickFolder(): String?
}
