package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.store

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Ssth(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "sst.h"

    // Format IV - reg, disp
    override fun execute() = op2.value(core, op1.value(core)[15..0])
}