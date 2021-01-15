/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.library.types

import ru.inforion.lab403.common.extensions.emptyString
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.InputParameterInfo
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import ru.inforion.lab403.kopycat.library.parseParametersAsMap

/**
 * {EN}
 * This class contains information about satisfying of current [factory] and received constructor params.
 * Current [factory] contains number of parameters [ModuleParameterInfo] name and types of current constructor.
 * This class is used for the following tasks:
 * - Get information about which constructor parameters from [factory] were found in the received parameters
 * - Get information about with received parameters was excess for current [factory]
 * - Get information on whether the received parameters satisfied to the [factory] constructor.
 * - Get printable information about parameters satisfies
 *
 * @property params - contains array of [ParameterStatus] (information about satisfying current constructor parameter and received parameters)
 * @property superfluousParameters - contains information about excess received parameters
 * @property isSatisfy - if true - current [factory] can be created with received list of parameters, else - false
 * {EN}
 *
 * {RU}
 * Этот класс содержит информацию о соответствии текущего [factory] и полученного списка параметров, из
 * которых будет создан экземпляр модуля. Текущий [factory] содержит массив [ModuleParameterInfo] (имя, тип и т.д.)
 * Для этого массива проверяется, соответствуют ли полученные параметры массиву [ModuleParameterInfo].
 * Этот класс используется для:
 * - Получения информации о том, какие из параметров конструктора [factory] были найдены в полученных параметров
 * - Получения информации о том, какие полученные параметры были лишние для текущего конструктора [factory]
 * - Получения информации о том, соответствуют ли полученные параметры выбранному конструктору [factory]
 * - Получения текстовой информации о соответствии параметров (какие подходят, а какие нет).
 *
 * @property params Массив [ParameterStatus] - Для каждого [factory] содержит информацию о том, найден ли параметр среди полученных для этого аргуменат конструктора
 * @property superfluousParameters Массив полученных параметров, которые оказались "лишними" для текущего конструктора [factory]
 * @property isSatisfy true - если текущая [factory] может быть создана из полученных параметров, иначе - false.
 * {RU}
 */
internal class SatisfactionChecker constructor(val factory: IModuleFactory, val parameters: List<InputParameterInfo>) {
    companion object {
        val log = logger()
    }

    data class ParameterStatus(val param: ModuleParameterInfo, val found: Boolean)

    private val superfluousParameters = parameters.toMutableList()

    private val params = factory.parameters.map { factoryParameter ->
        val candidates = superfluousParameters
                .filter { inputParameter -> inputParameter fits factoryParameter }
                .also {
                    // should be filtered out out previous parsing but check it just in case
                    require(it.size <= 1) { "Duplicate input parameters found with name '${factoryParameter.name}'" }
                }.onEach { superfluousParameters.remove(it) }

        ParameterStatus(factoryParameter, candidates.size == 1)
    }

    lateinit var ordered: Map<String, Any?>
        private set

    val isSatisfy get() = superfluousParameters.size == 0 && params.all { it.found || it.param.optional }

    fun parse() = runCatching {
        log.finer { "Parsing constructor with args: (${factory.getPrintableParams()})" }
        ordered = parseParametersAsMap(factory, parameters.associateBy { it.name })
    }.isSuccess

    fun getPrintableError(): String {
        val paramsInfo = if (params.isEmpty()) "<no params>" else {
            params.joinToString(",\n") {
                val sym = if (it.found) 'V' else 'X'
                "\t${it.param.name} [${it.param.type}] -> $sym"
            }
        }

        val superfluousInfo = if (superfluousParameters.isEmpty()) emptyString else {
            superfluousParameters.joinToString("\n", "Excess params:\n", "\n") {
                "\t${it.name} [${it.type ?: "auto type"}]"
            }
        }

        return "$paramsInfo\n$superfluousInfo"
    }
}