package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtPosSizeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * ext rt, rs, lsb, size
 *
 * To extract a bit field from GPR rs and store it right-justified into GPR rt.
 */
class ext(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        pos: MipsImmediate,
        siz: MipsImmediate) : RsRtPosSizeInsn(core, data, Type.VOID, rt, rs, pos, siz) {

    override val mnem = "ext"

    override fun execute() {
        vrs = if (lsb + msb <= 31) vrt[(lsb + msb)..lsb] else 0
    }

}
