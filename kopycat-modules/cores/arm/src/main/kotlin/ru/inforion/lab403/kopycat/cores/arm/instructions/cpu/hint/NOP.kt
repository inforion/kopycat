package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hint

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class NOP(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, size = size) {
    override val mnem = "NOP"

    override fun execute() = Unit
}