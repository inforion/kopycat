package ru.inforion.lab403.kopycat.library.enumerators

import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder


interface IFactoriesEnumerator {
    fun preload()
    fun load(): Map<String, IModuleFactoryBuilder>
}