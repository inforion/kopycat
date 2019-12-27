package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86COP
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 04.10.16.
 */
class Lidt(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "lidt"

    override fun execute() {
        val src = op1.value(core)
        if (prefs.is16BitOperandMode) {
            core.cop.idtr.limit = src[15..0]
            core.cop.idtr.base = src[47..16] and 0xFFFFFF
        } else {
            core.cop.idtr.limit = src[15..0]
            core.cop.idtr.base = src[47..16]
        }
        log.info { "[${core.cpu.pc.hex}] IDTR changed ${core.cop.idtr}" }
    }
}