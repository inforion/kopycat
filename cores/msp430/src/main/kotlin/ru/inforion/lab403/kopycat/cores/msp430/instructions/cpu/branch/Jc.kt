package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.enums.Condition
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagCondition
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core


class Jc(core: MSP430Core, size: Int, val cond : Condition, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "j${cond.mnem}"

    override fun execute() {
        if(FlagCondition.CheckCondition(core, cond))
            core.cpu.regs.r0ProgramCounter += 2 * op1.value(core)
    }
}