package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class FormatVI(core: v850ESCore, val construct: constructor, val isSignExt: Boolean) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val reg1 = v850esRegister.gpr(s[4..0].asInt)
        val reg2 = v850esRegister.gpr(s[15..11].asInt)
        val value = if (isSignExt) signext(s[31..16], 16).asLong else s[31..16]
        val imm = v850esImmediate(Datatype.DWORD, value, isSignExt)

        return construct(core, 4, arrayOf(reg1, reg2, imm))
    }
}