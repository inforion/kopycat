package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * RDHWR, RDPGPR, WRPGPR, WSBH, SEB, SEH
 */

class RdRt(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rt = data[20..16].toInt()
        val rd = data[15..11].toInt()
        return construct(core, data,
                GPR(rd),
                GPR(rt))
    }
}