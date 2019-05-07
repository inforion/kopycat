package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.clearBit
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.enums.Status
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by batman on 03/06/16.
 *
 * DI rt
 */
class di(core: MipsCore,
         data: Long,
         rt: GPR) : RtInsn(core, data, VOID, rt) {

    override val mnem = "di"

    override fun execute() {
        vrt = cop0.regs.Status
        cop0.regs.Status = clearBit(cop0.regs.Status, Status.IE.pos)
    }
}
