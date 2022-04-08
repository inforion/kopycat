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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * ADDI rt, rs, immediate
 *
 * To add a constant to a 32-bit integer. If overflow occurs, then trap.
 */
class addi(
        core: MipsCore,
        data: ULong,
        rt: MipsRegister,
        rs: MipsRegister,
        imm: MipsImmediate) : RtRsImmInsn(core, data, VOID, rt, rs, imm) {

    override val mnem = "addi"

    override fun execute() {
        // MIPS guide is weird and cause exception each time if second operand < 0
        // TODO: Refactor legacy operand class usage
        val op1 = vrs.int
        val op2 = imm.ssext.int
        val res = op1 + op2
        if (isIntegerOverflow(op1, op2, res))
            throw MipsHardwareException.OV(core.pc)
        vrt = res.ulong_s
    }
}