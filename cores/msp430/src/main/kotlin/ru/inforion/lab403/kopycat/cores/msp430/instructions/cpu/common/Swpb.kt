package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.common

import ru.inforion.lab403.common.extensions.swap16
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by shiftdj on 13/02/18.
 */

class Swpb(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "swpb"

    override fun execute() = op1.value(core, op1.value(core).swap16())
}