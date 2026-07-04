package com.nikolajew.photometadataeditor.data.metadata

import com.nikolajew.photometadataeditor.domain.model.GeoPoint
import kotlin.math.abs
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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

    override suspend fun writeMetadata(path: String, patch: MetadataPatch) {
        val args = buildList {
            add("-charset")
            add("filename=UTF8")
            add("-overwrite_original")
            patch.takenAt?.let { add("-AllDates=${it.toExifString()}") }
            patch.location?.let { location ->
                if (isVideo(path)) {
                    // QuickTime хранит координаты одним тегом
                    add("-GPSCoordinates=${location.latitude}, ${location.longitude}")
                } else {
                    add("-GPSLatitude=${abs(location.latitude)}")
                    add("-GPSLatitudeRef=${if (location.latitude >= 0) "N" else "S"}")
                    add("-GPSLongitude=${abs(location.longitude)}")
                    add("-GPSLongitudeRef=${if (location.longitude >= 0) "E" else "W"}")
                }
            }
            add(path)
        }

        val output = exifTool.execute(args)
        if (!output.contains("1 image files updated")) {
            throw MetadataWriteException(
                "exiftool не обновил файл: ${output.trim().ifEmpty { "подробности в логе" }}",
            )
        }
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

        val VIDEO_EXTENSIONS = setOf("mp4", "mov")

        fun isVideo(path: String): Boolean =
            path.substringAfterLast('.').lowercase() in VIDEO_EXTENSIONS

        fun Instant.toExifString(): String {
            val dt = toLocalDateTime(TimeZone.UTC)
            fun Int.pad2() = toString().padStart(2, '0')
            return "${dt.year.toString().padStart(4, '0')}:${dt.monthNumber.pad2()}:" +
                "${dt.dayOfMonth.pad2()} ${dt.hour.pad2()}:${dt.minute.pad2()}:${dt.second.pad2()}"
        }

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
