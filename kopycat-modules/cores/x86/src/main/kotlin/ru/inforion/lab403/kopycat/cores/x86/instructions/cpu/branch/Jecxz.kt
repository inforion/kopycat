package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Jecxz(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.COND_JUMP, opcode, prefs, operand) {
    override val mnem = "jecxz"

    override fun execute() {
        val reg = if(prefs.is16BitAddressMode) core.cpu.regs.cx else core.cpu.regs.ecx
        if (reg == 0L) core.cpu.regs.eip += op1.ssext(core)
    }
}