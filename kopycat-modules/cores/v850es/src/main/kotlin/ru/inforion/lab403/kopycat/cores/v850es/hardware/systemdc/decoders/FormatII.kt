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



class FormatII(core: v850ESCore, val construct: constructor, val isSignExt: Boolean) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val value = if (isSignExt) signext(s[4..0], 5).asLong else s[4..0]
        val imm = v850esImmediate(Datatype.DWORD, value, isSignExt)
        val reg2 = v850esRegister.gpr(s[15..11].toInt())

        return construct(core, 2, arrayOf(imm, reg2))
    }
}