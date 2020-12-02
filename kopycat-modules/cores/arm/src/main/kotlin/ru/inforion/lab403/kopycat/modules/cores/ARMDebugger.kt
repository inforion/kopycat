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

import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.common.extensions.swap32
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Endian
import java.util.logging.Level



class ARMDebugger(parent: Module, name: String, val endian: Endian): Debugger(parent, name) {

    constructor(parent: Module, name: String) : this(parent, name, Endian.LITTLE)

    companion object {
        @Transient val log = logger(Level.WARNING)

        const val GDB_REGS_COUNT = 42
    }

    inline val cpu get() = core.cpu as AARMCPU

    override fun ident() = "arm"

    override fun registers() = Array(GDB_REGS_COUNT) { regRead(it) }.toList()

    private var wrongRegisterIndex = mutableSetOf<Int>()

    override fun regRead(index: Int): Long {
        val value = when (index) {
            // GPR
            in 0..14 -> cpu.reg(index)
            // PC
            15 -> {
                val pc = cpu.reg(index)
                if (cpu.CurrentInstrSet() == AARMCore.InstructionSet.THUMB) (pc set 0) else pc
            }
            // FPU
            in 16..24 -> 0
            // CPSR
            25 -> cpu.flags()
            else -> {
                // TODO: https://youtrack.lab403.inforion.ru/issue/KC-1600
                if (wrongRegisterIndex.add(index))
                    log.severe { "Reading unknown register index = $index -> This message will be print only once!" }
                0
            }
        }
        val dataToRead = if (endian == Endian.BIG) value.swap32() else value
//        log.warning { "Read ${dataToRead.hex8} from $index" }
        return dataToRead
    }

    override fun regWrite(index: Int, value: Long) {
        val dataToWrite = if (endian == Endian.BIG) value.swap32() else value
        when (index) {
            // GPR
            in 0..14 -> cpu.reg(index, dataToWrite)
            // PC
            15 -> {
                cpu.reg(index, value)
                // dirty hack to make possible reset exception bypassing IDA Pro
                core.cpu.resetFault()
            }
            // FPU
            in 16..24 -> {

            }
            // CPSR
            25 -> if (core is ARMv6MCore)
                cpu.sregs.apsr.value = dataToWrite
            else
                cpu.sregs.cpsr.value = dataToWrite

            else -> log.severe { "Writing unknown register index = $index" }
        }
    }
}