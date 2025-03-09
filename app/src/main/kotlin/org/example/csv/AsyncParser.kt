package org.example.csv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private const val D_QUOTE = '"'
private const val QUOTE = D_QUOTE
private const val ESCAPE_CHAR = '\\'
private const val NL_CHAR = '\n'
private const val CR_CHAR = '\r'

data class CsvRow(val cells: List<String>, val header: List<String>? = null)

fun parseCsv(filePath: String, hasHeader: Boolean, charset: Charset = StandardCharsets.UTF_8): Flow<CsvRow> = flow {
    FileInputStream(filePath).use { fis ->
        BufferedReader(InputStreamReader(fis, charset)).use { reader ->
            val currentCell = StringBuilder()
            val cells = mutableListOf<String>()
            var inQuote = false
            var escaped = false
            var currentCsvRow = 0
            var header: List<String>? = null
            var charRead: Int
            var lastCharWasCR = false // Track CR for CR LF newlines

            while (reader.read().also { charRead = it } != -1) {
                val char = charRead.toChar()
                if (lastCharWasCR && char == NL_CHAR) {
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
                        if (inQuote && reader.ready()) {
                            reader.mark(1)
                            val nextChar = reader.read()
                            if (nextChar != -1 && nextChar.toChar() == QUOTE) {
                                currentCell.append(QUOTE)
                            } else {
                                reader.reset()
                                inQuote = false
                            }
                        } else {
                            inQuote = !inQuote
                        }
                    }

                    !inQuote && (char == NL_CHAR || char == CR_CHAR) -> {
                        lastCharWasCR = char == CR_CHAR
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

                    !inQuote && char == ',' -> {
                        cells.add(currentCell.toString().trim())
                        currentCell.clear()
                    }

                    else -> {
                        currentCell.append(char)
                    }
                }
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
    }
}.flowOn(Dispatchers.IO)
