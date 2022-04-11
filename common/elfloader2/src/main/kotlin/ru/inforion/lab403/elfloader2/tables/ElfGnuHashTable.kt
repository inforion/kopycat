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


class ElfGnuHashTable(
    val nbucket: UInt,
    val symbias: UInt,
    val hashCount: UInt,
    val bitmask_nwords: UInt,
    val l_gnu_shift: UInt,
    val l_gnu_bitmask: Array<ULong>,
    val l_gnu_buckets: Array<UInt>
) : IHashTable {
    companion object {

        fun IElfDataTypes.elfGnuHashTable(offset: ULong): ElfGnuHashTable {
            position = offset.requireInt

            val nbucket = word
            val symbias = word
            val bitmask_nwords = word
            val l_gnu_shift = word
            val l_gnu_bitmask_idxbits = bitmask_nwords - 1u
            require(bitmask_nwords and (bitmask_nwords - 1u) == 0u) { "Must be a power of 2 ($bitmask_nwords)" }
            val l_gnu_bitmask = Array(bitmask_nwords.requireInt) { wordpref }
            val l_gnu_buckets = Array(nbucket.requireInt) { word }

            val __ELF_NATIVE_CLASS = ptrSize.ulong_z * 8u

            val checkHash: (ULong) -> Boolean = { hash ->
                val mask = __ELF_NATIVE_CLASS - 1u
                val hashbit1 = hash and mask
                val hashbit2 = (hash ushr l_gnu_shift.requireInt) and mask
                val bitmask_index = (hash / __ELF_NATIVE_CLASS) and l_gnu_bitmask_idxbits.ulong_z
                val bitmask_word = l_gnu_bitmask[bitmask_index.requireInt]
                val isValid1 = ((bitmask_word ushr hashbit1.requireInt) and 1u).truth
                val isValid2 = ((bitmask_word ushr hashbit2.requireInt) and 1u).truth
                isValid1 && isValid2
            }

            var hashCount = 0u
            while (true) {
                val chain = word
                val valid = checkHash(chain.ulong_z)
                if (!valid)
                    break
                hashCount++
            }

            return ElfGnuHashTable(
                nbucket,
                symbias,
                hashCount,
                bitmask_nwords,
                l_gnu_shift,
                l_gnu_bitmask,
                l_gnu_buckets)
        }
    }

    override val symbolCount get() = hashCount + symbias
}