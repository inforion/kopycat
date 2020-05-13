package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.78
class LDREXH(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rn: ARMRegister,
             val rt: ARMRegister,
             val imm32: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn) {

    override val mnem = "LDREXH$mcnd"

    override fun execute() {
        val address = rn.value(core)
        // TODO: Single core - no need
        //SetExclusiveMonitors(address,2);
        rt.value(core, core.inw(address))
    }
}