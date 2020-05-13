package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.store

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Stb(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "st.b"

    // Format VII - reg, disp
    override fun execute() = op2.value(core, op1)
}