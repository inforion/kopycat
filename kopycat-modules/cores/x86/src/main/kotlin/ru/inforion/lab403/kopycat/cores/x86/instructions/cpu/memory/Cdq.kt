package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.eax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.edx
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.ax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.dx
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Cdq(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "cdq"

    override fun execute() {
        val operand1 = if (prefs.is16BitOperandMode) ax else eax
        val operand2 = if (prefs.is16BitOperandMode) dx else edx
        val sext = operand1.ssext(core)
        operand2.value(core, sext shr operand1.dtyp.bits)
    }
}