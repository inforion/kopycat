package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 26.09.16.
 */

class Shr(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "shr"

    override val cfChg = true
    override val ofChg = true
    override val afChg = true
    override val zfChg = true
    override val sfChg = true

    override fun execute() {
        val a1 = op1.value(core)
        val a2 = (op2.value(core) mask 5).toInt()
        val res = a1 ushr a2
        val result = Variable<x86Core>(0, op1.dtyp)
        result.value(core, res)
        FlagProcessor.processShiftFlag(core, result, op1, op2, false, false, a1[a2 - 1] == 1L)
        op1.value(core, result)
    }
}