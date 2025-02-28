import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    id("org.jetbrains.kotlin.jvm") // set version in buildSrc/build.gradle.kts
}

repositories {
    mavenCentral()
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = targetCompatibility // javac will deny higher version
}

kotlin.compilerOptions.jvmTarget.set(JvmTarget.fromTarget(java.targetCompatibility.majorVersion))

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.24.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
