package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hmultiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SMLAWx(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             val ra: ARMRegister,
             val rm: ARMRegister,
             val rn: ARMRegister,
             private val mHigh: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, ra, rm, rn) {
    override val mnem = "SMLAW${if(mHigh) "T" else "B"}"

    override fun execute() {
        val operand2 = if(mHigh) rm.value(core)[32..16] else rm.value(core)[15..0]
        val result = SInt(rn.value(core), 32) * SInt(operand2, 16) + (SInt(ra.value(core), 32).shl(16))
        rd.value(core, result[47..16])
        FlagProcessor.processHMulRegFlag(core, result, rd)
    }
}