package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.ebp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.bp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.sp
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Enter(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "enter"

    override fun execute() {
        val size = op1.value(core)
        val nestingLevel = op2.value(core) % 32
        val regEbp = if (prefs.is16BitAddressMode) bp else ebp
        val regEsp = if (prefs.is16BitAddressMode) sp else esp

        x86utils.push(core, core.cpu.regs.ebp, prefs.opsize, prefs)
        val frameTemp = regEsp.value(core)
        if(nestingLevel != 0L)
            TODO()

        regEbp.value(core, frameTemp)
        regEsp.value(core, frameTemp - size)
    }
}