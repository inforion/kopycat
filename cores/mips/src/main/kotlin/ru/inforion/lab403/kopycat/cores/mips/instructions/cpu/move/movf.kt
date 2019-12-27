package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.move

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsCcInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * MOVF rd, rs, cc
 */
class movf(core: MipsCore,
           data: Long,
           rd: GPR,
           rs: GPR,
           cc: MipsImmediate) : RdRsCcInsn(core, data, Type.VOID, rd, rs, cc) {

    override val mnem = "movf"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}
