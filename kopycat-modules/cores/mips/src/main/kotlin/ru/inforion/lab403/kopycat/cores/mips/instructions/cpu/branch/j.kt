package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.IndexInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * J target
 */
class j(core: MipsCore,
        data: Long,
        index: MipsNear) : IndexInsn(core, data, Type.JUMP, index) {

    override val mnem = "j"

    override fun execute() {
        core.cpu.branchCntrl.schedule(address)
    }
}

