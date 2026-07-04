package com.nikolajew.photometadataeditor.data.filesystem

import java.awt.Desktop
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DesktopFileDeleter : FileDeleter {

    override suspend fun delete(path: String): Unit = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext

        val movedToTrash = runCatching {
            Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH) &&
                Desktop.getDesktop().moveToTrash(file)
        }.getOrDefault(false)

        if (!movedToTrash && !file.delete()) {
            throw IOException("Не удалось удалить файл: $path")
        }
    }
}
