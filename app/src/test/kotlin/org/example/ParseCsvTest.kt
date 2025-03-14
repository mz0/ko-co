package org.example

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.example.csv.CsvRow
import org.example.csv.parseCsv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class ParseCsvTest {
    @Test
    fun `parseCsv should parse a simple CSV file with header`() = runTest {
        val csvContent = """
            name,age,city
            Alice,30,New York
            Bob,25,Los Angeles
            Charlie,35,Chicago
        """.trimIndent()
        val expectedHeader = listOf("name", "age", "city")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice", "30", "New York"), expectedHeader),
            CsvRow(listOf("Bob", "25", "Los Angeles"), expectedHeader),
            CsvRow(listOf("Charlie", "35", "Chicago"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv does not check row length`() = runTest {
        val csvContent = """
            name,age,city
            Alice, 30, New York
            Bob, Single, 25,Los Angeles
            Charlie,35
            Charlie,35,
        """.trimIndent()
        val expectedHeader = listOf("name", "age", "city")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice", "30", "New York"), expectedHeader),
            CsvRow(listOf("Bob", "Single", "25", "Los Angeles"), expectedHeader),
            CsvRow(listOf("Charlie", "35"), expectedHeader),
            CsvRow(listOf("Charlie", "35", ""), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `header length greater than buffer size 8192 is OK`() = runTest {
        val desc8K = "description".repeat(1024)
        val csvContent = "foo, name, $desc8K\n"
        val expectedHeader = listOf("foo", "name", desc8K)
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList() // bufferSize = 4
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle multibyte UTF-8 characters`() = runTest {
        val csvContent = """
            name,description
            José,"This is a description with an accented e: é."
            "Bjørn","Another UTF-8 character: ø"
            "中文","你好世界"
        """.trimIndent()
        val expectedHeader = listOf("name", "description")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("José", "This is a description with an accented e: é."), expectedHeader),
            CsvRow(listOf("Bjørn", "Another UTF-8 character: ø"), expectedHeader),
            CsvRow(listOf("中文", "你好世界"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle single-byte non-ASCII characters`() = runTest {
        val csvContent = """
            name,description
            José,  This is a description with an accented e: é
            Bjørn, Another ISO-8859-1 character: ø
        """.trimIndent()
        val expectedHeader = listOf("name", "description")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("José", "This is a description with an accented e: é"), expectedHeader),
            CsvRow(listOf("Bjørn", "Another ISO-8859-1 character: ø"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent, StandardCharsets.ISO_8859_1)
        val actualRows = parseCsv(csvFilePath, true, charset = StandardCharsets.ISO_8859_1).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should parse a CSV file, having only header line`() = runTest {
        val csvContent = "name,age (years),city\n"
        val expectedHeader = listOf("name", "age (years)", "city")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
        )

        var csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
        val csvContentNoNewline = "name,age (years),city"
        val actualRowsNNL = parseCsv(createTempCsvFile(csvContentNoNewline), true).toList()
        assertThat(actualRowsNNL).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv ignores whitespace after comma, or before comma`() = runTest {
        val csvContent = """
            Alice,30 ,New York
            Bob,  253,Los Angeles
            Bob, x y , Los Angeles
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("Alice", "30", "New York")),
            CsvRow(listOf("Bob", "253", "Los Angeles")),
            CsvRow(listOf("Bob", "x y", "Los Angeles")),
            )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, false).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle empty cells`() = runTest {
        val csvContent = """
            Bob,, Los Angeles
            Bob, ,Los Angeles
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("Bob", "", "Los Angeles")), // empty string
            CsvRow(listOf("Bob", "", "Los Angeles")),  // also an empty string (whitespace is trimmed)
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, false).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle quoted cells`() = runTest {
        val csvContent = """
            name,age,city
            "Alice, Smith",30,"New York"
            Bob,25,"Los Angeles, CA"
        """.trimIndent()
        val expectedHeader = listOf("name", "age", "city")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice, Smith", "30", "New York"), expectedHeader),
            CsvRow(listOf("Bob", "25", "Los Angeles, CA"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle escaped quotes within quoted cells`() = runTest {
        val csvContent = """
            name,description
            "Alice","She said ""Hello""."
            Bob,"Another ""quoted"" description"
        """.trimIndent()
        val expectedHeader = listOf("name", "description")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice", "She said \"Hello\"."), expectedHeader),
            CsvRow(listOf("Bob", "Another \"quoted\" description"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle escape char`() = runTest {
        val csvContent = """
            Alice,"back\\slash"
            Bob,"back\\slash \\""
        """.trimIndent()
        val expectedRows = listOf(
            CsvRow(listOf("Alice", "back\\slash")),
            CsvRow(listOf("Bob", "back\\slash \\\"")),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, false).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle empty files`() = runTest {
        val csvContent = ""
        val expectedRows = emptyList<CsvRow>()

        val csvFilePath = createTempCsvFile(csvContent)
        var actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)

        actualRows = parseCsv(csvFilePath, false).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle files with no newline in the last line`() = runTest {
        val csvContent = "name,age\nAlice,30"
        val expectedHeader = listOf("name", "age")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice", "30"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
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

        actualRows = parseCsv(createTempCsvFile(csv2rows), false).toList()
        assertThat(actualRows).isEqualTo(expect2rows)

        actualRows = parseCsv(createTempCsvFile(csv1row), false).toList()
        assertThat(actualRows).isEqualTo(expect1row)
    }

    @Test
    fun `parseCsv should handle CRLF line endings`() = runTest {
        val csvContent = "name,age\r\nAlice,30\r\nBob,25\r\n"
        val expectedHeader = listOf("name", "age")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice", "30"), expectedHeader),
            CsvRow(listOf("Bob", "25"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @Test
    fun `parseCsv should handle CR line endings`() = runTest {
        val csvContent = "name,age\rAlice,30\rBob,25\r"
        val expectedHeader = listOf("name", "age")
        val expectedRows = listOf(
            CsvRow(emptyList(), expectedHeader),
            CsvRow(listOf("Alice", "30"), expectedHeader),
            CsvRow(listOf("Bob", "25"), expectedHeader),
        )

        val csvFilePath = createTempCsvFile(csvContent)
        val actualRows = parseCsv(csvFilePath, true).toList()
        assertThat(actualRows).isEqualTo(expectedRows)
    }

    @TempDir
    lateinit var tempDir: Path

    private fun createTempCsvFile(csvContent: String, charset: Charset = StandardCharsets.UTF_8): String {
        val tempFile = File(tempDir.toFile(), "tmp.csv")
        FileWriter(tempFile, charset).use { writer ->
            writer.write(csvContent)
        }
        return tempFile.absolutePath
    }
}
