plugins {
    id("java")
    id("io.freefair.lombok") version "8.12.2"
    id("com.gradleup.shadow") version "9.0.0-beta9"
}

group = "me.millesant"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies.compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
}
