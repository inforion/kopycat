package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Xadd(core: x86Core, opcode: ByteArray, prefs: Prefixes,
           val dest: AOperand<x86Core>, val src: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, dest, src) {
    override val mnem = "xadd"

    override fun execute() {
        val temp = src.value(core) + dest.value(core)
        src.value(core, dest)
        dest.value(core, temp)
    }
}