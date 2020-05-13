package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.common

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core




class Push(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "push"

    override fun execute() {
        val stack = core.cpu.regs.r1StackPointer - 2
        core.cpu.regs.r1StackPointer = stack
        core.write(op1.dtyp, stack, op1.value(core))
    }
}