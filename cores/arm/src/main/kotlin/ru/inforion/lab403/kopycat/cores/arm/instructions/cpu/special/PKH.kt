package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 30.01.18
 */

class PKH(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          val rd: ARMRegister,
          private val shiftT: SRType,
          private val shiftN: Long,
          private val tbForm: Boolean,
          val rm: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rd, rm) {
    override val mnem = "PKH$mcnd"

    override fun execute() {
        val operand2 = Shift(rm.value(core), 32, shiftT, shiftN.asInt, core.cpu.flags.c.asInt)
        rd.bits(core,15..0,  if(tbForm) operand2[15..0] else rn.bits(core, 15..0))
        rd.bits(core,31..16, if(tbForm) rn.bits(core, 31..16) else operand2[31..16])
    }
}