package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * To do a boolbitman logical AND
 *
 * The contents of GPR rs are combined with the contents of GPR rt in a boolbitman logical AND operation.
 * The result is placed into GPR rd.
 */
class andr(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, Type.VOID, rd, rs, rt) {

    override val mnem = "and"

    override fun execute() {
        vrd = vrs and vrt
    }
}