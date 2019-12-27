package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by a.gladkikh on 22.07.17.
 */

class FormatIXCR(core: v850ESCore, val construct: constructor, val store: Boolean) : ADecoder<AV850ESInstruction>(core) {
    override fun decode(s: Long): AV850ESInstruction {
        return if (store) {
            val regId = v850esRegister.creg(s[4..0].asInt)
            val reg2 = v850esRegister.gpr(s[15..11].asInt)
            construct(core, 4, arrayOf(regId, reg2))
        } else {
            val regId = v850esRegister.creg(s[15..11].asInt)
            val reg2 = v850esRegister.gpr(s[4..0].asInt)
            construct(core, 4, arrayOf(reg2, regId))
        }
    }
}
