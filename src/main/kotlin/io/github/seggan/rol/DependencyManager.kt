package io.github.seggan.rol

import io.github.seggan.rol.meta.FileUnit
import java.nio.file.Path
import kotlin.io.path.extension

class DependencyManager(files: List<Path>) {

    private val unloadedDependencies = ArrayDeque(files)
    private val loadedDependencies = mutableListOf<FileUnit>()

    fun getPackage(pkg: String): FileUnit? {
        val alreadyLoaded = loadedDependencies.find { it.pkg == pkg }
        if (alreadyLoaded != null) {
            return alreadyLoaded
        }
        while (unloadedDependencies.isNotEmpty()) {
            val file = unloadedDependencies.removeFirst()
            val unit = getCompilationUnit(file)
            if (unit != null) {
                loadedDependencies.add(unit)
                if (unit.pkg == pkg) {
                    return unit
                }
            }
        }
        return null
    }
}

fun getCompilationUnit(path: Path): FileUnit? {
    return when (path.extension) {
        "rol" -> compile(path)
        "lua" -> FileUnit.parse(-1, path)
        else -> null
    }
}