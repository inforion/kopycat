package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * wsbh rd, rt
 *
 * To swap the bytes within each halfword of GPR rt and store the value into GPR rd.
 */
class wsbh(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rt: GPR) : RdRtInsn(core, data, Type.VOID, rd, rt) {

    override val mnem = "wsbh"

    override fun execute() {
//        rd = rt[23..16].shl(16) or rt[31..24].shl(24) or rt[7..0] or rt[15..8].shl(8)
        vrd = vrt[31..24].shl(16) or vrt[23..16].shl(24) or vrt[15..8] or vrt[7..0].shl(8)
    }

}