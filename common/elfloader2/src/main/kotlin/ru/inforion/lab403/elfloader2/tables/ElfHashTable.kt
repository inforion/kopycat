/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.elfloader2.tables

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.elfloader2.IElfDataTypes
//import ru.inforion.lab403.elfloader.assertMajorBit
import ru.inforion.lab403.elfloader2.requireInt


class ElfHashTable(
    val nbucket: UInt,
    val nchain: UInt,
    val buckets: Array<UInt>,
    val chains: Array<UInt>) : IHashTable {

    companion object {
        fun IElfDataTypes.elfHashTable(offset: ULong): ElfHashTable {
            position = offset.requireInt

            val nbucket = word
            val nchain = word
            return ElfHashTable(
                nbucket,
                nchain,
                buckets = Array(nbucket.requireInt) { word },
                chains = Array(nchain.requireInt) { word })
        }
    }

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

    override val symbolCount get() = nchain
}