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
package ru.inforion.lab403.elfloader.processors.x86

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.ElfRel
import ru.inforion.lab403.elfloader.enums.ElfEncoding
import ru.inforion.lab403.elfloader.enums.ElfObjectSize
import ru.inforion.lab403.elfloader.processors.x86.enum.X86RelocationType


internal class ElfDecoderX86Test {
    companion object {
        val log = logger()
    }

    private val elf = mock<ElfFile> {
        on { encoding } doReturn ElfEncoding.DATA_LSB.id
        on { objectSize } doReturn ElfObjectSize.CLASS_32.id
    }

    private val rel_386_32 = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_32.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_PC32 = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_PC32.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_PLT32 = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_PLT32.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_COPY = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_COPY.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_GLOB_DAT = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_GLOB_DAT.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_JMP_SLOT = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_JMP_SLOT.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_GOTOFF = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_GOTOFF.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_386_GOTPC = mock<ElfRel> {
        on { type } doReturn X86RelocationType.R_386_GOTPC.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val decoder = ElfDecoderX86(elf)

    @Test
    fun testSectionsPrefix() {
        Assertions.assertEquals("386", decoder.prefix)
    }

    @Test
    fun testPltHeadSize() {
        Assertions.assertEquals(16, decoder.pltHeadSize)
    }

    @Test
    fun testPltEntrySize() {
        Assertions.assertEquals(16, decoder.pltEntrySize)
    }

    @Test
    fun testCheckHeader() {
        decoder.checkHeader()
    }

    @Test
    fun testApplyStaticRelocation() {
        //From lz.386
        Assertions.assertEquals(0x0815DE50, decoder.applyStaticRelocation(rel_386_32, 0x08000044, 0x0815DE50, 0x096413C4, 0))
        Assertions.assertEquals(0x0015F221, decoder.applyStaticRelocation(rel_386_PC32, 0x080000AB, 0x0815F2D0, 0x096413C4, 0xFFFFFFFC) mask 32)

        //From objdump.386.elf
        Assertions.assertEquals(0x0809BC00, decoder.applyStaticRelocation(rel_386_GLOB_DAT, 0x0809AFEC, 0x0809BC00, 0, 0))
        Assertions.assertEquals(0, decoder.applyStaticRelocation(rel_386_COPY, 0x0809BC00, 0x0809BC00, 0, 0))
        Assertions.assertEquals(0x0809CB8C, decoder.applyStaticRelocation(rel_386_JMP_SLOT, 0x0809B00C, 0x0809CB8C, 0, 0))

    }

    @Test
    fun testGetRelocationNameById() {
        Assertions.assertEquals("R_386_32", decoder.getRelocationNameById(rel_386_32.type))
        Assertions.assertEquals("R_386_PC32", decoder.getRelocationNameById(rel_386_PC32.type))
        Assertions.assertEquals("R_386_PLT32", decoder.getRelocationNameById(rel_386_PLT32.type))
        Assertions.assertEquals("R_386_COPY", decoder.getRelocationNameById(rel_386_COPY.type))
        Assertions.assertEquals("R_386_GLOB_DAT", decoder.getRelocationNameById(rel_386_GLOB_DAT.type))
        Assertions.assertEquals("R_386_JMP_SLOT", decoder.getRelocationNameById(rel_386_JMP_SLOT.type))
        Assertions.assertEquals("R_386_GOTOFF", decoder.getRelocationNameById(rel_386_GOTOFF.type))
        Assertions.assertEquals("R_386_GOTPC", decoder.getRelocationNameById(rel_386_GOTPC.type))
    }

    @Test
    fun testFixAddr0x800107BC() {
        Assertions.assertEquals(0x800107BCL, decoder.fixPaddr(0x800107BCL))
    }

}