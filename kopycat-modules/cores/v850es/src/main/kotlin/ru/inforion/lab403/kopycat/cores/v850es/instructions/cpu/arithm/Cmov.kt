package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.arithm

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagCondition
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Cmov(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "cmov"

    override fun execute() {
        val op1Data = if(op1 is Immediate) op1.ssext(core) else op1.value(core)
        if(FlagCondition.CheckCondition(core, op4.value(core)))
            op3.value(core, op1Data)
        else
            op3.value(core, op2.value(core))
    }
}