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

/**
 * Created by r.valitov on 30.01.18
 */

class SXTAB(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rn: ARMRegister,
            val rd: ARMRegister,
            val rm: ARMRegister,
            private val rotate: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, rm, rotate) {
    override val mnem = "SXTAB$mcnd"

    override fun execute() {
        val rotated = ROR(rm.value(core), 32, rotate.value(core).asInt)
        rd.value(core, rn.value(core) + signext(rotated[7..0],8))
    }
}