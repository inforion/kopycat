package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 26.09.16.
 */
class Nop(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "nop"
    override fun execute() {

    }

    companion object {
        fun create(core: x86Core) = Nop(core, ByteArray(1, { 0x90.toByte() }), Prefixes(core))
    }
}