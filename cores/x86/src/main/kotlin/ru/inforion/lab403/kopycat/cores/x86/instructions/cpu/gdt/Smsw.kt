package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by a.gladkikh on 09.09.19.
 */

class Smsw(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "smsw"

    override fun execute() = op1.value(core, core.cpu.cregs.cr0[15..0])
}