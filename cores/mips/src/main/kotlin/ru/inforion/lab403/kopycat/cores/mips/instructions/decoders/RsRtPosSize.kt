package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03.06.2016.
 *
 * EXT, INS
 */

class RsRtPosSize(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR, MipsImmediate, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rs = data[25..21].toInt()
        val rt = data[20..16].toInt()
        val msb = data[15..11]
        val lsb = data[10..6]
        return construct(core, data,
                GPR(rt),
                GPR(rs),
                MipsImmediate(lsb),
                MipsImmediate(msb))
    }
}