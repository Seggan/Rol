package io.github.seggan.rol.resolution

import io.github.seggan.rol.Errors
import io.github.seggan.rol.meta.FileUnit
import io.github.seggan.rol.meta.StructUnit
import io.github.seggan.rol.meta.TraitUnit
import io.github.seggan.rol.meta.VariableUnit
import io.github.seggan.rol.tree.common.ConcreteType
import io.github.seggan.rol.tree.common.FunctionType
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.ResolvedType
import io.github.seggan.rol.tree.common.TraitType
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.UnresolvedType
import io.github.seggan.rol.tree.common.toType

class TypeResolver(private val manager: DependencyManager, val pkg: String, private val imports: Set<String>) {

    private val resolvedTypes = mutableMapOf<Identifier, ResolvedType>()

    private val variables = mutableMapOf<Identifier, Type>()

    val mangledVariables = mutableMapOf<Identifier, String>()

    fun resolveType(type: Type, location: Location): Type {
        checkPackage(type.name.pkg, location)
        val (result, units) = resolveTypeInternal(type, location)
        for (unit in units) {
            manager.usedDependencies.add(unit)
        }
        return result
    }

    private fun resolveTypeInternal(type: Type, location: Location): Pair<Type, Set<FileUnit>> {
        return when (type) {
            is UnresolvedType -> locateType(type, location).let { (r, u) -> r to setOfNotNull(u) }
            is FunctionType -> {
                val (returnType, unit) = resolveTypeInternal(type.returnType, location)
                val (paramTypes, units) = type.args.map { resolveTypeInternal(it, location) }.unzip()
                FunctionType(paramTypes, returnType) to (unit + units.flatten())
            }
            is ConcreteType -> {
                val fieldMap = type.fields.mapValues { resolveTypeInternal(it.value, location) }
                val (traits, units) = type.traits.map { resolveTypeInternal(it, location) }.unzip()
                ConcreteType(
                    type.name,
                    fieldMap.mapValues { it.value.first },
                    traits.filterIsInstance<TraitType>(),
                    type.nullable,
                ) to (fieldMap.flatMapTo(mutableSetOf()) { it.value.second } + units.flatten())
            }
            is TraitType -> {
                val (extends, units) = type.extends.map { resolveTypeInternal(it, location) }.unzip()
                TraitType(
                    type.name,
                    type.nullable,
                    extends.filterIsInstance<TraitType>()
                ) to units.flatten().toSet()
            }
            else -> type to setOf()
        }
    }

    private fun locateType(type: UnresolvedType, location: Location): Pair<ResolvedType, FileUnit?> {
        val t = type.nonNullable()
        if (t.name.pkg != null) {
            if (resolvedTypes.containsKey(t.name)) {
                return resolvedTypes[t.name]!!.withNullability(type.nullable) as ResolvedType to null
            }
            for (unit in manager.getPackage(t.name.pkg)) {
                val clazz = unit.findClass(t.name.name)
                if (clazz is StructUnit) {
                    return resolveStruct(clazz, location).withNullability(type.nullable) to unit
                } else if (clazz is TraitUnit) {
                    return resolveTrait(clazz, location).withNullability(type.nullable) to unit
                }
            }
        } else {
            for ((unit, pkg) in manager.getExplicitlyImported(t.name.name)) {
                if (unit is StructUnit) {
                    val resolved = resolveStruct(unit, location)
                    return ConcreteType(
                        t.name.copy(pkg = pkg.pkg),
                        resolved.fields,
                        resolved.traits,
                        type.nullable
                    ) to pkg
                } else if (unit is TraitUnit) {
                    val resolved = resolveTrait(unit, location)
                    return TraitType(
                        t.name.copy(pkg = pkg.pkg),
                        type.nullable,
                        resolved.extends
                    ) to pkg
                }
            }
        }
        Errors.undefinedReference(t.name.toString(), location)
    }

    private fun resolveStruct(unit: StructUnit, errorLocation: Location): ConcreteType {
        return ConcreteType(
            unit.name,
            unit.fields.mapValues { resolveTypeInternal(it.value, errorLocation).first },
            listOf(),
            false
        ).also { resolvedTypes[unit.name] = it }
    }

    private fun resolveTrait(unit: TraitUnit, errorLocation: Location): TraitType {
        val superTypes = unit.superTraits.map { resolveTypeInternal(it.toType(), errorLocation).first }
        for (superType in superTypes) {
            if (superType !is TraitType) {
                Errors.typeError(
                    "Cannot extend trait ${superType.name}",
                    errorLocation
                )
            }
        }
        return TraitType(
            unit.name,
            false,
            extends = superTypes.map { it as TraitType }
        ).also { resolvedTypes[unit.name] = it }
    }

    fun resolveVariable(name: Identifier, location: Location): Pair<Identifier, Type> {
        checkPackage(name.pkg, location)
        if (name in variables) {
            return name to variables[name]!!
        }
        if (name.pkg == null) {
            val possibleLocal = name.copy(pkg = pkg)
            if (possibleLocal in variables) {
                return possibleLocal to variables[possibleLocal]!!
            }
            for ((unit, pkg) in manager.getExplicitlyImported(name.name)) {
                if (unit is VariableUnit) {
                    manager.usedDependencies.add(pkg)
                    val fname = name.copy(pkg = pkg.pkg)
                    mangledVariables[fname] = unit.mangled
                    return fname to addVariable(
                        fname,
                        resolveTypeInternal(unit.type, location).first,
                        location
                    )
                }
            }
        } else {
            for (unit in manager.getPackage(name.pkg)) {
                val variable = unit.findVariable(name.name)
                if (variable != null) {
                    manager.usedDependencies.add(unit)
                    mangledVariables[name] = variable.mangled
                    return name to addVariable(
                        name,
                        resolveTypeInternal(variable.type, location).first,
                        location
                    )
                }
            }
        }
        Errors.undefinedReference(name.toString(), location)
    }

    fun addVariable(name: Identifier, type: Type, location: Location): Type {
        checkPackage(name.pkg, location)
        variables[name] = type
        return type
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