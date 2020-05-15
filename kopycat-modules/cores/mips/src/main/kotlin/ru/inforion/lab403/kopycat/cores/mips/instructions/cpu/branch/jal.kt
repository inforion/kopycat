package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.IndexInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * JAL target
 */
class jal(core: MipsCore,
          data: Long,
          index: MipsNear) : IndexInsn(core, data, Type.CALL, index) {

    override val mnem = "jal"

    override fun execute() {
        core.cpu.branchCntrl.schedule(address)
        vra = eaAfterBranch
    }
}
