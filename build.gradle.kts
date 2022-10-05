import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path as JPath
import java.nio.file.Files

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("antlr")
}

group = "io.github.seggan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.11.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.compileJava {
    dependsOn(tasks.generateGrammarSource)
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

tasks.generateGrammarSource {
    maxHeapSize = "128m"
    val path = JPath.of("$buildDir/generated-src/")
    val fullPath = path.resolve("antlr/main/io/github/seggan/rol/antlr/")
    doFirst {
        Files.createDirectories(fullPath)
    }
    arguments = arguments + listOf(
        "-lib", fullPath.toAbsolutePath().toString(),
        "-visitor",
        "-no-listener",
        "-encoding", "UTF-8",
        "-package", "io.github.seggan.rol.antlr"
    )
    outputDirectory = fullPath.toFile()
}