package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.arithm

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core




class Rra(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "rra"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        //TODO: op1 fetch optimisation?
        val op = op1.value(core)
        result.value(core, op shr 1)
        FlagProcessor.processShiftFlag(core, result, (op and 1).toBool())
        op1.value(core, result)
    }
}