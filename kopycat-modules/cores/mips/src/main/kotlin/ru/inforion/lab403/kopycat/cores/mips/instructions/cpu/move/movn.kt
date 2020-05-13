package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * MOVN rd, rs, rt
 */
class movn(core: MipsCore,
           data: Long,
           rd: GPR,
           rs: GPR,
           rt: GPR) : RdRsRtInsn(core, data, Type.VOID, rd, rs, rt) {

    override val mnem = "movn"

    override fun execute() {
        if (vrt != 0L) {
            vrd = vrs
        }
    }
}

