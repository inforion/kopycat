package ru.inforion.lab403.kopycat.library.enumerators

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.builders.ClassModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder
import java.util.logging.Level

class InternalFactoriesEnumerator(private val internalClassDirectory: String) : IFactoriesEnumerator {
    companion object {
        val log = logger(Level.INFO)

        val anonymousClassPattern = Regex("""\$.*\$""")
    }

    private lateinit var builders: List<ClassModuleFactoryBuilder>

    override fun preload() {
        val helper = ClasspathHelper.forPackage(internalClassDirectory)
        val reflections = Reflections(helper, SubTypesScanner())
        val types = reflections.getSubTypesOf(Module::class.java)
        builders = types
                .filter { it.name.startsWith(internalClassDirectory) && !it.name.contains(anonymousClassPattern)}
                .map { ClassModuleFactoryBuilder(it) }
    }

    override fun load(): Map<String, IModuleFactoryBuilder> {
        val result = builders
                .filter { it.load() }
                .fold(listOf<Pair<String, IModuleFactoryBuilder>>()) { acc, builder ->
                    acc + builder.plugins().map { it to builder }
                }

        // Name clashes verification
        val uniqueNames = result.map { it.first }.toSet()
        uniqueNames.forEach { name ->
            if (result.count { it.first == name } > 1)
                log.warning { "$name duplicated during loading factories builders!" }
        }

        return result.toMap()
    }
}
