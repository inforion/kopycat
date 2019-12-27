package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * ANDI rt, rs, immediate
 *
 * To do a boolbitman logical AND with a constant
 *
 * The 16-bit immediate is zero-extended to the left and combined with the contents of GPR rs in a boolbitman
 * logical AND operation. The result is placed into GPR rt.
 */
class andi(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm)  {

    override val mnem = "andi"

    override fun execute() {
        vrt = vrs and imm.zext
    }
}