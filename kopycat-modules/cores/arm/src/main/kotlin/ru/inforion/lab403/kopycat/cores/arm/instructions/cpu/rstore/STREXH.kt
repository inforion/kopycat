package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.215
class STREXH(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rn: ARMRegister,
             val rd: ARMRegister,
             val rt: ARMRegister,
             val imm32: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rt, rn) {

    override val mnem = "STREXH$mcnd"

    override fun execute() {
        val address = rn.value(core)
        // TODO: Single core - no need
        if (/*ExclusiveMonitorsPass(address, 2)*/ true) {
            core.outb(address, rt.value(core) like Datatype.WORD)
            rd.value(core,0L)
        }
        else
            rd.value(core, 1L)
    }
}