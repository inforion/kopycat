package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.RsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * MTLO rs
 */
class mtlo(core: MipsCore,
           data: Long,
           rs: GPR) : RsInsn(core, data, Type.VOID, rs) {

    override val mnem = "mtlo"

    override fun execute() {
        lo = vrs
    }
}