package ru.inforion.lab403.kopycat.library.types

import ru.inforion.lab403.kopycat.library.ModuleFactoryLibrary

data class LibraryInfo(
        val name: String,
        val library: ModuleFactoryLibrary,
        val modules: Collection<ModuleInfo>) {

    override fun toString() = modules.joinToString("\n", "\nLibrary '$library':\n") {
        "\tModule: [%20s] -> %s".format(it.name, it.builder)
    }

    fun getModulesNames() = modules.map { it.name }
}