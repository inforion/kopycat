package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esDisplacement
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 23.05.17.
 */

class FormatIXBits(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val reg1 = v850esRegister.gpr(s[4..0].asInt)
        val displ = v850esDisplacement(Datatype.BYTE, reg1)
        val reg2 = v850esRegister.gpr(s[15..11].asInt)

        return construct(core, 4, arrayOf(displ, reg2))
    }
}
