/*
 SPDX-FileCopyrightText: 2025 Mark Zhitomirski
 SPDX-License-Identifier: Apache-2.0
 */
package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.ByteBuffer

private const val BUFFER_SIZE = 8192
private const val D_QUOTE = '"'
private const val QUOTE = D_QUOTE
private const val ESCAPE_CHAR = '\\'
private const val NL_CHAR = '\n'

data class CsvRow(val cells: List<String>, val header: List<String>? = null)

// TODO parseCsv() is strict w.r.t. whitespace after comma, or before comma.
fun parseCsv(filePath: String, hasHeader: Boolean): Flow<CsvRow> = flow {
    FileInputStream(filePath).channel.use { fis ->
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        val currentCell = StringBuilder()
        val cells = mutableListOf<String>()
        var inQuote = false
        var escaped = false
        var currentCsvRow = 0
        var header: List<String>? = null
        var errorMessage: String? = null

        while (fis.read(buffer) != -1) {
            buffer.flip() // Prepare buffer for reading
            while (buffer.hasRemaining()) {
                val char = buffer.get().toInt().toChar()
                when {
                    escaped -> {
                        currentCell.append(char) // Literal character after escape
                        escaped = false
                    }

                    char == ESCAPE_CHAR -> {
                        escaped = true
                    }

                    char == QUOTE -> {
                        if (inQuote
                            && currentCell.isNotEmpty()
                            && buffer.hasRemaining()
                            && buffer.get(buffer.position()).toInt().toChar() == QUOTE
                        ) { // Escaped quote within quoted cell
                            currentCell.append(QUOTE)
                            buffer.position(buffer.position() + 1) // Skip the extra quote
                        } else {
                            inQuote = !inQuote
                        }
                    }

                    char == NL_CHAR && !inQuote -> {
                        currentCsvRow++
                        cells.add(currentCell.toString())
                        if (currentCsvRow == 1 && hasHeader) {
                            header = cells.toList()
                            emit(CsvRow(emptyList(), header))
                        } else {
                            if (hasHeader && (header != null) && (header.size < cells.size)) {
                                errorMessage = "Row $currentCsvRow column count ${cells.size} " +
                                    "is greater than header column count ${header.size}"
                            }
                            emit(CsvRow(cells.toList(), header))
                        }
                        currentCell.clear()
                        cells.clear()
                    }

                    char == ',' && !inQuote -> {
                        cells.add(currentCell.toString())
                        currentCell.clear()
                    }

                    else -> {
                        currentCell.append(char)
                    }
                }
            }

            buffer.clear() // Prepare buffer for next read

            // Handle last row if no newline at end of file
            if (currentCell.isNotEmpty() || cells.isNotEmpty()) {
                currentCsvRow++
                cells.add(currentCell.toString())
                if (hasHeader && currentCsvRow == 1) {
                    header = cells.toList()
                    emit(CsvRow(emptyList(), header))
                } else {
                    if (hasHeader && header != null && header.size != cells.size) {
                        errorMessage = "Row $currentCsvRow column count does not match header column count," +
                            " header size is ${header.size}, row size is ${cells.size}"
                    }
                    emit(CsvRow(cells.toList(), header))
                }
            }
            if (errorMessage != null) {
                throw IOException(errorMessage)
            }
        }
    }
}.flowOn(Dispatchers.IO)


suspend fun main() {
    // TODO cycle over /home/mz0/e/shsha/src/functionalTest/testResources/parseTest/*/expected_data/*.csv
    val filePath: Path
    val resourcesDir = "src/test/resources/org/example"
    var testResources: Path
    if (Files.isDirectory(Paths.get("app"))) { // in IDE
        testResources = Paths.get("app", resourcesDir)
        filePath = testResources.resolve("dsg_user_event_info.csv")
    } else if (Files.isDirectory(Paths.get("../app"))) { // standalone Gradle
        filePath = Paths.get(resourcesDir, "dsg_user_event_info.csv")
    } else {
        throw IllegalStateException("unexpected working directory: " + Paths.get("").toAbsolutePath())
    }
    parseCsv(filePath.toString(), true).collect { row ->
        println(row)
    }
}
