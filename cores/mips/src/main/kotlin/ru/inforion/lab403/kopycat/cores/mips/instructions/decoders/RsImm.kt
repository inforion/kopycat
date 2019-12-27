package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 03.06.2016.
 *
 * TEQI, TGEI, TGEUI, TLTI, TLTIU, TNEI
 */

class RsImm(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, MipsImmediate) -> AMipsInstruction,
        val signed: Boolean
) : ADecoder(core) {
    override fun decode(data: Long): AMipsInstruction {
        val rs = data[25..21].toInt()
        val imm = data[15..0]
        return construct(core, data,
                GPR(rs),
                MipsImmediate(imm, signed = signed))
    }
}