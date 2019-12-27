package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.common

import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 13/02/18.
 */

class Sxt(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "sxt"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        val res = signext(op1.value(core), 8).toLong()
        result.value(core, res)
        FlagProcessor.processSxtFlag(core, result)
        op1.value(core, result)
    }
}