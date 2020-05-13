package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class QDADD(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rn: ARMRegister,
            val rm: ARMRegister,
            val rd: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rd, rm) {
    override val mnem = "QDADD$mcnd"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        TODO()
    }
}