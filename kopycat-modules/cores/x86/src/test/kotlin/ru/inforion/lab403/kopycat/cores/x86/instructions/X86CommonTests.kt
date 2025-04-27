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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU

object X86CommonTests {
    fun AX86InstructionTest.relativeJumpDecodeTestInner() {
        x86.pc = 0x100uL

        fun ULong.formatAddr() = "0x%04X".format(
            (
                this like when (x86.cpu.mode) {
                    x86CPU.Mode.R16 -> Datatype.WORD
                    x86CPU.Mode.R32 -> Datatype.DWORD
                    x86CPU.Mode.R64 -> Datatype.QWORD
                }
            ).long
        )

        // Call; E8
        assertDecode("call near 1", "call 0x0001")
        assertDecode("call near 6", "call 0x0006")
        assertDecode("call near -0x10", "call ${0xfffffffffffffff0uL.formatAddr()}")

        // Jcc; 7C
        assertDecode("7c06".unhexlify(), "jl 0x0008")
        assertDecode("7cf0".unhexlify(), "jl ${0xfffffffffffffff2uL.formatAddr()}")

        // Jcc; 8C
        assertDecode("jl near 0x10", "jl 0x0010")
        assertDecode("jl near -0x10", "jl ${0xfffffffffffffff0uL.formatAddr()}")

        // Jmp; EB
        assertDecode("eb10".unhexlify(), "jmp 0x0012")
        assertDecode("ebf0".unhexlify(), "jmp ${0xfffffffffffffff2uL.formatAddr()}")

        // Jmp; E9
        assertDecode("jmp near 0x10", "jmp 0x0010")
        assertDecode("jmp near -0x10", "jmp ${0xfffffffffffffff0uL.formatAddr()}")

        // Loop; E2
        assertDecode("loop 0x10", "loop 0x0010")
        assertDecode("loop -0x10", "loop ${0xfffffffffffffff0uL.formatAddr()}")

        x86.pc = 0uL
    }
}
