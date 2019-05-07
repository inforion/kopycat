package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by ra on 03.06.2016.
 *
 * CEIL.W.fmt,
 * CVT.D.fmt, CVT.S.fmt, CVT.W.fmt
 * FLOOR.W.fmt
 * ROUND.W.fmt
 * TRUNC.W.fmt
 * MOV.fmt
 */
class FdFs(
        core: MipsCore,
        val construct: (MipsCore, Long, FPR, FPR) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val fd = data[10..6].toInt()
        val fs = data[15..11].toInt()
        return construct(core, data, FPR(fd), FPR(fs))
    }
}
