package ru.inforion.lab403.kopycat.library.builders

import com.fasterxml.jackson.module.kotlin.isKotlinClass
import ru.inforion.lab403.common.proposal.DynamicClassLoader
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.annotations.DontExportModule
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import java.io.File
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaType

class ClassModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    companion object {
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

    override fun plugins(): Set<String> = setOf(moduleJavaClass.simpleName)

    override fun load(): Boolean {
        constructors = if (!::moduleJavaClass.isInitialized) {
            loadModuleFromFile(path, jar)
        } else {
            loadModuleFromClass(moduleJavaClass)
        }
        return constructors.isNotEmpty()
    }

    private fun getTypename(type: KType): String = type.javaType.typeName.substringAfterLast(".")

    override fun factory(pluginName: String, registry: ModuleLibraryRegistry): List<IModuleFactory> {
        return constructors.map { constructor ->
            object : IModuleFactory {
                override val canBeTop: Boolean =
                        constructor.parameters.first().type.javaType == Module::class.java &&
                        constructor.parameters.first().type.isMarkedNullable

                override val parameters = constructor.parameters
                        .filter { it.index >= 2 }
                        .map { ModuleParameterInfo(it.index, it.name!!, -1, getTypename(it.type)) }

                override fun create(parent: Component?, name: String, vararg parameters: Any?): Module =
                        constructor.call(parent, name, *parameters)
            }
        }
    }

    override fun getFilePath(): String = TODO("This case is unexpected")
}