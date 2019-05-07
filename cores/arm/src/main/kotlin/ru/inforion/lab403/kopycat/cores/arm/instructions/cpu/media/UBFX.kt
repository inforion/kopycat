package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class UBFX(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rd: ARMRegister,
           val rn: ARMRegister,
           private val widthMinus1: Long,
           private val lsBit: Long):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn) {

    override val mnem = "UBFX$mcnd"
    override fun execute() {
        val msBit = lsBit + widthMinus1
        if(msBit <= 31) rd.value(core, rn.value(core)[msBit.asInt..lsBit.asInt])
        else throw Unpredictable
    }
}
