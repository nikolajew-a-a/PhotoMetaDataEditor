package com.nikolajew.photometadataeditor.data.filesystem

interface FileDeleter {

    /**
     * Удаляет файл (по возможности — в корзину). Отсутствующий файл — не ошибка.
     * Бросает исключение, если удаление не удалось.
     */
    suspend fun delete(path: String)
}
