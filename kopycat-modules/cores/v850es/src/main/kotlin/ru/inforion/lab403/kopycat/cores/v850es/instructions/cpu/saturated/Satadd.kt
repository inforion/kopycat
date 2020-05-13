package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.saturated

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Satadd(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "satadd"

    override fun execute() {
//        FlagProcessor.processSatFlag(cpu, res, op1.value(cpu), op2.value(cpu), op1.dtyp, op1.dtyp, op2.dtyp)
        throw GeneralException("Not implemented")
    }
}