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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.mips.enums.InstructionSet
import java.math.BigInteger


class MipsDebugger(parent: Module, name: String): Debugger(parent, name) {
    companion object {
        @Transient val log = logger(WARNING)

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
        REG_STATUS -> mips.cop.regs.Status.value.bigint
        REG_LO -> mips.cpu.lo.bigint
        REG_HI -> mips.cpu.hi.bigint
        REG_EPC -> mips.cop.regs.EPC.value.bigint
        REG_CAUSE -> mips.cop.regs.Cause.value.bigint
        REG_PC -> if (mips.cpu.iset == InstructionSet.MIPS32) mips.cpu.pc.bigint else (mips.cpu.pc set 0).bigint
        else -> mips.cpu.regs.read(index).bigint
    }

    override fun regWrite(index: Int, value: BigInteger) = when (index) {
        REG_STATUS -> mips.cop.regs.Status.value = value.ulong

        REG_LO -> mips.cpu.lo = value.ulong
        REG_HI -> mips.cpu.hi = value.ulong

        REG_EPC -> mips.cop.regs.EPC.value = value.ulong
        REG_CAUSE -> mips.cop.regs.Cause.value = value.ulong

        REG_PC -> {
            mips.cpu.branchCntrl.setIp(value.ulong clr 0)
            mips.cpu.iset = if (value.ulong[0].truth) InstructionSet.MIPS16 else InstructionSet.MIPS32
            // dirty hack to make possible reset exception bypassing IDA Pro
            mips.cpu.resetFault()
        }

        else -> mips.cpu.regs.write(index, value.ulong)
    }
}