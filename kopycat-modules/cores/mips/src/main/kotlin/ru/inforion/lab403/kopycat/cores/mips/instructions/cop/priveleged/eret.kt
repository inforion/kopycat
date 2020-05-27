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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.clearBit
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.mips.enums.SRSCtl
import ru.inforion.lab403.kopycat.cores.mips.enums.Status
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 *
 * ERET
 */
class eret(core: MipsCore,
           data: Long,
           imm: MipsImmediate) : Code19bitInsn(core, data, Type.IRET, imm) {

    override val mnem = "eret"

    override fun execute() {
        val pc = if (core.cop.regs.Status[Status.ERL.pos] == 1L) {
            core.cop.regs.Status = clearBit(core.cop.regs.Status, Status.ERL.pos)
            core.cop.regs.ErrorEPC
        } else {
            val StatusBEV = core.cop.regs.Status[Status.BEV.pos].asInt
            val SRSCtlHSS = core.cop.regs.SRSCtl[SRSCtl.HSS.range]
            if (core.ArchitectureRevision >= 2 && SRSCtlHSS > 0 && StatusBEV == 0) {
                // SRSCtlCSS = SRSCtlPSS
                val SRSCtlPSS = core.cop.regs.SRSCtl[SRSCtl.PSS.range]
                core.cop.regs.SRSCtl = core.cop.regs.SRSCtl.insert(SRSCtlPSS, SRSCtl.CSS.range)
            }
            core.cop.regs.Status = clearBit(core.cop.regs.Status, Status.EXL.pos)
            core.cop.regs.EPC
        }
        core.cpu.branchCntrl.jump(pc)
        core.cpu.llbit = 0
        // ClearHazards()
    }
}
