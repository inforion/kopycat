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
package ru.inforion.lab403.kopycat.modules.debuggers

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.mips.enums.SRVC
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.util.logging.Level


class MipsDebugger(parent: Module, name: String): Debugger(parent, name) {
    companion object {
        val log = logger(Level.WARNING)
    }

    override fun ident() = "mips"

    override fun registers(): MutableList<Long> {
        val core = core as MipsCore

        val regvals = core.cpu.regs.toMutableList()

        regvals.add(core.cop.regs.Status)

        regvals.add(core.cpu.lo)
        regvals.add(core.cpu.hi)

        regvals.add(core.cop.regs.EPC)
        regvals.add(core.cop.regs.Count) // this is Cause register in IDA but output Count
        regvals.add(core.cpu.pc)

        return regvals
    }

    override fun regRead(index: Int): Long {
        val core = core as MipsCore
        return when (index) {
            SRVC.pc.id -> core.cpu.pc
            SRVC.hi.id -> core.cpu.hi
            SRVC.lo.id -> core.cpu.lo
            else -> core.cpu.regs.readIntern(index)
        }
    }

    override fun regWrite(index: Int, value: Long) {
        val core = core as MipsCore
        return when (index) {
            SRVC.pc.id -> {
                core.cpu.branchCntrl.setIp(value)
                // dirty hack to make possible reset exception bypassing IDA Pro
                core.cpu.exception = null
            }

            // ida wants PC reg by index 0x25 and 0x27!
            0x22 -> {
                core.cpu.branchCntrl.setIp(value)
                core.cpu.exception = null
            }

            SRVC.lo.id -> core.cpu.lo = value
            SRVC.hi.id -> core.cpu.hi = value
            else -> core.cpu.regs.writeIntern(index, value)
        }
    }
}