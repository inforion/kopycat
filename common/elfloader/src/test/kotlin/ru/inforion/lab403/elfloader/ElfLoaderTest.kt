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
import java.nio.ByteBuffer

internal class ElfLoaderTest {
    companion object {
        val log = logger()
    }

    private val data = "7F454C460101010000000000000000000200080001000000700E4000340000005833000007100010340020000000280000001500"
    private val dataBytes: ByteBuffer
    private val elfLoader: ElfLoader

    init {
        dataBytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        elfLoader = ElfLoader(dataBytes)
    }

    @Test
    fun testEntryPoint() {
        assertEquals(0x00400E70, elfLoader.entryPoint)
    }

    @Test
    fun testElfType() {
        assertEquals("ET_EXEC", elfLoader.elfType)
    }

    @Test
    fun testSectionLoading() {
        assertEquals(false, elfLoader.sectionLoading)
    }

    @Test
    fun testSymbols() {
        assertEquals(null, elfLoader.symbols)
    }

    @Test
    fun testDynamicSymbols() {
        assertEquals(null, elfLoader.dynamicSymbols)
    }

    @Test
    fun testStaticRelocations() {
        assertArrayEquals(emptyArray<ElfLoader.ElfRelocation>(), elfLoader.staticRelocations.toTypedArray())
    }
}