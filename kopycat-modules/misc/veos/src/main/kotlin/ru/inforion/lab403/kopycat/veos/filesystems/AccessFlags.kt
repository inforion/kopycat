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
package ru.inforion.lab403.kopycat.veos.filesystems

import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.veos.exceptions.InvalidArgument

data class AccessFlags(
        var readable: Boolean = false,
        var writable: Boolean = false,
        var append: Boolean = false,
        var create: Boolean = false,
        var truncate: Boolean = false,
        var exclusive: Boolean = false
) : IConstructorSerializable {
    companion object {
        fun String.toAccessFlags(): AccessFlags {
            var i = 0
            val flags = AccessFlags()

            when (this[i]) {
                'r' -> {
                    flags.readable = true
                }
                'w' -> {
                    flags.writable = true
                    flags.create = true
                    flags.truncate = true
                }
                'a' -> {
                    flags.writable = true
                    flags.create = true
                    flags.append = true
                }
                else -> throw InvalidArgument()
            }

            i++

            if (i < length && this[i] == 'b') i++

            if (i < length && this[i] == '+') {
                i++
                flags.readable = true
                flags.writable = true
            }

            while (i < this.length) {
                when {
                    this[i] == 'x' -> flags.exclusive = true
                    this[i] != 'b' && this[i] != 't' -> break
                }

                i++
            }

            if (i != length) throw InvalidArgument()

            return flags
        }
    }

    override fun toString(): String {
        val flags = mutableListOf<String>()
        if (readable) flags.add("READ")
        if (writable) flags.add("WRITE")
        if (append) flags.add("APPEND")
        if (create) flags.add("CREATE")
        if (truncate) flags.add("TRUNC")
        if (exclusive) flags.add("EXCL")
        return "AccessFlags(${flags.joinToString(" | ")})"
    }
}