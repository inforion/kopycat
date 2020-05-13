package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.logic

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Sar(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "sar"

    override fun execute() {
//        val res = op2.value(cpu) shr op1.value(cpu).toInt()
//        op2.value(cpu, res)
//        FlagProcessor.processShrSarFlag(cpu, res, op1.value(cpu), op2.value(cpu), op1.dtyp, op1.dtyp, op2.dtyp)
        throw GeneralException("Not implemented")
    }
}