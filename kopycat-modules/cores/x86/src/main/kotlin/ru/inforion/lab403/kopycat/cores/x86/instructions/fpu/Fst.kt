package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Fst(core: x86Core, opcode: ByteArray, prefs: Prefixes, val popCount: Int, vararg operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operand) {
    override val mnem = "fst"

    override fun execute() {
        val value = op2.value(core)
        op1.value(core, value)
        core.fpu.pop(popCount)
    }
}