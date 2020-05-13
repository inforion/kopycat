package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * COP2
 */

abstract class Cofun25Bit(
        core: MipsCore,
        val construct: (MipsCore, Long, MipsImmediate) -> AMipsInstruction
) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        val code = data[24..0]
        return construct(core, data, MipsImmediate(code))
    }
}
