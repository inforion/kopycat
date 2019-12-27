package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Memory
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Phrase
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 28.09.16.
 */
class Lea(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "lea"

    override fun execute() {
        val ea = if(op2 is x86Displacement)
            (op2 as x86Displacement).effectiveAddress(core)
        else if(op2 is x86Memory)
            (op2 as x86Memory).effectiveAddress(core)
        else if(op2 is x86Phrase)
            (op2 as x86Phrase).effectiveAddress(core)
        else
            throw GeneralException("Incorrect operand type")

        op1.value(core, ea)
    }
}