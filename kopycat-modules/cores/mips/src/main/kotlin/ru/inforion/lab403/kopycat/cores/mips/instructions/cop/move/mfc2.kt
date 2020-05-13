package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRdSelInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * MFC2 rt, rd, sel
 */
class mfc2(core: MipsCore,
           data: Long,
           rt: GPR,
           rd: MipsRegister<*>) : RtRdSelInsn(core, data, Type.VOID, rt, rd) {

    override val mnem = "mfc2"

    override fun execute() {
        vrt = vrd
    }
}
