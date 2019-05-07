package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hmultiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 22.01.18
 */

class SMULxy(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             val ra: ARMRegister,
             val rm: ARMRegister,
             val rn: ARMRegister,
             private val nHigh: Boolean,
             private val mHigh: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, rn) {
    override val mnem = "SMUL${if(mHigh) "T" else "B"}${if(mHigh) "T" else "B"}"

    override fun execute() {
        val operand1 = if(nHigh) rn.ssext(core)[31..16] else rn.ssext(core)[15..0]
        val operand2 = if(mHigh) rm.ssext(core)[31..16] else rm.ssext(core)[15..0]
        val result = SInt(operand1, 16) * SInt(operand2, 16)
        rd.value(core, result[31..0])
    }
}