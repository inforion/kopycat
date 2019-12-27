package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.logic

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esVariable
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 27.05.17.
 */

class Andi(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "andi"

    override val ovChg = true
    override val sChg = true
    override val zChg = true

    private val result = v850esVariable(Datatype.DWORD)

    // Format VI - reg1, reg2, imm
    override fun execute() {
        result.value(core, op3.value(core) and op1.value(core))
        FlagProcessor.processLogicalFlag(core, result)
        op2.value(core, result)
    }
}