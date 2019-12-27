package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 17.01.18.
 */

class IT(cpu: AARMCore,
         opcode: Long,
         cond: Condition,
         private val mask: Long,
         private val firstCond: Long,
         size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, size = size) {
    override val mnem = "IT"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        core.cpu.status.ITSTATE = (firstCond shl 4) + mask
    }
}