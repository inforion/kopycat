package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class BFC(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          val rn: ARMRegister,
          private val msBit: Long,
          private val lsBit: Long):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {

    override val mnem = "BFC$mcnd"
    override fun execute() {
        if(msBit >= lsBit) rd.value(core, rd.value(core).clr(msBit.asInt..lsBit.asInt))
        else throw Unpredictable
    }
}
