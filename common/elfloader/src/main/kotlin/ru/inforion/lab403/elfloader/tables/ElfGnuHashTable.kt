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
package ru.inforion.lab403.elfloader.tables

import ru.inforion.lab403.elfloader.assertMajorBit
import java.nio.ByteBuffer


 
class ElfGnuHashTable(input: ByteBuffer, offset: Int)  : IHashTable {
    init {
        input.position(offset)
    }

    val nbucket = input.int.also { assertMajorBit(it) } // TODO: as ULong?
    val symbias = input.int.also { assertMajorBit(it) }
    val bitmask_nwords = input.int.also { require(it and (it - 1) == 0) } // Must be a power of 2
    val l_gnu_shift = input.int
    val l_gnu_bitmask = Array(bitmask_nwords) { input.int }
    val l_gnu_buckets = Array(nbucket) { input.int }
//    val l_gnu_chain_zero // TODO: we don't know size of table
}