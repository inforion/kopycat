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

import org.junit.Test
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType
import java.nio.ByteBuffer
import kotlin.test.assertEquals

internal class ElfFileProgramHeaderTest {
    companion object {
        private val log = logger()
    }

    private val data =
        "7F454C4601010100000000000000000002000300010000005C8304083400000000000000000000003400200008002800"+
           "000000000600000034000000348004083480040800010000000100000500000004000000030000005401000054810408"+
           "548104081300000013000000040000000100000001000000000000000080040800800408ED040000ED04000005000000"+
           "0010000001000000080F0000089F0408089F04081401000018010000060000000010000002000000140F0000149F0408"+
           "149F0408E8000000E8000000060000000400000004000000000000000000000000000000000000000000000004000000"+
           "0400000050E574640000000000000000000000000000000000000000040000000400000051E574640000000000000000"+
           "0000000000000000000000000600000004000000"
    private val data_bytes = ByteBuffer.wrap(data.unhexlify())
    private val elf = ElfFile(data_bytes)

    
}