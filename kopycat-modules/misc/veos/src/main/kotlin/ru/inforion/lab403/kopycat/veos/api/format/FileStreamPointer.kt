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
import ru.inforion.lab403.kopycat.veos.VEOS


class FileStreamPointer(val os: VEOS<*>, val fd: Int) : ICharArrayConstPointer {
    init {
        os.filesystem.file(fd) // IONotFoundError
    }

    private var i = 0
    private var cache: Char? = null

    override val get: Char get() {
        if (cache == null) {
            val data = os.filesystem.read(fd, 1)
            cache = if (data.isEmpty()) 0.asChar else data.single().asChar
        }
        return cache!!
    }

    override val offset get() = i

    override fun next() {
        i++
        if (cache == null)
            os.filesystem.read(fd, 1)
        cache = null
    }

    override fun read(count: Int) = List(count) { get.also { next() } }.joinToString("")

    override fun readUntil(predicate: (Char) -> Boolean): String {
        val result = mutableListOf<Char>()

        while (true) {
            val sym = get
            if (predicate(sym))
                break
            next()
            result.add(sym)
        }
        return result.joinToString("")
    }

}