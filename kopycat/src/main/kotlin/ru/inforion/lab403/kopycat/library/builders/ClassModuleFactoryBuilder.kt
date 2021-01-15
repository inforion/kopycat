/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.library.builders

import com.fasterxml.jackson.module.kotlin.isKotlinClass
import ru.inforion.lab403.common.extensions.DynamicClassLoader
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.FINEST
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.stringify
import ru.inforion.lab403.kopycat.annotations.DontExportModule
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import java.io.File
import java.lang.IllegalArgumentException
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaType

class ClassModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    companion object {
        @Transient val log = logger(INFO)

        private fun getFileFromClass(klass: Class<*>): String {
            val path = File(klass.protectionDomain.codeSource.location.path)
            val name = klass.canonicalName.replace(".", "/") + ".class"
            return File(path, name).path
        }
    }

    constructor(klass: Class<out Module>) : this(getFileFromClass(klass), null) {
        moduleJavaClass = klass
    }

    private lateinit var constructors: List<KFunction<Module>>
    private lateinit var moduleJavaClass: Class<out Module>

    private fun checkOnSupportedKotlinClass(klass: Class<*>): Boolean {
        if (!klass.isKotlinClass()) {
            log.finer { "Can't load module -> class ${klass.simpleName} isn't Kotlin class (Java currently not supported)" }
            return false
        }

        val kotlinClass = klass.kotlin

        val isModule = try {
            kotlinClass.isSubclassOf(Module::class)
        } catch (exc: UnsupportedOperationException) {
            log.finest { "Can't load module -> class ${klass.simpleName} is anonymous class" }
            return false
        }

        if (!isModule) {
            log.finest { "Can't load module -> class ${klass.simpleName} isn't Module class" }
            return false
        }

        if (kotlinClass.isAbstract) {
            log.finest { "Can't load module -> class ${klass.simpleName} is abstract" }
            return false
        }

        if (kotlinClass.findAnnotation<DontExportModule>() != null) {
            log.finer { "Can't load module -> class ${klass.simpleName} marked as DontExportModule" }
            return false
        }

        return true
    }

    private fun loadModuleFromClass(klass: Class<*>): List<KFunction<Module>> {
        if (!checkOnSupportedKotlinClass(klass))
            return emptyList()

        @Suppress("UNCHECKED_CAST")
        moduleJavaClass = klass as Class<out Module>

        log.fine { "Loading ${klass.simpleName} from $path from jar $jar" }

        return moduleJavaClass.kotlin.constructors.filter {
            it.parameters.size >= 2 &&
                    it.parameters[0].type.javaType == Module::class.java &&
                    it.parameters[1].type.javaType == String::class.java
        }
    }

    private fun loadModuleFromFile(path: String, jar: File?): List<KFunction<Module>> {
        // If jar not null then we should patch path
        val fullname = if (jar != null)
            path.substringBeforeLast(".")
                    .replace("/", ".")
        else path

        if (File(path).extension != "class")
            return emptyList()

        val klass = try {
            // Classpath loaded into system classpath in JAR loader in preload
            DynamicClassLoader.loadClass(fullname)
        } catch (error: ClassNotFoundException) {
            log.warning { "Class not found: $fullname" }
            return emptyList()
        } catch (error: NoClassDefFoundError) {
            log.severe { "Dependent class ${error.message} definition not found for $fullname" }
            return emptyList()
        }

        return loadModuleFromClass(klass)
    }

    override val plugins get() = setOf(moduleJavaClass.simpleName)

    override fun load(): Boolean {
        constructors = if (!::moduleJavaClass.isInitialized) {
            loadModuleFromFile(path, jar)
        } else {
            loadModuleFromClass(moduleJavaClass)
        }
        return constructors.isNotEmpty()
    }

    private fun getTypename(type: KType): String = type.javaType.typeName.substringAfterLast(".")

    override fun factory(name: String, registry: ModuleLibraryRegistry): List<IModuleFactory> = constructors.map { constructor ->
        object : IModuleFactory {
            override val canBeTop = with(constructor.parameters.first()) {
                type.javaType == Module::class.java && type.isMarkedNullable
            }

            override val parameters = constructor.parameters
                    .filter { it.index >= 2 }
                    .map { ModuleParameterInfo(it.index, it.name!!, getTypename(it.type), it.isOptional) }

            private fun arguments(
                    parent: Component?,
                    name: String,
                    parameters: Map<String, Any?>
            ) = with (constructor.parameters) {
                val kParameterParent = singleOrNull { it.name == "parent" }
                        .sure { "Mandatory argument 'parent' not found in constructor $constructor" }

                val kParameterName = singleOrNull { it.name == "name" }
                        .sure { "Mandatory argument 'name' not found in constructor $constructor" }

                val mandatoryArguments = mapOf(kParameterParent to parent, kParameterName to name)

                val optionalArguments = parameters.map { (name, value) ->
                    val kParameter = singleOrNull { it.name == name }
                            .sure { "Optional argument '$name' not found in constructor $constructor"}
                    kParameter to value
                }.toMap()

                mandatoryArguments + optionalArguments
            }.also {
                log.finest {
                    val stringOfArguments = it
                            .map { (param, value) -> "\t#${param.index} ${param.name}: ${param.type} = $value" }
                            .joinToString("\n")
                    "\n${constructor.stringify()} -> \n$stringOfArguments"
                }
            }

            override fun create(parent: Component?, name: String, parameters: Map<String, Any?>) =
                    constructor.callBy(arguments(parent, name, parameters))
        }
    }
}