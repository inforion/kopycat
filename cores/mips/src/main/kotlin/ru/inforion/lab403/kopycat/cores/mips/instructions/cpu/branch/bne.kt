package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * BNE rs, rt, offset
 */
class bne(
        core: MipsCore,
        data: Long,
        rs: MipsRegister<*>,
        rt: MipsRegister<*>,
        off: MipsNear) : RsRtOffsetInsn(core, data, Type.COND_JUMP, rs, rt, off) {

    override val mnem = "bne"

    override fun execute() {
        core.cpu.branchCntrl.validate()
        if (vrs != vrt) {
            core.cpu.branchCntrl.schedule(address)
        } else {
            core.cpu.branchCntrl.nop()
        }
    }
}