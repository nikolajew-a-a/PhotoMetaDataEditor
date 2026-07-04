package com.nikolajew.photometadataeditor.data.metadata

import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ExifToolMetadataEngine(
    private val exifTool: ExifToolProcess,
) : MetadataEngine {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun readMetadata(paths: List<String>): Map<String, FileMetadata> {
        if (paths.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, FileMetadata>()
        paths.chunked(CHUNK_SIZE).forEach { chunk ->
            result += readChunk(chunk)
        }
        return result
    }

    private suspend fun readChunk(paths: List<String>): Map<String, FileMetadata> {
        val output = exifTool.execute(
            listOf(
                "-charset", "filename=UTF8",
                "-j",
                "-n",
                "-DateTimeOriginal",
                "-CreateDate",
                "-GPSLatitude",
                "-GPSLongitude",
            ) + paths,
        )
        if (output.isBlank()) return emptyMap()

        // exiftool отдаёт SourceFile с прямыми слэшами — сопоставляем
        // с исходными путями по нормализованной форме
        val originalByNormalized = paths.associateBy { it.replace('\\', '/') }

        return json.parseToJsonElement(output).jsonArray
            .mapNotNull { element ->
                val obj = element.jsonObject
                val sourceFile = obj["SourceFile"]?.jsonPrimitive?.contentOrNull
                    ?: return@mapNotNull null
                val originalPath = originalByNormalized[sourceFile.replace('\\', '/')]
                    ?: return@mapNotNull null

                val takenAtRaw = obj["DateTimeOriginal"]?.jsonPrimitive?.contentOrNull
                    ?: obj["CreateDate"]?.jsonPrimitive?.contentOrNull
                val lat = obj["GPSLatitude"]?.jsonPrimitive?.doubleOrNull
                val lon = obj["GPSLongitude"]?.jsonPrimitive?.doubleOrNull

                originalPath to FileMetadata(
                    takenAt = takenAtRaw?.let(::parseExifDate),
                    location = if (lat != null && lon != null) GeoPoint(lat, lon) else null,
                )
            }
            .toMap()
    }

    private companion object {
        const val CHUNK_SIZE = 500

        /** Формат exiftool: "2023:05:12 14:30:22" (+ опциональные субсекунды/таймзона). */
        val EXIF_DATE_REGEX =
            Regex("""(\d{4}):(\d{2}):(\d{2})[ T](\d{2}):(\d{2}):(\d{2})""")

        fun parseExifDate(raw: String): Instant? {
            val match = EXIF_DATE_REGEX.find(raw) ?: return null
            val (year, month, day, hour, minute, second) =
                match.destructured.toList().map(String::toInt)
            if (year == 0) return null

            return runCatching {
                LocalDateTime(year, month, day, hour, minute, second)
                    .toInstant(TimeZone.UTC)
            }.getOrNull()
        }

        private operator fun <T> List<T>.component6(): T = this[5]
    }
}
