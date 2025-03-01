plugins {
    id("buildlogic.java-common-conventions")
    application
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect") // log4j in Kotlin needs it, but states this fact
    implementation(project(":libs"))

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.10.1")
}

application {
    mainClass = "org.example.AppKt"
}
