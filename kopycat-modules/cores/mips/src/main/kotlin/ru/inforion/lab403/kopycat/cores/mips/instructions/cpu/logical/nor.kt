package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * NOR rd, rs, rt
 *
 * To do a boolbitman logical NOT OR
 *
 * The contents of GPR rs are combined with the contents of GPR rt in a boolbitman logical NOR operation.
 * The result is placed into GPR rd.
 */
class nor(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, Type.VOID, rd, rs, rt) {

    override val mnem = "nor"

    override fun execute() {
        vrd = (vrs or vrt).inv()
    }

}