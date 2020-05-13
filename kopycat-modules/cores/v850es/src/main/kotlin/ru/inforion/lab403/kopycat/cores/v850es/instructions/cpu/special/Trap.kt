package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Trap(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "trap"

    // Format X - imm
    override fun execute() {
        // insnSize add in CPU execute
        if(op1.value(core) <= 0x1F) {
            core.cpu.cregs.eipc = core.cpu.regs.pc + size
            core.cpu.cregs.eipsw = core.cpu.cregs.psw

            val prefix = if (op1.value(core) <= 0xF) 0x40L else 0x50
            // Exception code of non-maskable interrupt (NMI)
            val fecc = core.cpu.cregs.ecr[31..16]
            // Exception code of exception or maskable interrupt
            val eicc = prefix + op1.value(core)[3..0]
            core.cpu.cregs.ecr = (fecc shl 16) + eicc

            core.cpu.flags.ep = true
            core.cpu.flags.id = true

            core.cpu.regs.pc = prefix - size
        } else throw GeneralException("Wrong vector value on TRAP operation!")
    }
}