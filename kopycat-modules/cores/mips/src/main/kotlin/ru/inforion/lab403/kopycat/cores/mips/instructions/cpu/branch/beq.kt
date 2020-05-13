package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * BEQ rs, rt, offset
 *
 * To compare GPRs then do a PC-relative conditional branch
 */
class beq(core: MipsCore,
          data: Long,
          rs: MipsRegister<*>,
          rt: MipsRegister<*>,
          off: MipsNear) : RsRtOffsetInsn(core, data, Type.COND_JUMP, rs, rt, off) {

    override val mnem = "beq"

    override fun execute() {
        core.cpu.branchCntrl.validate()
        if (vrs == vrt) {
            core.cpu.branchCntrl.schedule(address)
        } else {
            core.cpu.branchCntrl.nop()
        }
    }

}