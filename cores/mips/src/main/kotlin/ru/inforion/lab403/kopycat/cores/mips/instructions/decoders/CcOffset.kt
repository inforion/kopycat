package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by ra on 03.06.2016.
 *
 * BC1F, BC1FL, BC1T, BC1TL, BC2F, BC2FL, BC2T, BC2TL
 */

class CcOffset(
        core: MipsCore,
        val construct: (MipsCore, Long, MipsImmediate, MipsNear) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val cc = data[20..18]
        if (cc != 0L) throw UnsupportedOperationException()
        val offset = signext(data[15..0] shl 2, n = 18)
        return construct(core, data, MipsImmediate(cc), MipsNear(offset))
    }
}