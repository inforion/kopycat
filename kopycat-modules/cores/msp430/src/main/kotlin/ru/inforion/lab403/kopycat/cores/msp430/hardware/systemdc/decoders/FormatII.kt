package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.msp430.constructor
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core




class FormatII(core: MSP430Core, val construct:  constructor) : MSP430Decoder(core) {

    override fun decode(s: Long): AMSP430Instruction {
        val aSrc = s[5..4].asInt
        val dtype = if (s[6] == 1L) BYTE else WORD

        val regInd = s[3..0].asInt
        val nextWord = s[31..16]

        val isImm = ((aSrc == 0b01) and (regInd != MSP430GPR.r3.id)) or ((aSrc == 0b11) and (regInd == MSP430GPR.r0.id))
        val size = if (isImm) 4 else 2

        val op = decodeFirstOp(aSrc, regInd, nextWord, dtype, size.toLong())


        return construct(core, size, arrayOf(op))
    }

}