package ru.inforion.lab403.kopycat.library.builders

import ru.inforion.lab403.common.extensions.getInternalFileURL
import ru.inforion.lab403.common.proposal.DynamicClassLoader
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.exceptions.WrongModulePluginNameError
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.jar.JarEntry
import java.util.jar.JarInputStream


class JarModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    private lateinit var builders: Map<String, AFileModuleFactoryBuilder>

    private fun enumJarEntries(jar: File): Sequence<JarEntry> {
        val jis = JarInputStream(jar.inputStream())
        return generateSequence { jis.nextJarEntry }
    }

    override fun plugins(): Set<String> = builders.keys

    override fun preload(): Boolean {
        val jar = File(path)

        if (jar.extension != settings.jarFileExt && jar.extension != settings.zipFileExt) {
            log.finest { "Can't load file ${jar.path} -> only jar and zip supported" }
            return false
        }

        DynamicClassLoader.loadIntoClasspath(jar)

        return true
    }

    override fun load(): Boolean {
        val jar = File(path)

        log.finer { "Loading $jar" }

        val exportFileUrl = jar.getInternalFileURL(settings.exportFilename)

        val dirs = try {
            val stream = exportFileUrl.openStream()
            val reader = InputStreamReader(stream)
            reader.readLines()
        } catch (error: FileNotFoundException) {
            log.warning { "Jar $jar doesn't contain export.txt file, so it can be performance disgrace!" }
            emptyList<String>()
        }

        builders = enumJarEntries(jar)
                .filter {
                    !it.isDirectory
                            // $ in filename is bad sign (used by Kotlin in nested classes)
                            && "$" !in it.name
                            // Module not in export directory
                            && (dirs.isEmpty() || dirs.any { d -> it.name.startsWith(d) })
                }
                .mapNotNull {
                    when {
                        it.name.endsWith(settings.classFileExt) -> ClassModuleFactoryBuilder(it.name, jar)
                        it.name.endsWith(settings.jsonFileExt) -> JsonModuleFactoryBuilder(it.name, jar)
                        else -> null
                    }
                }
                .filter { it.load() }
                .associateBy { it.plugins().first() }

        return builders.isNotEmpty()
    }

    fun getClasspath(pluginName: String): String {
        val builder = builders[pluginName] ?: throw WrongModulePluginNameError(pluginName)
        return builder.path.replace(File.separator, ".").removeSuffix(".class")
    }

    override fun factory(pluginName: String, registry: ModuleLibraryRegistry): List<IModuleFactory> {
        val builder = builders[pluginName] ?: throw WrongModulePluginNameError(pluginName)
        return builder.factory(pluginName, registry)
    }

    override fun getFilePath(): String = TODO("This case is unexpected.")
}