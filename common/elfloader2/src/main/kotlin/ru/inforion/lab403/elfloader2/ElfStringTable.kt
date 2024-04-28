/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.elfloader2.exceptions.EBadStringTable

class ElfStringTable(private val table: MutableMap<UInt, String>) {

    companion object {
        val MutableMap<UInt, String>.elfStringTable get() = ElfStringTable(this)

        fun IElfDataTypes.elfStringTable(offset: ULong, size: ULong): ElfStringTable {
            position = offset.requireInt
            val bytes = ByteArray(size.requireInt)
            get(bytes, 0, size.requireInt)

            var prev = 0
            return bytes.mapIndexed { i, byte ->
                if (byte.int_z == 0) i else null
            }.filterNotNull().associate {
                val range = prev until it
                prev = it + 1
                val str = if (range.isEmpty()) String() else String(bytes[range])
                range.first.uint to str
            }.toMutableMap().elfStringTable
        }
    }

    //TODO: By symtab type?
    fun middleString(offset: UInt): String? {
        val entry = table.entries.find {
            offset in it.key until (it.key + it.value.length)
        }
        return entry?.value?.substring((offset - entry.key).int)
    }

    operator fun get(key: UInt) = table[key]
        ?: middleString(key)
        ?: throw EBadStringTable("Not found offset in string table: 0x${key.hex8}")

    operator fun set(key: UInt, value: String) = run { table[key] = value }

    fun allocate(value: String): UInt {
        return table.entries.find { it.value == value }?.key ?: let {
            val lastItem = table.entries.maxByOrNull { it.key } ?: return@let 0u
            lastItem.key + lastItem.value.length + 1u
        }.also { set(it, value) }
    }
}