package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Rcr(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "rcr"

    override val cfChg = true
    override val ofChg = true

    override fun execute() {
        // http://stackoverflow.com/questions/10395071/what-is-the-difference-between-rcr-and-ror
        val maskCf = if(core.cpu.flags.cf) 1L.shl(op1.dtyp.bits) else 0L
        val a1 = op1.value(core) or maskCf
        val a2 = (op2.value(core) % op1.dtyp.bits).toInt()
        val lowPart = a1[a2 - 1..0]
        val msb = op1.dtyp.bits + 1
        val lsb = msb - a2
        val res = a1.ushr(a2).insert(lowPart, msb..lsb)
        val result = Variable<x86Core>(0, op1.dtyp)
        result.value(core, res)
        FlagProcessor.processRotateFlag(core, result, op2, false, a1[a2 - 1] == 1L)
        op1.value(core, res)
    }
}