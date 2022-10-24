package io.github.seggan.rol

import io.github.seggan.rol.meta.FileUnit
import java.nio.file.Path

class DependencyManager(files: List<Path>) {

    private val loadedDependencies by lazy {
        files.mapNotNull { FileUnit.parse(-1, it) }
    }
    val usedDependencies = mutableSetOf<FileUnit>()

    fun getPackage(pkg: String): List<FileUnit> {
        val loaded = loadedDependencies.filter { it.pkg == pkg }
        if (loaded.isNotEmpty()) {
            return loaded
        }
        return emptyList()
    }
}