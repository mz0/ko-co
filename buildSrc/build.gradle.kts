plugins {
    `kotlin-dsl` // Support convention plugins written in Kotlin.
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
}
