package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.logic

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class Rrc(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "rrc"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        val op = op1.value(core) or (core.cpu.flags.c.toLong() shl op1.dtyp.bits)
        result.value(core, op ushr 1)
        FlagProcessor.processShiftFlag(core, result, (op and 1).toBool())
        op1.value(core, result)
    }
}