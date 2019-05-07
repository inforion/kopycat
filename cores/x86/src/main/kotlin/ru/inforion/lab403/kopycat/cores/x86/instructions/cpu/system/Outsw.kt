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
class Outsw(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, false, *operands) {
    override val mnem = "outsw"

    override fun executeStringInstruction() {
        val dst = op1
        val src = op2 as x86Displacement

        val address = dst.value(core)
        val data = src.value(core)

        var pos = src.reg.value(core)
        if (!x86Register.eflags.df(core))
            pos += src.dtyp.bytes
        else
            pos -= src.dtyp.bytes

        src.reg.value(core, pos)

        core.ports.io.write(dst.dtyp, address, data)
    }
}