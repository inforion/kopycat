package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 14.06.17.
 */
class Bts(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "bts"

    override fun execute() {
        val a1 = op1.value(core)
        val a2 = if (prefs.is16BitOperandMode) op2.value(core) % 16 else op2.value(core) % 32
        core.cpu.flags.cf = a1[a2.toInt()] == 1L
        op1.value(core, a1.insert(1, a2.toInt()))
    }
}