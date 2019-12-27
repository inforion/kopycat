package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtRsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * SRLV rd, rt, rs
 */
class srlv(core: MipsCore,
           data: Long,
           rd: GPR,
           rt: GPR,
           rs: GPR) : RdRtRsInsn(core, data, Type.VOID, rd, rt, rs) {

    override val mnem = "srlv"

    override fun execute() {
        val s = vrs[4..0].asInt
        vrd = vrt ushr s
    }
}