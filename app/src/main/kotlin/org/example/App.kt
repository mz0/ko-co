/*
 SPDX-FileCopyrightText: 2025 Mark Zhitomirski
 SPDX-License-Identifier: Apache-2.0
 */
package org.example

import org.example.csv.parseCsv
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


suspend fun main() {
    // TODO cycle over /home/mz0/e/shsha/src/functionalTest/testResources/parseTest/*/expected_data/*.csv
    val filePath: Path
    val resourcesDir = "src/test/resources/org/example"
    val fileName = "dsg_user_event_info.csv"
    var testResources: Path
    if (Files.isDirectory(Paths.get("app"))) { // in IDE
        testResources = Paths.get("app", resourcesDir)
        filePath = testResources.resolve(fileName)
    } else if (Files.isDirectory(Paths.get("../app"))) { // standalone Gradle
        filePath = Paths.get(resourcesDir, fileName)
    } else {
        throw IllegalStateException("unexpected working directory: " + Paths.get("").toAbsolutePath())
    }
    parseCsv(filePath.toString(), true).collect { row ->
        println(row)
    }
}
