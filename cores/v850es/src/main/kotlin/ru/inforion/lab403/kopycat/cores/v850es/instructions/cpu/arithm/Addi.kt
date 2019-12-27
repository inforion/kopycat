package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.arithm

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esVariable
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 01.06.17.
 */

class Addi(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "addi"

    override val cyChg = true
    override val ovChg = true
    override val sChg = true
    override val zChg = true

    private val result = v850esVariable(DWORD)

    // Format VI - reg1, reg2, imm
    override fun execute() {
        val a3 = op3.usext(core)
        val a1 = op1.value(core)
        val tmp = a1 + a3
        result.value(core, tmp)
        FlagProcessor.processArithmFlag(core, result, op1, op3, false)
        op2.value(core, result)
    }
}