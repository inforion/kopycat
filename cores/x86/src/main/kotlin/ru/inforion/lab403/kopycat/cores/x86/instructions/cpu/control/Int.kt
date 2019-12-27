package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86COP
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 14.12.16.
 */

class Int(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>): AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "int"

    override fun execute() {
        val interruptId = op1.value(core)
        (core.cop as x86COP).INT = true
        core.cop.IRQ = interruptId.asInt
    }
}