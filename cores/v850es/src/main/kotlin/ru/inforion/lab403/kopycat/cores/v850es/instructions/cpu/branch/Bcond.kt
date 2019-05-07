package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagCondition
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR.pc
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 27.05.17.
 */

class Bcond(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.COND_JUMP, size, *operands) {
    override val mnem = "bcond"

    //Format III - disp, cond
    override fun execute() {
        // size add in CPU execute
        if (FlagCondition.CheckCondition(core, op2.value(core)))
            pc.value(core, op1.value(core) + pc.value(core) - size)
    }
}
