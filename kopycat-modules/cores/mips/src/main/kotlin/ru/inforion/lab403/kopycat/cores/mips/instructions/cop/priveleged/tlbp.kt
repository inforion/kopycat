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

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsMMU
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * TLBP
 */
class tlbp(core: MipsCore,
           data: Long,
           imm: MipsImmediate) : Code19bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "tlbp"

    override fun execute() {
        index = -1
        val match = MipsMMU.TLBEntry(-1, pageMask, entryHi, entryLo0, entryLo1)
        val mask = match.VPN2 and match.Mask.inv()
        for (i in 0 until core.mmu.tlbEntries) {
            val TLB = core.mmu.readTlbEntry(i)
            val cond1 = (TLB.VPN2 and TLB.Mask.inv()) == mask
            val cond2 = TLB.G == 1 || TLB.ASID == match.ASID
            if (cond1 && cond2)
                index = i.asULong
        }
//        log.severe { "${core.cpu.pc.hex8} -> $mnem $index" }
    }
}

