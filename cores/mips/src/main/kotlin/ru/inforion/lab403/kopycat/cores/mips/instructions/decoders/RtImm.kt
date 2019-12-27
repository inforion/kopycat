package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 03.06.2016.
 *
 * LUI
 */

class RtImm(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        val rt = data[20..16].toInt()
        val imm = data[15..0]
        return construct(core, data,
                GPR(rt),
                MipsImmediate(imm))
    }
}