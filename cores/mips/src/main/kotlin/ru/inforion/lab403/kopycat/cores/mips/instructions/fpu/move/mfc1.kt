package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRdSelInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * MFC1 rt, fs
 */
class mfc1(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rd: MipsRegister<*>
) : RtRdSelInsn(core, data, Type.VOID, rt, rd) {

    override val mnem = "mfc1"

    override fun execute() {
        vrt = vrd
    }
}

