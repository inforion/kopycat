package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.setBit
import ru.inforion.lab403.kopycat.cores.mips.enums.Status
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 *
 * EI rt
 */
class ei(core: MipsCore,
         data: Long,
         rt: GPR) : RtInsn(core, data, Type.VOID, rt) {

    override val mnem = "ei"

    override fun execute() {
        vrt = cop0.regs.Status
        cop0.regs.Status = setBit(cop0.regs.Status, Status.IE.pos)
    }
}