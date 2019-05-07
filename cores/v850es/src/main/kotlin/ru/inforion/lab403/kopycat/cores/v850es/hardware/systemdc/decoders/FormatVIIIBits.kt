package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esDisplacement
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 23.05.17.
 */

class FormatVIIIBits(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val bit = v850esImmediate(Datatype.BYTE, s[13..11], false)
        val reg = v850esRegister.gpr(s[4..0].toInt())
        val imm = v850esImmediate(Datatype.WORD, signext(s[31..16], 16).asLong, true)
        val displ = v850esDisplacement(Datatype.BYTE, reg, imm)
        return construct(core, 4, arrayOf(displ, bit))
    }
}