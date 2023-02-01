package io.github.seggan.rol.resolution

import io.github.seggan.rol.Errors
import io.github.seggan.rol.meta.ClassUnit
import io.github.seggan.rol.meta.FileUnit
import io.github.seggan.rol.meta.FunctionUnit
import io.github.seggan.rol.meta.InterfaceUnit
import io.github.seggan.rol.tree.common.ConcreteType
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.InterfaceType
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.ResolvedType
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.UnresolvedType
import io.github.seggan.rol.tree.common.toType

class TypeResolver(private val manager: DependencyManager, private val pkg: String, private val imports: Set<String>) {

    private val resolvedTypes = mutableMapOf<Identifier, ResolvedType>()

    private val localFunctions = mutableSetOf<FunctionHeader>()

    fun registerFunctionHeader(header: FunctionHeader) {
        localFunctions.add(header)
    }

    fun resolveType(type: Type, location: Location): Type {
        checkPackage(type.name.pkg, location)
        val (result, unit) = resolveTypeInternal(type, location)
        if (unit != null) {
            manager.usedDependencies.add(unit)
        }
        return result
    }

    private fun resolveTypeInternal(type: Type, location: Location): Pair<Type, FileUnit?> {
        return if (type is UnresolvedType) locateType(type, location) else type to null
    }

    private fun locateType(type: UnresolvedType, location: Location): Pair<ResolvedType, FileUnit?> {
        val t = type.nonNullable()
        if (t.name.pkg != null) {
            if (resolvedTypes.containsKey(t.name)) {
                return resolvedTypes[t.name]!!.withNullability(type.nullable) as ResolvedType to null
            }
            for (unit in manager.getPackage(t.name.pkg)) {
                val clazz = unit.findClass(t.name.name)
                if (clazz is ClassUnit) {
                    return resolveClass(clazz, location).withNullability(type.nullable) to unit
                } else if (clazz is InterfaceUnit) {
                    return resolveInterface(clazz, location).withNullability(type.nullable) to unit
                }
            }
        } else {
            for ((unit, pkg) in manager.getExplicitlyImported(t.name.name)) {
                if (unit is ClassUnit) {
                    val resolved = resolveClass(unit, location)
                    return ConcreteType(
                        t.name.copy(pkg = pkg.pkg),
                        type.nullable,
                        resolved.superclass
                    ) to pkg
                } else if (unit is InterfaceUnit) {
                    val resolved = resolveInterface(unit, location)
                    return InterfaceType(
                        t.name.copy(pkg = pkg.pkg),
                        type.nullable,
                        resolved.extends
                    ) to pkg
                }
            }
        }
        Errors.undefinedReference(t.name.toString(), location)
    }

    private fun resolveClass(clazz: ClassUnit, errorLocation: Location): ConcreteType {
        val superType = clazz.superClass.toType()
        if (superType is InterfaceType) {
            Errors.typeError(
                "Cannot extend interface ${superType.name}",
                errorLocation
            )
        }
        return ConcreteType(
            clazz.name,
            false,
            superclass = resolveTypeInternal(superType, errorLocation).first as ConcreteType
        ).also { resolvedTypes[clazz.name] = it }
    }

    private fun resolveInterface(clazz: InterfaceUnit, errorLocation: Location): InterfaceType {
        val superTypes = clazz.superInterfaces.map { resolveTypeInternal(it.toType(), errorLocation).first }
        if (superTypes.any { it !is InterfaceType }) {
            Errors.typeError(
                "Cannot extend non-interface ${superTypes.first { it !is InterfaceType }}",
                errorLocation
            )
        }
        return InterfaceType(
            clazz.name,
            false,
            extends = superTypes.map { it as InterfaceType }
        ).also { resolvedTypes[clazz.name] = it }
    }

    fun findFunction(pkg: String?, name: String, args: List<Type>, location: Location): Pair<String?, Type>? {
        checkPackage(pkg, location)
        if (pkg == null || pkg == this.pkg) {
            for (func in localFunctions) {
                if (func.matches(name, args)) {
                    return pkg to func.returnType
                }
            }
        }
        if (pkg == null) {
            for ((unit, pack) in manager.getExplicitlyImported(name)) {
                if (unit is FunctionUnit) {
                    manager.usedDependencies.add(pack)
                    return pack.pkg to resolveTypeInternal(unit.returnType, location).first
                }
            }
        } else {
            for (dep in manager.getPackage(pkg)) {
                val func = dep.findFunction(name, args)
                if (func != null) {
                    manager.usedDependencies.add(dep)
                    return pkg to resolveTypeInternal(func.returnType, location).first
                }
            }
        }
        return null
    }

    private fun checkPackage(pkg: String?, location: Location) {
        if (pkg != null && pkg != this.pkg && pkg !in imports) {
            Errors.genericError(
                "Resolution",
                "Cannot find package $pkg",
                location
            )
        }
    }
}