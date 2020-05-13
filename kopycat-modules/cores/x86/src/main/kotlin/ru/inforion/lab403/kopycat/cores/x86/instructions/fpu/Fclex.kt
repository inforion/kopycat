package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Fclex(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "fclex"

    override fun execute() {
        core.fpu.swr.b = false
        core.fpu.swr.ie = false
        core.fpu.swr.de = false
        core.fpu.swr.xe = false
        core.fpu.swr.oe = false
        core.fpu.swr.ue = false
        core.fpu.swr.pe = false
        core.fpu.swr.sf = false
        core.fpu.swr.es = false
    }
}