package ru.inforion.lab403.kopycat.library.builders.api

import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module

interface IModuleFactory {
    val canBeTop: Boolean
    val parameters: List<ModuleParameterInfo>
    fun create(parent: Component?, name: String, vararg parameters: Any?): Module
    fun getPrintableParams(): String = if (parameters.isEmpty()) "<no params>" else
        parameters.joinToString(separator = ",\n", postfix = "\n") { arg -> "\t${arg.name} [${arg.type}]" }
}