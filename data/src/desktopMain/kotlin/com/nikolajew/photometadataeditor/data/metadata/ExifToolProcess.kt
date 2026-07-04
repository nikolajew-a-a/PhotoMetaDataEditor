package com.nikolajew.photometadataeditor.data.metadata

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Долгоживущая сессия exiftool в режиме -stay_open: процесс стартует один раз,
 * дальнейшие команды идут через stdin без цены запуска Perl на каждый вызов.
 */
class ExifToolProcess(
    private val locateBinary: () -> String?,
) {

    private class Session(
        val process: Process,
        val writer: BufferedWriter,
        val reader: BufferedReader,
    )

    private val mutex = Mutex()
    private var session: Session? = null

    init {
        Runtime.getRuntime().addShutdownHook(thread(start = false) { closeQuietly() })
    }

    /** Выполняет одну команду exiftool (список аргументов) и возвращает её stdout. */
    suspend fun execute(args: List<String>): String = mutex.withLock {
        withContext(Dispatchers.IO) {
            val session = ensureSession()
            args.forEach { session.writer.appendLine(it) }
            session.writer.appendLine("-execute")
            session.writer.flush()

            buildString {
                while (true) {
                    val line = session.reader.readLine()
                        ?: throw IOException("Процесс exiftool неожиданно завершился")
                    if (line.trim() == "{ready}") break
                    appendLine(line)
                }
            }
        }
    }

    private fun ensureSession(): Session {
        session?.takeIf { it.process.isAlive }?.let { return it }

        val binaryPath = locateBinary()
            ?: throw IOException(
                "exiftool не найден: положите его в tools/exiftool или задайте EXIFTOOL_PATH",
            )

        val process = ProcessBuilder(binaryPath, "-stay_open", "True", "-@", "-").start()

        thread(isDaemon = true, name = "exiftool-stderr") {
            process.errorStream.bufferedReader(Charsets.UTF_8).forEachLine {
                System.err.println("exiftool: $it")
            }
        }

        return Session(
            process = process,
            writer = process.outputStream.bufferedWriter(Charsets.UTF_8),
            reader = process.inputStream.bufferedReader(Charsets.UTF_8),
        ).also { session = it }
    }

    fun closeQuietly() {
        runCatching {
            session?.let {
                it.writer.appendLine("-stay_open")
                it.writer.appendLine("False")
                it.writer.flush()
                if (!it.process.waitFor(2, TimeUnit.SECONDS)) {
                    it.process.destroy()
                }
            }
        }
        session = null
    }
}
