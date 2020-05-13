package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Shrd(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "shrd"

    override val cfChg = true
    override val ofChg = true
    override val afChg = true
    override val zfChg = true
    override val sfChg = true

    override fun execute() {
        var dst = op1.value(core)
        val src = op2.value(core)
        val operandSize = op1.dtyp.bits
        val count = op3.value(core).asInt % 32
        if (count > operandSize) {
            log.warning { "count > opsize for shrd" }
        } else {
            val cf = dst[count - 1] == 1L
            dst = (dst ushr count) or (src shl (operandSize - count))
            val result = Variable<x86Core>(dst, op1.dtyp)
            FlagProcessor.processShiftFlag(core, result, op1, op2, false, false, cf)
            op1.value(core, result)
        }
    }
}