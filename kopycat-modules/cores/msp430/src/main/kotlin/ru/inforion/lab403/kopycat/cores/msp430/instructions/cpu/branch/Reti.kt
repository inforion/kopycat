package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core




class Reti(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "reti"

    override fun execute() {
        val stack = core.cpu.regs.r1StackPointer
        core.cpu.regs.r2StatusRegister = core.inw(stack + 0)
        core.cpu.regs.r0ProgramCounter = core.inw(stack + 2)
        core.cpu.regs.r1StackPointer = stack + 4
    }
}