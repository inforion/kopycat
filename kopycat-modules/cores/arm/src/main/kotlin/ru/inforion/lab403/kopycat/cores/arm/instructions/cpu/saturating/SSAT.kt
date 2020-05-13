package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.SignedSatQ
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SSAT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rd: ARMRegister,
           private val shiftT: SRType,
           private val shiftN: Long,
           private val saturateTo: Immediate<AARMCore>,
           val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, saturateTo, rn) {
    override val mnem = "SSAT$mcnd"

    override fun execute() {
        val operand = Shift(rn.value(core), 32, shiftT, shiftN.asInt, core.cpu.flags.c.asInt)
        val (result, sat) = SignedSatQ(SInt(operand, 32).asInt, saturateTo.value.asInt)
        rd.value(core, signext(result.asLong, saturateTo.value.asInt).asLong)
        if(sat) FlagProcessor.processSatFlag(core)
    }
}