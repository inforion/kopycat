package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsHintInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * JAL.HB target
 */
class jalrhb(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        hint: MipsImmediate) : RdRsHintInsn(core, data, Type.IND_CALL, rd, rs, hint) {

    override val mnem = "jalr.hb"

    override fun execute() {
        // As usual rd is equal to $ra
        vrd = eaAfterBranch
        core.cpu.branchCntrl.schedule(vrs)
        // TODO: clear execution and instruction hazards
    }
}
