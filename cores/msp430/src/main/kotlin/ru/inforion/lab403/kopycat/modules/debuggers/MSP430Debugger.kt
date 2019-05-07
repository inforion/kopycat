package ru.inforion.lab403.kopycat.modules.debuggers

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

class MSP430Debugger(parent: Module, name: String): Debugger(parent, name) {
    override fun ident() = "msp430"

    override fun registers(): MutableList<Long> {
        val core = core as MSP430Core
        val gprRegs = Array(core.cpu.regs.count()) { regRead(it) }
        val flags = Array(core.cpu.flags.count()) { core.cpu.regs.r2StatusRegister[it] }
        val result = gprRegs.toMutableList()
        result.addAll(flags)
        return result
    }
}