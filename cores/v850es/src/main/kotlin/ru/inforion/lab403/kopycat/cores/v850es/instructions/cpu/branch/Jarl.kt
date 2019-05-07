package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 29.05.17.
 */

class Jarl(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.CALL, size, *operands) {
    override val mnem = "jarl"

    // Format V - disp, reg2
    override fun execute() {
        val a1 = GPR.pc.value(core) + size
        op2.value(core, a1)
        val res = op1.value(core) + GPR.pc.value(core)
        // size add in CPU execute
        GPR.pc.value(core, res - size)
    }
}