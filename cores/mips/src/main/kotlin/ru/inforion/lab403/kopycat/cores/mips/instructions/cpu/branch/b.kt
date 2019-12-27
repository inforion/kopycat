package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * B offset
 */
class b(core: MipsCore,
        data: Long,
        rs: MipsRegister<*>,
        rt: MipsRegister<*>,
        off: MipsNear) : RsRtOffsetInsn(core, data, Type.COND_JUMP, rs, rt, off) {

    override val mnem = "b"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}