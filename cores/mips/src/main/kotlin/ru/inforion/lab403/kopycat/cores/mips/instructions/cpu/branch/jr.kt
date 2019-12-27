package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.enums.eGPR
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * JR rs
 */
class jr(
        core: MipsCore,
        data: Long,
        rs: GPR) : RsInsn(core, data, Type.IND_JUMP, rs) {

    override val mnem = "jr"

    override val isRet: Boolean get() = rs.reg == eGPR.RA.id

    override fun execute() {
        core.cpu.branchCntrl.schedule(vrs)
    }
}
