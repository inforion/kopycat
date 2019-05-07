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

class SMULWx(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             val ra: ARMRegister,
             val rm: ARMRegister,
             val rn: ARMRegister,
             private val mHigh: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, rn) {
    override val mnem = "SMULW${if(mHigh) "T" else "B"}"

    override fun execute() {
        val operand2 = if(mHigh) rm.value(core)[31..16] else rm.value(core)[15..0]
        val product = SInt(rn.value(core), 32) * SInt(operand2, 16)
        rd.value(core, product[47..16])
    }
}