import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
    implementation("org.luaj:luaj-jse:3.0.1")

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
    val path = File("$buildDir/generated-src/")
    val fullPath = path.resolve("antlr/main/io/github/seggan/rol/antlr/")
    doFirst {
        fullPath.mkdirs()
    }
    arguments = arguments + listOf(
        "-lib", fullPath.absoluteFile.toString(),
        "-visitor",
        "-no-listener",
        "-encoding", "UTF-8",
        "-package", "io.github.seggan.rol.antlr"
    )
    outputDirectory = fullPath
}

tasks.shadowJar {
    archiveFileName.set(jarName)
}

tasks.register("compileStdlib") {
    dependsOn(tasks.shadowJar)
    doLast {
        val compiler = File("$buildDir/libs/$jarName")
        val stdlib = File("$projectDir/src/main/rol")
        val dest = File("$projectDir/src/main/resources/stdlib")
        val list = mutableListOf<String>()
        dest.deleteRecursively()
        dest.mkdirs()
        stdlib.walk().filter(File::isFile).forEach { file ->
            if (file.extension == "rol") {
                val name = file.nameWithoutExtension
                list += name
                javaexec {
                    classpath = files(compiler)
                    args = listOf(file.absoluteFile.toString(), "-o", dest.resolve("$name.lua").absoluteFile.toString())
                }
            }
        }
        dest.resolve("list.txt").writeText(list.joinToString("\n"))
    }
}