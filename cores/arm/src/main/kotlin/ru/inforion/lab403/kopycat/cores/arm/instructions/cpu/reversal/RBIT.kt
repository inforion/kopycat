package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 30.01.18
 */

class RBIT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rd: ARMRegister,
           val rm: ARMRegister,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, size = size) {
    override val mnem = "RBIT$mcnd"

    override fun execute() {
        for(i in 0..31)
            rd.bit(core, 31-i, rm.bit(core, i))
    }
}