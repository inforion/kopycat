package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by ra on 03.06.2016.
 *
 * ROTR, ROTRV, SLL, SRA, SRAV, SRL, SRLV, EHB, NOP, SSNOP
 */
class RdRtSa(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rt = data[20..16].toInt()
        val rd = data[15..11].toInt()
        val sa = data[10..6]
        return construct(core, data,
                GPR(rd),
                GPR(rt),
                MipsImmediate(sa))
    }
}
