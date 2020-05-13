package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Je(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>): AX86Instruction(core, Type.COND_JUMP, opcode, prefs, operand) {
    override val mnem = "je"

    override fun execute() {
        if(core.cpu.flags.zf)
            core.cpu.regs.eip += op1.ssext(core)
    }
}