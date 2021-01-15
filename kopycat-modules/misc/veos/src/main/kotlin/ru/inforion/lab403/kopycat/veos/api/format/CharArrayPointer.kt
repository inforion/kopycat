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
package ru.inforion.lab403.kopycat.veos.api.format

import ru.inforion.lab403.common.extensions.asChar


class CharArrayPointer(val data: CharArray, private var i: Int = 0): ICharArrayPointer {
    override val hasRemaining get() = i < data.size
    override val remaining get() = data.size - i

    override fun isNotEmpty() = data.isNotEmpty()

    override val get get() = if (i < data.size) data[i] else 0.asChar
    override val offset get() = i
    override val copy get() = CharArrayPointer(data, i)

    override fun set(c: Char) {
        data[i] = c
    }

    override fun next() {
        i++
        check(i <= data.size)
    }
    override fun prev() {
        i--
        check(i >= 0)
    }
    override operator fun plus(n: Int) = CharArrayPointer(data, i + n)
    override operator fun minus(n: Int) = CharArrayPointer(data, i - n)

    override operator fun plusAssign(n: Int) {
        i += n
        check (i <= data.size)
    }
    override operator fun minusAssign(n: Int) {
        i -= n
        check (i >= 0)
    }
    override fun read(count: Int) = data.slice(i until (i + count)).joinToString("").also { i += count }
    override fun readUntil(predicate: (Char) -> Boolean): String {
        val j = (i until data.size).find { predicate(data[it]) } ?: return read(remaining)
        return read(j - i)
    }

    override fun readLast(count: Int): String = data.slice((i - count) until i).joinToString("")
}