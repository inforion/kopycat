package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 23.05.17.
 */

class FormatIXIRR(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val value = s[4..0].insert(s[22..18], 9..5)
        val imm = v850esImmediate(Datatype.DWORD, value, false)
        val reg2 = v850esRegister.gpr(s[15..11].asInt)
        val reg3 = v850esRegister.gpr(s[31..27].asInt)

        return construct(core, 4, arrayOf(imm, reg2, reg3))
    }
}