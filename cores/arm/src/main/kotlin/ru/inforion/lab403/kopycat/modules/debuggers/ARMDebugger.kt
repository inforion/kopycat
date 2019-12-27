package ru.inforion.lab403.kopycat.modules.debuggers

import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.common.extensions.swap32
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Endian
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.ARMv6Core
import java.util.logging.Level

/**
 * Created by a.gladkikh on 13.01.18.
 */

class ARMDebugger(parent: Module, name: String, val endian: Endian): Debugger(parent, name) {

    constructor(parent: Module, name: String) : this(parent, name, Endian.LITTLE)

    companion object {
        val log = logger(Level.WARNING)

        const val GDB_REGS_COUNT = 26
    }

    inline val cpu get() = core.cpu as AARMCPU

    override fun ident() = "arm"

    override fun registers() = Array(GDB_REGS_COUNT) { regRead(it) }.toList()

    override fun regRead(index: Int): Long {
        val value = when (index) {
            // GPR
            in 0..14 -> {
                val tmpIndex = if (index == 13) cpu.StackPointerSelect() else index
                cpu.reg(tmpIndex)
            }
            // PC
            15 -> {
                val pc = cpu.reg(index)
                if (cpu.CurrentInstrSet() == AARMCore.InstructionSet.THUMB) (pc set 0) else pc
            }
            // FPU
            in 16..24 -> {
                0
            }
            // CPSR
            25 -> {
                if (core is ARMv6Core) cpu.sregs.apsr else cpu.sregs.cpsr
            }
            else -> {
                log.severe { "Reading unknown register index = $index" }
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
            in 0..14 -> {
                val tmpIndex = if (index != 13) index else cpu.StackPointerSelect()
                cpu.reg(tmpIndex, dataToWrite)
            }
            // PC
            15 -> {
                cpu.reg(index, value)
                // dirty hack to make possible reset exception bypassing IDA Pro
                core.cpu.exception = null
            }
            // FPU
            in 16..24 -> {

            }
            // CPSR
            25 -> if (core is ARMv6Core)
                cpu.sregs.apsr = dataToWrite
            else
                cpu.sregs.cpsr = dataToWrite

            else -> log.severe { "Writing unknown register index = $index" }
        }
    }
}