import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.nio.file.Path as JPath

plugins {
    kotlin("jvm") version "1.7.10"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("antlr")
}

group = "io.github.seggan"
version = "1.0-SNAPSHOT"

val jarName = "Rol-$version.jar"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.11.1")

    implementation("com.beust:klaxon:5.6")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.github.seggan.rol.MainKt")
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

tasks.shadowJar {
    archiveFileName.set(jarName)
}

tasks.register("compileStdlib") {
    dependsOn(tasks.shadowJar)
    doLast {
        val compiler = JPath.of("$buildDir/libs/$jarName")
        val stdlib = JPath.of("$projectDir/src/main/rol")
        Files.walk(stdlib).forEach { file ->
            if (file.fileName.toString().endsWith(".rol")) {
                javaexec {
                    classpath = files(compiler)
                    args = listOf("-f", file.toAbsolutePath().toString())
                }
            }
        }
    }
}