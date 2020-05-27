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
package ru.inforion.lab403.kopycat.cores.x86.hardware

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.*
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.ebp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.interfaces.IMemoryStream


class x86OperandStream(stream: IMemoryStream): IMemoryStream by stream {
    private fun mem(optype: Datatype, addrype: Datatype, ssr: x86Register = ds): x86Memory = x86Memory(optype, read(addrype), ssr)
    fun mem(prefixes: Prefixes): x86Memory = x86Memory(prefixes.opsize, read(prefixes.addrsize), prefixes.ssr)
    fun mem8(prefixes: Prefixes): x86Memory = mem(Datatype.BYTE, prefixes.addrsize, prefixes.ssr)
    fun mem16(prefixes: Prefixes): x86Memory = mem(Datatype.WORD, prefixes.addrsize, prefixes.ssr)

    private fun imm(optype: Datatype) = x86Immediate(optype, read(optype))
    fun imm(prefixes: Prefixes) = x86Immediate(prefixes.opsize, read(prefixes.opsize))
    val imm8 get() = imm(Datatype.BYTE)
    val imm16 get() = imm(Datatype.WORD)
    val imm32 get() = imm(Datatype.DWORD)

    // offset = 0 if offsetSize = UNKNOWN
    fun sib(opsize: Datatype, mod: Int, ssr: x86Register, offsetSize: Datatype = Datatype.UNKNOWN): x86Phrase {
        val sib = readOpcode()
        val scale = 1 shl sib[7..6]
        val index = sib[5..3]
        val base = sib[2..0]
        val ri = if (index == 4) x86Register.none else x86Register.gpr(Datatype.DWORD, index)
        val rb = if (base == 5)
            when (mod) {
                0 -> x86Register.none
                // BUGFIX: wrong register was specified, see Intel Instruction Set Vol. 2A 2-7
                1, 2 -> ebp
                else -> throw GeneralException("Incorrect mod value in SIB decoder")
            }
        else x86Register.gpr(Datatype.DWORD, base)
        val vOffsetSize = if (offsetSize == Datatype.UNKNOWN && base == 5) Datatype.DWORD else offsetSize
        val imm = if(vOffsetSize == Datatype.UNKNOWN) zero else imm(vOffsetSize)
        return x86Phrase(opsize, rb, ri, imm, scale, ssr)
    }

    fun far(prefixes: Prefixes): x86Far {
        val address = read(prefixes.opsize)
        val ss = read(Datatype.WORD)
        return x86Far(address, ss)
    }

    private fun near(optype: Datatype, ssr: x86Register = ds): x86Near = x86Near(optype, read(optype).toInt(), ssr)
    fun near(prefixes: Prefixes): x86Near = x86Near(prefixes.opsize, read(prefixes.opsize).toInt(), prefixes.ssr)
    fun near8(prefixes: Prefixes): x86Near = near(Datatype.BYTE, prefixes.ssr)
}