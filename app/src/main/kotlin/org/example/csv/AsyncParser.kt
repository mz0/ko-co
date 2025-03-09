package org.example.csv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.FileInputStream
import java.nio.ByteBuffer

private const val BUFFER_SIZE = 8192
private const val D_QUOTE = '"'
private const val QUOTE = D_QUOTE
private const val ESCAPE_CHAR = '\\'
private const val NL_CHAR = '\n'

data class CsvRow(val cells: List<String>, val header: List<String>? = null)

fun parseCsv(filePath: String, hasHeader: Boolean, bufferSize: Int = BUFFER_SIZE): Flow<CsvRow> = flow {
    FileInputStream(filePath).channel.use { fis ->
        val buffer = ByteBuffer.allocate(bufferSize)
        val currentCell = StringBuilder()
        val cells = mutableListOf<String>()
        var inQuote = false
        var escaped = false
        var currentCsvRow = 0
        var header: List<String>? = null
        var lastCharWasCR = false // Track CR for CR LF newlines

        while (fis.read(buffer) != -1) {
            buffer.flip() // Prepare buffer for scanning
            while (buffer.hasRemaining()) {
                val char = buffer.get().toInt().toChar()
                if (lastCharWasCR && char == '\n'){
                     lastCharWasCR = false
                     continue // Skip the newline character
                }
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
                            && buffer.hasRemaining()
                            && buffer.get(buffer.position()).toInt().toChar() == QUOTE
                        ) { // Escaped quote within quoted cell
                            currentCell.append(QUOTE)
                            buffer.position(buffer.position() + 1) // Skip the extra quote
                        } else {
                            inQuote = !inQuote
                        }
                    }

                    (char == NL_CHAR && !inQuote) || (char == '\r' && !inQuote) -> {
                        lastCharWasCR = char == '\r'
                        currentCsvRow++
                        cells.add(currentCell.toString().trim())
                        if (currentCsvRow == 1 && hasHeader) {
                            header = cells.toList()
                            emit(CsvRow(emptyList(), header))
                        } else {
                            emit(CsvRow(cells.toList(), header))
                        }
                        currentCell.clear()
                        cells.clear()
                    }

                    char == ',' && !inQuote -> {
                        cells.add(currentCell.toString().trim())
                        currentCell.clear()
                    }

                    else -> {
                        currentCell.append(char)
                    }
                }
            }

            buffer.clear() // Prepare buffer for next read
        }
        // Handle last row if no newline at end of file
        if (currentCell.isNotEmpty() || cells.isNotEmpty()) {
            currentCsvRow++
            cells.add(currentCell.toString().trim())
            if (hasHeader && currentCsvRow == 1) {
                header = cells.toList()
                emit(CsvRow(emptyList(), header))
            } else {
                emit(CsvRow(cells.toList(), header))
            }
        }
    }
}.flowOn(Dispatchers.IO)
