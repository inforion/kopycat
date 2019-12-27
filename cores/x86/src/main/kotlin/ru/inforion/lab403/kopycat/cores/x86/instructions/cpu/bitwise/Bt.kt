package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 13.06.17.
 */
class Bt(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "bt"

    override fun execute() {
        val (a1, bitpos) = x86utils.bitBase(core, op1, op2, prefs)
        val cf = a1.bit(core, bitpos)
        core.cpu.flags.cf = cf.toBool()
    }
}