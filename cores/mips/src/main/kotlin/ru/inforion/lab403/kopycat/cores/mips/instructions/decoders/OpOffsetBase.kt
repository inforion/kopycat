package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03.06.2016.
 *
 * CACHE, PREF
 */

class OpOffsetBase(
        core: MipsCore,
        val construct: (MipsCore, Long, MipsImmediate, MipsDisplacement) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val imm = data[20..16]
        val base = data[25..21].toInt()
        val offset = signext(data[15..0], n = 16)
        return construct(core, data,
                MipsImmediate(imm),
                MipsDisplacement(DWORD, base, offset))
        // what to do with it?
    }
}