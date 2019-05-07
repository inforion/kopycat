package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 21.09.16.
 */

class Adc(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "adc"

    override val cfChg = true
    override val pfChg = true
    override val afChg = true
    override val zfChg = true
    override val sfChg = true
    override val ofChg = true

    override fun execute() {
        val a1 = op1.value(core)
        val a2 = op2.value(core)
        val res = a1 + a2 + core.cpu.flags.cf.toInt()
        val result = Variable<x86Core>(0, op1.dtyp)
        result.value(core, res)
        FlagProcessor.processAddSubCmpFlag(core, result, op1, op2, false)
        op1.value(core, result)
    }
}