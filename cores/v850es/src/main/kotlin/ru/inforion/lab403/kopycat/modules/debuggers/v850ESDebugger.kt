package ru.inforion.lab403.kopycat.modules.debuggers

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by v.davydov on 02.06.17.
 */
class v850ESDebugger(parent: Module, name: String): Debugger(parent, name) {
    override fun ident() = "v850es"

    override fun registers(): MutableList<Long> {
        val core = core as v850ESCore
        val gprRegs = Array(core.cpu.regs.count()) { k -> regRead(k) }
        val ctrlRegs = Array(core.cpu.cregs.count()) { k -> readCtrlRegister(core, k) }
        val flags = Array(core.cpu.flags.count()) { k -> readFlags(core, k) }
        val result = gprRegs.toMutableList()
        result.addAll(ctrlRegs)
        result.addAll(flags)

        return result
    }

    // TODO(): Fix readFlags and readCtrlRegister
    private fun readFlags(core: v850ESCore, index: Int): Long = core.cpu.cregs.psw[index]

    private fun readCtrlRegister(core: v850ESCore, index: Int): Long = core.cpu.cregs.readIntern(index)
}