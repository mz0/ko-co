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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.ByteBuffer

private const val BUFFER_SIZE = 8192
private const val D_QUOTE = '"'
private const val QUOTE = D_QUOTE
private const val ESCAPE_CHAR = '\\'
private const val NL_CHAR = '\n'

data class CsvRow(val cells: List<String>)

fun parseCsv(filePath: String): Flow<CsvRow> = flow {
        FileInputStream(filePath).channel.use { fis ->
            val buffer: ByteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
            val currentCell = StringBuilder()
            val cells = mutableListOf<String>()
            var inQuote = false
            var escaped = false

            while (fis.read(buffer) != -1) {
                buffer.flip() // Prepare buffer for reading

                while (buffer.hasRemaining()) {
                    val char = buffer.get().toInt().toChar()

                    if (escaped) {
                        currentCell.append(char) // Literal character after escape
                        escaped = false
                    } else if (char == ESCAPE_CHAR) {
                        escaped = true
                    } else if (char == QUOTE) {
                        if (inQuote
                            && currentCell.isNotEmpty()
                            && buffer.hasRemaining()
                            && buffer.get(buffer.position()).toInt().toChar() == QUOTE) {
                            // Escaped quote within quoted cell
                            currentCell.append(QUOTE)
                            buffer.position(buffer.position() + 1) // Skip the extra quote
                        } else {
                            inQuote = !inQuote
                        }
                    } else if (char == NL_CHAR && !inQuote) {
                        cells.add(currentCell.toString())
                        emit(CsvRow(cells.toList())) // Emit the row
                        currentCell.clear()
                        cells.clear()
                    } else if (char == ',' && !inQuote) {
                        cells.add(currentCell.toString())
                        currentCell.clear()
                    } else {
                        currentCell.append(char)
                    }
                }
                buffer.clear() // Prepare buffer for next read
            }

            // Handle last row if no newline at end of file
            if (currentCell.isNotEmpty() || cells.isNotEmpty()) {
                cells.add(currentCell.toString())
                emit(CsvRow(cells.toList()))
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
    parseCsv(filePath.toString()).collect { row ->
        println(row) // Or process the row as needed
    }
}
