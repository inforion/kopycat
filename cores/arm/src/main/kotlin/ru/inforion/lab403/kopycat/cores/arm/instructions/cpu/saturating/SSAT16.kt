package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.SignedSatQ
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 30.01.18
 */

class SSAT16(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             private val saturateTo: Immediate<AARMCore>,
             val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, saturateTo, rn) {
    override val mnem = "SSAT16$mcnd"

    override fun execute() {
        val (result1, sat1) = SignedSatQ(SInt(rn.bits(core, 15..0), 32).asInt, saturateTo.value.asInt)
        val (result2, sat2) = SignedSatQ(SInt(rn.bits(core, 31..16), 32).asInt, saturateTo.value.asInt)
        rd.bits(core, 15..0, signext(result1.asLong, 16).asLong)
        rd.bits(core, 31..16, signext(result2.asLong, 16).asLong)
        if(sat1 || sat2) FlagProcessor.processSatFlag(core)
    }
}