package org.example

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileWriter
import java.nio.file.Path

class ParseCsvTest {
    @Test
    fun `parseCsv should parse a simple CSV file`() = runTest {
        val csvContent = """
            name,age,city
            Alice,30,New York
            Bob,25,Los Angeles
            Charlie,35,Chicago
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("name", "age", "city")),
            CsvRow(listOf("Alice", "30", "New York")),
            CsvRow(listOf("Bob", "25", "Los Angeles")),
            CsvRow(listOf("Charlie", "35", "Chicago")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv is strict WRT whitespace after comma, or before comma`() = runTest {
        val csvContent = """
            Alice,30 ,New York
            Bob, 25,Los Angeles
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("Alice", "30 ", "New York")),
            CsvRow(listOf("Bob", " 25", "Los Angeles")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle empty cells`() = runTest {
        val csvContent = """
            Bob,, Los Angeles
            Bob, ,Los Angeles
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("Bob", "", " Los Angeles")),
            CsvRow(listOf("Bob", " ", "Los Angeles")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle quoted cells`() = runTest {
        val csvContent = """
            name,age,city
            "Alice, Smith",30,"New York"
            Bob,25,"Los Angeles, CA"
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("name", "age", "city")),
            CsvRow(listOf("Alice, Smith", "30", "New York")),
            CsvRow(listOf("Bob", "25", "Los Angeles, CA")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle escaped quotes within quoted cells`() = runTest {
        val csvContent = """
            name,description
            "Alice","She said ""Hello""."
            Bob,"Another ""quoted"" description"
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("name", "description")),
            CsvRow(listOf("Alice", "She said \"Hello\".")),
            CsvRow(listOf("Bob", "Another \"quoted\" description")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle escape char`() = runTest {
        val csvContent = """
            name,description
            Alice,"back\\slash"
            Bob,"back\\slash \\""
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("name", "description")),
            CsvRow(listOf("Alice", "back\\slash")),
            CsvRow(listOf("Bob","back\\slash \\\"")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle empty files`() = runTest {
        val csvContent = ""
        val expectedRows = emptyList<CsvRow>()

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle files with no newline in the last line`() = runTest {
        val csvContent = "name,age\nAlice,30"
        val expectedRows = listOf(
            CsvRow(listOf("name", "age")),
            CsvRow(listOf("Alice", "30")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle multi-line cells`() = runTest {
        val csv2rows = "Alice,30\nBob,25"
        val csv1row = "Alice,\"30\nBob\",25"
        val expect2rows = listOf(
            CsvRow(listOf("Alice", "30")),
            CsvRow(listOf("Bob", "25")),
        )
        val expect1row = listOf(
            CsvRow(listOf("Alice", "30\nBob", "25")),
        )

        var actualRows: List<CsvRow>

        actualRows = parseCsv(createTempCsvFile(csv2rows)).toList()
        assertThat(actualRows).isEqualTo(expect2rows)

        actualRows = parseCsv(createTempCsvFile(csv1row)).toList()
        assertThat(actualRows).isEqualTo(expect1row)
    }

    @TempDir
    lateinit var tempDir: Path

    private fun createTempCsvFile(csvContent: String): String {
        val tempFile = File(tempDir.toFile(), "tmp.csv")
        FileWriter(tempFile).use { writer ->
            writer.write(csvContent)
        }
        return tempFile.absolutePath
    }
}
