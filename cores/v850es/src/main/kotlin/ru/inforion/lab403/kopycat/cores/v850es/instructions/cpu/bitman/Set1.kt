package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.bitman

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esVariable
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 29.05.17.
 */

class Set1(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "set1"

    override val zChg = true

    private val result = v850esVariable(Datatype.DWORD)

    // Format VIII - disp, bit
    // Format IX - reg1, reg2
    override fun execute() {
        val bit = op2.bits(core,2..0).asInt
        val data = op1.value(core)
        result.value(core, data set bit)
        FlagProcessor.processBitManFlag(core, result, op1, op2)
        op1.value(core, result)
    }
}