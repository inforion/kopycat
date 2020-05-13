package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRdSelInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * CTC1 rt, fs
 *
 * To copy a word from a GPR to an FPU control register
 */
class ctc1(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rd: MipsRegister<*>
) : RtRdSelInsn(core, data, Type.VOID, rt, rd) {

    override val mnem = "ctc1"

    override fun execute() {
        vrd = vrt
    }
}
