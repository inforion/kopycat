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
package ru.inforion.lab403.elfloader.tables

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.inv
import ru.inforion.lab403.common.extensions.uint_s
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.elfloader.assertMajorBit
import java.nio.ByteBuffer



class ElfHashTable(input: ByteBuffer, offset: Int) : IHashTable {
    val nbucket: Int
    val nchain: Int

    val buckets: Array<Int>
    val chains: Array<Int>

    fun elfHash(name: String): Int {
        var h = 0u
        var g: UInt
        name.forEach { i ->
            h = (h shl 4) + i.uint_s
            g = h and 0xf0000000u
            if (g != 0u)
                h = h xor (g ushr 24)
            h = h and inv(g)
        }
        return h.int
    }

    init {
        input.position(offset)

        nbucket = input.int
        nchain = input.int

        assertMajorBit(nbucket)
        assertMajorBit(nchain)

        buckets = Array(nbucket) { input.int }
        chains = Array(nchain) { input.int }
    }

}