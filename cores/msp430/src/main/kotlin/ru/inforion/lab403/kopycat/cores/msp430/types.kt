package ru.inforion.lab403.kopycat.cores.msp430

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.enums.Condition
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 6/02/18.
 */

//TODO: lower case for type makes some ambiguity
typealias constructor = (MSP430Core, Int, Array<AOperand<MSP430Core>>) -> AMSP430Instruction
typealias constructorCond = (MSP430Core, Int, Condition, Array<AOperand<MSP430Core>>) -> AMSP430Instruction
typealias MSP430Operand = AOperand<MSP430Core>