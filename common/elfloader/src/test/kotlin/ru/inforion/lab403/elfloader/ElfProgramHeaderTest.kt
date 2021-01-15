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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderFlag
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType
import ru.inforion.lab403.elfloader.headers.ElfProgramHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ElfProgramHeaderTest {
    companion object {
        val log = logger()
    }

    private val data: String
    private val data_bytes: ByteBuffer
    private val programHeader: ElfProgramHeader

    init {
        data = "06000000340000003400010034000100C0000000C00000000500000004000000"
        data_bytes = ByteBuffer.wrap(data.unhexlify()).apply { order(ByteOrder.LITTLE_ENDIAN) }
        programHeader = ElfProgramHeader.fromPosition(data_bytes, 0, 0x20, 0)
    }

    @Test
    fun testPHeaderType() {
        assertEquals(ElfProgramHeaderType.PT_PHDR.id, programHeader.type)
    }

    @Test
    fun testPHeaderOffset() {
        assertEquals(0x34, programHeader.offset)
    }

    @Test
    fun testPHeaderVAddr() {
        assertEquals(0x00010034, programHeader.vaddr)
    }

    @Test
    fun testPHeaderPAddr() {
        assertEquals(0x00010034, programHeader.paddr)
    }

    @Test
    fun testPHeaderFilesz() {
        assertEquals(192, programHeader.filesz)
    }

    @Test
    fun testPHeaderMemsz() {
        assertEquals(192, programHeader.memsz)
    }

    @Test
    fun testPHeaderPFlags() {
        assertEquals(ElfProgramHeaderFlag.PF_R.id or ElfProgramHeaderFlag.PF_X.id, programHeader.flags)
    }

    @Test
    fun testPHeaderAlign() {
        assertEquals(4, programHeader.align)
    }
}