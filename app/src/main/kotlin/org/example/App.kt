/*
 SPDX-FileCopyrightText: 2025 Mark Zhitomirski
 SPDX-License-Identifier: Apache-2.0
 */
package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.logger
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private const val BUFFER_SIZE = 8192 // Adjust buffer size as needed
private const val D_QUOTE = '"'
private const val S_QUOTE = '\''
private const val QUOTE = D_QUOTE
private const val ESCAPE_CHAR = '\\'
private const val NL_CHAR = '\n'
private const val CR = '\r'

data class CsvRow(val cells: List<String>)

fun parseCsv(filePath: String): Flow<CsvRow> = flow {
    val fileInputStream = FileInputStream(filePath)
    val inputStreamReader = InputStreamReader(fileInputStream)

    val buffer = CharArray(BUFFER_SIZE)
    var currentCell = StringBuilder()
    val cells = mutableListOf<String>()
    var inQuote = false
    var escaped = false
    var charsRead: Int
    var bufferIndex = 0

    try {
        while (inputStreamReader.read(buffer).also { charsRead = it } != -1) {
            bufferIndex = 0
            while (bufferIndex < charsRead) {
                val char = buffer[bufferIndex]

                when {
                    escaped -> {
                        currentCell.append(char)
                        escaped = false
                        bufferIndex++
                    }

                    char == ESCAPE_CHAR -> {
                        escaped = true
                        bufferIndex++
                    }

                    char == QUOTE -> {
                        if (inQuote
                            && currentCell.isNotEmpty()
                            && bufferIndex + 1 < charsRead // Ensure we don't go out of bounds
                            && buffer[bufferIndex + 1] == QUOTE) {
                            currentCell.append(QUOTE)
                            bufferIndex += 2 // Skip the next quote
                        } else {
                            inQuote = !inQuote
                            bufferIndex++
                        }
                    }

                    char == NL_CHAR && !inQuote -> {
                        cells.add(currentCell.toString())
                        emit(CsvRow(cells.toList()))
                        currentCell.clear()
                        cells.clear()
                        bufferIndex++
                    }

                    char == ',' && !inQuote -> {
                        cells.add(currentCell.toString())
                        currentCell.clear()
                        bufferIndex++
                    }

                    else -> {
                        currentCell.append(char)
                        bufferIndex++
                    }
                }
            }
        }

        // Handle last row if no newline at end of file
        if (currentCell.isNotEmpty() || cells.isNotEmpty()) {
            cells.add(currentCell.toString())
            emit(CsvRow(cells.toList()))
        }
    } catch (e: IOException) {
        logger.error("Error reading file: ${e.message}")
    } finally {
        // Ensure resources are closed in the finally block
        try {
            inputStreamReader.close()
        } catch (e: IOException) {
            logger.error("Error closing reader: ${e.message}")
        }
        try {
            fileInputStream.close()
        } catch (e: IOException) {
            logger.error("Error closing stream: ${e.message}")
        }
    }
}.flowOn(Dispatchers.IO)


fun main() = runBlocking {
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
    launch {
        parseCsv(filePath.toString()).collect { row ->
            println(row) // Or process the row as needed
        }
    }
    return@runBlocking
}
