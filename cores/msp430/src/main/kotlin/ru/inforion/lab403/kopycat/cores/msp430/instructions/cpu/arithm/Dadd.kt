package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.arithm

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by shiftdj on 16/02/18.
 */

class Dadd(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>):
        AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "dadd"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        val valFir = op1.value(core)
        val valSec = op2.value(core)
        var valRes : Long = 0
        var carry = core.cpu.flags.c.toLong()
        for (i in 0 until op1.dtyp.bytes) {
            val lsb = i * 4
            val msb = i + 3
            val sum = valFir[msb..lsb] + valSec[msb..lsb] + carry
            val part = if (sum[3..1] > 0b100) {
                carry = 1
                sum - 10
            }
            else {
                carry = 0
                sum
            }
            valRes = valRes.insert(part, msb..lsb)
        }
        result.value(core, valRes)
        FlagProcessor.processDaddFlag(core, result, carry.toBool())
        op2.value(core, result)
    }
}