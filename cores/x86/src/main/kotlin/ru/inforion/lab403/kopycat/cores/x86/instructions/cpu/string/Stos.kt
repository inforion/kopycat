package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 12.10.16.
 */
class Stos(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, false, *operands) {
    override val mnem = "stos"

    override fun executeStringInstruction() {
        val dst = op1 as x86Displacement
        val src = op2

        val data = src.value(core)
        dst.value(core, data)

        var pos = dst.reg.value(core)
        if (!x86Register.eflags.df(core))
            pos += dst.dtyp.bytes
        else
            pos -= dst.dtyp.bytes

        dst.reg.value(core, pos)
    }
}