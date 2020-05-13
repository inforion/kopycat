package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esMemory
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Switch(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "switch"

    // Format I - reg1, reg2
    override fun execute() {
        // insnSize add in CPU execute
        val pc = core.cpu.regs.pc + size
        val adr = pc + (op1.value(core) shl 1)
        val memory = v850esMemory(Datatype.WORD, adr)

        val result = memory.ssext(core) shl 1
        core.cpu.regs.pc = pc + result - size
    }
}