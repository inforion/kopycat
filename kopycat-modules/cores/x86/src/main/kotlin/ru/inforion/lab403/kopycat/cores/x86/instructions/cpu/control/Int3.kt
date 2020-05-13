package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Int3(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "int3"

    override fun execute() {
        core.cop.INT = true
        core.cop.IRQ = 3
    }

    companion object {
        fun create(core: x86Core) = Int3(core, ByteArray(1, { 0xCC.toByte() }), Prefixes(core))
    }
}