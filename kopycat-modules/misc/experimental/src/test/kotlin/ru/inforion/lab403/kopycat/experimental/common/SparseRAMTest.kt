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
package ru.inforion.lab403.kopycat.experimental.common

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.interfaces.inl
import ru.inforion.lab403.kopycat.interfaces.inw
import ru.inforion.lab403.kopycat.interfaces.outl
import ru.inforion.lab403.kopycat.interfaces.outw
import java.nio.ByteOrder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SparseRAMTest {
    private fun sparseRAM(regionSize: ULong, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN) = SparseRAM(
        Module(null, "dummy"),
        "testRAM",
        0x1000u,
        regionSize,
        endian,
    )

    @Test
    fun simple() {
        val ram = sparseRAM(0x100u)
        ram.area.outl(0uL, 0xDEADBEEFuL)
        assertEquals(0xDEADBEEFuL, ram.area.inl(0uL))
        assertTrue(ram.hasMemoryForAddress(0uL))
        assertFalse(ram.hasMemoryForAddress(0x100u))
    }

    @Test
    fun simpleEndian() {
        val ram1 = sparseRAM(0x100u, ByteOrder.BIG_ENDIAN)
        ram1.area.outl(0uL, 0xDEADBEEFuL)
        assertEquals("DEADBEEF", ram1.area.load(0uL, 4).hexlify())

        val ram2 = sparseRAM(0x100u, ByteOrder.LITTLE_ENDIAN)
        ram2.area.outl(0uL, 0xDEADBEEFuL)
        assertEquals("EFBEADDE", ram2.area.load(0uL, 4).hexlify())
    }

    @Test
    fun beforeRegionBorder() {
        // Checks that writes just before the region border do not create unnecessary regions
        val ram = sparseRAM(0x100u)
        ram.area.outl(0x100uL - 4uL, 0xDEADBEEFuL)
        assertEquals(0xDEADBEEFuL, ram.area.inl(0x100uL - 4uL))
        assertTrue(ram.hasMemoryForAddress(0uL))
        assertFalse(ram.hasMemoryForAddress(0x100u))
    }

    @Test
    fun crossRegion01() {
        (1uL..3uL).forEach {
            val ram = sparseRAM(0x100u)
            ram.area.outl(0x100uL - it, 0xDEADBEEFuL)
            assertEquals(0xDEADBEEFuL, ram.area.inl(0x100uL - it))
            assertTrue(ram.hasMemoryForAddress(0uL))
            assertTrue(ram.hasMemoryForAddress(0x100u))
            assertFalse(ram.hasMemoryForAddress(0x200u))
        }
    }

    @Test
    fun crossRegion02() {
        val ram1 = sparseRAM(0x100u, ByteOrder.BIG_ENDIAN)
        ram1.area.outl(0x100uL - 2uL, 0xDEADBEEFuL)
        assertEquals(0xDEADuL, ram1.area.inw(0x100uL - 2uL))
        assertEquals(0xBEEFuL, ram1.area.inw(0x100uL))

        val ram2 = sparseRAM(0x100u, ByteOrder.LITTLE_ENDIAN)
        ram2.area.outl(0x100uL - 2uL, 0xDEADBEEFuL)
        assertEquals(0xBEEFuL, ram2.area.inw(0x100uL - 2uL))
        assertEquals("EFBE", ram2.area.load(0x100uL - 2uL, 2).hexlify())
        assertEquals(0xDEADuL, ram2.area.inw(0x100uL))
        assertEquals("ADDE", ram2.area.load(0x100uL, 2).hexlify())
    }

    @Test
    fun crossRegion03() {
        // Single write to four regions
        val ram = sparseRAM(1uL)
        ram.area.outl(1uL, 0xDEADBEEFuL)
        assertEquals(0xDEADBEEFuL, ram.area.inl(1uL))
        assertFalse(ram.hasMemoryForAddress(0uL))
        (1uL..4uL).forEach {
            assertTrue(ram.hasMemoryForAddress(it))
        }
        assertFalse(ram.hasMemoryForAddress(5uL))
    }

    @Test
    fun emptyRegionRead() {
        // Checks that reads from an empty region do not lead to its creation
        val ram = sparseRAM(0x100u)
        assertEquals(0uL, ram.area.inl(0uL))
        assertFalse(ram.hasMemoryForAddress(0uL))
    }

    @Test
    fun zeroWrite01() {
        // Checks that zero writes to nonexistent regions do not lead to their creation
        val ram = sparseRAM(0x100u)
        ram.area.outl(0uL, 0uL)
        assertEquals(0uL, ram.area.inl(0uL))
        assertFalse(ram.hasMemoryForAddress(0uL))
    }

    @Test
    fun zeroWrite02() {
        // Checks that zero writes to existing regions work correctly
        val ram = sparseRAM(0x100u, ByteOrder.BIG_ENDIAN)
        ram.area.outl(0x100uL - 4uL, 0xDEADBEEFuL)
        ram.area.outl(0x100uL - 6uL, 0uL)
        assertEquals(0xBEEFuL, ram.area.inl(0x100uL - 4uL))
        assertTrue(ram.hasMemoryForAddress(0uL))
        assertFalse(ram.hasMemoryForAddress(0x100uL))
    }

    @Test
    fun crossRegionZeroWrite01() {
        val ram = sparseRAM(0x100u, ByteOrder.BIG_ENDIAN)
        ram.area.outl(0x100uL - 2uL, 0xDEADBEEFuL)
        ram.area.outw(0x100uL - 1uL, 0uL)
        assertEquals(0xDE0000EFuL, ram.area.inl(0x100uL - 2uL))
    }

    @Test
    fun crossRegionZeroWrite02() {
        val ram = sparseRAM(0x100u, ByteOrder.BIG_ENDIAN)
        ram.area.outl(0x100uL - 2uL, 0uL)
        assertFalse(ram.hasMemoryForAddress(0uL))
        assertFalse(ram.hasMemoryForAddress(0x100uL))
    }

    @Test
    fun crossRegionZeroWrite03() {
        val ram = sparseRAM(1uL, ByteOrder.BIG_ENDIAN)
        ram.area.outl(0uL, 0xDE_00_BE_EFuL)
        assertEquals(0xDE_00_BE_EFuL, ram.area.inl(0uL))
        assertTrue(ram.hasMemoryForAddress(0uL))
        assertFalse(ram.hasMemoryForAddress(1uL))
        assertTrue(ram.hasMemoryForAddress(2uL))
        assertTrue(ram.hasMemoryForAddress(3uL))
        assertFalse(ram.hasMemoryForAddress(4uL))

        ram.area.outl(0uL, 0x00_AD_BE_EFuL)
        assertEquals(0x00_AD_BE_EFuL, ram.area.inl(0uL))
        assertTrue(ram.hasMemoryForAddress(0uL))
        assertTrue(ram.hasMemoryForAddress(1uL))
        assertTrue(ram.hasMemoryForAddress(2uL))
        assertTrue(ram.hasMemoryForAddress(3uL))
        assertFalse(ram.hasMemoryForAddress(4uL))
    }

    @Test
    fun emptyReadWrite() {
        // Integer overflows test
        val ram = sparseRAM(0x100uL)
        ram.area.write(0uL, 0, 0, 0uL)
        assertFalse(ram.hasMemoryForAddress(0uL))

        ram.area.putBytes(0uL, byteArrayOf())
        ram.area.getBytes(0uL, 0)
        assertFalse(ram.hasMemoryForAddress(0uL))
    }
}
