package ru.inforion.lab403.kopycat.library.builders.api

import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry

interface IModuleFactoryBuilder {
    fun preload(): Boolean = true

    /**
     * {EN}This function should perform a check whether or not plugin can be loaded{EN}
     *
     * {RU}Выполняет загрузку модуля и возвращает true в случае успеха и false, если модуль не может быть загружен{RU}
     */
    fun load(): Boolean

    fun factory(pluginName: String, registry: ModuleLibraryRegistry): List<IModuleFactory>

    fun plugins(): Set<String>

    override fun toString(): String
}

