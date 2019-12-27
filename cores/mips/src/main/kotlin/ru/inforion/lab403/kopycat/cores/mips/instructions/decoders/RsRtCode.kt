package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 03.06.2016.
 *
 * TEQ, TGE, TGEU, TLT, TLTU, TNE
 */

class RsRtCode(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rs = data[25..21].toInt()
        val rt = data[20..16].toInt()
        val code = data[15..6]
        return construct(core, data,
                GPR(rs),
                GPR(rt),
                MipsImmediate(code))
    }
}