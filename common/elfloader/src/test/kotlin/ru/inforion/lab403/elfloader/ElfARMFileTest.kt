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
package ru.inforion.lab403.elfloader

import org.junit.Assert.assertThrows
import org.junit.Test
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.enums.ElfMachine
import ru.inforion.lab403.elfloader.enums.ElfType
import ru.inforion.lab403.elfloader.enums.ElfVersion
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmFlags.EF_ARM_ABIMASK
import java.nio.ByteBuffer
import kotlin.test.assertEquals


internal class ElfARMFileTest {
    companion object {
        val log = logger()
    }

    private val elf = run {
        val data = "7F454C460101010000000000000000000200280001000000B4080100340000009C2A000000020005340020000000280000001700"
        val bytes = data.unhexlify()
        val buffer = ByteBuffer.wrap(bytes)      // raw bytes, BigEndian order
        ElfFile(buffer)
    }

    @Test
    fun testFileMagic() {
        val data = "7F554466"
        val data_bytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        val t = assertThrows(Exception::class.java) { ElfFile(data_bytes) }
        assertEquals("Bad ELF magic number", t.message)
    }

    @Test
    fun testFileType() {
        assertEquals(ElfType.ET_EXEC.id, elf.type)
    }

    @Test
    fun testFileMachineType() {
        assertEquals(ElfMachine.EM_ARM.id, elf.machine)
    }

    @Test
    fun testFileVersion() {
        assertEquals(ElfVersion.EV_CURRENT.id, elf.version)
    }

    @Test
    fun testFileEntry() {
        assertEquals(0x000108B4u, elf.entry)
//        assertEquals(0x800107BCL, elf.entry)
    }

    @Test
    fun testFilePhoff() {
        assertEquals(0x34, elf.phoff)
    }

    @Test
    fun testFileShoff() {
        assertEquals(0x2A9C, elf.shoff)
    }

    @Test
    fun testFileFlagsABIVersion() {
        assertEquals(0x05000000u, elf.flags and EF_ARM_ABIMASK.id)
    }

    @Test
    fun testFileFlagsABIFloatSoft() {
        assertEquals(0x05000200u, elf.flags)
    }

    @Test
    fun testFilePhentsize() {
        assertEquals(0x0020, elf.phentsize)
    }

    @Test
    fun testFilePhnum() {
        assertEquals(0x0000, elf.phnum)
    }

    @Test
    fun testFileShentsize() {
        assertEquals(0x0028, elf.shentsize)
    }

    @Test
    fun testFileShnum() {
        assertEquals(0x0000, elf.shnum)
    }

    @Test
    fun testFileShtrndx() {
        assertEquals(0x0017, elf.shstrndx)
    }
}