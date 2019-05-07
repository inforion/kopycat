package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.multiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 29.05.17.
 */

class Mulh(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "mulh"

    // Format I - reg1, reg2
    // Format II - imm, reg2
    override fun execute() {
        val res = if(op1 is Immediate) op2.value(core) * op1.value(core)
                    else op2.value(core)[15..0] * op1.value(core)[15..0]
        op2.value(core, res)
    }
}