package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtRsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SRAV rd, rt, rs
 */
class srav(core: MipsCore,
           data: Long,
           rd: GPR,
           rt: GPR,
           rs: GPR) : RdRtRsInsn(core, data, Type.VOID, rd, rt, rs) {

    override val mnem = "srav"

    override fun execute() {
        val v = vrt.asInt
        val s = vrs[4..0].asInt
        vrd = (v shr s).asULong
    }
}