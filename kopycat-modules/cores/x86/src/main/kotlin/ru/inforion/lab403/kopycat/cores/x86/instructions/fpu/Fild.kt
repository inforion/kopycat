package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Fild(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operand) {
    override val mnem = "fild"

    override fun execute() {
        val value = op2.value(core).toDouble().ieee754()
        (op1 as x86FprRegister).push(core, value)
    }
}