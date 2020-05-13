package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtRsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * ROTRV rd, rt, rs
 *
 * To execute a logical right-rotate of a word by a variable id of bits
 */
class rotrv(core: MipsCore,
            data: Long,
            rd: GPR,
            rt: GPR,
            rs: GPR) : RdRtRsInsn(core, data, Type.VOID, rd, rt, rs) {

    override val mnem = "rotrv"

    override fun execute() {
        val s = vrs[4..0].toInt()
        vrd = vrt[s - 1..0].shl(32 - s) or vrt[31..s]
    }
}