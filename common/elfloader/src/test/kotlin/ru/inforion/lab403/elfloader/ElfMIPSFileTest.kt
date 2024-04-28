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
package ru.inforion.lab403.elfloader

import org.junit.Test
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.enums.ElfMachine
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsFlags
import java.nio.ByteBuffer
import kotlin.test.assertEquals

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
        assertEquals(0x800107BCu, elf.entry)
    }

    @Test
    fun testFileFlags() {
        assertEquals(
            MipsFlags.EF_MIPS_ARCH_2.id or MipsFlags.E_MIPS_ABI_O32.id or
                    MipsFlags.EF_MIPS_CPIC.id or MipsFlags.EF_MIPS_PIC.id or MipsFlags.EF_MIPS_NOREORDER.id, elf.flags
        )
    }



}