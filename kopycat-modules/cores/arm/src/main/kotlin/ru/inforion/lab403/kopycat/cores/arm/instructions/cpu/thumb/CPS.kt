package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class CPS(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          private val im: Boolean,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, size = size) {
    override val mnem = "CPS"

    override fun execute() {
        core.cpu.spr.primask.pm = im
    }
}