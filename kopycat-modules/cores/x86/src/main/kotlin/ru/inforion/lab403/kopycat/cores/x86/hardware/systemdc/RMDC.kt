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
package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.Regtype
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Memory
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Phrase
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.*
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class RMDC(val stream: x86OperandStream, val prefixes: Prefixes) {
    private var opcode: Int = -1

    private fun m32(opsize: Datatype, mod: Int, rm: Int): AOperand<x86Core> {
        return when (mod) {
            0 -> when (rm) {
//                4 -> stream.sib(prefixes.opsize, ssr = prefixes.ssr)
                4 -> stream.sib(opsize, mod, ssr = prefixes.ssr)
                5 -> x86Memory(opsize, stream.readDword(), ssr = prefixes.ssr)
                else -> x86Displacement(opsize, x86Register.gpr32(rm), ssr = prefixes.ssr)
            }
            1 -> when (rm) {
//                4 -> stream.sib(prefixes.opsize, ssr = prefixes.ssr, offsetSize = Datatype.BYTE)
                4 -> stream.sib(opsize, mod, ssr = prefixes.ssr, offsetSize = Datatype.BYTE)
                else -> x86Displacement(opsize, x86Register.gpr32(rm), stream.imm8, ssr = prefixes.ssr)
            }
            2 -> when (rm) {
//                4 -> stream.sib(prefixes.opsize, ssr = prefixes.ssr, offsetSize = Datatype.DWORD)
                4 -> stream.sib(opsize, mod, ssr = prefixes.ssr, offsetSize = Datatype.DWORD)
                else -> x86Displacement(opsize, x86Register.gpr32(rm), stream.imm32, ssr = prefixes.ssr)
            }
            3 -> x86Register.gpr(opsize, rm)

            else -> throw GeneralException("Wrong mod = $mod and rm = $rm")
        }
    }

    private fun m16(opsize: Datatype, mod: Int, rm: Int): AOperand<x86Core> {
        return when (mod) {
            0 -> when (rm) {
                0 -> x86Phrase(opsize, bx, si, ssr = prefixes.ssr)
                1 -> x86Phrase(opsize, bx, di, ssr = prefixes.ssr)
                2 -> x86Phrase(opsize, bp, si, ssr = prefixes.ssr)
                3 -> x86Phrase(opsize, bp, di, ssr = prefixes.ssr)
                4 -> x86Displacement(opsize, si, ssr = prefixes.ssr)
                5 -> x86Displacement(opsize, di, ssr = prefixes.ssr)
//                6 -> stream.mem(prefixes)
                6 -> x86Memory(opsize, stream.readWord(), ssr = prefixes.ssr)
                7 -> x86Displacement(opsize, bx, ssr = prefixes.ssr)
                else -> throw GeneralException("Unknown mod = $mod and rm = $rm")
            }
            1 -> when (rm) {
                0 -> x86Phrase(opsize, bx, si, stream.imm8, ssr = prefixes.ssr)
                1 -> x86Phrase(opsize, bx, di, stream.imm8, ssr = prefixes.ssr)
                2 -> x86Phrase(opsize, bp, si, stream.imm8, ssr = prefixes.ssr)
                3 -> x86Phrase(opsize, bp, di, stream.imm8, ssr = prefixes.ssr)
                4 -> x86Displacement(opsize, si, stream.imm8, ssr = prefixes.ssr)
                5 -> x86Displacement(opsize, di, stream.imm8, ssr = prefixes.ssr)
                6 -> x86Displacement(opsize, bp, stream.imm8, ssr = prefixes.ssr)
                7 -> x86Displacement(opsize, bx, stream.imm8, ssr = prefixes.ssr)
                else -> throw GeneralException("Unknown mod = $mod and rm = $rm")
            }
            2 -> when (rm) {
                0 -> x86Phrase(opsize, bx, si, stream.imm16, ssr = prefixes.ssr)
                1 -> x86Phrase(opsize, bx, di, stream.imm16, ssr = prefixes.ssr)
                2 -> x86Phrase(opsize, bp, si, stream.imm16, ssr = prefixes.ssr)
                3 -> x86Phrase(opsize, bp, di, stream.imm16, ssr = prefixes.ssr)
                4 -> x86Displacement(opsize, si, stream.imm16, ssr = prefixes.ssr)
                5 -> x86Displacement(opsize, di, stream.imm16, ssr = prefixes.ssr)
                6 -> x86Displacement(opsize, bp, stream.imm16, ssr = prefixes.ssr)
                7 -> x86Displacement(opsize, bx, stream.imm16, ssr = prefixes.ssr)
                else -> throw GeneralException("Unknown mod = $mod and rm = $rm")
            }
            3 -> x86Register.gpr(opsize, rm)

            else -> throw GeneralException("Wrong mod = $mod and rm = $rm")
        }
    }

    private fun m(opsize: Datatype = Datatype.UNKNOWN): AOperand<x86Core> {
        if (opcode == -1)
            opcode = stream.readOpcode()
        val mod = opcode[7..6]
        val rm = opcode[2..0]
        val vopsize = if (opsize != Datatype.UNKNOWN) opsize else prefixes.opsize
        return if (prefixes.is16BitAddressMode) m16(vopsize, mod, rm) else m32(vopsize, mod, rm)
    }

    val m8: AOperand<x86Core> get() = m(Datatype.BYTE)
    val m16: AOperand<x86Core> get() = m(Datatype.WORD)
    val m32: AOperand<x86Core> get() = m(Datatype.DWORD)
    val m64: AOperand<x86Core> get() = m(Datatype.QWORD)
    val m80: AOperand<x86Core> get() = m(Datatype.FPU80)
    val m16x32: AOperand<x86Core> get() = m(Datatype.FWORD)
    val mpref: AOperand<x86Core> get() = m()

    fun r(opsize: Datatype = Datatype.UNKNOWN, rtyp: Regtype = Regtype.GPR): AOperand<x86Core> {
        if (opcode == -1)
            opcode = stream.readOpcode()
        val reg = opcode[5..3]
        val vopsize = if (opsize != Datatype.UNKNOWN) opsize else prefixes.opsize
        return when (rtyp) {
            Regtype.GPR -> x86Register.gpr(vopsize, reg)
            Regtype.SSR -> x86Register.sreg(reg)
            else -> throw GeneralException("Wrong rtyp = $rtyp for rm")
        }
    }

    val r8: AOperand<x86Core> get() = r(Datatype.BYTE)
    val r16: AOperand<x86Core> get() = r(Datatype.WORD)
    val r32: AOperand<x86Core> get() = r(Datatype.DWORD)
    val rpref: AOperand<x86Core> get() = r()
}