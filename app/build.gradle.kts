plugins {
    id("buildlogic.java-application-conventions")
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect") // log4j in Kotlin needs it, but states this fact
    implementation(project(":libs"))
}

application {
    mainClass = "org.example.AppKt"
}
