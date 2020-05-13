package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Fucom(core: x86Core, opcode: ByteArray, prefs: Prefixes, val popNumber: Int, vararg operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operand) {
    override val mnem = "fucom"

    override fun execute() {
        val a1 = op1.value(core).ieee754()
        val a2 = op2.value(core).ieee754()
        when {
            a1 > a2 -> {
                core.fpu.swr.c0 = false
                core.fpu.swr.c2 = false
                core.fpu.swr.c3 = false
            }
            a1 < a2 -> {
                core.fpu.swr.c0 = true
                core.fpu.swr.c2 = false
                core.fpu.swr.c3 = false
            }
            else -> {
                core.fpu.swr.c0 = false
                core.fpu.swr.c2 = false
                core.fpu.swr.c3 = true

            }
        }
        core.fpu.pop(popNumber)
    }
}