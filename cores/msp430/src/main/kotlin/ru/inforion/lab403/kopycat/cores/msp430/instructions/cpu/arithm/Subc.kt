package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.arithm

import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 13/02/18.
 */

class Subc(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "subc"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        result.value(core, op2.value(core) + signext(-op1.value(core), op1.dtyp.bits) + core.cpu.flags.c.toLong())
        FlagProcessor.processArithmFlag(core, result, op1, op2, true)
        op2.value(core, result)
    }
}