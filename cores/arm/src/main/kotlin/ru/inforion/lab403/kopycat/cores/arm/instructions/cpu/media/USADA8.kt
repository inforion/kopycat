package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.UInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import java.lang.Math.abs

/**
 * Created by r.valitov on 18.01.18
 */

class USADA8(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             val ra: ARMRegister,
             val rm: ARMRegister,
             val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, ra, rm, rn) {

    override val mnem = "USADA8$mcnd"

    override fun execute() {
        TODO()
        val absDiff1 = abs(UInt(rn.value(core)[7..0], 32) - UInt(rm.value(core)[7..0], 32))
        val absDiff2 = abs(UInt(rn.value(core)[15..8], 32) - UInt(rm.value(core)[15..8], 32))
        val absDiff3 = abs(UInt(rn.value(core)[23..16], 32) - UInt(rm.value(core)[23..16], 32))
        val absDiff4 = abs(UInt(rn.value(core)[31..24], 32) - UInt(rm.value(core)[31..24], 32))
        val result = UInt(ra.value(core), 32) + absDiff1 + absDiff2 + absDiff3 + absDiff4

        rd.value(core, result[31..0])
    }
}
