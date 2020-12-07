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
package ru.inforion.lab403.elfloader.processors.arm

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.*
import ru.inforion.lab403.elfloader.enums.ElfObjectSize
import ru.inforion.lab403.elfloader.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmRelocationType
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmSectionType
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmSegmentType

internal class ElfDecoderArmTest {
    companion object {
        val log = logger()
    }

    private val elf = mock<ElfFile> {
        on { entry } doReturn 0x08000000
        on { flags } doReturn 0x05000402
        on { objectSize} doReturn ElfObjectSize.CLASS_32.id
        on { eh_osabi } doReturn 0
    }

    private val rel_ARM_COPY = mock<ElfRel> {
        on { type } doReturn  ArmRelocationType.R_ARM_COPY.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_ARM_JUMP_SLOT = mock<ElfRel> {
        on { type } doReturn  ArmRelocationType.R_ARM_JUMP_SLOT.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val rel_ARM_GLOB_DAT = mock<ElfRel> {
        on { type } doReturn  ArmRelocationType.R_ARM_GLOB_DAT.id
        on { withAddend } doReturn false
        on { addend } doReturn 0
    }
    private val decoder = ElfDecoderArm(elf)

    @Test
    fun testSectionsPrefix() {
        assertEquals("ARM", decoder.prefix)
    }

    @Test
    fun testPltHeadSize() {
        assertEquals(20, decoder.pltHeadSize)
    }

    @Test
    fun testPltEntrySize() {
        assertEquals(12, decoder.pltEntrySize)
    }

    @Test
    fun testThumb() {
        assertEquals(false, decoder.thumb)
    }

    @Test
    fun testAbiVersion() {
        assertEquals(5, decoder.abi)
    }

    @Test
    fun testGccVersion() {
        assertEquals(0x402, decoder.gcc)
    }

    @Test
    fun testArmhf() {
        assertEquals(true, decoder.armhf)
    }

    @Test
    fun testCheckHeader() {
        decoder.checkHeader()
    }

    @Test
    fun testSetFlags() {
        decoder.checkFlags()
    }

    @Test
    fun testCheckSectionTypeARM_ATTRIBUTES() {
        decoder.checkSectionType(ArmSectionType.SHT_ARM_ATTRIBUTES.id)
    }

    //Reversive test
    @Test
    fun testCheckSectionFlags0x20000000() {
        assertThrows(EDecodeFault::class.java) { decoder.checkSectionFlags(0x20000000) }
    }

    @Test
    fun testCheckSectionNameARM_attributes() {
        decoder.checkSectionName(".ARM.attributes", ArmSectionType.SHT_ARM_ATTRIBUTES.id, 0)
    }

    @Test
    fun testCheckSegmentType() {
        decoder.checkSegmentType(ArmSegmentType.PT_ARM_ARCHEXT.id)
        decoder.checkSegmentType(ArmSegmentType.PT_ARM_EXIDX.id)
    }

    @Test
    fun testParseDynamic() {
        // TODO: decoder.parseDynamic(, ArmDynamicSectionTag.DT_ARM_SYMTABSZ.id, 0)
    }

    @Test
    fun testApplyStaticRelocation() {
        //From kill.arm.elf
        assertEquals(0, decoder.applyStaticRelocation(rel_ARM_COPY, 0x00021EB0, 0x00021EB0, 0x00021D0C, 0))
        assertEquals(0x00022044, decoder.applyStaticRelocation(rel_ARM_JUMP_SLOT, 0x00021D18, 0x00022044, 0x00021D0C, 0x10720))

        //From cp
        assertEquals(0, decoder.applyStaticRelocation(rel_ARM_COPY, 0x00109380, 0x00109380, 0, 4))
        assertEquals(0x0010AB10, decoder.applyStaticRelocation(rel_ARM_JUMP_SLOT, 0x00109218, 0x0010AB10, 0, 0x100F40))
        assertEquals(0x0010AB68, decoder.applyStaticRelocation(rel_ARM_GLOB_DAT, 0x00109324, 0x0010AB68, 0, 0))
    }

    @Test
    fun testIsLoadableSection() {
        assertEquals(true, decoder.isLoadableSection(ArmSectionType.SHT_ARM_EXIDX.id, ElfAccess(0b1000)))
    }

    @Test
    fun testIsLoadableSegment() {
        assertEquals(true, decoder.isLoadableSegment(ArmSegmentType.PT_ARM_ARCHEXT.id))
        assertEquals(true, decoder.isLoadableSegment(ArmSegmentType.PT_ARM_EXIDX.id))
    }

    @Test
    fun testGetProgramHeaderTypeNameById() {
        assertEquals("ARM_ARCHEXT", decoder.getProgramHeaderTypeNameById(ArmSegmentType.PT_ARM_ARCHEXT.id))
        assertEquals("ARM_EXIDX", decoder.getProgramHeaderTypeNameById(ArmSegmentType.PT_ARM_EXIDX.id))
    }

    @Test
    fun testGetRelocationNameById() {
        assertEquals("R_ARM_COPY", decoder.getRelocationNameById(rel_ARM_COPY.type))
        assertEquals("R_ARM_JUMP_SLOT", decoder.getRelocationNameById(rel_ARM_JUMP_SLOT.type))
        assertEquals("R_ARM_GLOB_DAT", decoder.getRelocationNameById(rel_ARM_GLOB_DAT.type))
    }

    @Test
    fun testFixAddr0x800107BC() {
        assertEquals(0x800107BCL, decoder.fixPaddr(0x800107BCL))
    }

}