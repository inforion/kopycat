package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by a.gladkikh on 03/06/16.
 *
 * MTHI rs
 */
class mthi(core: MipsCore, data: Long = WRONGL, rs: GPR) : RsInsn(core, data, Type.VOID, rs) {

    override val mnem = "mthi"

    override fun execute() {
        hi = vrs
    }
}