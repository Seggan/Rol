package io.github.seggan.rol

import io.github.seggan.rol.meta.FileUnit
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

class DependencyManager(files: List<Path>) {

    private val loadedDependencies by lazy {
        val sorted = files.sortedBy { it.extension == "rol" }.filterNot {
            it.extension == "rol" && it.resolveSibling("${it.nameWithoutExtension}.lua").exists()
        }
        val unloadedDependencies = ArrayDeque(sorted)
        val loaded = mutableListOf<FileUnit>()
        while (unloadedDependencies.isNotEmpty()) {
            val file = unloadedDependencies.removeFirst()
            val unit = getCompilationUnit(file)
            if (unit != null) {
                loaded.add(unit)
            }
        }
        loaded
    }
    val usedDependencies = mutableListOf<FileUnit>()

    fun getPackage(pkg: String): List<FileUnit> {
        val loaded = loadedDependencies.filter { it.pkg == pkg }
        if (loaded.isNotEmpty()) {
            return loaded
        }
        return emptyList()
    }
}

fun getCompilationUnit(path: Path): FileUnit? {
    return when (path.extension) {
        "rol" -> compile(path)
        "lua" -> FileUnit.parse(-1, path)
        else -> null
    }
}