package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 04.10.16.
 */
class Lgdt(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "lgdt"

    override fun execute() {
        val src = op1.value(core)
        if (prefs.is16BitOperandMode) {
            core.mmu.gdtr.limit = src[15..0]
            core.mmu.gdtr.base = src[47..16] and 0xFFFFFF
        } else {
            core.mmu.gdtr.limit = src[15..0]
            core.mmu.gdtr.base = src[47..16]
        }
    }
}