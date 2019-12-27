package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by a.gladkikh on 09.09.19.
 */

class Lmsw(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "lmsw"

    override fun execute() {
        val cr0 = core.cpu.cregs.cr0
        val value = op1.bits(core, 3..0)
        core.cpu.cregs.cr0 = cr0.insert(value, 3..0)
    }
}