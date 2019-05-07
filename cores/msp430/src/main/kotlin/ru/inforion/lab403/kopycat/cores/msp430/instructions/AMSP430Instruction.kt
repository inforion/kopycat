package ru.inforion.lab403.kopycat.cores.msp430.instructions

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by shiftdj on 6/02/18.
 */

abstract class AMSP430Instruction(
        core: MSP430Core,
        type: Type,
        override val size: Int,
        vararg operands: AOperand<MSP430Core>) : AInstruction<MSP430Core>(core, type, *operands)