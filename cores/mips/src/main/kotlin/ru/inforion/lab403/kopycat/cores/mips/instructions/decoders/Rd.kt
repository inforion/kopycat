package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03.06.2016.
 *
 * MFHI, MFLO
 */

class Rd(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR) -> AMipsInstruction
) : ADecoder(core) {

//    var rd: Long by LongOperandField(1)

    override fun decode(data: Long): AMipsInstruction {
        val rd = data[15..11].toInt()
        return construct(core, data, GPR(rd))
    }
}