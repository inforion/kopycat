package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 19.01.17.
 */

class Lar(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>): AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "lar"

    override fun execute() {
        val ss = op2.value(core)
        // TODO: Whether we should cache take into account?
        val desc = core.mmu.gdt(ss)
        val result = if(prefs.is16BitOperandMode){
            TODO()
        } else {
            desc.dataHi and 0xF0FF00L
        }
        op1.value(core, result)
        core.cpu.flags.zf = true
    }
}