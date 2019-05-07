package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core


/**
 * Created by shiftdj on 13/02/18.
 */

class Call(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "call"

    override fun execute() {
        core.cpu.regs.r1StackPointer -= 2
        core.outw(core.cpu.regs.r1StackPointer, core.cpu.regs.r0ProgramCounter)
        core.cpu.regs.r0ProgramCounter = op1.value(core)
    }
}