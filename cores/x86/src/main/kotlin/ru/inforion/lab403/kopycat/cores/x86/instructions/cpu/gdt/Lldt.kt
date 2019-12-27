package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 17.01.17.
 */

class Lldt(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "lldt"

    override fun execute() {
        core.mmu.ldtr = op1.value(core)
        log.info { "[${core.cpu.pc.hex}] LDTR changed to 0x${core.mmu.ldtr.hex}" }
    }
}