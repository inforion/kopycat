package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.kopycat.device.TestCPU
import java.util.logging.Level.WARNING

class InstructionTableTest(parent: Module, name: String): Debugger(parent, name) {
    inline val cpu get() = core.cpu as TestCPU
    override fun ident() = "test"
}