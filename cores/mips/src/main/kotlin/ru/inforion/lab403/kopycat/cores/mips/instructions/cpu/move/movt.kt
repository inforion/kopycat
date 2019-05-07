package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsCcInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * MOVT rd, rs, cc
 */
class movt(core: MipsCore,
           data: Long,
           rd: GPR,
           rs: GPR,
           cc: MipsImmediate) : RdRsCcInsn(core, data, Type.VOID, rd, rs, cc) {

    override val mnem = "movt"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}

