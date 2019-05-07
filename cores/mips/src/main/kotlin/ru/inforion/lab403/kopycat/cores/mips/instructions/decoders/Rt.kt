package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by the bat on 03.06.2016.
 *
 * RDHWR, RDPGPR, WRPGPR, WSBH, SEB, SEH
 */

class Rt(core: MipsCore, val construct: (MipsCore, Long, GPR) -> AMipsInstruction) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        return construct(core, data, GPR(data[20..16].toInt()))
    }
}