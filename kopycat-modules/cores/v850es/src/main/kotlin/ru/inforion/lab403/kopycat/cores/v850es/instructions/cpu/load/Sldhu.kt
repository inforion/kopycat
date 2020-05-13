package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.load

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Sldhu(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "sld.hu"

    // Format IV - reg, disp
    override fun execute() = op1.value(core, op2.zext(core))
}