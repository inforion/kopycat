package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.arithm

import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class Sub(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "sub"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        val data = op2.value(core) + (op1.value(core).inv() like op1.dtyp) + 1
        result.value(core, data)
        FlagProcessor.processArithmFlag(core, result, op1, op2, true)
        op2.value(core, result)
    }
}