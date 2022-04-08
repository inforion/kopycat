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
package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.Regtype
import ru.inforion.lab403.kopycat.cores.x86.enums.Regtype.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.ADecodable
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU.Mode.R32
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU.Mode.R64
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Phrase
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class RMDC(val stream: x86OperandStream, val prefixes: Prefixes) : ADecodable(stream.core) {
    private var opcode: Int = -1

    private fun m32(opsize: Datatype, mod: Int, rm: Int, regtype: Regtype = GPR, opsize2: Datatype) = when (mod) {
        0 -> when (rm) {
            4, 12 -> stream.sib(opsize, mod, prefixes)
            5 -> when (prefixes.core.cpu.mode) {
                R64 -> x86Displacement(opsize, xip(QWORD), prefixes.core.cpu.sregs.cs.toOperand(), stream.imm32)
                R32 -> x86Displacement(opsize, none(prefixes.addrsize), prefixes, stream.imm32)
                else -> error("Unknown behaviour in real-mode")  // if you are sure please copy line above to here
            }
            else -> x86Displacement(opsize, gpr(rm, prefixes.addrsize), prefixes)
        }
        1 -> when (rm) {
            4, 12 -> stream.sib(opsize, mod, prefixes, BYTE)
            else -> x86Displacement(opsize, gpr(rm, prefixes.addrsize), prefixes, stream.imm8)
        }
        2 -> when (rm) {
            4, 12 -> stream.sib(opsize, mod, prefixes, DWORD)
            else -> x86Displacement(opsize, gpr(rm, prefixes.addrsize), prefixes, stream.imm32)
        }
        3 -> when (regtype) {
            GPR -> gpr(rm, opsize2, prefixes.rex)
            MMX -> mmx(rm)
            XMM -> xmm(rm)
            else -> error("Wrong regtype = $regtype")
        }

        else -> error("Wrong mod = $mod and rm = $rm")
    }

    private fun m16(opsize: Datatype, mod: Int, rm: Int, opsize2: Datatype) = when (mod) {
        0 -> when (rm) {
            0 -> x86Phrase(opsize, bx, si, prefixes)
            1 -> x86Phrase(opsize, bx, di, prefixes)
            2 -> x86Phrase(opsize, bp, si, prefixes)
            3 -> x86Phrase(opsize, bp, di, prefixes)
            4 -> x86Displacement(opsize, si, prefixes)
            5 -> x86Displacement(opsize, di, prefixes)
            6 -> x86Displacement(opsize, none(WORD), prefixes, stream.imm16)  // This was x86Memory before, if this won't work see git-blame
            7 -> x86Displacement(opsize, bx, prefixes)
            else -> error("Unknown mod = $mod and rm = $rm")
        }
        1 -> when (rm) {
            0 -> x86Phrase(opsize, bx, si, prefixes, stream.imm8)
            1 -> x86Phrase(opsize, bx, di, prefixes, stream.imm8)
            2 -> x86Phrase(opsize, bp, si, prefixes, stream.imm8)
            3 -> x86Phrase(opsize, bp, di, prefixes, stream.imm8)
            4 -> x86Displacement(opsize, si, prefixes, stream.imm8)
            5 -> x86Displacement(opsize, di, prefixes, stream.imm8)
            6 -> x86Displacement(opsize, bp, prefixes, stream.imm8)
            7 -> x86Displacement(opsize, bx, prefixes, stream.imm8)
            else -> error("Unknown mod = $mod and rm = $rm")
        }
        2 -> when (rm) {
            0 -> x86Phrase(opsize, bx, si, prefixes, stream.imm16)
            1 -> x86Phrase(opsize, bx, di, prefixes, stream.imm16)
            2 -> x86Phrase(opsize, bp, si, prefixes, stream.imm16)
            3 -> x86Phrase(opsize, bp, di, prefixes, stream.imm16)
            4 -> x86Displacement(opsize, si, prefixes, stream.imm16)
            5 -> x86Displacement(opsize, di, prefixes, stream.imm16)
            6 -> x86Displacement(opsize, bp, prefixes, stream.imm16)
            7 -> x86Displacement(opsize, bx, prefixes, stream.imm16)
            else -> error("Unknown mod = $mod and rm = $rm")
        }
        3 -> gpr(rm, opsize2)

        else -> error("Wrong mod = $mod and rm = $rm")
    }

    // r/m
    internal fun m(
        opsize: Datatype = UNKNOWN,
        rtyp: Regtype = GPR,
        opsize2: Datatype = opsize
    ): AOperand<x86Core> {
        if (opcode == -1)
            opcode = stream.readOpcode()
        val mod = opcode[7..6]
        val rm = opcode[2..0] or (prefixes.rexB.int shl 3)
        val vopsize = if (opsize != UNKNOWN) opsize else prefixes.opsize
        val vopsize2 = if (opsize2 != UNKNOWN) opsize else prefixes.opsize
        return when {
            prefixes.is16BitAddressMode -> m16(vopsize, mod, rm, vopsize2)
            else -> m32(vopsize, mod, rm, rtyp, vopsize2)
        }
    }

    val m8 get() = m(BYTE)
    val m16 get() = m(WORD)
    val m32 get() = m(DWORD)
    val m64 get() = m(QWORD)
    val m80 get() = m(FPU80)
    val m16x32 get() = m(FWORD)
    val m16x64 get() = m(DFWORD)
    val mpref get() = m()
    val xmmpref get() = m(XMMWORD, XMM)
    val mxpref get() = if (prefixes.opsize == QWORD) m16x64 else m16x32

    private fun r(opsize: Datatype = UNKNOWN, rtyp: Regtype = GPR): AOperand<x86Core> {
        if (opcode == -1)
            opcode = stream.readOpcode()
        val reg = opcode[5..3] or (prefixes.rexR.int shl 3)
        val vopsize = if (opsize != UNKNOWN) opsize else prefixes.opsize
        return when (rtyp) {
            GPR -> gpr(reg, vopsize, prefixes.rex)
            SSR -> sreg(reg)
            MMX -> mmx(reg)
            XMM -> xmm(reg)
            else -> error("Wrong rtyp = $rtyp for rm")
        }
    }

    val r8 get() = r(BYTE)
    val r16 get() = r(WORD)
    val r32 get() = r(DWORD)
    val rpref get() = r()
    val rssr get() = r(rtyp = SSR)
    val rmmx get() = r(MMXWORD, MMX)
    val rxmm get() = r(XMMWORD, XMM)
}