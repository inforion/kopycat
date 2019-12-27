package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.logic

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 29.05.17.
 */

class Zxb(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "zxb"

    // Format I - reg1, reg2
    override fun execute() = op1.value(core, op1.value(core)[7..0])
}