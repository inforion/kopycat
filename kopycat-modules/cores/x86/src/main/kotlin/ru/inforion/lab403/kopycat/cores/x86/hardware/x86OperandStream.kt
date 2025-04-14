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
package ru.inforion.lab403.kopycat.cores.x86.hardware

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.*
import ru.inforion.lab403.kopycat.interfaces.IMemoryStream
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class x86OperandStream(core: x86Core, stream: IMemoryStream): ADecodable(core), IMemoryStream by stream {
    fun mem(prefixes: Prefixes) = x86Memory(prefixes.opsize, read(prefixes.addrsize), prefixes)
    fun mem8(prefixes: Prefixes) = x86Memory(Datatype.BYTE, read(prefixes.addrsize), prefixes)
    fun mem16(prefixes: Prefixes) = x86Memory(Datatype.WORD, read(prefixes.addrsize), prefixes)

    private fun readImm(dtype: Datatype) = if (dtype == Datatype.QWORD)
        read(Datatype.DWORD) signext 31
    else
        read(dtype)

    private fun imm(optype: Datatype) = x86Immediate(optype, readImm(optype))
    fun imm(prefixes: Prefixes) = x86Immediate(prefixes.opsize, readImm(prefixes.opsize))
    // Some variants of MOV can read 64-bit integers from the stream
    fun immMov(prefixes: Prefixes) = x86Immediate(prefixes.opsize, read(prefixes.opsize))
    val imm8 get() = imm(Datatype.BYTE)
    val imm16 get() = imm(Datatype.WORD)
    val imm32 get() = imm(Datatype.DWORD)

    // offset = 0 if offsetSize = UNKNOWN
    fun sib(opsize: Datatype, mod: Int, prefixes: Prefixes, offsetSize: Datatype = Datatype.UNKNOWN): x86Phrase {
        val sib = readOpcode()
        val scale = 1 shl sib[7..6]
        val index = sib[5..3] or (prefixes.rexX.int shl 3)
        val base = sib[2..0] or (prefixes.rexB.int shl 3)
        val ri = if (index == 4) none(prefixes.addrsize) else gpr(index, prefixes.addrsize)
        val rb = if (base == 5)
            when (mod) {
                0 -> none(prefixes.addrsize)
                // BUGFIX: wrong register was specified, see Intel Instruction Set Vol. 2A 2-7
                1, 2 -> gpr(x86GPR.RBP, prefixes.addrsize)
                else -> throw GeneralException("Incorrect mod value in SIB decoder")
            }
        else gpr(base, prefixes.addrsize)
        val vOffsetSize = if (offsetSize == Datatype.UNKNOWN && base == 5) prefixes.addrsize else offsetSize
        val imm = if(vOffsetSize == Datatype.UNKNOWN) zero else imm(vOffsetSize)
        return x86Phrase(opsize, rb, ri, prefixes, imm, scale)
    }

    fun far(prefixes: Prefixes): x86Far {
        val address = read(prefixes.opsize)
        val ss = read(Datatype.WORD)
        return x86Far(address, ss)
    }

    fun near(prefixes: Prefixes, opcodeLen: Int = 1) = x86Near(
        prefixes.opsize,
        readImm(prefixes.opsize),
        prefixes,
        opcodeLen + prefixes.opsize.bytes.coerceAtMost(4),
    )

    fun near8(prefixes: Prefixes, opcodeLen: Int = 1) = x86Near(
        Datatype.BYTE,
        read(Datatype.BYTE),
        prefixes,
        opcodeLen + 1,
    )
}
