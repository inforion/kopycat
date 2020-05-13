package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class B(cpu: AARMCore,
        opcode: Long,
        cond: Condition,
        val offset: Immediate<AARMCore>,
        size: Int = 4):
        AARMInstruction(cpu, Type.VOID, cond, opcode, offset, size = size) {
    override val mnem = "B$mcnd"

    override fun execute() {
        if (core.cpu.ConditionPassed(cond)) {
            if (core.cpu.InITBlock()) throw ARMHardwareException.Unpredictable
            core.cpu.BranchWritePC(core.cpu.pc + offset.value)
        }
    }
}