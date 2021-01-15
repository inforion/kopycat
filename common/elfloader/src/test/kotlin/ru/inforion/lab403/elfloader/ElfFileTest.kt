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
import ru.inforion.lab403.elfloader.enums.ElfMachine
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType.*
import ru.inforion.lab403.elfloader.enums.ElfType
import ru.inforion.lab403.elfloader.enums.ElfVersion
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmFlags.*
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsFlags.*
import java.nio.ByteBuffer


internal class ElfARMFileTest {
    companion object {
        val log = logger()
    }

    private val data: String
    private val data_bytes: ByteBuffer
    private val elf: ElfFile

    init {
        data = "7F454C460101010000000000000000000200280001000000B4080100340000009C2A000000020005340020000000280000001700"
        data_bytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        elf = ElfFile(data_bytes)
    }

    @Test
    fun testFileMagic() {
        val data = "7F554466"
        val data_bytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        val t = assertThrows(Exception::class.java, { ElfFile(data_bytes) })
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
        assertEquals(0x000108B4L, elf.entry)
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
        assertEquals(0x05000000, elf.flags and EF_ARM_ABIMASK.id)
    }

    @Test
    fun testFileFlagsABIFloatSoft() {
        assertEquals(0x05000200, elf.flags)
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


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ElfFileProgramHeaderTest {
    companion object {
        private val log = logger()
    }

    private val data: String
    private val data_bytes: ByteBuffer
    private val elf: ElfFile

    init {
        data = "7F454C4601010100000000000000000002000300010000005C8304083400000000000000000000003400200008002800"+
               "000000000600000034000000348004083480040800010000000100000500000004000000030000005401000054810408"+
               "548104081300000013000000040000000100000001000000000000000080040800800408ED040000ED04000005000000"+
               "0010000001000000080F0000089F0408089F04081401000018010000060000000010000002000000140F0000149F0408"+
               "149F0408E8000000E8000000060000000400000004000000000000000000000000000000000000000000000004000000"+
               "0400000050E574640000000000000000000000000000000000000000040000000400000051E574640000000000000000"+
               "0000000000000000000000000600000004000000"
        data_bytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        elf = ElfFile(data_bytes)
    }

    @Test
    fun testPHeadersCount() {
        assertEquals(8, elf.programHeaderTable.size)
    }

    @Test
    fun testPHeadersSizeBytes() {
        assertEquals(0x100,  elf.phentsize * elf.programHeaderTable.size)
    }

    @Test
    fun testPHeadersIterate() {
        val pTypes = arrayOf(PT_PHDR.id, PT_INTERP.id, PT_LOAD.id, PT_LOAD.id, PT_DYNAMIC.id, PT_NOTE.id, PT_GNU_EH_FRAME.id, PT_GNU_STACK.id)
        val pairs = pTypes.zip(elf.programHeaderTable)
        pairs.forEach { assertEquals(it.first, it.second.type) }
    }


}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ElfFileSectionHeaderTest {
    companion object {
        private val log = logger()
    }

    private val data: String
    private val data_bytes: ByteBuffer
    private val elf: ElfFile

    init {
        data = "7F454C460101010000000000000000000200080001000000700E4000340000003400000007100010340020000800280018001500"+

                "000000000000000000000000000000000000000000000000000000000000000000000000000000001B00000001000000"+
                "0200000034014000340100001300000000000000000000000100000000000000230000002A0000700200000048014000"+
                "480100001800000000000000000000000800000018000000320000000600007002000000600140006001000018000000"+
                "000000000000000004000000180000003B00000006000000020000007801400078010000D80000000700000000000000"+
                "040000000800000044000000050000000200000050024000500200003C01000006000000000000000400000004000000"+
                "4A0000000B000000020000008C0340008C03000080020000070000000100000004000000100000005200000003000000"+
                "020000000C0640000C06000096010000000000000000000001000000000000005A000000FFFFFF6F02000000A2074000"+
                "A2070000500000000600000000000000020000000200000067000000FEFFFF6F02000000F4074000F407000070000000"+
                "070000000200000004000000000000007600000001000000060000007008400070080000701800000000000000000000"+
                "10000000000000007C0000000100000006000000E0204000E02000001002000000000000000000000400000000000000"+
                "880000000100000002000000F0224000F022000060020000000000000000000010000000000000009000000001000000"+
                "030000005025410050250000100100000000000000000000100000000000000096000000010000000300000060264100"+
                "6026000004000000000000000000000004000000000000009F000000010000000300001070264100702600009C000000"+
                "00000000000000001000000004000000A40000000800000003000000102741000C270000100100000000000000000000"+
                "1000000000000000A90000000100000030000000000000000C2700003400000000000000000000000100000001000000"+
                "B200000001000000000000000000000040270000A001000000000000000000000400000000000000B7000000F5FFFF6F"+
                "0000000000000000E02800001000000000000000000000000100000000000000C7000000010000000000000000000000"+
                "F0280000000000000000000000000000010000000000000011000000030000000000000000000000F4030000D5000000"+
                "0000000000000000010000000000000001000000020000000000000000000000F0280000A0050000170000001E000000"+
                "040000001000000009000000030000000000000000000000902E0000F103000000000000000000000100000000000000"+

                "002E73796D746162002E737472746162002E7368737472746162002E696E74657270002E4D4950532E616269666C6167"+
                "73002E726567696E666F002E64796E616D6963002E68617368002E64796E73796D002E64796E737472002E676E752E76"+
                "657273696F6E002E676E752E76657273696F6E5F72002E74657874002E4D4950532E7374756273002E726F6461746100"+
                "2E64617461002E726C645F6D6170002E676F74002E627373002E636F6D6D656E74002E706472002E676E752E61747472"+
                "696275746573002E6D64656275672E616269333200"
        data_bytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        elf = ElfFile(data_bytes)
    }

    @Test
    fun testSectionHeadersCount() {
        assertEquals(24, elf.sectionHeaderTable.size)
    }

    @Test
    fun testSectionNames() {
        val sections = arrayOf("", ".interp", ".MIPS.abiflags", ".reginfo", ".dynamic", ".hash", ".dynsym", ".dynstr",
                ".gnu.version", ".gnu.version_r", ".text", ".MIPS.stubs", ".rodata", ".data", ".rld_map", ".got", ".bss",
                ".comment", ".pdr", ".gnu.attributes", ".mdebug.abi32", ".shstrtab", ".symtab", ".strtab")
        val pairs = sections.zip(elf.sectionHeaderTable)
        pairs.forEach {
          //  it.second.loadName(data_bytes, elf.stringTableOffset)
            assertEquals(it.first, it.second.name)
        }
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ElfMIPSFileTest {
    companion object {
        private val log = logger()
    }

    private val data: String
    private val data_bytes: ByteBuffer
    private val elf: ElfFile

    init {
        data = "7F454C460101010000000000000000000200080001000000BC070180340000005833000007100010340020000000280000001500"
        data_bytes = ByteBuffer.wrap(data.unhexlify())      // raw bytes, BigEndian order
        elf = ElfFile(data_bytes)
    }

    @Test
    fun testFileMachineType() {
        assertEquals(ElfMachine.EM_MIPS.id, elf.machine)
    }

    @Test
    fun testFileEntry() {
        assertEquals(0x800107BCL, elf.entry)
    }

    @Test
    fun testFileFlags() {
        assertEquals(EF_MIPS_ARCH_2.id or E_MIPS_ABI_O32.id or
                EF_MIPS_CPIC.id or EF_MIPS_PIC.id or EF_MIPS_NOREORDER.id , elf.flags)
    }



}