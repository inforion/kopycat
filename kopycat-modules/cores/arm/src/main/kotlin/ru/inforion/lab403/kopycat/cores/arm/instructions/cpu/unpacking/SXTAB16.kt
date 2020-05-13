package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unpacking

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.ROR
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SXTAB16(cpu: AARMCore,
              opcode: Long,
              cond: Condition,
              val rn: ARMRegister,
              val rd: ARMRegister,
              val rm: ARMRegister,
              private val rotate: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rd, rm, rotate) {
    override val mnem = "SXTAB16$mcnd"

    override fun execute() {
        val rotated = ROR(rm.value(core), 32, rotate.value(core).asInt)
        val rdValueL = rn.value(core)[15..0] + signext(rotated[7..0], 8)
        val rdValueM = rn.value(core)[31..16] + signext(rotated[23..16], 8)
        rd.value(core, rdValueM[15..0].shl(16) + rdValueL[15..0])
    }
}