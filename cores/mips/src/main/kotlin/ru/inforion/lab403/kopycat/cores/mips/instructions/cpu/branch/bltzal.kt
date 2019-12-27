package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.RsOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * BLTZAL rs, offset
 */
class bltzal(
        core: MipsCore,
        data: Long,
        rs: GPR,
        off: MipsNear) : RsOffsetInsn(core, data, Type.COND_JUMP, rs, off) {

    override val mnem = "bltzal"

    override fun execute() {
        core.cpu.branchCntrl.validate()
        vra = eaAfterBranch
        if (vrs.toInt() < 0) {
            core.cpu.branchCntrl.schedule(address)
        } else core.cpu.branchCntrl.nop()
    }
}