package com.nikolajew.photometadataeditor.data.metadata

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object ExifToolLocator {

    /**
     * Ищет exiftool.exe: переменная окружения, бандл в репозитории
     * (запуск из IDEA/Gradle с разным рабочим каталогом), затем PATH.
     */
    fun locate(): String? {
        val candidates = sequenceOf(
            System.getenv("EXIFTOOL_PATH"),
            "tools/exiftool/exiftool.exe",
            "../tools/exiftool/exiftool.exe",
        )
        candidates
            .filterNotNull()
            .map(Path::of)
            .firstOrNull(Files::exists)
            ?.let { return it.toAbsolutePath().normalize().toString() }

        return System.getenv("PATH")
            ?.split(File.pathSeparator)
            ?.asSequence()
            ?.map { Path.of(it.trim(), "exiftool.exe") }
            ?.firstOrNull(Files::exists)
            ?.toString()
    }
}
