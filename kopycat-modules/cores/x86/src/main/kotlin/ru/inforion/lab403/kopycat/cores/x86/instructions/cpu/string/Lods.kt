package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Lods(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, true, *operands) {
    override val mnem = "lods"

    override fun executeStringInstruction() {
        val dst = op1
        val src = op2 as x86Displacement

        val data = src.value(core)
        dst.value(core, data)

        var pos = src.reg.value(core)
        if (!x86Register.eflags.df(core))
            pos += src.dtyp.bytes
        else
            pos -= src.dtyp.bytes

        src.reg.value(core, pos)
    }
}