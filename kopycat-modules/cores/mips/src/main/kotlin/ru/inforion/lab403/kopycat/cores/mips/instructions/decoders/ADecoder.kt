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
package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

abstract class ADecoder(val core: MipsCore): ITableEntry {
    protected fun gpr(id: Int) = core.cpu.regs[id].toOperand()

    protected fun cpr(id: Int, sel: Int) = core.cop.regs[id].toOperand()

    protected fun fpr(id: Int) = core.fpu.regs[id].toOperand()
    protected fun fcr(id: Int) = core.fpu.cntrls[id].toOperand()

    protected fun hwr(id: Int) = core.cpu.hwrs[id].toOperand()

    protected fun addr(value: Int) = MipsImmediate(value.asULong)

    protected fun imm(value: Long, signed: Boolean = false) = MipsImmediate(value, signed = signed)

    protected fun near(value: Int) = MipsNear(value)

    protected fun displ(dtyp: Datatype, base: Int, offset: Int) = MipsDisplacement(dtyp, gpr(base), addr(offset))

    protected fun any(core: ProcType, designation: Designation, reg: Int, sel: Int) = when (core) {
        ProcType.CentralProc -> gpr(reg)
        ProcType.SystemControlCop -> when (designation) {
            Designation.General -> cpr(reg, sel)
            Designation.Control -> TODO("Bank is reserved")
        }
        ProcType.FloatingPointCop -> when (designation) {
            Designation.General -> fpr(reg)
            Designation.Control -> fcr(reg)
        }
        ProcType.ImplementSpecCop -> hwr(reg)
    }

    abstract fun decode(data: Long): AMipsInstruction
}