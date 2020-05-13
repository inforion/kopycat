package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unconditional

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.32
class CLREX(cpu: AARMCore,
             opcode: Long,
             cond: Condition):
        AARMInstruction(cpu, Type.VOID, cond, opcode) {

    override val mnem = "CLREX"

    override fun execute() {
        // TODO: Single core - no need
//        core.cpu.ClearExclusiveLocal(core.cpu.ProcessorID())
    }
}