package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * ORI rt, rs, immediate
 *
 * To do a boolbitman logical OR with a constant
 *
 * The 16-bit immediate is zero-extended to the left and combined with the contents of GPR rs in a boolbitman
 * logical OR operation. The result is placed into GPR rt.
 */
class ori(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm)  {

//    override val isSigned = false
//    override val construct = ::ori
    override val mnem = "ori"

    override fun execute() {
        vrt = vrs or imm.zext
    }
}