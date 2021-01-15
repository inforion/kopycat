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
package ru.inforion.lab403.kopycat.veos.kernel

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotFoundError


class IdAllocator(val begin: Int = 0, val end: Long = 0x1_0000_0000): IAutoSerializable {
    private var lastId = begin - 1
    private val allocated = mutableSetOf<Int>()

    fun allocate(): Int {
        for (i in 1 until end) {
            val newId = ((lastId + i) % end).asInt
            if (newId !in allocated) {
                allocated.add(newId)
                lastId = newId
                return newId
            }
        }
        throw GeneralException("No available id")
    }

    operator fun contains(id: Int) = id in allocated

    fun reserve(id: Int): Int {
        check(allocated.add(id)) { "Id $id already taken" }
        return id
    }

    fun free(id: Int) {
        if (!allocated.remove(id)) throw IONotFoundError(id)
    }

    fun reset() {
        allocated.clear()
        lastId = begin - 1
    }

    override fun toString() = "Allocator[last = $lastId allocated = $allocated]"
}