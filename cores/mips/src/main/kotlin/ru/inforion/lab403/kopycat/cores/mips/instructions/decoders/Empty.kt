package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 03.06.2016.
 *
 * RDHWR, RDPGPR, WRPGPR, WSBH, SEB, SEH
 */

class Empty(core: MipsCore, val construct: (MipsCore, Long) -> AMipsInstruction) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction = construct(core, data)
}