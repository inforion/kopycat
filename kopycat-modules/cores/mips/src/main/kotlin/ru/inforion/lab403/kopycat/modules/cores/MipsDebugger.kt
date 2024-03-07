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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Endian
import ru.inforion.lab403.kopycat.cores.mips.enums.InstructionSet
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsCPU
import ru.inforion.lab403.kopycat.modules.BUS32
import java.math.BigInteger


class MipsDebugger(
    parent: Module,
    name: String,
    dbgAreaSize: ULong = BUS32,
    val endian: Endian = Endian.LITTLE,
) : Debugger(parent, name, dbgAreaSize = dbgAreaSize) {
    companion object {
        @Transient
        val log = logger(WARNING)

        const val REG_CP0_STATUS = 0x20
        const val REG_LO = 0x21
        const val REG_HI = 0x22
        const val REG_BADVADDR = 0x23
        const val REG_CAUSE = 0x24
        const val REG_PC = 0x25

        const val REG_TOTAL = 0x26
    }

    private inline val mips get() = core as MipsCore

    override fun ident() : String = "mips"

    override fun target(): String = when (mips.cpu.mode) {
        MipsCPU.Mode.R32 -> super.target()
        MipsCPU.Mode.R64 -> when (endian) {
            Endian.LITTLE -> super.target()
            Endian.BIG -> "mips64-linux.xml"
        }
    }

    override fun registers(): List<BigInteger> = Array(REG_TOTAL) { regRead(it) }.toMutableList()

    override fun regSize(index: Int) = if (mips.cpu.mode == MipsCPU.Mode.R32) Datatype.DWORD else Datatype.QWORD

    override fun regRead(index: Int): BigInteger = when (index) {
        REG_CP0_STATUS -> mips.cop.regs.Status.value.bigint
        REG_LO -> mips.cpu.lo.bigint
        REG_HI -> mips.cpu.hi.bigint
        REG_BADVADDR -> mips.cop.regs.BadVAddr.value.bigint
        REG_CAUSE -> mips.cop.regs.Cause.value.bigint
        REG_PC -> if (mips.cpu.iset == InstructionSet.MIPS32) mips.cpu.pc.bigint else (mips.cpu.pc.bigint set 0)
        else -> mips.cpu.regs.read(index).bigint
    }.let {
        when (endian) {
            Endian.BIG -> when (mips.cpu.mode) {
                MipsCPU.Mode.R32 -> it.swap32()
                MipsCPU.Mode.R64 -> it.swap64()
            }
            else -> it
        }
    }

    override fun regWrite(index: Int, value: BigInteger) {
        val dataToWrite = when (endian) {
            Endian.BIG -> when (mips.cpu.mode) {
                MipsCPU.Mode.R32 -> value.ulong.swap32()
                MipsCPU.Mode.R64 -> value.ulong.swap64()
            }
            else -> value.ulong
        }

        when (index) {
            REG_CP0_STATUS -> mips.cop.regs.Status.value = dataToWrite

            REG_LO -> mips.cpu.lo = dataToWrite
            REG_HI -> mips.cpu.hi = dataToWrite

            REG_BADVADDR -> mips.cop.regs.BadVAddr.value = dataToWrite
            REG_CAUSE -> mips.cop.regs.Cause.value = dataToWrite

            REG_PC -> {
                mips.cpu.branchCntrl.setIp(dataToWrite clr 0)
                mips.cpu.iset = if (dataToWrite[0].truth) InstructionSet.MIPS16 else InstructionSet.MIPS32
                // dirty hack to make possible reset exception bypassing IDA Pro
                mips.cpu.resetFault()
            }

            else -> mips.cpu.regs.write(index, dataToWrite)
        }
    }
}
