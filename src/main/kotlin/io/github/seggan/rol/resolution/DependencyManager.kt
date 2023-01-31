package io.github.seggan.rol.resolution

import io.github.seggan.rol.meta.CompilationUnit
import io.github.seggan.rol.meta.FileUnit
import java.nio.file.Path

class DependencyManager(files: List<Path>, explicit: Map<String, Set<String>>) {

    private val loadedDependencies by lazy {
        files.mapNotNull { FileUnit.parse(-1, it) }
    }
    val usedDependencies = mutableSetOf<FileUnit>()

    private val explicitlyImported = buildMap {
        for (import in explicit) {
            val pkg = import.key
            val objs = import.value
            val pkgUnits = getPackage(pkg)
            if (objs.isEmpty()) {
                // import all
                putAll(pkgUnits.associateWith { it.variables + it.functions + it.classes + it.interfaces })
            } else {
                // import specific
                for (obj in objs) {
                    for (unit in pkgUnits) {
                        put(unit, unit.findSubunits(obj).toSet())
                    }
                }
            }
        }
    }

    fun getPackage(pkg: String): List<FileUnit> {
        return loadedDependencies.filter { it.pkg == pkg }
    }

    fun getExplicitlyImported(obj: String): Map<CompilationUnit<*>, FileUnit> {
        return buildMap {
            for (imported in explicitlyImported) {
                for (unit in imported.value) {
                    if (unit.simpleName == obj) {
                        put(unit, imported.key)
                    }
                }
            }
        }
    }
}