package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.move

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRdSelInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * MFHC1 rt, fs
 */
class mfhc1(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rd: MipsRegister<*>
) : RtRdSelInsn(core, data, Type.VOID, rt, rd) {

    override val mnem = "mfhc1"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}