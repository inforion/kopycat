package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by ra on 03.06.2016.
 *
 * B, BEQ, BEQL, BNE, BNEL
 */

class RsRtOffset(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR, MipsNear) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rs = data[25..21].toInt()
        val rt = data[20..16].toInt()
        val offset = signext(data[15..0] shl 2, n = 18)
        return construct(core, data,
                GPR(rs),
                GPR(rt),
                MipsNear(offset))
    }
}