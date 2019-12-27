package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * ADDIU rt, rs, immediate
 *
 * To add a constant to a 32-bit integer
 */
class addiu(
        core: MipsCore,
        data: Long = WRONGL,
        rt: GPR,
        rs: GPR,
        imm: MipsImmediate) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm) {

    override val mnem = "addiu"
//    override val isSigned = false

    override fun execute() {
        vrt = vrs + imm.usext
    }
}