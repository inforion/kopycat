package ru.inforion.lab403.kopycat.modules.debuggers

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.mips.enums.SRVC
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.util.logging.Level

/**
 * Created by batman on 05/06/16.
 */
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

        // damned ida wants PC reg by index 0x25 and 0x27!
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