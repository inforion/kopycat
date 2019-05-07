package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class Rs(core: MipsCore, val construct: (MipsCore, Long, GPR) -> AMipsInstruction) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        val rs = data[25..21].toInt()
        return construct(core, data, GPR(rs))
    }
}