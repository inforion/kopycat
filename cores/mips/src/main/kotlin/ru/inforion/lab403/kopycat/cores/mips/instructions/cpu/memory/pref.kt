package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.mips.instructions.OpOffsetBaseInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * PREF hint,offset(base)
 */
class pref(core: MipsCore,
           data: Long,
           imm: MipsImmediate,
           off: MipsDisplacement) : OpOffsetBaseInsn(core, data, Type.VOID, imm, off) {

    override val mnem = "pref"

    override fun execute() {
//        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}