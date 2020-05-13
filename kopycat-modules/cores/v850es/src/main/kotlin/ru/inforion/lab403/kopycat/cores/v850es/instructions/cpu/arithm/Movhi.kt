package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.arithm

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Movhi(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "movhi"

    // Format VI - reg1, reg2, imm
    override fun execute() {
        val a1 = op1.value(core)
        val a2 = op3.value(core) shl 16
        val res = a1 + a2
        op2.value(core, res)
    }
}