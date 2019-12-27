package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 06.02.17.
 */

class Lsl(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "lsl"

    override fun execute() {
        val a2 = op2.value(core) and 0xFFFF
        // TODO: Whether we should cache take into account?
        val tempGLDT = core.mmu.readSegmentDescriptor(a2)
        val tempLimit = if(tempGLDT.g) tempGLDT.limit.shl(12) or 0x0FFFL else tempGLDT.limit
        op1.value(core, tempLimit)
    }
}