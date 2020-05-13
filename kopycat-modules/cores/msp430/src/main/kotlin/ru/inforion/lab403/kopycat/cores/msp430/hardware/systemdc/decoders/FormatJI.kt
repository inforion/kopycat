package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.msp430.constructorCond
import ru.inforion.lab403.kopycat.cores.msp430.enums.Condition
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Immediate
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class FormatJI(core: MSP430Core, val construct:  constructorCond) : MSP430Decoder(core) {
    override fun decode(s: Long): AMSP430Instruction {
        val imm = MSP430Immediate(WORD, signext(s[9..0], 10).asLong, true)
        val cond = find<Condition> { it.opcode == s[12..10].asInt }
        return construct(core, 2, cond!!, arrayOf(imm))
    }
}