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
package ru.inforion.lab403.kopycat.library.builders.api

import ru.inforion.lab403.kopycat.settings

/**
 * {EN}
 * This class is used for contain information about received argument (value and type, is type is specified, else - null)
 * By default received argument from JSON looks like "<name>" to <value> or "<name>:<type>" to <value>. It is not useful,
 * so this class parse this construction to get data class.
 * Method receives Map<String, Any?> (name to value) (map of arguments),
 * parse them and returns Map<String, [InputParameterInfo]> (name to data class (value, type)).
 * {EN}
 *
 * {RU}
 * Класс используется для хранения информации о полученном аргументе (значение и типа, если тип явно указал, иначе - null)
 * Изначально из JSON получают аргумент вида "<name>" to <value> или "<name>:<type>" to <value>.
 * Для дальнейшего анализа это не удобно, поэтому был  создан этот класс, который обрабатывает эти конструкции.
 * Метод принимает Map<String, Any?> (name to value) (map аргументов), разбирает их и возвращает
 * Map<String, [InputParameterInfo]> (имя к дата классу (value, type)).
 * {RU}
 */
data class InputParameterInfo constructor(val name: String, val value: Any?, val type: String?) {
    companion object {
        private val availableTypes = settings.availableTypes.map { it.toLowerCase() }

        fun fromKeyValue(key: String, value: Any?): InputParameterInfo {
            val tokens = key.replace(" ", "").split(":")

            val name = tokens.first()
            val type = tokens.getOrNull(1)

            return InputParameterInfo(name, value, type)
        }
    }

    val isTypeValid get() = type == null || type.toLowerCase() in availableTypes

    infix fun fits(other: ModuleParameterInfo): Boolean {
        val typesEqual = if (type != null) type.toLowerCase() == other.type.toLowerCase() else true
        return name == other.name && typesEqual
    }
}