package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.logic

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 16/02/18.
 */

class And(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "and"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        result.value(core, op2.value(core) and op1.value(core))
        FlagProcessor.processLogicalFlag(core, result)
        op2.value(core, result)
    }
}