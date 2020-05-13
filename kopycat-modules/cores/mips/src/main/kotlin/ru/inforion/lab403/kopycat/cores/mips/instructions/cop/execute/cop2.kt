package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.execute

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.Cofun25BitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 *
 * COP2 func
 */
class cop2(
        core: MipsCore,
        data: Long,
        cofun: MipsImmediate) : Cofun25BitInsn(core, data, Type.VOID, cofun) {

    override val mnem = "cop2"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}
