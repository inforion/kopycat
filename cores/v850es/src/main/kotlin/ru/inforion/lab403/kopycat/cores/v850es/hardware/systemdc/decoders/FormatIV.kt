package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esDisplacement
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR.r30
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 23.05.17.
 */

class FormatIV(core: v850ESCore, val construct: constructor, val dtyp: Datatype, val range: IntRange, val offset: Int) :
        ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val imm = v850esImmediate(Datatype.DWORD, s[range] shl offset, true)
        val disp = v850esDisplacement(dtyp, r30, imm)
        val reg2 = v850esRegister.gpr(s[15..11].asInt)

        return construct(core, 2, arrayOf(reg2, disp))
    }
}