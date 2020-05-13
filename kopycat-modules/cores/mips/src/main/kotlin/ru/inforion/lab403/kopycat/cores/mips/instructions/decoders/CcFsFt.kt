package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * C.cond.fmt
 */

class CcFsFt(
        core: MipsCore,
        val construct: (MipsCore, Long, FPR, FPR, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val cond = data[3..0]
        val cc = data[10..8]
        if (cc != 0L) throw UnsupportedOperationException()
        val ft = data[20..16].toInt()
        val fs = data[15..11].toInt()
        return construct(core, data,
                FPR(fs),
                FPR(ft),
                MipsImmediate(cond))
    }
}