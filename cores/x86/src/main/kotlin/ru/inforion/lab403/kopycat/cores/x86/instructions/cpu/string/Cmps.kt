package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 28.07.17.
 */

class Cmps(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, true, *operands) {
    override val mnem = "cmps"

    override val cfChg = true
    override val pfChg = true
    override val afChg = true
    override val zfChg = true
    override val sfChg = true
    override val ofChg = true

    override fun executeStringInstruction() {
        val a1 = op1 as x86Displacement
        val a2 = op2 as x86Displacement
        val res = a1.value(core) - a2.value(core)
        val result = Variable<x86Core>(0, op1.dtyp)
        result.value(core, res)
        var pos1 = a1.reg.value(core)
        var pos2 = a2.reg.value(core)
        if (!x86Register.eflags.df(core)) {
            pos1 += a1.dtyp.bytes
            pos2 += a2.dtyp.bytes
        } else {
            pos1 -= a1.dtyp.bytes
            pos2 -= a2.dtyp.bytes
        }
        FlagProcessor.processAddSubCmpFlag(core, result, op1, op2, true)
        a1.reg.value(core, pos1)
        a2.reg.value(core, pos2)
    }
}