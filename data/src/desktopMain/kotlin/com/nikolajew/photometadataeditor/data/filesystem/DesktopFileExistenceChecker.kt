package com.nikolajew.photometadataeditor.data.filesystem

import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DesktopFileExistenceChecker : FileExistenceChecker {

    override suspend fun existingOf(paths: Collection<String>): Set<String> =
        withContext(Dispatchers.IO) {
            paths.filterTo(hashSetOf()) { Files.exists(Path.of(it)) }
        }
}
