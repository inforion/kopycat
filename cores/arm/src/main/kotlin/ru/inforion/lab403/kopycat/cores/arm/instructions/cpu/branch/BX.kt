package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 28.01.18
 */

class BX(cpu: AARMCore,
         opcode: Long,
         cond: Condition,
         val rm: ARMRegister,
         size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rm, size = size) {
    override val mnem = "BX$mcnd"

     override fun execute() = core.cpu.BXWritePC(rm.value(core))
}