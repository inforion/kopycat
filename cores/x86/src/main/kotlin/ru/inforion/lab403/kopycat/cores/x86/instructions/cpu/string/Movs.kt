package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 12.10.16.
 */
class Movs(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, false, *operands) {
    override val mnem = "movs"

    override fun executeStringInstruction() {
        val dst = op1 as x86Displacement
        val src = op2 as x86Displacement

        val data = src.value(core)
        dst.value(core, data)

        var spos = src.reg.value(core)
        var dpos = dst.reg.value(core)
        if (!x86Register.eflags.df(core)) {
            spos += src.dtyp.bytes
            dpos += dst.dtyp.bytes
        } else {
            spos -= src.dtyp.bytes
            dpos -= dst.dtyp.bytes
        }
        src.reg.value(core, spos)
        dst.reg.value(core, dpos)
    }
}