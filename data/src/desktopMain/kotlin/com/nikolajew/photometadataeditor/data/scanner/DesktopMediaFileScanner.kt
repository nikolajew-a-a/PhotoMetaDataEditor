package com.nikolajew.photometadataeditor.data.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.streams.asSequence

class DesktopMediaFileScanner : MediaFileScanner {

    override suspend fun scan(folderPath: String): List<ScannedFile> =
        withContext(Dispatchers.IO) {
            Files.walk(Path.of(folderPath)).use { stream ->
                stream.asSequence()
                    .filter { Files.isRegularFile(it) }
                    .map { file ->
                        ScannedFile(
                            path = file.toString(),
                            fileName = file.name,
                            extension = file.extension.lowercase(),
                        )
                    }
                    .toList()
            }
        }
}
