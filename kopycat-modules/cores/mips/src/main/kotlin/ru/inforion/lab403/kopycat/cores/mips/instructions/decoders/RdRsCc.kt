package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * MOVF, MOVT
 */

class RdRsCc(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        val rs = data[25..21].toInt()
        val cc = data[20..18]
        val rd = data[15..11].toInt()
        return construct(core, data,
                GPR(rd),
                GPR(rs),
                // TODO: Immediate(cc, FL_FSR or FL_READ)
                MipsImmediate(cc))
    }
}