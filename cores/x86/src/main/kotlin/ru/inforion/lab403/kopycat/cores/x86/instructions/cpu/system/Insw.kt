package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string.AStringInstruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 29.06.17.
 */
class Insw(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, false, *operands) {
    override val mnem = "insw"

    override fun executeStringInstruction() {
        val dst = op1 as x86Displacement
        val src = op2

        val data = core.ports.io.read(op1.dtyp, src.value(core))
        dst.value(core, data)

        var pos = dst.reg.value(core)
        if (!x86Register.eflags.df(core))
            pos += dst.dtyp.bytes
        else
            pos -= dst.dtyp.bytes

        dst.reg.value(core, pos)
    }
}