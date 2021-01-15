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
import org.junit.jupiter.api.TestInstance
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderFlag.*
import java.nio.ByteBuffer
import java.util.logging.Level

internal class ElfLoaderRegionTest {
    companion object {
        val log = logger()
    }

    private val elfRegion: ElfLoader.ElfRegion

    init {
        val size = 0x100
        val data = ByteArray(size)
        val access = ElfAccess.fromSectionHeaderFlags(SHF_ALLOC.id or SHF_EXECINSTR.id)
        elfRegion = ElfLoader.ElfRegion("testRegion", 1,1,  0x08000000, 0x60, size, data, access, 4)
    }

    @Test
    fun testOffset() {
        val offset = elfRegion.toOffset(0x0800_1000)
        assertEquals(0x1000, offset)
    }

    @Test
    fun testAddressRange() {
        assertEquals(0x08000000L..0x080000FFL, elfRegion.addressRange)
    }

    @Test
    fun testIsAddressIncluded() {
        assertEquals(true, elfRegion.isAddressIncluded(0x08000000))
    }

    @Test
    fun testIsAddressNotIncluded() {
        assertEquals(false, elfRegion.isAddressIncluded(0x08000100))
    }

}