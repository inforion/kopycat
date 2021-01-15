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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.util.logging.Level


class MipsDebugger(parent: Module, name: String): Debugger(parent, name) {
    companion object {
        @Transient val log = logger(Level.WARNING)

        const val REG_LO = 0x20
        const val REG_HI = 0x21
        const val REG_STATUS = 0x22
        const val REG_EPC = 0x23
        const val REG_CAUSE = 0x24
        const val REG_PC = 0x25

        const val REG_TOTAL = 0x26
    }

    private inline val mips get() = core as MipsCore

    override fun ident() = "mips"

    override fun registers() = Array(REG_TOTAL) { regRead(it) }.toMutableList()

    override fun regRead(index: Int) = when (index) {
        REG_STATUS -> mips.cop.regs.Status.value
        REG_LO -> mips.cpu.lo
        REG_HI -> mips.cpu.hi
        REG_EPC -> mips.cop.regs.EPC.value
        REG_CAUSE -> mips.cop.regs.Cause.value
        REG_PC -> mips.cpu.pc
        else -> mips.cpu.regs.read(index)
    }

    override fun regWrite(index: Int, value: Long) = when (index) {
        REG_STATUS -> mips.cop.regs.Status.value = value

        REG_LO -> mips.cpu.lo = value
        REG_HI -> mips.cpu.hi = value

        REG_EPC -> mips.cop.regs.EPC.value = value
        REG_CAUSE -> mips.cop.regs.Cause.value = value

        REG_PC -> {
            mips.cpu.branchCntrl.setIp(value)
            // dirty hack to make possible reset exception bypassing IDA Pro
            mips.cpu.resetFault()
        }

        else -> mips.cpu.regs.write(index, value)
    }
}