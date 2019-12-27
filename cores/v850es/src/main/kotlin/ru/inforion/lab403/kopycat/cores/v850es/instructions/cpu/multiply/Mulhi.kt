package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.multiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 29.05.17.
 */

class Mulhi(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "mulhi"

    // Format VI - reg1, reg2, imm
    override fun execute() {
        val res = op3.value(core)[15..0] * op1.value(core)[15..0]
        op2.value(core, res)
    }
}