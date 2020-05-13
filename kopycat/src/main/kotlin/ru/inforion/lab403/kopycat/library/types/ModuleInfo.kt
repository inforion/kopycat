package ru.inforion.lab403.kopycat.library.types

import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder

data class ModuleInfo(val name: String, val builder: IModuleFactoryBuilder)