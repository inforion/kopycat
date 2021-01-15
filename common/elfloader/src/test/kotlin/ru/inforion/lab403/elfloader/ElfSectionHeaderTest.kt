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

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.elfloader.headers.ElfSectionHeader.Companion.elfSectionHeader
import ru.inforion.lab403.elfloader.enums.ElfEncoding
import ru.inforion.lab403.elfloader.enums.ElfObjectSize
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderFlag
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType
import ru.inforion.lab403.elfloader.headers.ElfSectionHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class ElfSectionHeaderTest {
    companion object {
        val log = logger()
    }

    private val elf = mock<ElfFile> {
        on { encoding } doReturn ElfEncoding.DATA_LSB.id
        on { objectSize } doReturn ElfObjectSize.CLASS_32.id
    }

    private val data = "1B0000000100000002000000F4000100F40000001100000000000000000000000100000000000000"
    private val bytes = ByteBuffer.wrap(data.unhexlify()).apply { order(ByteOrder.LITTLE_ENDIAN) }
    private val sectionHeader: ElfSectionHeader

    init {
        whenever(elf.input).thenReturn(bytes)
        sectionHeader = elf.input.elfSectionHeader(elf, 0, 0, 0x28)
    }

    @Test
    fun testSHeaderNameOffset() = assertEquals(0x1B, sectionHeader.nameOffset)

    @Test
    fun testSHeaderType() = assertEquals(ElfSectionHeaderType.SHT_PROGBITS.id, sectionHeader.type)

    @Test
    fun testSHeaderFlags() = assertEquals(ElfSectionHeaderFlag.SHF_ALLOC.id, sectionHeader.flags)

    @Test
    fun testSHeaderAddr() = assertEquals(0x000100F4, sectionHeader.addr)

    @Test
    fun testSHeaderOffset() = assertEquals(0xF4, sectionHeader.offset)

    @Test
    fun testSHeaderSize() = assertEquals(17, sectionHeader.size)

    @Test
    fun testSHeaderLink() = assertEquals(0, sectionHeader.link)

    @Test
    fun testSHeaderInfo() = assertEquals(0, sectionHeader.info)

    @Test
    fun testSHeaderAddrAlign() = assertEquals(1, sectionHeader.addralign)

    @Test
    fun testSHeaderEntsize() = assertEquals(0, sectionHeader.entsize)

    @Test
    fun testSHeader() {
        //TODO: change input data to be able to set name from mock of ElfFile
    }
}