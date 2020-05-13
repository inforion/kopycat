package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore


class FormatXII(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val imm1 = v850esImmediate(Datatype.DWORD, signext(s[4..0], 5).asLong, true)
        val reg2 = v850esRegister.gpr(s[15..11].toInt())
        val reg3 = v850esRegister.gpr(s[31..27].toInt())
        val cond = v850esImmediate(Datatype.BYTE, s[20..17], false)

        return construct(core, 4, arrayOf(imm1, reg2, reg3, cond))
    }
}