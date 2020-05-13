package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.logical

import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * XORI rt, rs, immediate
 *
 * To do a boolbitman logical XOR with a constant
 *
 * The 16-bit immediate is zero-extended to the left and combined with the contents of GPR rs in a boolbitman
 * logical XOR operation. The result is placed into GPR rt.
 */
class xori(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm)  {

//    override val isSigned = true
    override val mnem = "xori"

    override fun execute() {
        vrt = vrs xor imm.zext
    }
}