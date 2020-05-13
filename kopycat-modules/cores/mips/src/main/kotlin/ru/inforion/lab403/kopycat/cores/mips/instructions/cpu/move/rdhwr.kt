package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRdSelInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * RDHWR rt,rd
 */
class rdhwr(core: MipsCore,
            data: Long,
            rt: GPR,
            rd: MipsRegister<*>
) : RtRdSelInsn(core, data, Type.VOID, rt, rd) {

    override val mnem = "rdhwr"

    override fun execute() {
        vrt = vrd
    }
}