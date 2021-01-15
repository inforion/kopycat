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
package ru.inforion.lab403.elfloader

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.common.logging.ALL
import ru.inforion.lab403.elfloader.tables.ElfHashTable
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class ElfHashTableTest {
    companion object {
        val log = logger(ALL)
    }

    val testData_kill_arm = "110000002400000023000000000000000E0000001D000000220000001E0000001300000000000000000000000C00" +
            "00001F00000021000000090000002000000017000000190000000000000000000000000000000000000000000000030000" +
            "00000000000000000000000000010000000600000008000000000000000000000000000000000000000400000007000000" +
            "0D000000000000000A0000001100000012000000000000001000000002000000150000000500000000000000180000001B" +
            "0000000B0000000F000000160000001A000000140000001C000000"
    val data_kill_arm = ByteBuffer.wrap(testData_kill_arm.unhexlify()).apply { order(ByteOrder.LITTLE_ENDIAN) }
    val hashTable_kill_arm = ElfHashTable(data_kill_arm, 0)

    val testData_empty = "0000000000000000"
    val data_empty = ByteBuffer.wrap(testData_empty.unhexlify()).apply { order(ByteOrder.LITTLE_ENDIAN) }
    val hashTable_empty = ElfHashTable(data_empty, 0)


    @Test fun testNBucket() {
        assertEquals(0x11, hashTable_kill_arm.nbucket)
    }

    @Test fun testNChain() {
        assertEquals(0x24, hashTable_kill_arm.nchain)
    }

    @Test fun testBuckets() {
        val expected = arrayOf(35, 0, 14, 29, 34, 30, 19, 0, 0, 12, 31, 33, 9, 32, 23, 25, 0)
        assertArrayEquals(expected, hashTable_kill_arm.buckets)
    }

    @Test fun testChains() {
        val expected = arrayOf(0, 0, 0, 0, 3, 0, 0, 0, 1, 6, 8, 0, 0, 0, 0, 4, 7, 13, 0, 10, 17, 18, 0, 16, 2, 21, 5, 0, 24, 27, 11, 15, 22, 26, 20, 28)
        assertArrayEquals(expected, hashTable_kill_arm.chains)
    }

    @Test fun testEmptyNBucket() {
        assertEquals(0, hashTable_empty.nbucket)
    }

    @Test fun testEmptyNChain() {
        assertEquals(0, hashTable_empty.nchain)
    }

    @Test fun testEmptyBuckets() {
        val expected = emptyArray<Int>()
        assertArrayEquals(expected, hashTable_empty.buckets)
    }

    @Test fun testHashFunc_strcmp() {
        assertEquals(0x7ab8a40, hashTable_empty.elfHash("strcmp"))
    }

    @Test fun testHashFunc_empty() {
        assertEquals(0, hashTable_empty.elfHash(""))
    }


}