package ru.inforion.lab403.kopycat.library.builders

import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import java.io.File

class ProtobufModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    override fun plugins(): Set<String> = emptySet()

    override fun load(): Boolean = false

    override fun factory(pluginName: String, registry: ModuleLibraryRegistry): List<IModuleFactory> {
        return listOf(object : IModuleFactory {
            override val canBeTop: Boolean
                get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

            override val parameters: List<ModuleParameterInfo>
                get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

            override fun create(parent: Component?, name: String, vararg parameters: Any?): Module {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    override fun getFilePath(): String = TODO("This case is unexpected.")
}