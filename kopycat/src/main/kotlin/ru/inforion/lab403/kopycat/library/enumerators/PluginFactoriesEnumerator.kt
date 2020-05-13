package ru.inforion.lab403.kopycat.library.enumerators

import ru.inforion.lab403.common.extensions.walk
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.library.builders.*
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder
import java.io.File
import java.nio.file.Files
import java.util.logging.Level

class PluginFactoriesEnumerator(private val userPluginsDirectory: String) : IFactoriesEnumerator {
    companion object {
        val log = logger(Level.FINER)
    }

    private val buildersClasses = arrayOf(
            ::JsonModuleFactoryBuilder,
            ::ClassModuleFactoryBuilder,
            ::JarModuleFactoryBuilder,
            ::XmlModuleFactoryBuilder,
            ::ProtobufModuleFactoryBuilder)

    private val builders = ArrayList<AFileModuleFactoryBuilder>()

    override fun preload() {
        walk(File(userPluginsDirectory).absoluteFile)
                .filter { Files.isRegularFile(it.toPath()) }
                .map { it.absolutePath }
                .toMutableSet()
                .forEach { path ->
                    builders += buildersClasses
                            .map { makeBuilder -> makeBuilder(path, null) }
                            .filter { builder -> builder.preload() }
                }
    }

    override fun load(): Map<String, IModuleFactoryBuilder> {
        val result = HashMap<String, IModuleFactoryBuilder>()
        builders.filter { it.load() }
                .map { builder ->
                    result += builder.plugins().associate { it to builder }
                }
        return result
    }
}
