package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.jtag

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code20bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * SDBBP code
 */
class sdbbp(
        core: MipsCore,
        data: Long,
        imm: MipsImmediate) : Code20bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "sdbbp"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}
