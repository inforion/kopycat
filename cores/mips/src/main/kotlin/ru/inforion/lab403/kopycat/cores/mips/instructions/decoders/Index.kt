package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 03.06.2016.
 *
 * J, JAL
 */

class Index(
        core: MipsCore,
        val construct: (MipsCore, Long, MipsNear) -> AMipsInstruction
) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        val offset = (data[25..0] shl 2).toInt()
        return construct(core, data, MipsNear(offset))
    }
}