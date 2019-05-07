package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 30.01.18
 */

class REV16(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rd: ARMRegister,
            val rm: ARMRegister,
            size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, size = size) {
    override val mnem = "REV16$mcnd"

    override fun execute() {
        rd.bits(core,31..24, rm.bits(core,23..16))
        rd.bits(core,23..16, rm.bits(core,31..24))
        rd.bits(core,15..8,  rm.bits(core,7..0))
        rd.bits(core,7..0,   rm.bits(core,15..8))
    }
}