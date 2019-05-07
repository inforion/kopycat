package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtPosSizeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * ins rt, rs, lsb, size
 *
 * To merge a right-justified bit field from GPR rs into a specified field in GPR rt.
 */
class ins(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        pos: MipsImmediate,
        siz: MipsImmediate) : RsRtPosSizeInsn(core, data, Type.VOID, rt, rs, pos, siz) {

    override val mnem = "ins"

    override fun execute() {
        vrs = if (lsb <= msb) {
            val high = vrs[31..msb + 1]
            val inserted = vrt[msb - lsb..0]
            val low = vrs[lsb - 1..0]
            high.shl(msb + 1) or inserted.shl(lsb) or low
        } else 0
    }

}