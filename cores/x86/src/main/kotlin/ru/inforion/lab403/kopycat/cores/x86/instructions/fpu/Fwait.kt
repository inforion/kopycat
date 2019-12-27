package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 10.09.19.
 */


class Fwait(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "fwait"

    override fun execute() {
        log.warning { "I don't know what to do with insn $mnem" }
    }
}