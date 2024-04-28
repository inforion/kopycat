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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * TLBR
 */
class tlbr(core: MipsCore,
           data: ULong,
           imm: MipsImmediate) : Code19bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "tlbr"

    override fun execute() {
        val i = index
        val entry = core.mmu.readTlbEntry(i)

        if (entry.EHINV) {
            pageMask = 0u
            entryHi = 1uL shl 10
            entryLo0 = 0u
            entryLo1 = 0u
        } else {
            pageMask = entry.pageMask
            entryHi = if (core.cop.regs.Config5.mi) {
                entry.VPN2
            } else {
                entry.VPN2 or entry.ASID(core.cop.regs.Config5.mi)
            }
            entryLo0 = entry.entryLo0
            entryLo1 = entry.entryLo1
            memoryMapId = entry.mmid
        }
//        log.warning { "${core.cpu.pc.hex8} -> $mnem $entry" }
    }
}
