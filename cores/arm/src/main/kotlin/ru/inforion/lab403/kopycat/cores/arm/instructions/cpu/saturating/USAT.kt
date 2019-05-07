package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.UnsignedSatQ
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 30.01.18
 */

class USAT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rd: ARMRegister,
           private val shiftT: SRType,
           private val shiftN: Long,
           private val saturateTo: Immediate<AARMCore>,
           val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, saturateTo, rn) {
    override val mnem = "USAT$mcnd"

    override fun execute() {
        val operand = Shift(rn.value(core), 32, shiftT, shiftN.asInt, core.cpu.flags.c.asInt)
        val (result, sat) = UnsignedSatQ(SInt(operand, 32).asInt, saturateTo.value.asInt)
        rd.value(core, result.asLong)
        if(sat) FlagProcessor.processSatFlag(core)
    }
}