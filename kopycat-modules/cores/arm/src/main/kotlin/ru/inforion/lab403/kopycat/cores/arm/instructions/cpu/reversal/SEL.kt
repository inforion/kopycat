package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SEL(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          val rd: ARMRegister,
          val rm: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rd, rm) {
    override val mnem = "SEL$mcnd"

    override fun execute() {
        rd.bits(core,7..0,   if(core.cpu.status.ge[0] == 1L) rn.bits(core,7..0)   else rm.bits(core,7..0))
        rd.bits(core,15..8,  if(core.cpu.status.ge[1] == 1L) rn.bits(core,15..8)  else rm.bits(core,15..8))
        rd.bits(core,23..16, if(core.cpu.status.ge[2] == 1L) rn.bits(core,23..16) else rm.bits(core,23..16))
        rd.bits(core,31..24, if(core.cpu.status.ge[3] == 1L) rn.bits(core,31..24) else rm.bits(core,31..24))
    }
}