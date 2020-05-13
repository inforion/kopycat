package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * MOVZ rd, rs, rt
 */
class movz(core: MipsCore,
           data: Long,
           rd: GPR,
           rs: GPR,
           rt: GPR) : RdRsRtInsn(core, data, Type.VOID, rd, rs, rt) {

    override val mnem = "movz"

    override fun execute() {
        if (vrt == 0L) {
            vrd = vrs
        }
    }
}

